package server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

//TODO: import package utils for ReaderWriterSem and ClientHandler


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

            //Ciclo di vita del thread principale del server
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
            //listener.close();

        } catch (IOException e) {
            System.err.println("Error during I/O operation:");
            e.printStackTrace();
        }
    }
}
