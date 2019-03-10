/**
 * Node class that include Node environment variables
 */

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node {
    private int nodeId;
    private Lock lock = new ReentrantLock();
    private Map<String, Appointment> apptIdMap;
    private String[][][] globalTimetable;
    private ArrayList<EventRecord> allEvents;

    private Proposer proposer;
    private Acceptor accepter;
    private Learner learner;

    private int localApptId;

    /* Constructor: initialize the environment */
    public Node(int id) {
        nodeId = id;
        try {
            deserializeEvents();
            deserializeCalendar();
        } catch (Exception e) {
            System.err.println("error");
            System.exit(1);
        }
        proposer = new Proposer(nodeId);
        accepter = new Acceptor(nodeId);
        learner = new Learner(this);

        lock.lock();
        localApptId = allEvents.size() + 1;
        lock.unlock();
    }

    /**
     *  Destructor */
    public void close() {
        try {
            serializeEvents();
            serializeCalendar();
        } catch (Exception e) {
            System.err.println("Serialization failure");
        }
    }

    /**** Getters ****/
    public int getNodeId() {
        return nodeId;
    }

    public Map<String, Appointment> getApptIdMap() {
        lock.lock();
        Map<String, Appointment> result = apptIdMap;
        lock.unlock();
        return result;
    }

    public String[][][] getGlobalTimetable() {
        lock.lock();
        String[][][] result = globalTimetable;
        lock.unlock();
        return result;
    }

    public ArrayList<EventRecord> getAllEvents() {
        lock.lock();
        ArrayList<EventRecord> result = allEvents;
        lock.unlock();
        return result;
    }

    public int getAllEventsSize() {
        lock.lock();
        int result = allEvents.size();
        lock.unlock();
        return result;
    }

    public Acceptor getAccepter() {
        return accepter;
    }

    public Proposer getProposer() {
        return proposer;
    }

    public Learner getLearner() {
        return learner;
    }

    /**** Setters ****/
    public boolean addToAllEvents(int index, EventRecord er) {
        lock.lock();
        while (allEvents.size() <= index) {
            allEvents.add(null);
        }
        if (allEvents.get(index) != null) {
            allEvents.add(index, er);
            lock.unlock();
            return true;
        }
        lock.unlock();
        return false;
    }

    /**
     * updateCalendar: provide a method for Learner to call to update calendar
     * with given EventRecord object that is already under consensus
     * @param er
     */
    public void updateCalendar(EventRecord er) {
        Appointment appt = er.getAppointment();
        System.out.println("appt = " + appt);
        switch (er.getOperation()) {
            case ADD:
                insertAppointment(appt);
                break;
            case DELETE:
                removeAppointment(appt);
                break;
            default:
                break;
        }
    }

    /**
     * addAppointment: add given appointment information to apptIdMap and
     * globalTimetable
     * @param name
     * @param day
     * @param start
     * @param end
     * @param p
     * @return
     */
    public boolean addAppointment(String name, int day, int start, int end,
                                   ArrayList<Integer> p) {
        String newApptId = generateNewApptId();
        System.out.println("Adding new appointment");
        Appointment newAppt = new Appointment(newApptId, name, day, start, end,
                p, nodeId);
        boolean addEventResult = false;
        lock.lock();
        int eventLogId = allEvents.size();
        lock.unlock();

        while (!hasConflict(newAppt) && addEventResult == false) {
            System.out.println("No conflict, start paxos");
            EventRecord newEvent = new EventRecord(EventOperation.ADD, 0,
                    nodeId, newAppt);
            int newEventLogId = allEvents.size();
            if (newEventLogId == eventLogId) {
                proposer.incrementPrepareId();
            } else {
                eventLogId = newEventLogId;
                proposer.restart();
            }
            addEventResult = proposer.initEvent(newEventLogId, newEvent);
        }

        return addEventResult;
    }

    public boolean deleteAppointment(String id) {
        lock.lock();
        if (!apptIdMap.containsKey(id)) {
            lock.unlock();
            return false;
        }
        Appointment deleteAppt = apptIdMap.get(id);
        boolean removeEventResult = false;
        int eventLogId = allEvents.size();

        while (apptIdMap.containsKey(id) && removeEventResult == false) {
            EventRecord newEvent = new EventRecord(EventOperation.DELETE, 0,
                    nodeId, deleteAppt);
            int newEventLogId = allEvents.size();
            if (newEventLogId == eventLogId) {
                proposer.incrementPrepareId();
            } else {
                eventLogId = newEventLogId;
                proposer.restart();
            }
            removeEventResult = proposer.initEvent(newEventLogId, newEvent);
        }

        lock.unlock();
        return removeEventResult;
    }

    public void displayCalendarAll() {
        for (int nodeId = 0; nodeId < Constants.NODE_COUNT; ++nodeId) {
            displayCalendar(nodeId);
        }
    }

    public void displayCalendar(int nodeId) {
        System.out.println("node: " + nodeId);
        System.out.printf("day/time ");
        for (int i = 0; i < Constants.SLOT_PER_DAY; ++i) {
            System.out.printf("%10s ", Integer.toString(i));
        }
        System.out.println();
        for (int i = 0; i < Constants.TOTAL_DAY; ++i) {
            System.out.printf("%8d ", i);
            for (int j = 0; j < Constants.SLOT_PER_DAY; ++j) {
                System.out.printf("%10s ", globalTimetable[nodeId][i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    public void updateMissingEvents() {
        lock.lock();
        int allEventsSize = allEvents.size();
        lock.unlock();
        int newLogId = allEventsSize;
        System.out.println("Get missing events, please wait...");
        while (true) {
            for (int i = 0; i < Constants.MISSING_EVENT_BATCH_SIZE; ++i) {
                PaxosMessage requestMsg = new PaxosMessage(
                        PaxosMessageType.LEARNER_REQUEST, -1, newLogId + i,
                        -1, nodeId, null);
                try {
                    requestMsg.sendToAll();
                } catch (Exception e) {
                    ;
                }
            }
            try {
                Thread.sleep(Constants.SLEEP_LENGTH);
            } catch (Exception e) {
                ;
            }
            lock.lock();
            int updatedAllEventsSize = allEvents.size();
            lock.unlock();
            if (updatedAllEventsSize == allEventsSize) {
                break;
            }
        }
        System.out.println("All events are update to date");
    }

    /**** Helper functions ****/
    private String generateNewApptId() {
        String id = "n" + Integer.toString(nodeId) + "a" +
                Integer.toString(localApptId);
        ++localApptId;
        return id;
    }

    private boolean hasConflict(Appointment appt) {
        int day = appt.getDay();
        int start = appt.getStartTime();
        int end = appt.getEndTime();
        ArrayList<Integer> participants = appt.getParticipantsId();
        lock.lock();
        for (Integer p: participants) {
            for (int i = start; i <= end; ++i) {
                if (globalTimetable[p][day][i] != null) {
                    lock.unlock();
                    return true;
                }
            }
        }
        lock.unlock();
        return false;
    }

    private void insertAppointment(Appointment appt) {
//        System.out.println("Inserting appt");
        String apptId = appt.getId();
        int day = appt.getDay();
        int start = appt.getStartTime();
        int end = appt.getEndTime();
        ArrayList<Integer> participants = appt.getParticipantsId();
        apptIdMap.put(apptId, appt);
        for (Integer p: participants) {
            for (int i = start; i <= end; ++i) {
                globalTimetable[p][day][i] = apptId;
            }
        }
    }

    private void removeAppointment(Appointment appt) {
        String apptId = appt.getId();
        int day = appt.getDay();
        int start = appt.getStartTime();
        int end = appt.getEndTime();
        ArrayList<Integer> participants = appt.getParticipantsId();
        lock.lock();
        for (Integer p: participants) {
            for (int i = start; i <= end; ++i) {
                globalTimetable[p][day][i] = null;
            }
        }
        apptIdMap.remove(apptId);
        lock.unlock();
    }

    /**
     * deserializeEvents: initialize the object variable allEvents
     * @return
     * @throws Exception
     */
    private boolean deserializeEvents() throws Exception {
        String filename = Constants.EVENTRECORD_FILENAME;
        File fd = new File(filename);
        if (fd.exists()) {
            FileInputStream fileIn = null;
            try {
                fileIn = new FileInputStream(fd);
            } catch (Exception e) {
                throw new Exception("Cannot create FileInputStream object");
            }

            ObjectInputStream objIn = null;
            try {
                objIn = new ObjectInputStream(fileIn);
            } catch (Exception e) {
                throw new Exception("Cannot create ObjectInputStream");
            }

            allEvents = (ArrayList)objIn.readObject();
        } else {
            allEvents = new ArrayList<>();
        }
        return true;
    }

    /**
     * deserializeCalendar: initialize the object variables apptIdMap and
     * globalTimetable
     * @return
     * @throws Exception
     */
    private boolean deserializeCalendar() throws Exception {
        globalTimetable = new String[3][Constants.TOTAL_DAY][Constants.SLOT_PER_DAY];
        apptIdMap = new HashMap<>();

        String filename = Constants.CALENDAR_FILENAME;
        File fd = new File(filename);
        if (fd.exists()) {
            FileInputStream fileIn = null;
            try {
                fileIn = new FileInputStream(fd);
            } catch (Exception e) {
                throw new Exception("Cannot create FileInputStream object");
            }

            ObjectInputStream objIn = null;
            try {
                objIn = new ObjectInputStream(fileIn);
            } catch (Exception e) {
                throw new Exception("Cannot create ObjectInputStream");
            }

            apptIdMap = (Map<String, Appointment>)objIn.readObject();

            for (Map.Entry<String, Appointment> pair: apptIdMap.entrySet()) {
                Appointment appt = pair.getValue();
                String apptId = appt.getId();
                int day = appt.getDay();
                int start = appt.getStartTime();
                int end = appt.getEndTime();
                ArrayList<Integer> participants = appt.getParticipantsId();
                for (Integer p: participants) {
                    for (int i = start; i <= end; ++i) {
                        globalTimetable[p][day][i] = apptId;
                    }
                }
            }
        }
        return true;
    }

    /**
     * serializeEvents: store object variable allEvents to file
     * @return
     * @throws Exception
     */
    private boolean serializeEvents() throws Exception {
        String filename = Constants.EVENTRECORD_FILENAME;
        File fd = new File(filename);
        FileOutputStream fileOut = new FileOutputStream(fd);
        ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
        objOut.writeObject(allEvents);
        return true;
    }

    /**
     * serializeCalendar: store object variable apptIdMap to file
     * @return
     * @throws Exception
     */
    private boolean serializeCalendar() throws Exception {
        String filename = Constants.CALENDAR_FILENAME;
        File fd = new File(filename);
        FileOutputStream fileOut = new FileOutputStream(fd);
        ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
        objOut.writeObject(apptIdMap);
        return true;
    }
}
