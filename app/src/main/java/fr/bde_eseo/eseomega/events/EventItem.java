package fr.bde_eseo.eseomega.events;

import android.content.Intent;
import android.provider.CalendarContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private final static String JSON_KEY_ITEM_NAME = "titre";
    private final static String JSON_KEY_ITEM_DETAIL = "detail";
    private final static String JSON_KEY_ITEM_DATE = "date";
    private final static String JSON_KEY_ITEM_CLUB = "club";
    private final static String JSON_KEY_ITEM_URL = "url";
    private final static String JSON_KEY_ITEM_LIEU = "lieu";
    private final static String JSON_KEY_ITEM_DATEFIN = "dateFin";
    private final static String JSON_KEY_ARRAY_COLOR = "color";

    private static final String HOUR_PASS_ALLDAY = "00:02";
    private static final int MAX_CHAR_DESC = 36;
    private String name, details, club, url, lieu;
    private boolean isHeader;
    private Date date, datefin;
    private int color; // aarrggbb, set alpha to 0xFF
    private String shorted;
    private Calendar cal, calFin;
    private boolean isPassed;
    private double price;

    public EventItem(JSONObject obj) throws JSONException {
        this(obj.getString(JSON_KEY_ITEM_NAME), obj.getString(JSON_KEY_ITEM_DETAIL), obj.getString(JSON_KEY_ITEM_DATE), obj.getString(JSON_KEY_ITEM_DATEFIN));
        this.setAdditionnal(
                obj.getString(JSON_KEY_ITEM_CLUB),
                obj.getString(JSON_KEY_ITEM_URL),
                obj.getString(JSON_KEY_ITEM_LIEU));
        this.performShortedDetails();

        JSONArray colorsJSON = obj.getJSONArray(JSON_KEY_ARRAY_COLOR);
        ArrayList<String> colors = new ArrayList<>();

        for (int a = 0; a < colorsJSON.length(); a++) {
            if (this.getDate().before(new Date())) {
                colors.add("127"); // Gray
                this.setIsPassed(true);
            } else {
                colors.add(colorsJSON.getInt(a)+""); // TODO pass integer directly without using string
                this.setIsPassed(false);
            }
        }

        this.setColors(colors);
    }

    public EventItem(String name, String details, boolean isHeader, Date date, int color) {
        this.name = name;
        this.details = details;
        this.isHeader = isHeader;
        this.date = date;
        this.color = color;
    }

    public EventItem(String name, String details, String strDate, String strDateFin) {
        this.name = name;
        this.details = details;

        // ARGB -> alpha 255
        setDateAsString(strDate, strDateFin);
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

    public void setColors (ArrayList<String> colors) {
        this.color = 0xFF000000 | (Integer.parseInt(colors.get(0)) << 16) | (Integer.parseInt(colors.get(1)) << 8) | (Integer.parseInt(colors.get(2)));
    }

    // Like : Heure · club · lieu · description (size limited -> ~35 chars)
    // V2.1 :
    // À : heure début si != 00:02 · Fin : date fin si != date debut + heure fin si != heure debut
    // Par : club si != null
    public void performShortedDetails () {
        String  sLim = "", sT = null; // Time cannot be null !

        String outFormat = "", stTime = getTimeAsString(this.date), endTime = getTimeAsString(this.datefin);
        String dayStartStr = getDayAsString(this.date), dayEndStr = getDayAsString(this.datefin);

        if (stTime.length() > 0) outFormat += "À : " + stTime;
        if (stTime.length() > 0 && (!endTime.equals(stTime) || !dayEndStr.equals(dayStartStr))) outFormat += " · Fin : ";
        if (!dayEndStr.equals(dayStartStr)) outFormat += dayEndStr + " ";
        if (!endTime.equals(stTime)) outFormat += endTime;
        if (outFormat.length() > 1 && club != null && club.length() > 0) outFormat += "\n";
        if (club != null && club.length() > 0) outFormat += "Par : " + club;

        this.shorted = outFormat;
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
    public String getTimeAsString (Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH':'mm", Locale.FRANCE);
        String sDate = sdf.format(d);
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

    public String getDayAsString(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMM", Locale.FRANCE);
        String sDate = sdf.format(d);
        if (sDate.equals(HOUR_PASS_ALLDAY))
            sDate = "";
        return sDate;
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

    public void setIsPassed(boolean isPassed) {
        this.isPassed = isPassed;
    }

    public boolean setIsPassed () {
        return isPassed;
    }
}
