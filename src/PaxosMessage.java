/**
 * PaxosMessage class
 */

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;

public class PaxosMessage implements Serializable {
    private PaxosMessageType msgType;
    private int pId;
    private int logId;
    private int acceptedId;
    private int nodeId;
    private EventRecord er;

    /* Constructor */
    public PaxosMessage(PaxosMessageType tp, int p_id, int log_id,
                        int accepted_id, int node_id,
                        EventRecord event_record) {
        msgType = tp;
        pId = p_id;
        logId = log_id;
        acceptedId = accepted_id;
        nodeId = node_id;
        er = event_record;
    }

    /* Getters */
    public PaxosMessageType getMsgType() {
        return msgType;
    }

    public int getPId() {
        return pId;
    }

    public int getLogId() {
        return logId;
    }

    public int getPromisedId() {
        return acceptedId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public EventRecord getER() {
        return er;
    }

    public void sendToAll() {
        for (NodeAddress addr: Constants.NODEID_ADDR_MAP.values()) {
//            System.out.println("send to ip: " + addr.getIp() + ", port: " +
//                    addr.getPort());
            sendToAddr(addr.getIp(), addr.getPort());
        }
    }

    public void sendToAddr(String ip, int port) {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("sendToAddr sleep failed " + e);
        }
        Socket socket = null;
        ObjectOutputStream oos = null;
        try {
            socket = new Socket(ip, port);
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(this);
        } catch (Exception e) {
            System.err.println("sendToAddr " + ip + ", port: " + port +
                    ", failed + " + e);
            return;
        }
        try {
            oos.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("oos or socket close failed. " + e);
        }
    }
}
