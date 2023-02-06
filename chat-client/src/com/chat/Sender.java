package com.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class [Sender] handles outgoing traffic for the client by processing the user input,
 * translates the user input into Message and sends them to the Chat Server
 *
 * @author dheeraj
 */
public class Sender extends Thread implements MessageTypes {

    // Server Connection
    Socket serverConnection = null;
    Scanner userInput = new Scanner(System.in);
    String inputData = null;

    // flag to indicate whether we have joined the chat or not
    boolean hasJoined = false;

    // object streams
    private ObjectOutputStream writeToNet;
    private ObjectInputStream readFromNet;

    // thread entry point
    @Override
    public void run() {

        // run loop forever until user enters SHUTDOWN or SHUTDOWN ALL
        while (true) {
            inputData = userInput.nextLine();

            if(inputData.startsWith("JOIN")) {
                handleJoinRequest(inputData);
            } else if(inputData.startsWith("LEAVE")) {
                handleLeaveRequest();
            } else if(inputData.startsWith("SHUTDOWN ALL")) {
                handleShutdownAllRequest();
            } else if(inputData.startsWith("SHUTDOWN")) {
                handleShutdownRequest();
            } else {
                handleNoteRequest(inputData);
            }
        }
    }

    /**
     * handles the JOIN request
     *
     * @param data contains information about user provide server IP and port
     */
    private void handleJoinRequest(String data) {
        // ignore, if already joined
        if(hasJoined == true) {
            System.err.println("You have already joined the chat");
            return;
        }

        // read user provided server IP and port
        String[] connectionInfo = data.split("[ ]+");

        // if the user provided details about server is valid, go ahead with those configs
        try {
            ChatClient.serverNodeInfo = new NodeInfo(connectionInfo[1], Integer.parseInt(connectionInfo[2]));
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
            Logger.getLogger(Sender.class.getName()).info("Server address and port Invalid; falling back to default configurations");
        }

        // check if user provided server information is valid or not
        if(ChatClient.serverNodeInfo == null) {
            System.err.println("[Sender].handleJoinCommand No server connectivity information found");
            return;
        }

        try {
            // open connection to server
            serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

            // open object streams
            readFromNet = new ObjectInputStream(serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

            // send join request
            writeToNet.writeObject(new Message(JOIN, ChatClient.clientNodeInfo));

            // close connection
            serverConnection.close();
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error Connecting to server or writing/reading object streams or closing connection", ex);
        }

        // we are in the chat
        hasJoined = true;
        System.out.println("Joined chat...");
    }

    /**
     * handles the LEAVE request
     */
    private void handleLeaveRequest() {
        // check if we are in the chat
        if(hasJoined == false) {
            System.err.println("You have not joined a chat yet...");
            return;
        }

        // send leave request
        try {
            // open connection to server
            serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

            // open object streams
            readFromNet = new ObjectInputStream(serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

            // send leave request
            writeToNet.writeObject(new Message(LEAVE, ChatClient.clientNodeInfo));

            // close connection
            serverConnection.close();
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error Connecting to server or writing/reading object streams or closing connection", ex);
        }

        // we are out
        hasJoined = false;
        System.out.println("Left chat..");
    }

    /**
     * handles the SHUTDOWN ALL request
     */
    private void handleShutdownAllRequest() {

        if(hasJoined == false) {
            System.err.println("To shutdown the whole chat, you need to first join the chat");
            return;
        }

        try {
            // open connection to server
            serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

            // open object streams
            readFromNet = new ObjectInputStream(serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

            // send shutdown all request
            writeToNet.writeObject(new Message(SHUTDOWN_ALL, ChatClient.clientNodeInfo));

            // close connection
            serverConnection.close();
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error Connecting to server or writing/reading object streams or closing connection", ex);
        }

        System.out.println("Sent shutdown all request...\n");
        System.exit(0);
    }

    /**
     * handles the SHUTDOWN request
     */
    private void handleShutdownRequest() {
        if(hasJoined == true) {
            System.err.println("To shutdown the chat, you need to first leave the chat");
            return;
        }

        try {
            // open connection to server
            serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

            // open object streams
            readFromNet = new ObjectInputStream(serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

            // send shutdown request
            writeToNet.writeObject(new Message(SHUTDOWN, ChatClient.clientNodeInfo));

            // close connection
            serverConnection.close();
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error Connecting to server or writing/reading object streams or closing connection", ex);
        }

        System.out.println("Left the chat...");
        System.out.println("Exiting...\n");
        System.exit(0);

    }

    /**
     * handles the NOTE request
     *
     * @param data to be sent as a content to the Chat Server
     */
    private void handleNoteRequest(String data) {
        // don't send note if user is not joined in the chat
        if(hasJoined == false) {
            System.err.println("To send a note, you need to first join the chat");
            return;
        }

        try {
            // open connection to server
            serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

            // open object streams
            readFromNet = new ObjectInputStream(serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());

            // send note request along with data
            writeToNet.writeObject(new Message(NOTE, "Message from " + ChatClient.clientNodeInfo.getName() + "\n" + data));

            // close connection
            serverConnection.close();
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error Connecting to server or writing/reading object streams or closing connection", ex);
        }

        System.out.println("Message sent...\n");
    }
}
