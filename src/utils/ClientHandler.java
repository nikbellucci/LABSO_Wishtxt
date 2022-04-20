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
            FileHandler fileHandler = new FileHandler(/*Insert path here*/"");
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            Scanner in = new Scanner(s.getInputStream());
            while (in.hasNextLine()) {
                String input = in.nextLine(),requestType=null;
                String[] spitArg=null,splitRequest=null;

                try {
                    splitRequest = input.split(":", 2);
                    requestType = splitRequest[0];
                    spitArg = splitRequest[1].split(";", 2);
                }catch (ArrayIndexOutOfBoundsException e){}

                if (requestType.equalsIgnoreCase("read")) 
                    out.println(fileHandler.readFile(splitRequest[1]));
                else if (requestType.equalsIgnoreCase("new"))  
                    out.println(fileHandler.newFile(splitRequest[1]));
                else if (requestType.equalsIgnoreCase("rename"))  
                    out.println(fileHandler.renameFile(spitArg[0],spitArg[1]));
                else if (requestType.equalsIgnoreCase("write"))
                    System.out.println("write");
                else if (requestType.equalsIgnoreCase("quit"))  break;
                else System.out.println("Unknown command");
            }

            out.close();
            in.close();
        }catch (IOException e) {e.printStackTrace();}

    }
}
