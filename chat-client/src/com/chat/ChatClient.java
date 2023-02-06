package com.chat;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class [ChatClient] reads the connectivity information of the client and starts up the client
 * Starts the Sender and Receiver to handle incoming and outgoing traffic
 *
 * @author surya
 */
public class ChatClient implements Runnable {

    // static references to Sender and Receiver
    static Receiver receiver;
    static Sender sender;

    // client connectivity information (both for server and client)
    public static NodeInfo clientNodeInfo;
    public static NodeInfo serverNodeInfo;

    // constructor
    public ChatClient(String propertiesFile) {
        Properties properties = null;
        try {
            // get properties from the properties file
            properties = new PropertyHandler(propertiesFile);
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, "Could not open default properties file", ex);
            // abnormal termination of the program
            System.exit(1);
        }

        // get client port
        int clientPort = 0;
        try {
            clientPort = Integer.parseInt(properties.getProperty("MY_PORT"));
        } catch(NumberFormatException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, "Could not read client port", ex);
            System.exit(1);
        }

        // get Client name
        String clientName = properties.getProperty("MY_NAME");
        if(clientName == null) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, "Could not read client name");
            System.exit(1);
        }

        // create chat client connectivity NodeInfo
        clientNodeInfo = new NodeInfo(NetworkUtilities.getMyIP(), clientPort, clientName);

        // get server default port
        int serverPort = 0;
        try {
            serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));
        } catch(NumberFormatException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, "Could not read server port", ex);
        }
        // get server default IP address
        String serverIP = properties.getProperty("SERVER_IP");

        if(serverIP == null) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, "Could not read server IP");
        }

        // create chat server default connectivity NodeInfo
        if(serverPort != 0 && serverIP != null) {
            serverNodeInfo = new NodeInfo(serverIP, serverPort);
        }
    }

    // entry point to start the chat
    @Override
    public void run() {
        // start the Receiver
        (receiver = new Receiver()).start();
        // start the Sender
        (sender = new Sender()).start();
    }

    // Chat application main method
    public static void main(String[] args) {
        String propertiesFile;

        try{
            propertiesFile = args[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            propertiesFile = "resources"+ File.separator +"ChatApplicationDefaultConfig.properties";
        }
        // start the chat node
        new Thread(new ChatClient(propertiesFile)).start();
    }
}
