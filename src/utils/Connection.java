package utils;

import java.net.Socket;
import java.util.ArrayList;

public class Connection {
    private static ArrayList<Socket> clients = new ArrayList<Socket>();

    public static ArrayList<Socket> getClients() {
        // System.out.println(clients);
        return clients;
    }

    public static void addElement(Socket elementToAdd) {
        // System.out.println(elementToAdd);
        clients.add(elementToAdd);
        System.out.println("ArrayList = " + Connection.getClients());
    }

    public static void removeElement(Socket clientToRemove) {
        // System.out.println(clients.indexOf(clientToRemove));
        clients.remove(clients.indexOf(clientToRemove));
        System.out.println("ArrayList = " + Connection.getClients());
    }
}
