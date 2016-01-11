package fr.bde_eseo.eseomega.events.tickets.model;

import android.util.Log;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.events.EventItem;

/**
 * Created by Rascafr on 11/01/2016.
 */
public class TicketStore {

    private static TicketStore instance;

    private TicketStore (){}

    public static TicketStore getInstance() {
        if (instance == null)
            instance = new TicketStore();
        return instance;
    }

    /**
     * The event list
     */
    private ArrayList<EventItem> eventItems;

    /**
     * The ticket list
     */
    private ArrayList<EventTicketItem> eventTicketItems;

    /**
     * Reset function
     */
    public void reset () {
        if (eventItems == null) eventItems = new ArrayList<>();
        eventItems.clear();
        if (eventTicketItems == null) eventTicketItems = new ArrayList<>();
        eventTicketItems.clear();
    }

    public ArrayList<EventTicketItem> getEventTicketItems() {
        return eventTicketItems;
    }

    public ArrayList<EventItem> getEventItems() {
        return eventItems;
    }

    public void describe() {

        for (int i=0;i<eventItems.size();i++) {
            Log.d("DSC", "Event : " + eventItems.get(i).getName() + "#" + eventItems.get(i).getIdevent());
        }
        for (int i=0;i<eventTicketItems.size();i++) {
            Log.d("DSC", "Ticket : " + eventTicketItems.get(i).getTicketNumberAsString() + "#" + eventTicketItems.get(i).getIdevent());
        }

    }

    /**
     * Assigne aux ID tickets un nom
     */
    public void autoTicketAttributes () {
        for (int et=0;et<eventTicketItems.size();et++) {
            for (int ei=0;ei<eventItems.size();ei++) {
                if (eventItems.get(ei).getIdevent() != null && eventItems.get(ei).getIdevent().equals(eventTicketItems.get(et).getIdevent())) {
                    eventTicketItems.get(et).setName(eventItems.get(ei).getName());
                }
            }
        }
    }
}
