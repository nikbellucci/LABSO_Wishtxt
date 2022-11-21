/**
 * It's a class that manages the connection between the server and the clients
 */
package utils;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It's a class that contains a list of all the clients connected to the server
 * and a hashmap of all
 * the clients connected to the server
 */
public class Connection {
    private static ArrayList<Socket> clients = new ArrayList<Socket>();
    private static ConcurrentHashMap<Socket, Integer> clientHashmap = new ConcurrentHashMap<Socket, Integer>();
    private static ConcurrentHashMap<Socket, ObjectOutputStream> clientStream = new ConcurrentHashMap<Socket, ObjectOutputStream>();

    /**
     * It returns the list of clients
     * 
     * @return The ArrayList of clients.
     */
    public static ArrayList<Socket> getClients() {
        return clients;
    }

    /**
     * It adds a socket to the ArrayList
     * 
     * @param elementToAdd the Socket to add to the ArrayList
     */
    public static void addElement(Socket elementToAdd) {
        clients.add(elementToAdd);
    }

    /**
     * It removes the client from the ArrayList of clients
     * 
     * @param clientToRemove the socket to remove from the ArrayList
     */
    public static void removeElement(Socket clientToRemove) {
        clients.remove(clients.indexOf(clientToRemove));
    }

    /**
     * It returns a HashMap of all the clients connected to the server
     * 
     * @return A HashMap of Socket and Integer.
     */
    public static ConcurrentHashMap<Socket, Integer> getClientsMap() {
        return clientHashmap;
    }

    /**
     * This function takes a socket as a parameter and adds it to the clientHashmap
     * with a value of 0
     * 
     * @param socket The socket that the client is connected to.
     */
    public static void initializeClientOnMap(Socket elementToAdd) {
        clientHashmap.put(elementToAdd, 0);
    }

    /**
     * This function is called when the client is writing a message. It changes the
     * value of the
     * client's socket in the hashmap to 2
     * 
     * @param socket The socket that is being written to.
     */
    public static void isWriting(Socket socket) {
        clientHashmap.replace(socket, 2);
    }

    /**
     * If the client is reading, then the value of the socket in the hashmap is set
     * to 1
     * 
     * @param socket The socket that is being read from.
     */
    public static void isReading(Socket socket) {
        clientHashmap.replace(socket, 1);
    }

    /**
     * If the client is idle, then the value of the client in the hashmap is set to
     * 0
     * 
     * @param socket The socket that is being checked.
     */
    public static void isIdle(Socket socket) {
        clientHashmap.replace(socket, 0);
    }

    /**
     * This function removes the socket from the hashmap
     * 
     * @param socket The socket that is to be removed from the hashmap.
     */
    public static void removeElementFromMap(Socket clientToRemove) {
        clientHashmap.remove(clientToRemove);
    }

    
    /** 
     * @param socket
     * @return ObjectOutputStream
     */
    public static ObjectOutputStream getElementOnClientStream(Socket socket) {
        return clientStream.get(socket);
    }

    
    /** 
     * @param socket
     * @param toClient
     */
    public static void addElementOnClientStream(Socket socket, ObjectOutputStream toClient) {
        clientStream.put(socket, toClient);
    }

    
    /** 
     * @param socket
     */
    public static void removeElementFromClientStream(Socket socket) {
        clientStream.remove(socket);
    }

    
    /** 
     * @param client
     * @return boolean
     */
    public static boolean isInClientHashmap(Socket client) {
        if (clientHashmap.get(client) != null) {
            return true;
        } else {
            return false;
        }
    }

    
    /** 
     * @param client
     * @return boolean
     */
    public static boolean isInClientStream(Socket client) {
        if (clientStream.get(client) != null) {
            return true;
        } else {
            return false;
        }

    }

    public static void flushSocket() {
        for (Socket socket : clients) {
            if (isInClientHashmap(socket) && isInClientStream(socket)) {
                System.out.println("esiste");
            } else {
                removeElement(socket);
            }
        }
    }

    
    /** 
     * @param socket
     */
    public static void removeClientConnection(Socket socket) {
        removeElementFromClientStream(socket);
        removeElementFromMap(socket);
    }
}
