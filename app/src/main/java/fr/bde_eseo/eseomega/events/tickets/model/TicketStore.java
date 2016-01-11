package fr.bde_eseo.eseomega.events.tickets.model;

import android.util.Log;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.events.tickets.EventItem;

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

    /**
     * Token
     */
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Second reset function
     */
    public void resetOrder () {
        token = null;
    }

    public ArrayList<EventTicketItem> getEventTicketItems() {
        return eventTicketItems;
    }

    public ArrayList<EventItem> getEventItems() {
        return eventItems;
    }

    /**
     * Assigne aux ID tickets un nom
     */
    public void autoTicketAttributes () {

        // For each ticket
        for (int et=0;et<eventTicketItems.size();et++) {

            Log.d("DBG", "Ticket : " + eventTicketItems.get(et).getTicketNumberAsString());

            // For each event
            for (int ei=0;ei<eventItems.size();ei++) {

                Log.d("DBG", "Event : " + eventItems.get(ei).getName());

                // If event is not a header / content not null
                if (!eventItems.get(ei).isHeader() && eventItems.get(ei).getSubEventItems() != null) {

                    Log.d("DBG", "Event : not header / content not null");

                    // For each sub-event item
                    for (int si = 0; si < eventItems.get(ei).getSubEventItems().size(); si++) {

                        Log.d("DBG", "Sub-item : " + eventItems.get(ei).getSubEventItems().get(si).getTitre());

                        // If event if not header / non null content
                        if (eventItems.get(ei).getSubEventItems().get(si).getId().equals(eventTicketItems.get(et).getIdevent())) {
                            eventTicketItems.get(et).setLinkedEvent(eventItems.get(ei));
                            Log.d("DBG", "Linked : " + eventTicketItems.get(et).getTicketNumberAsString() + " ↔ " + eventItems.get(ei).getName());
                        }
                    }
                }
            }
        }
    }
}
