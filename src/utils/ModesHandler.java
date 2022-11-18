package utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ModesHandler{
    ObjectInputStream fromClient;
    ObjectOutputStream toClient;
    FileHandler fileHandler;

    public ModesHandler(ObjectInputStream fromClient, ObjectOutputStream toClient, FileHandler fileHandler) {
        this.fromClient = fromClient;
        this.toClient = toClient;
        this.fileHandler = fileHandler;
    }

    // internal loop for the edit mode
    /**
     * It reads a message from the client,
     * if the message is "backspace:" it calls the backspace function of the
     * fileHandler,
     * if the message is "exit:" it breaks the loop,
     * otherwise it calls the writeLine function of the fileHandler
     *
     * This method is an internal loop of client-server communication, hence all
     * those passed variables.
     *
     * @param semaphore   a ReaderWriterSem object
     * @param fileHandler is a class that handles the file, it has a method to write
     *                    a line, a method
     *                    to delete a line and a method to read a line.
     * @param fromClient  ObjectInputStream
     * @param toClient    the output stream to the client
     */
    public void editFile(String fileName) throws IOException, ClassNotFoundException {
        while (true) {
            String message = (String) fromClient.readObject();
            if (message.equalsIgnoreCase(":backspace")) {
                fileHandler.backSpace(fileName);
                toClient.writeObject("delete last row");
            } else if (message.equalsIgnoreCase(":close"))
                break;
            else {
                fileHandler.writeLine(fileName, message + "\n");
                toClient.writeObject("you just wrote a line");
            }

        }
    }


    public String readFile(String fileName) throws IOException, ClassNotFoundException {
        String response;
        try{
            response = fileHandler.readFile(fileName);
        }catch (FileNotFoundException e) {
            return "File not found";
        }

        if (response.length() == 0) {
            return "file empty";
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
        return null;
    }

}
