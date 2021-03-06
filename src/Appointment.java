/**
 *
 * Appointment object
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Appointment implements Serializable {
    private String id;
    private String name;
    private int day;
    private int startTime;
    private int endTime;
    private ArrayList<Integer> participantsId;
    private int initNode;

    /* Constructor */
    public Appointment(String i, String n, int d, int start, int end,
                       ArrayList<Integer> p, int in) {
        id = i;
        name = n;
        day = d;
        startTime = start;
        endTime = end;
        participantsId = new ArrayList<>(p);
        initNode = in;
    }

    /* Getters */
    public String getId() {
        return id;
    }

    public String getName() { return name; }

    public int getDay() { return day; }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public ArrayList<Integer> getParticipantsId() { return participantsId; }
}
