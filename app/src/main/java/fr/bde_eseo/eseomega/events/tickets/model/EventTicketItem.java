package fr.bde_eseo.eseomega.events.tickets.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.bde_eseo.eseomega.events.EventItem;

/**
 * Created by Rascafr on 11/01/2016.
 * Décrit un achat de place
 */
public class EventTicketItem {

    private String name, datetime, idevent, strcmd, frenchDate;
    private int idcmd, modcmd;
    private double price;
    private Date date;
    private boolean isHeader;
    private EventItem linkedEvent;
    private boolean passed;

    public EventTicketItem(JSONObject obj) throws JSONException {
        idevent = obj.getString("idevent");
        datetime = obj.getString("datetime");
        idcmd = obj.getInt("idcmd");
        modcmd = obj.getInt("modcmd");
        strcmd = obj.getString("strcmd");
        price = obj.getDouble("price");
        name = obj.getString("name");
        date = getParsedDate();
        frenchDate = getFrenchDate(false);
        isHeader = false;
        passed = false;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public boolean isPassed() {
        return passed;
    }

    public EventTicketItem(String name) {
        isHeader = true;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLinkedEvent(EventItem linkedEvent) {
        this.linkedEvent = linkedEvent;
    }

    public String getLinkedName() {
        return linkedEvent == null ? null : linkedEvent.getName();
    }

    public Date getLinkedDatefin() {
        return linkedEvent == null ? null : linkedEvent.getDatefin();
    }

    public Date getDate() {
        return date;
    }

    public String getDatetime() {
        return datetime;
    }

    public int getIdcmd() {
        return idcmd;
    }

    public String getIdevent() {
        return idevent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public String getFrenchDate() {
        return frenchDate;
    }

    public String getTicketNumberAsString() {
        return strcmd + new DecimalFormat("000").format(modcmd);
    }

    public Date getParsedDate () {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
        Date date = null;
        try {
            date = format.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String getFrenchDate (boolean simpleDate) {
        Date d = getParsedDate();
        SimpleDateFormat sdf;
        if (simpleDate)
            sdf = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE);
        else
            sdf = new SimpleDateFormat("E dd MMMM yyyy, 'à' HH:mm", Locale.FRANCE);
        return sdf.format(d);
    }

    public String getTicketPriceAsString(){
        return new DecimalFormat("0.00").format(price) + "€";
    }
}
