package utils;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

/**
 * a thread class that is created with every new client connecting to the server
 * <p>
 * The class handles the communication with the client, by reciving and sending messages.
 * The class categori
 * </p>
 */
public class ClientHandler implements Runnable {
    private Socket client;
    // This hashmap links a semaphore with each used file by the clients
    private HashMap<String, ReaderWriterSem> criticHandle = new HashMap<String, ReaderWriterSem>(); // string nomeFile
    private String[] splitArg = null;
    private String splitRequest = "";
    private ObjectOutputStream toClient;
    private ObjectInputStream fromClient;
    ModesHandler modesHndlr;
    
    FileHandler fileHandler; // Path di ogni sistema operativo
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
    

    public ClientHandler(Socket client, HashMap<String, ReaderWriterSem> criticHandle, String path) {
        this.client = client;
        this.criticHandle = criticHandle;
        fileHandler = new FileHandler(path);
    }

    /**
     * It reads the message from the client, splits it into a command and an
     * argument, and then calls
     * the getResponse function
     */
    @Override
    public void run() {
        try {
            toClient = new ObjectOutputStream(client.getOutputStream());
            fromClient = new ObjectInputStream(client.getInputStream());
            modesHndlr = new ModesHandler(fromClient, toClient, fileHandler);
            Connection.initializeClientOnMap(client);
            Connection.addElementOnClientStream(client, toClient);
            while (true) {
                // Checking if the message from the client contains a colon. If it does not, it
                // sends
                // an error message to the client.
                splitRequest = "";
                splitArg = null;

                try {
                    String message = (String) fromClient.readObject();
                    splitMessage(message);
                } catch (ArrayIndexOutOfBoundsException | EOFException e) {
                    // e.printStackTrace();

                }
                if (!getResponse())
                    break;

            }
        } catch (SocketException e) {
            System.out.println("Client disconected: " + client);
            Connection.removeClientConnection(client);

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client connection closed");
            e.printStackTrace();
        }
    }

    private void splitMessage(String message) {
        for (String command : commands) {
            if (message.indexOf(command) != -1) {
                String[] tmpString = message.split("\\s", 2);
                splitRequest = tmpString[0];
                if (command.equals(splitRequest)) {
                    System.out.println("Client: " + client.getInetAddress() + ":" + client.getPort()
                            + ", command: " + splitRequest);
                }
                if (tmpString.length > 1) {
                    splitArg = tmpString[1].split("\\s", 2);
                    if (!tmpString[1].contains(".txt")) {
                        tmpString[1] = tmpString[1] + ".txt";
                    }
                    fileName = tmpString[1];
                }
                break;
            }
        }
    }

    /**
     * It gets a request from the client, splits the request into an array of
     * strings, and then calls the
     * appropriate method of the FileHandler class.
     * If the request is "read" or "write", the method will load the semaphores of
     * the required file.
     *
     * @param toClient   the output stream to the client
     * @param fromClient the input stream from the client
     * @return The method returns a boolean value.
     */

    public void startWrite() {
        semaphore = getSemaphore();
        semaphore.startWrite();
        Connection.isWriting(client);
    }
    
    public void startWrite(String s) {
        semaphore = getSemaphore(s);
        semaphore.startWrite();
        Connection.isWriting(client);
    }

    public void startRead() {
        semaphore = getSemaphore();
        semaphore.startRead();
        Connection.isReading(client);
    }

    public void endRead() {
        semaphore.endRead();
        Connection.isIdle(client);
    }

    public void endWrite() {
        semaphore.endWrite();
        Connection.isIdle(client);
    }

    private boolean getResponse() throws IOException, ClassNotFoundException { 
        // Following comands require at leats one argument
        switch(splitRequest.toLowerCase()) {
            case "create":
                if (splitArg != null) {
                    toClient.writeObject("\n" + fileHandler.newFile(fileName));
                } else {
                    toClient.writeObject("\n" + "Invalid argument(s)...");
                }
                break;
            case "rename":
                if (splitArg != null) {
                    String[] tmp = fileName.split(".txt"); // [test 1].txt[ test 2].txt
                    if (tmp.length == 2) {
                        tmp[0] = tmp[0] + ".txt"; // [test 1.txt]
                        tmp[1] = tmp[1].substring(1) + ".txt"; // [test 2.txt]
                        startWrite(tmp[0]);
                        toClient.writeObject("\n" + fileHandler.renameFile(tmp[0], tmp[1]));
                        endWrite();
                    } else {
                        toClient.writeObject("\n" + "Invalid syntax: rename [oldName].txt [newName].txt");
                        return true;
                    }
                } else {
                    toClient.writeObject("\n" + "Invalid argument(s)...");
                }
                break;
            case "delete":
                if (splitArg != null) {
                    startWrite();
                    toClient.writeObject("\n" + fileHandler.deleteFile(fileName));
                    criticHandle.remove(fileName);
                    endWrite();
                } else {
                    toClient.writeObject("\n" + "Invalid argument(s)...");
                }
                break;
            case "edit":
                if (splitArg != null) {
                    String fileText=null;
                    try{
                        fileText = readFile();
                    }catch(FileNotFoundException e){
                        toClient.writeObject("\n" + "File not found\n" + "exiting editor...");
                    }
                    if(!(fileText == null)){
                        toClient.writeObject("\n" + fileText);
                        startWrite();
                        modesHndlr.editFile(fileName);
                        endWrite();
                        toClient.writeObject("\n" + "exiting editor...");
                    }
                } else {
                    toClient.writeObject("\n" + "Invalid argument(s)...");
                }
                break;
            case "read":
                if (splitArg != null) {
                    startRead();
                    String res = modesHndlr.readFile(fileName);
                    endRead();
                    if(res==null)
                        toClient.writeObject("\n" + "exiting reading mode...");
                    else
                        toClient.writeObject("\n" +res + "\nexiting reading mode...");

                } else {
                    toClient.writeObject("\n" + "Invalid argument(s)...");
                }
                break;
            case "list":
                ReaderWriterSem semaphore = null;
                HashMap<String, String> listOfFiles = fileHandler.getFilesName();
                String response = "";
                if (listOfFiles.isEmpty()) {
                    response = "Empty directory";
                } else {
                    for (String nameFile : listOfFiles.keySet()) {
                        semaphore = getSemaphore(nameFile);
                        response += "name file: " + nameFile + "\nlast modified: " + listOfFiles.get(nameFile)
                                + "\nuser reading: " + semaphore.getReaderCount() + "\nuser writing: "
                                + semaphore.isDbWriting() + "\n\n";
                    }
                }
                toClient.writeObject("\n" + response);
                break;
            case "quit":
                System.out.println("Client at address: " + client.getInetAddress() + ":" + client.getPort() + " closed");
                quitStuff();

                return false;
            default:
                toClient.writeObject("\n" + "Invalid command!\nSyntax: [command] [fileName]");
        }

        return true;
    }

    private void quitStuff() throws IOException {
        fromClient.close();
        toClient.close();
        Connection.removeElement(client);
        Connection.removeElementFromMap(client);
        Connection.removeElementFromClientStream(client);
        client.close();
    }


    // little method to enter and exit the crit. section and read from file. I added
    // it as getResponse() uses the same code twice
    /**
     * The function takes a semaphore and a file handler as parameters. It starts
     * the read operation,
     * reads the file, and ends the read operation
     * 
     * @param semaphore   a ReaderWriterSem object
     * @param fileHandler a class that handles the file operations
     * @return The response from the fileHandler.readFile() method.
     */
    private String readFile() throws FileNotFoundException{
        startRead();
        String response;
        response = fileHandler.readFile(fileName);
        endRead();
        if (response.length() == 0) {
            return "file empty, write...";
        } else {
            return response;
        }
    }

    /**
     * If the file is not in the hashmap, create a new semaphore for it and adds it
     * to the hashmap. If
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
