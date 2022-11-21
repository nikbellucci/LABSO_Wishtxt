package server;

import utils.*;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class Main {

    
    /** 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java server.Main <path> <port>");
            return;
        }
        String path = System.getProperty("user.dir") + File.separator + args[0];
        File directory = new File(path);
        int port = Integer.parseInt(args[1]);

        HashMap<String, ReaderWriterSem> critSecHndl = new HashMap<>();
        try {
            ServerSocket listener = new ServerSocket(port);
            if (!directory.exists()) {
                directory.mkdir();
            }
            Thread utilityHandler = new Thread(new ServerHandler(listener, directory));
            utilityHandler.start();

            while (true) {
                try {
                    System.out.println("Listening...");
                    // Connection to the requesting client.
                    Socket client = listener.accept();
                    Connection.addElement(client);
                    System.out.println("Connected");
                    // Delegate handling of the new connection to a dedicated ClientHandler thread.
                    Thread clientHandlerThread = new Thread(new ClientHandler(client, critSecHndl, path));
                    clientHandlerThread.start();
                } catch (SocketException e) {
                    break;
                }

            }
            System.out.println("Server closed");
        } catch (IOException e) {
            System.err.println("Error during I/O operation");
        }
    }

}
