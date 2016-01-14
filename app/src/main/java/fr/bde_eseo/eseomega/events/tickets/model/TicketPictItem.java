package fr.bde_eseo.eseomega.events.tickets.model;

import java.text.DecimalFormat;

import fr.bde_eseo.eseomega.events.EventItem;

/**
 * Created by Rascafr on 13/01/2016.
 */
public class TicketPictItem {

    private String imgUrl, title, description, lowPrice;
    private EventItem externalEventItem;
    private double bestPrice;

    public TicketPictItem(EventItem eventItem) {
        externalEventItem = eventItem;
        title = eventItem.getName();
        description = eventItem.getDetails();
        imgUrl = eventItem.getImgUrl();

        bestPrice = 0;
        for (int i=0;i<externalEventItem.getSubEventItems().size();i++) {
            double p = externalEventItem.getSubEventItems().get(i).getPrice();
            if (bestPrice == 0 || p < bestPrice)
                bestPrice = p;
        }

        lowPrice = new DecimalFormat("0.00").format(bestPrice) + "€";
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

    public String getLowPrice() {
        return lowPrice;
    }
}
