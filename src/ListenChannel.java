/**
 * ListenChannel class for multithreading
 */

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class ListenChannel extends Thread {
    private ServerSocket server;
    private Node node;

    /* Constructor */
    public ListenChannel(ServerSocket s, Node n) {
        server = s;
        node = n;
    }

    public void run() {
        while (true) {
            try {
                Socket socket = server.accept();
                ObjectInputStream ois = new ObjectInputStream(
                        socket.getInputStream());
                PaxosMessage paxosMsg = (PaxosMessage)ois.readObject();
                PaxosMessageType type = paxosMsg.getMsgType();
                System.out.println("Received paxos message " + type);
                // ois.close();
                switch (type) {
                    case PREPARE:
                        node.getAccepter().handlePrepare(paxosMsg);
                        break;
                    case PROMISE:
                        node.getProposer().handlePromise(paxosMsg);
                        break;
                    case PROPOSE:
                        node.getAccepter().handlePropose(paxosMsg);
                        break;
                    case ACCEPT:
                        node.getProposer().handleAccept(paxosMsg);
                        break;
                    case LEARNER_NOTICE:
                        node.getLearner().handleLearnerNotice(paxosMsg);
                        break;
                    case LEARNER_REQUEST:
                        node.getLearner().handleLearnerRequest(paxosMsg);
                        break;
                    default:
                        break;
                }
                ois.close();
                socket.close();
            } catch (Exception e) {
                System.out.println("receiving failed " + e);
            }
        }
    }
}
