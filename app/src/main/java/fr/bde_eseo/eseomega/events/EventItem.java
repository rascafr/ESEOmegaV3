package fr.bde_eseo.eseomega.events;

import android.content.Intent;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Rascafr on 14/08/2015.
 */
public class EventItem {

    private static final String HOUR_PASS_ALLDAY = "00h02";
    private static final int MAX_CHAR_DESC = 36;
    private String name, details, club, url, lieu;
    private boolean isHeader;
    private Date date, datefin;
    private int color; // aarrggbb, set alpha to 0xFF
    private String shorted;
    private Calendar cal, calFin;


    public EventItem(String name, String details, boolean isHeader, Date date, int color) {
        this.name = name;
        this.details = details;
        this.isHeader = isHeader;
        this.date = date;
        this.color = color;
    }

    public EventItem(String name, String details, String strDate, String strDateFin, ArrayList<String> colors) {
        this.name = name;
        this.details = details;

        // ARGB -> alpha 255
        setDateAsString(strDate, strDateFin);
        this.color = 0xFF000000 | (Integer.parseInt(colors.get(0)) << 16) | (Integer.parseInt(colors.get(1)) << 8) | (Integer.parseInt(colors.get(2)));
        this.isHeader = false;

        // Calendar
        cal = new GregorianCalendar();
        cal.setTime(date);
        calFin = new GregorianCalendar();
        calFin.setTime(datefin);
    }

    public void setAdditionnal (String club, String url, String lieu) {
        this.club = club;
        this.url = url;
        this.lieu = lieu;
    }

    // Like : Heure · club · lieu · description (size limited -> ~35 chars)
    public void performShortedDetails () {
        String sTime = getTimeAsString(), sLim = "", sT = null; // Time cannot be null !
        boolean prevNotNull = false;
        ArrayList<String> strings = new ArrayList<>();
        strings.add(sTime); strings.add(club); strings.add(lieu); strings.add(details);
        for (int i=0;i<strings.size();i++) {

            sT = strings.get(i);

            // Add '*' if previously successfully added char is not null
            if (prevNotNull) {
                sLim += " · ";
            }

            if (sT != null && sT.length() != 0) {
                sLim += sT;
                prevNotNull = true;
            } else
                prevNotNull = false;
        }
        /*
        if (club != null && club.length() != 0) sLim += sTime.length()>0?" · ":"" + club;
        if (lieu != null && lieu.length() != 0) sLim += (club != null && club.length() != 0)?" · ":"" + lieu;
        if (details != null && details.length() != 0) sLim += (lieu != null && lieu.length() != 0)?" · ":"" + details;*/
        this.shorted = sLim;
    }

    // Get only, no operation -> faster in adapter
    public String getShortedDetails () {
        return shorted;
    }

    public EventItem(String name) {
        this.name = name;
        this.isHeader = true;
    }

    public Intent toCalendarIntent () {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, name);
        calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, (lieu!=null?lieu:""));
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, (details!=null?details:""));

        SimpleDateFormat sdf = new SimpleDateFormat("HH'h'mm", Locale.FRANCE);
        String sDate = sdf.format(this.date);
        boolean allDay = sDate.equals(HOUR_PASS_ALLDAY);
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, allDay);
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
        if (!allDay) calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calFin.getTimeInMillis());

        return calIntent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMonthHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.FRANCE);
        return sdf.format(this.date).toUpperCase(Locale.FRANCE);
    }

    // If equal to hour_pass (00h02 ?) set it all day, no specific hour
    public String getTimeAsString () {
        SimpleDateFormat sdf = new SimpleDateFormat("HH'h'mm", Locale.FRANCE);
        String sDate = sdf.format(this.date);
        if (sDate.equals(HOUR_PASS_ALLDAY))
            sDate = "";
        return sDate;
    }

    public void setDateAsString(String dateAsString, String dateAsStringEnd) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
        Date date = null;
        Date datefin = null;
        try {
            date = format.parse(dateAsString);
            datefin = format.parse(dateAsStringEnd);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.datefin = datefin;
        this.date = date;
    }

    // Number as String, why ? for TextView call !
    public String getDayNumero () {
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.FRANCE);
        return sdf.format(this.date);
    }

    public String getDayName () {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.FRANCE);
        return sdf.format(this.date);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public Date getDate() {
        return date;
    }

    public int getColor() {
        return color;
    }

    public String getUrl() {
        return url;
    }

    public String getLieu() {
        return lieu;
    }
}
