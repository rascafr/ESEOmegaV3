package fr.bde_eseo.eseomega.events.tickets.model;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.events.tickets.EventItem;

/**
 * Created by Rascafr on 13/01/2016.
 */
public class TicketPictItem {

    private String imgUrl, title, description;
    private EventItem externalEventItem;

    public TicketPictItem(EventItem eventItem) {
        externalEventItem = eventItem;
        title = eventItem.getName();
        description = eventItem.getDetails();
        imgUrl = eventItem.getImgUrl();
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public EventItem getExternalEventItem() {
        return externalEventItem;
    }
}
