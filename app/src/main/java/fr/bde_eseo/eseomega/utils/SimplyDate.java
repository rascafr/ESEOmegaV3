package fr.bde_eseo.eseomega.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rascafr on 23/12/2015.
 * Simple date / time delay container
 */
public class SimplyDate {
    private long hours, days, weeks, months;
    private Date remote_date;
    private SimpleDateFormat simpleDateFormat;

    private static final long ONE_HOUR = 60*60*1000;    // 1 heure = 3600 * 1000 millis
    private static final long ONE_DAY = 24*ONE_HOUR;    // 1 jour = 24 heures
    private static final long ONE_WEEK = 7*ONE_DAY;     // 1 semaine = 7 jours
    private static final long ONE_MONTH = 30*ONE_DAY;   // En moyenne, 1 mois = 30 jours, la flemme de faire une fonction adéquate

    /**
     * If date fail : try with seconds or without (english style)
     * @param remote_date The date to convert as a string
     */
    public SimplyDate(String remote_date)  {
        try {
            simpleDateFormat = new SimpleDateFormat("dd/MM/yy hh:mm", Locale.FRANCE); // Thomas's way
            this.remote_date = simpleDateFormat.parse(remote_date);
            calcIntervalFromNow();
        } catch (ParseException e) {
            try {
                simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.FRANCE); // Good way, Thomas is a sheep
                this.remote_date = simpleDateFormat.parse(remote_date);
                calcIntervalFromNow();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void calcIntervalFromNow() {
        long diff = Calendar.getInstance().getTime().getTime() - remote_date.getTime();

        //Log.d("Sim", "From : " + Calendar.getInstance().getTime().getTime() + " to " + remote_date.getTime() + " → " + diff);

        // Calc intervals
        hours = diff / ONE_HOUR;
        days = diff / ONE_DAY;
        weeks = diff / ONE_WEEK;
        months = diff / ONE_MONTH;
    }

    /**
     * Returns a string which says the interval
     */
    public String simplify() {

        if (months == 0) {
            if (weeks == 0) {
                if (days == 0) {
                    if (hours == 0) {
                        return "Il y a moins d'une heure";
                    } else {
                        return "Il y a " + hours + " heure" + (hours>1?"s":"");
                    }
                } else {
                    return "Il y a " + days + " jour" + (days>1?"s":"");
                }
            } else {
                return "Il y a " + weeks + " semaine" + (weeks>1?"s":"");
            }
        } else {
            return "Il y a " + months + " mois";
        }
    }
}
