package utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    
    private Socket s;


    public ClientHandler(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            Scanner fromClient = new Scanner(s.getInputStream());
            PrintWriter toClient = new PrintWriter(s.getOutputStream(), true);

            while (true) {
                String request = fromClient.nextLine();
                String[] splitRequest = request.split(":", 2);
                String requestType = splitRequest[0];
                String requestArg = splitRequest.length > 1 ? splitRequest[1] : "";

                if (requestType.equals("read")) {
                    System.out.println("read");
                } else if (requestType.equals("write")) {
                    System.out.println("write");
                } else if (requestType.equals("quit")) {
                    break;
                } else {
                    System.out.println("Unknown command");
                }
            }

            s.close();
            toClient.println("Client terminated");

        } catch (IOException e) {
            System.err.println("Error during I/O operation:");
            e.printStackTrace();
        }
    }
}
