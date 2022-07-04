package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main (String args[]) {

        //Prendi in input l'indirizzo e la porta a cui connettersi
        if (args.length < 2) {
            System.err.println("Usage: java Client <host> <port>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            //Connettiti a host:port
            Socket s = new Socket(host, port);
            System.out.println("Connected");

            Scanner scan = new Scanner(System.in);
            ObjectOutputStream toServer = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream fromServer = new ObjectInputStream(s.getInputStream());

           // A while loop that reads the input from the user and sends it to the server.
           while(scan.hasNext()) {
                String msg = scan.nextLine();
                toServer.writeObject(msg);

                String message = (String) fromServer.readObject();
                System.out.println(message);

                if(scan.equals(":quit")) break;

            }
            fromServer.close();
            toServer.close();
            scan.close();
            System.out.println("Closed");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error during an I/O operation:");
            e.printStackTrace();
        }

    }
}
