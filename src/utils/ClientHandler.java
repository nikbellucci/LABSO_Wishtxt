package utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

/**
 * This class handles the client's connection to the server.
 */
public class ClientHandler implements Runnable {
    private Socket client;
    // This hashmap links a semaphore with each used file by the clients
    private HashMap < String, ReaderWriterSem > criticHandle = new HashMap < String, ReaderWriterSem > (); // string nomeFile
    private String[] splitArg = null;
    private String splitRequest = "";
    private String path;
    private ReaderWriterSem semaphore;
    private String fileName;

    private String[] commands = {
        "create",
        "rename",
        "delete",
        "list",
        "edit",
        "read",
        "quit"
    };
    // private String routePath = this.getClass().getClassLoader().getResource(File.separator).getPath();

    public ClientHandler(Socket client, HashMap < String, ReaderWriterSem > criticHandle, String path) {
        this.client = client;
        this.criticHandle = criticHandle;
        this.path = path;
    }

    /**
     * It reads the message from the client, splits it into a command and an argument, and then calls
     * the getResponse function
     */
    @Override
    public void run() {
        try {
            ObjectOutputStream toClient = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream fromClient = new ObjectInputStream(client.getInputStream());
            Connection.initializeClientOnMap(client);
            Connection.addElementOnClientStream(client, toClient);

            while (true) {
                // Checking if the message from the client contains a colon. If it does not, it sends
                // an error message to the client.
                splitRequest = "";
                splitArg = null;

                // System.out.println(Connection.getClientsMap().get(client));

                try {
                    String message = (String) fromClient.readObject();
                    for (String command: commands) {
                        if (message.indexOf(command) != -1) {
                            String[] tmpString = message.split("\\s", 2);
                            splitRequest = tmpString[0];
                            System.out.println("Client: " + client.getInetAddress() + ":" + client.getPort() + ", command: " + splitRequest);
                            if (tmpString.length > 1) {
                                splitArg = tmpString[1].split("\\s", 2);
                            }
                            if(tmpString.length > 1){
                                if (!tmpString[1].contains(".txt")) {
                                    tmpString[1] = tmpString[1] + ".txt";
                                }
                                fileName = tmpString[1];
                            }
                            break;
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException | EOFException | SocketException e) {
                    e.printStackTrace();
                    // System.out.println("Socket closed: " + client);
                    break;
                }

                if (!getResponse(toClient, fromClient))
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client connection closed");
            e.printStackTrace();
        }
    }

    /**
     * It gets a request from the client, splits the request into an array of strings, and then calls the
     * appropriate method of the FileHandler class. 
     *  If the request is "read" or "write", the method will load the semaphores of the required file.  
     *
     * @param toClient the output stream to the client
     * @param fromClient the input stream from the client
     * @return The method returns a boolean value.
     */

    public void enterCritSec(){
        semaphore = getSemaphore();
        semaphore.startWrite();
        Connection.isWriting(client);
    }

    public void exitCritSec(){
        semaphore.endWrite();
        Connection.isIdle(client);
    }


    

    private boolean getResponse(ObjectOutputStream toClient, ObjectInputStream fromClient) throws IOException, ClassNotFoundException {
        FileHandler fileHandler = new FileHandler(path); // Path di ogni sistema operativo
        if (splitRequest.equalsIgnoreCase("create")) {
            if (splitArg != null) {
                toClient.writeObject("\n" + fileHandler.newFile(fileName));
            } else {
                toClient.writeObject("\n" + "Invalid argument(s)...]");
            }
        }
        else if (splitRequest.equalsIgnoreCase("rename")) {
            if (splitArg != null || splitArg.length < 2) {
                String[] tmp = fileName.split(".txt");         //[test 1].txt[ test 2].txt
                tmp[0] = tmp[0] + ".txt";                            //[test 1.txt]
                tmp[1] = tmp[1].substring(1)+".txt";      //[test 2.txt]
                enterCritSec();
                    toClient.writeObject("\n" + fileHandler.renameFile(tmp[0], tmp[1]));
                exitCritSec();
                
            } else {
                toClient.writeObject("\n" + "Invalid syntax: rename [oldName.txt] [newName.txt]");
            }
        } else if (splitRequest.equalsIgnoreCase("delete")) {
            if (splitArg != null) {
                enterCritSec();
                    toClient.writeObject("\n" + fileHandler.deleteFile(fileName));
                    criticHandle.remove(fileName);
                exitCritSec();
            } else {
                toClient.writeObject("\n" + "Invalid syntax: delete [fileName]");
            }
        } else if (splitRequest.equalsIgnoreCase("list")) {
            ReaderWriterSem semaphore = null;
            HashMap < String, String > listOfFiles = fileHandler.getFilesName();
            String response = "";
            if (listOfFiles.isEmpty()) {
                response = "Directory is empty";
            }
            for (String nameFile: listOfFiles.keySet()) {
                semaphore = getSemaphore(nameFile);
                response += "name file: " + nameFile + "\nlast modified: " + listOfFiles.get(nameFile) + "\nuser reading: " + semaphore.getReaderCount() + "\nuser writing: " + semaphore.isDbWriting() + "\n\n";
            }
            toClient.writeObject("\n" + response);
        } else if (splitRequest.equalsIgnoreCase("edit")) {
            if (splitArg != null) {
                toClient.writeObject("\n" + readFile(semaphore, fileHandler));
                editFile(fileHandler, fromClient, toClient);
                toClient.writeObject("\n" + "exiting editor...");
            } else {
                toClient.writeObject("\n" + "Invalid syntax: edit [fileName]");
            }
        } else if (splitRequest.equalsIgnoreCase("read")) {
            if (splitArg != null) {
                ReaderWriterSem semaphore = getSemaphore();
                Connection.isReading(client);
                readFile(semaphore, fileHandler, fromClient, toClient);
                toClient.writeObject("\n" + "exiting reading mode...");
                Connection.isIdle(client);
            } else {
                toClient.writeObject("\n" + "Invalid syntax: read [fileName]");
            }
        } else if (splitRequest.equalsIgnoreCase("quit")) {
            // ArrayList<Socket> clients = new ArrayList<Socket>(Connection.getClients());
            System.out.println("Client at address: " + client.getInetAddress() + ":" + client.getPort() + " closed");
            quitStuff(fromClient, toClient);
            
            return false;
        } else toClient.writeObject("\n" + "Invalid command!\nSyntax: [command] [argument(s)...]");
        return true;
    }


    private void quitStuff(ObjectInputStream fromClient, ObjectOutputStream toClient) throws IOException {
        fromClient.close();
        toClient.close();
        Connection.removeElement(client);
        Connection.removeElementFromMap(client);
        Connection.removeElementFromClientStream(client);
        client.close();
    }

    //internal loop for the edit mode
    /**
     * It reads a message from the client, 
     *  if the message is "backspace:" it calls the backspace function of the fileHandler, 
     *  if the message is "exit:" it breaks the loop, 
     *  otherwise it calls the writeLine function of the fileHandler
     *
     *  This method is an internal loop of client-server communication, hence all those passed variables.  
     *
     * @param semaphore a ReaderWriterSem object
     * @param fileHandler is a class that handles the file, it has a method to write a line, a method
     * to delete a line and a method to read a line.
     * @param fromClient ObjectInputStream
     * @param toClient the output stream to the client
     */
    private void editFile(FileHandler fileHandler, ObjectInputStream fromClient, ObjectOutputStream toClient) throws IOException, ClassNotFoundException {
            enterCritSec();
                while (true) {
                    String message = (String) fromClient.readObject();
                    if (message.equalsIgnoreCase(":backspace")) {
                        fileHandler.backSpace(fileName);
                        toClient.writeObject("delete last row");
                    } else if (message.equalsIgnoreCase(":close"))
                        break;           
                    else{
                        fileHandler.writeLine(fileName, message + "\n");
                        toClient.writeObject("you just wrote a line");
                    }
                        
                }
            exitCritSec();
    }


    //little method to enter and exit the crit. section and read from file. I added it as getResponse() uses the same code twice
    /**
     * The function takes a semaphore and a file handler as parameters. It starts the read operation,
     * reads the file, and ends the read operation
     * 
     * @param semaphore a ReaderWriterSem object
     * @param fileHandler a class that handles the file operations
     * @return The response from the fileHandler.readFile() method.
     */
    private String readFile(ReaderWriterSem semaphore, FileHandler fileHandler) {
        semaphore.startRead(); //start of critical section
        String response = fileHandler.readFile(fileName);

        if (response.length() == 0) {
            semaphore.endRead();
            return "file empty, write...";
        } else {
            semaphore.endRead();
            return response;
        }

    }

    private void readFile(ReaderWriterSem semaphore, FileHandler fileHandler, ObjectInputStream fromClient, ObjectOutputStream toClient) throws IOException, ClassNotFoundException {
        semaphore.startRead(); //start of critical section
        String response = fileHandler.readFile(fileName);

        if (response.length() == 0) {
            toClient.writeObject("file empty");
        } else {
            toClient.writeObject("\n" + response);

        }

        while (true) {
            String message = (String) fromClient.readObject();
            if (message.equalsIgnoreCase(":close"))
                break;
            else
                toClient.writeObject("to close session use command \":close\"");
        }
        semaphore.endRead();

    }

    /**
     * If the file is not in the hashmap, create a new semaphore for it and adds it to the hashmap. If
     * it is in the hashmap, return the semaphore for that file
     * 
     * @return The semaphore for the file.
     */
    private synchronized ReaderWriterSem getSemaphore() {
        ReaderWriterSem fileSemaphore = criticHandle.get(fileName); // checks if semaphoreHandler for file exists
        if (fileSemaphore == null) {
            fileSemaphore = new ReaderWriterSem();
            criticHandle.put(fileName, fileSemaphore);
        }

        return fileSemaphore;
    }

    private synchronized ReaderWriterSem getSemaphore(String nameFile) {
        ReaderWriterSem fileSemaphore = criticHandle.get(nameFile); // checks if semaphoreHandler for file exists
        if (fileSemaphore == null) {
            fileSemaphore = new ReaderWriterSem();
            criticHandle.put(nameFile, fileSemaphore);
        }

        return fileSemaphore;
    }

}
