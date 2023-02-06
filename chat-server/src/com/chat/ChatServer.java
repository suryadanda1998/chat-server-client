package com.chat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class [ChatServer] starts up the Server to listen to the incoming traffic
 *
 * @author Surya
 */
public class ChatServer implements Runnable {

    // fetch serverIP
    String serverIP = NetworkUtilities.getMyIP();

    // list to store the participants info
    public static List<NodeInfo> participants = new ArrayList<>();

    @Override
    public void run() {
        try {
            // server socket creation
            ServerSocket serverSocket = new ServerSocket(5000, 50, InetAddress.getByName(serverIP));
            System.out.println("Server socket created, listening on port " + serverIP + ":" + 5000);
            while (true) {
                new ChatServerWorker(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, "Error: unable to start the server");
        }
    }

    // main()
    public static void main(String[] args) {
        // entry point for Chat server
        new Thread(new ChatServer()).start();
    }
}
