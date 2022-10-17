/**
 * It's a thread that listens to the server's input and executes the command
 */
package utils;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * > This class is a Runnable that handles a client connection
 */
public class ServerHandler implements Runnable {

    private ServerSocket listener;
    private File directory;
    private ObjectOutputStream toClient = null;

    // It's a constructor that initializes the listener and the directory
    public ServerHandler(ServerSocket listener, File directory) {
        this.listener = listener;
        this.directory = directory;
    }

    /**
     * The function runs a loop that waits for user input. If the user inputs
     * "quit", the program
     * terminates. If the user inputs "info", the program prints out the current
     * state of the program
     */
    @Override
    public void run() {
        Scanner scannerStr = new Scanner(System.in);
        while (true) {
            String inputString = scannerStr.nextLine();
            if (inputString.equals("quit")) {
                // se non ci sono client
                killer();
                scannerStr.close();
                break;
            } else if (inputString.equals("info")) {
                info();
            } else {
                System.out.println("Syntax error");
            }
        }
    }

    /**
     * It closes all the sockets and the listener
     */
    public void killer() {
        try {
            if (Connection.getClients().size() > 0) {
                // System.out.println("size: " + Connection.getClients().size());
                for (Socket client : Connection.getClients()) {
                    toClient = Connection.getElementOnClientStream(client);
                    toClient.writeObject("-1");
                    toClient.close();
                    Connection.removeElementFromMap(client);
                    Connection.removeElementFromClientStream(client);
                    client.close();
                    System.out.println(
                            "Client at address: " + client.getInetAddress() + ":" + client.getPort() + " closed");
                }
            }
            this.listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) { // errore qui, entra in deadlock
            e2.printStackTrace();
        }

    }

    /**
     * It counts the number of files in the directory, the number of clients in idle
     * mode, the number
     * of clients in reading mode and the number of clients in writing mode
     */
    public void info() {
        int clientsReader = 0;
        int clientsWriter = 0;
        int clientsIdle = 0;
        for (int value : Connection.getClientsMap().values()) {
            if (value == 0) {
                clientsIdle++;
            } else if (value == 1) {
                clientsReader++;
            } else if (value == 2) {
                clientsWriter++;
            }
        }
        System.out.println("---------------------INFO---------------------");
        System.out.println("|   Number of file(s) in the directory: " + directory.list().length);
        System.out.println("|   Number of client(s) in idle mode: " + clientsIdle);
        System.out.println("|   Number of client(s) in reading mode: " + clientsReader);
        System.out.println("|   Number of client(s) in writing mode: " + clientsWriter);
        System.out.println("|   Number of total client(s) connected: " + (clientsWriter + clientsIdle + clientsReader));
        System.out.println("----------------------------------------------");
    }
}
