package com.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class [ChatServerWorker] handles all the incoming and outgoing traffic
 *
 * @author Surya
 */
public class ChatServerWorker extends Thread implements MessageTypes {

    // chat client connection
    private Socket chatConnection;

    // object streams
    private ObjectOutputStream writeToNet;
    private ObjectInputStream readFromNet;

    // reference for Message object
    private Message message;

    // constructor
    public ChatServerWorker(Socket chatClientConnection) {
        chatConnection = chatClientConnection;
    }

    // entry point for the [ChatServerWorker] class
    @Override
    public void run() {
        NodeInfo participantsInfo = null;
        Iterator<NodeInfo> participantIterator;

        try {
            // open object streams
            writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
            readFromNet = new ObjectInputStream(chatConnection.getInputStream());

            // read message from the object stream
            message = (Message) readFromNet.readObject();

            // close the chat client connection
            chatConnection.close();
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("[ChatServerWorker.run] Failed to open object streams");
            System.exit(1);
        }

        switch(message.getType()) {
            case JOIN:
                // read participant's info
                NodeInfo joiningParticipantInfo = (NodeInfo) message.getContent();
                // add participant info to the participants' list
                ChatServer.participants.add(joiningParticipantInfo);
                // show who joined
                System.out.println(joiningParticipantInfo.getName() + " joined. All current participants: ");

                participantIterator = ChatServer.participants.iterator();
                // print out all the participants
                while (participantIterator.hasNext()) {
                    participantsInfo = participantIterator.next();
                    System.out.print(participantsInfo.getName() + " ");
                }
                System.out.println();
                break;
            case LEAVE:
            case SHUTDOWN:
                // remove this participant's info
                NodeInfo leavingParticipantInfo = (NodeInfo) message.getContent();

                if(ChatServer.participants.remove(leavingParticipantInfo)) {
                    System.err.println(leavingParticipantInfo.getName() + " removed");

                    // show who left
                    System.out.println(leavingParticipantInfo.getName() + " left. Remaining participants: ");
                } else {
                    System.err.println(leavingParticipantInfo.getName() + " not found");
                    return;
                }

                // print out all the remaining participants
                participantIterator = ChatServer.participants.iterator();
                while (participantIterator.hasNext()) {
                    participantsInfo = participantIterator.next();
                    System.out.print(participantsInfo.getName() + " ");
                }
                System.out.println();
                break;
            case SHUTDOWN_ALL:
                // get all the participants info
                participantIterator = ChatServer.participants.iterator();
                // run through all the participants and send the note to every single participant
                while (participantIterator.hasNext()) {
                    // get next participant
                    participantsInfo = participantIterator.next();

                    try {
                        // open socket to client
                        chatConnection = new Socket(participantsInfo.getAddress(), participantsInfo.getPort());

                        // open object streams
                        writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
                        readFromNet = new ObjectInputStream(chatConnection.getInputStream());

                        // send shutdown message
                        writeToNet.writeObject(new Message(SHUTDOWN, null));

                        // close the connection
                        chatConnection.close();

                    } catch (IOException ex) {
                        Logger.getLogger(ChatServerWorker.class.getName()).log(Level.SEVERE, "Error in sending SHUTDOWN request to the clients", ex);
                    }
                }
                System.out.println("Shutting down all the clients..");
                // now exit myself
                System.exit(0);
            case NOTE:
                // show the note
                System.out.println((String) message.getContent());

                // get all the participants info
                participantIterator = ChatServer.participants.iterator();

                // Iterate through all the participants and send the note every single participant.
                while (participantIterator.hasNext()) {
                    // get next participant
                    participantsInfo = participantIterator.next();

                    try {
                        // open socket to client
                        chatConnection = new Socket(participantsInfo.getAddress(), participantsInfo.getPort());

                        // open object streams to write and read the data
                        writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
                        readFromNet = new ObjectInputStream(chatConnection.getInputStream());

                        // write message
                        writeToNet.writeObject(message);

                        // close the connection
                        chatConnection.close();

                    } catch (IOException ex) {
                        Logger.getLogger(ChatServerWorker.class.getName()).log(Level.SEVERE, "Error in sending Note to the clients", ex);
                    }
                }
                break;
            default:
                // it cannot occur
                System.err.println("not a valid request");

        }
    }
}
