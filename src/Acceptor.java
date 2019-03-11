/**
 * Acceptor class
 */

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Acceptor {
    private Map<Integer, AcceptorStore> logIdToStoreMap;
//    private int promisedId;
//    private int acceptedId;
//    private EventRecord acceptedER;
    private int nodeId;

    /* Constructor */
    public Acceptor(int node_id) {
        logIdToStoreMap = new HashMap<>();
//        promisedId = -1;
//        acceptedId = -1;
//        acceptedER = null;
        nodeId = node_id;
    }

    public void handlePrepare(PaxosMessage msg) {
        int pId = msg.getPId();
        int logId = msg.getLogId();
        if (!logIdToStoreMap.containsKey(logId)) {
            logIdToStoreMap.put(logId, new AcceptorStore());
        }
        AcceptorStore as = logIdToStoreMap.get(logId);

        if (pId < as.promisedId) {
            return;
        }
        as.promisedId = pId;

        int acceptedId = as.acceptedId;
        EventRecord acceptedER = as.acceptedER;

        PaxosMessage promiseMsg = new PaxosMessage(PaxosMessageType.PROMISE,
                pId, msg.getLogId(), acceptedId, nodeId, acceptedER);
        int proposerId = msg.getNodeId();
        NodeAddress proposerAddr = Constants.NODEID_ADDR_MAP.get(proposerId);
        try {
            System.out.println("Sending out promise msg for pId " + pId);
            promiseMsg.sendToAddr(proposerAddr.getIp(), proposerAddr.getPort());
        } catch (Exception e) {
            System.err.println("Send promise failed " + e);
        }
    }

    public void handlePropose(PaxosMessage msg) {
        System.out.println("Handling propose message");
        int logId = msg.getLogId();
        int msgPId = msg.getPId();

        int curPromisedId = logIdToStoreMap.getOrDefault(logId,
                new AcceptorStore()).promisedId;
        if (msgPId < curPromisedId) {
            System.out.println("Reject propose msg, pId = " + msgPId);
            return;
        }

        logIdToStoreMap.get(logId).acceptedId = msgPId;
        logIdToStoreMap.get(logId).promisedId = msgPId;
        logIdToStoreMap.get(logId).acceptedER = msg.getER();

        PaxosMessage acceptMsg = new PaxosMessage(PaxosMessageType.ACCEPT,
                msgPId, msg.getLogId(), msgPId, nodeId, msg.getER());
        int proposerId = msg.getNodeId();
        NodeAddress proposerAddr = Constants.NODEID_ADDR_MAP.get(proposerId);
        try {
            System.out.println("Sending accept msg");
            acceptMsg.sendToAddr(proposerAddr.getIp(), proposerAddr.getPort());
        } catch (Exception e) {
            System.out.println("Send accept failed");
        }
    }

    private class AcceptorStore {
        private int promisedId;
        private int acceptedId;
        private EventRecord acceptedER;

        /* Constructor */
        public AcceptorStore() {
            promisedId = -1;
            acceptedId = -1;
            acceptedER = null;
        }
    }
}
