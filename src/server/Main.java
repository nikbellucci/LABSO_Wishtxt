package server;

import utils.*;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import utils.ClientHandler;
import utils.ReaderWriterSem;



public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Server <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        HashMap<String, ReaderWriterSem> critSecHndl = new HashMap<>();
        try {
            ServerSocket listener = new ServerSocket(port);
            File directory = new File(System.getProperty("user.dir") + File.separator + "data");
            if (! directory.exists()){
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
            }

            // Listening for new connections.
            while (true) {
                
                System.out.println("Listening...");
                Socket s = listener.accept(); //Connettiti a un client
                System.out.println("Connected");
                //Delega la gestione della nuova connessione a un thread ClientHandler dedicato
                Thread clientHandlerThread = new Thread(new ClientHandler(s, critSecHndl));
                clientHandlerThread.start();
                // FIXME il server processa solo il primo comando del client
                //E rimettiti in ascolto

            }

            //L'interruzione del server e la conseguente chiusura del ServerSocket non è implementata per semplicità

        } catch (IOException e) {
            System.err.println("Error during I/O operation:");
            e.printStackTrace();
        }
    }
}
