package com.chat;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class [ReceiverWorker] handles the communication received fromm server based on [MessageType]
 *
 * @author bhavana
 */
public class ReceiverWorker extends Thread implements MessageTypes {

    // Server Connection
    private Socket serverConnection;

    // object stream to read and write data to the net
    private ObjectInputStream readFromNet;
    private ObjectOutputStream writeToNet;

    // reference to class [Message] Object
    private Message message;

    // constructor
    public ReceiverWorker(Socket serverConnection) {
        this.serverConnection = serverConnection;
        try {
            readFromNet = new ObjectInputStream(this.serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(this.serverConnection.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(ReceiverWorker.class.getName()).log(Level.SEVERE, "could not open object streams", ex);
        }
    }

    // thread entry point
    @Override
    public void run() {
        try {
            // read message
            message = (Message) readFromNet.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ReceiverWorker.class.getName()).log(Level.SEVERE, "Message could not be read", ex);

            // no use of going further
            System.exit(1);
        }

        // decide what to do depending on the type of message received from the server
        switch (message.getType())
        {
            case SHUTDOWN:
                System.out.println("Received shutdown message from Server, shutting down...");
                try {
                    serverConnection.close();
                } catch (IOException e) {
                    // something went wrong that we don't care as we are exiting anyway
                }
                System.exit(0);
                break;
            case NOTE:
                // display the note
                System.out.println((String) message.getContent());
                System.out.println();

                try {
                    serverConnection.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReceiverWorker.class.getName()).log(Level.WARNING, "Error in closing the server connection", ex);
                }
                break;
            default:
                Logger.getLogger(ReceiverWorker.class.getName()).log(Level.SEVERE, "This case should not occur");
        }
    }
}
