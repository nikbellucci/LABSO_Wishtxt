package client;

import java.io.IOException;
import java.io.PrintWriter;
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

            Scanner fromServer = new Scanner(s.getInputStream()); //Canale di ricezione dal server
            PrintWriter toServer = new PrintWriter(s.getOutputStream(), true); //Canale di invio verso il server

            Scanner userInput = new Scanner(System.in); //Lettura dell'input da terminale

            //Ciclo di vita del client
            while (true) {
                String request = userInput.nextLine(); //Leggi la richiesta dell'utente...
                toServer.println(request); //... e inoltrala al server
                if (request.equals("quit")) {
                    //Se l'utente chiede di uscire, termina il ciclo while
                    break;
                }
                String response = fromServer.nextLine(); //Leggi la risposta del server...
                System.out.println(response); //... e stampala sul terminale
            }

            //Prima di arrestare il client, chiudi la connessione e lo scanner
            s.close();
            userInput.close();
            System.out.println("Closed");

        } catch (IOException e) {
            System.err.println("Error during an I/O operation:");
            e.printStackTrace();
        }
        
    }
}
