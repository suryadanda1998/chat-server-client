package com.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class [Receiver] represents the receiver side of the chat client to
 * handle incoming traffic coming from the chat server
 *
 * @author bhavana
 */
public class Receiver extends Thread {

    // acts as a server to receive the incoming traffic
    private ServerSocket receiverSocket;

    // constructor
    public Receiver() {
        try {
            receiverSocket = new ServerSocket(ChatClient.clientNodeInfo.getPort());
            System.out.println("[Receiver.Receiver] Chat Client receiver socket created, listening on port " + ChatClient.clientNodeInfo.getPort());
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, "creating Chat Client receiver socket failed", ex);
        }

        System.out.println(ChatClient.clientNodeInfo.getName() + " listening on " + ChatClient.clientNodeInfo.getAddress() +
                ":" + ChatClient.clientNodeInfo.getPort());
    }

    // thread entrypoint
    @Override
    public void run() {
        // run server in the loop
        while (true) {
            try {
                new ReceiverWorker(receiverSocket.accept()).start();
            } catch (IOException ex) {
                System.err.println("[Receiver.run] warning: Error accepting the client");
            }
        }
    }
}
