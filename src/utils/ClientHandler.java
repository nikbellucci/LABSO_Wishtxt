package utils;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class ClientHandler implements Runnable {
    private Socket s;
    private HashMap<String, ReaderWriterSem> criticHandle = new HashMap<>(); // string nomeFile
    private String[] spitArg=null,splitRequest=null;
    private String path = System.getProperty("user.dir") + File.separator + "data";

    public ClientHandler(Socket s, HashMap<String, ReaderWriterSem> criticHandle) {
        this.s = s;
        this.criticHandle = criticHandle;
    }

    /*
     * this method is called from the client thread.
     * 
     */
    @Override
    public void run() {
        try {
            ObjectOutputStream toClient = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream fromClient = new ObjectInputStream(s.getInputStream());
            while (true) {
                // Checking if the message from the client contains a colon. If it does not, it sends
                // an error message to the client.
                String message = (String) fromClient.readObject();
                if(!message.contains(":")){
                    toClient.writeObject("Invalid command! Syntax: [command]:[argument1];[argument2]");
                    continue;
                }
                // fixInputStream(message);
                try {
                    splitRequest = message.split(":", 2);
                    spitArg = splitRequest[1].split(";", 2);

                }catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                }
                if(!getResponse(toClient, fromClient))  
                    break;
            }
            toClient.close();
        }catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
    }

    /**
     * It gets a request from the client, splits the request into an array of strings, and then calls the
     * appropriate method of the FileHandler class
     * 
     * @param toClient the output stream to the client
     * @param fromClient the input stream from the client
     * @return The method returns a boolean value.
     */
    private boolean getResponse(ObjectOutputStream toClient, ObjectInputStream fromClient) throws IOException, ClassNotFoundException {
        FileHandler fileHandler = new FileHandler(path ); // Path di ogni sistema operativo
        if (splitRequest[0].equalsIgnoreCase("new"))
            toClient.writeObject("\n" +fileHandler.newFile(splitRequest[1]));
        else if (splitRequest[0].equalsIgnoreCase("rename")){ // METTERE UN IF SE NON ESISTE spitArg[1] MI DA ERRORE
        toClient.writeObject("\n" +fileHandler.renameFile(spitArg[0], spitArg[1]));
        }
        else if (splitRequest[0].equalsIgnoreCase("delete"))
            toClient.writeObject("\n" +fileHandler.deleteFile(splitRequest[1]));
        else if (splitRequest[0].equalsIgnoreCase("dir"))
            toClient.writeObject("\n" +fileHandler.getFilesName());
        else if (splitRequest[0].equalsIgnoreCase("edit")){
            ReaderWriterSem semaphore = getSemaphore();
            toClient.writeObject("\n" +readFile(semaphore, fileHandler));  //read file
            editFile(semaphore, fileHandler, fromClient,toClient);
            toClient.writeObject("\n" +"exiting editor...");
        }
        else if (splitRequest[0].equalsIgnoreCase("read")){
            ReaderWriterSem semaphore = getSemaphore();
            toClient.writeObject("\n" +readFile(semaphore, fileHandler));
        }
        else if (splitRequest[0].equalsIgnoreCase("quit"))  return false;
        else toClient.writeObject("\n" +"Unknown command");
        return true;
    }


    //internal loop for the edit mode
    /**
     * It reads a message from the client, 
     *  if the message is "backspace:" it calls the backspace function of the fileHandler, 
     *  if the message is "exit:" it breaks the loop, 
     *  otherwise it calls the writeLine function of the fileHandler
     * 
     * @param semaphore a ReaderWriterSem object
     * @param fileHandler is a class that handles the file, it has a method to write a line, a method
     * to delete a line and a method to read a line.
     * @param fromClient ObjectInputStream
     * @param toClient the output stream to the client
     */
    private void editFile(ReaderWriterSem semaphore, FileHandler fileHandler, ObjectInputStream fromClient, ObjectOutputStream toClient) throws IOException, ClassNotFoundException {
        semaphore.startWrite();
        while (true) {
            String message = (String) fromClient.readObject();
            if (message.equalsIgnoreCase("backspace:")) {
                fileHandler.backSpace(splitRequest[1]);
            }else if (message.equalsIgnoreCase("exit:"))
                break;
            else
                fileHandler.writeLine(splitRequest[1], message + "\n"); //TODO aggiungere \n alla prima riga

            toClient.writeObject("saved...");
        }
        semaphore.endWrite();
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
    private String readFile(ReaderWriterSem semaphore, FileHandler fileHandler){
        semaphore.startRead();       //start of critical section
        
        String response = fileHandler.readFile(splitRequest[1]);
        
        if(response.length() == 0){
            semaphore.endRead();
            return "file empty, write..";
        } else {
            semaphore.endRead();
            return response;
        }
        
    }

    /**
     * If the file is not in the hashmap, create a new semaphore for it and add it to the hashmap. If
     * it is in the hashmap, return the semaphore for that file
     * 
     * @return The semaphore for the file.
     */
    private synchronized ReaderWriterSem getSemaphore(){
        ReaderWriterSem fileSemaphore = criticHandle.get(splitRequest[1]); // checks if semaphoreHandler for file exists
        if(fileSemaphore == null) {
            fileSemaphore = new ReaderWriterSem();
            criticHandle.put(splitRequest[1], fileSemaphore);
        }
        return fileSemaphore;
    }

}
