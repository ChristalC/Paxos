/**
 * Constants class: keeps all the constant variables
 */

import java.util.Map;
import static java.util.Map.entry;

public class Constants {
    public static final Map<Integer, NodeAddress> NODEID_ADDR_MAP =
            Map.ofEntries(
                    entry(0, new NodeAddress("Localhost", 5001)),
                    entry(1, new NodeAddress("Localhost", 5002)),
                    entry(2, new NodeAddress("Localhost", 5003))
            );

//    public static final Map<Integer, NodeAddress> NODEID_ADDR_MAP =
//            Map.ofEntries(
//                    entry(0, new NodeAddress("localhost", 5001))
//            );


    public static final int NODE_COUNT = NODEID_ADDR_MAP.size();
    public static final int MAJORITY_COUNT = NODE_COUNT / 2 + 1;

    public static final String CALENDAR_FILENAME = "calendar.ser";
    public static final String EVENTRECORD_FILENAME = "log.ser";
//    public static final int TOTAL_DAY = 7;
//    public static final int SLOT_PER_DAY = 48;
    public static final int TOTAL_DAY = 2;
    public static final int SLOT_PER_DAY = 12;

    public static final int PREPARE_ID_INCREMENT = NODE_COUNT;
    public static final int WAIT_TIMEOUT = 5;   // Seconds
    public static final int NULL_ID = -1;

    public static final int MISSING_EVENT_BATCH_SIZE = 10;
    public static final int SLEEP_LENGTH = 5000;    // Milliseconds
}
