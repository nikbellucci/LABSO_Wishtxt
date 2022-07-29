package utils;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerHandler implements Runnable {

    private ServerSocket listener;
    private File directory;

    public ServerHandler(ServerSocket listener, File directory) {
        this.listener = listener;
        this.directory = directory;
    }

    @Override
    public void run() {
        Scanner scannerStr = new Scanner(System.in);
        while (true) {
            String inputString = scannerStr.nextLine();
            if (inputString.equals("quit")) {
                killer();
                scannerStr.close();
                break;  
            } else if (inputString.equals("info")) {
                info();
            }
        }
        
    }

    public void killer() {
        try {
            if (Connection.getClients().size() > 0) {
                System.out.println("size: " + Connection.getClients().size());
                for (Socket client : Connection.getClients()) {
                    client.close();
                    System.out.println("Client at address: " + client.getInetAddress() + ":" + client.getPort() + " closed");
                }
            }
            this.listener.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void info() {
        System.out.println("Number of file(s) in the directory: " + directory.list().length);
    }
}
