package client;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Main {
    public static void main(String args[]) {

        // Prendi in input l'indirizzo e la porta a cui connettersi
        if (args.length < 2) {
            System.err.println("Usage: java client.Main <host> <port>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            // Connettiti a host:port
            Socket socket = new Socket(host, port);
            System.out.println("Commands: create, rename, delete, list, edit, read, quit");
            System.out.println("Connected\n");

            ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
            Scanner scan = new Scanner(System.in);

            // A while loop that reads the input from the user and sends it to the server.
            // se esce dal cliclo while con un ctrl+c non manda un messaggio al server
            while (scan.hasNextLine()) {

                String msg = scan.nextLine();
                toServer.writeObject(msg);
                String message = (String) fromServer.readObject();

                if (!(msg.equals("quit") || message.equals("-1"))) {
                    System.out.println(message);
                } else {
                    if (message.equals("-1"))
                        System.out.println("Server closed");
                    scan.close();
                    fromServer.close();
                    toServer.close();
                    socket.close();
                    break;
                }
            }

            System.out.println("Client disconnected");
        } catch (ConnectException e) {
            System.out.println("Connection refused");
        } catch (EOFException e) {
            System.out.println("Client closed");
            // e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            System.out.println("The server was disconnected from socket");
        } catch (IOException e) {
            System.err.println("Error during an I/O operation:");
            e.printStackTrace();
        }

    }
}
