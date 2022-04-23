
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket s;
    private HashMap<String, ReaderWriterSem> criticHandle = new HashMap<>();
    private String[] spitArg=null,splitRequest=null;


    public ClientHandler(Socket s, HashMap<String, ReaderWriterSem> criticHandle) {
        this.s = s;
        this.criticHandle = criticHandle;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream toClient = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream fromClient = new ObjectInputStream(s.getInputStream());
            while (true) {

                String message = (String) fromClient.readObject();
                if(!message.contains(":")){
                    toClient.writeObject("Invalid command! Syntax: [command]:[argument1];[argument2]");
                    continue;
                }
                fixInputStream(message);
                if(!getResponse(toClient, fromClient))  break;
            }

            toClient.close();
        }catch (IOException | ClassNotFoundException e) {e.printStackTrace();}

    }

    private void fixInputStream(String input){

        try {
            splitRequest = input.split(":", 2);
            spitArg = splitRequest[1].split(";", 2);
        }catch (ArrayIndexOutOfBoundsException e){}
    }


    private boolean getResponse(ObjectOutputStream toClient, ObjectInputStream fromClient) throws IOException, ClassNotFoundException {
        FileHandler fileHandler = new FileHandler(/*Insert path here*/);

        if (splitRequest[0].equalsIgnoreCase("new"))
            toClient.writeObject("\n" + fileHandler.newFile(splitRequest[1]));
        else if (splitRequest[0].equalsIgnoreCase("rename"))
            toClient.writeObject("\n" +fileHandler.renameFile(spitArg[0], spitArg[1]));
        else if (splitRequest[0].equalsIgnoreCase("delete"))
            toClient.writeObject("\n" +fileHandler.deleteFile(splitRequest[1]));
        else if (splitRequest[0].equalsIgnoreCase("dir"))
            toClient.writeObject("\n" +fileHandler.getFilesName());
        else if (splitRequest[0].equalsIgnoreCase("edit")){
            ReaderWriterSem semaphore = getSemaphore();
            toClient.writeObject("\n" +readFile(semaphore, fileHandler));
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
    private void editFile(ReaderWriterSem semaphore, FileHandler fileHandler, ObjectInputStream fromClient, ObjectOutputStream toClient) throws IOException, ClassNotFoundException {
        semaphore.startWrite();
        while (true) {
            String message = (String) fromClient.readObject();
            System.out.println(message);
            if (message.equalsIgnoreCase("backspace:")) {
                fileHandler.backSpace(splitRequest[1]);
            }else if (message.equalsIgnoreCase("exit:"))
                break;
            else
                fileHandler.writeLine(splitRequest[1], message);

            toClient.writeObject("saved...");
        }
        semaphore.endWrite();
    }


    //little method to enter and exit the crit. section and read from file. I added it as getResponse() uses the same code twice
    private String readFile(ReaderWriterSem semaphore, FileHandler fileHandler){
        semaphore.startRead();       //start of critical section
        String response = fileHandler.readFile(splitRequest[1]);
        semaphore.endRead();
        return response;
    }

    private ReaderWriterSem getSemaphore(){
        ReaderWriterSem fileSemaphore = criticHandle.get(splitRequest[1]);     // checks if semaphoreHandler for file exists
        if(fileSemaphore == null) {
            fileSemaphore = new ReaderWriterSem();
            criticHandle.put(splitRequest[1], fileSemaphore);
        }
        System.out.println(criticHandle.size());
        return fileSemaphore;
    }


    //TODO: remove this method and the getters associated
    private String printSemaphores(){
        String rtrn="";
        for(Map.Entry<String, ReaderWriterSem> entry : criticHandle.entrySet()) {
            String key = entry.getKey();
            ReaderWriterSem value = entry.getValue();

            rtrn += "File: " + key;
            rtrn += "\nis in reading mode: " + value.isDbReading();
            rtrn += "\nnr. of readers: " + value.getReaderCount();
            rtrn += "\nis in writing mode: " + value.isDbWriting();
            rtrn += "\n--------------";

        }
        return rtrn;
    }

}
