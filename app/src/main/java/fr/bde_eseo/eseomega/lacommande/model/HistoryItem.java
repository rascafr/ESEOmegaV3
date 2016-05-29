/**
 * Copyright (C) 2016 - François LEPAROUX
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.bde_eseo.eseomega.lacommande.model;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Rascafr on 20/07/2015.
 */
public class HistoryItem {

    public final static int STATUS_PREPARING = 0;
    public final static int STATUS_READY = 1;
    public final static int STATUS_DONE = 2;
    public final static int STATUS_NOPAID = 3;

    private String commandName;
    private int commandStatus;
    private String commandDate, commandStr;
    private double commandPrice;
    private boolean isHeader, isFooter;
    private int commandNumber, commandModulo;

    public HistoryItem (String commandName, int commandStatus, double commandPrice, String commandDate, int commandNumber, int commandModulo, String commandStr, boolean simpleDate) {
        this.commandName = commandName;
        this.commandStatus = commandStatus;
        this.commandPrice = commandPrice;
        this.commandDate = commandDate;
        this.commandDate = getFrenchDate(simpleDate);
        this.commandNumber = commandNumber;
        this.isHeader = false;
        this.commandModulo = commandModulo;
        this.commandStr = commandStr;
    }

    /*
    @Deprecated
    public HistoryItem (String commandName, int commandStatus, double commandPrice, String commandDate) {
        this.commandName = commandName;
        this.commandStatus = commandStatus;
        this.commandPrice = commandPrice;
        this.commandDate = commandDate;
        this.isHeader = false;
    }*/

    // Command for today
    public HistoryItem (String commandName, int commandStatus, double commandPrice) {
        this.commandName = commandName;
        this.commandStatus = commandStatus;
        this.commandPrice = commandPrice;
        this.isHeader = false;
        this.isFooter = false;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE);

        this.commandDate = sdf.format(Calendar.getInstance(TimeZone.getDefault(), Locale.FRANCE).getTime());
    }

    // Header / footer (if true)
    public HistoryItem (String titleName, boolean isFooter) {
        this.commandName = titleName;
        this.isHeader = true;
        this.isFooter = isFooter;
    }

    public String getCommandName() {
        return commandName;
    }

    public int getCommandStatus() {
        return commandStatus;
    }

    public String getCommandStatusAsString () {
        switch (commandStatus) {
            case STATUS_DONE:
                return "Commande terminée";
            case STATUS_PREPARING:
                return "En préparation";
            case STATUS_READY:
                return "Commande prête";
            case STATUS_NOPAID:
                return "Commande non payée";
            default:
                return "Erreur dans la commande";
        }
    }

    public String getCommandDate() {
        return commandDate;
    }

    public double getCommandPrice() {
        return commandPrice;
    }

    public String getCommandPriceAsString(){
        return new DecimalFormat("0.00").format(commandPrice) + "€";
    }

    public String getCommandNumberAsString() {
        return commandStr + new DecimalFormat("000").format(commandModulo);
    }

    public boolean isHeader() {
        return isHeader;
    }

    public boolean isFooter() {
        return isFooter;
    }

    public int getCommandNumber() {
        return commandNumber;
    }

    /*public String getCommandNumberAsString() {
        return "№" + commandNumber;
    }*/

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setCommandStatus(int commandStatus) {
        this.commandStatus = commandStatus;
    }

    public void setCommandDate(String commandDate) {
        this.commandDate = commandDate;
    }

    public void setCommandPrice(double commandPrice) {
        this.commandPrice = commandPrice;
    }

    public String toString() {
        return "Command Data = {\""+getCommandName()+"\" the "+getCommandDate()+", price = "+commandPrice+"€, status = "+getCommandStatusAsString()+"}";
    }

    public Date getParsedDate () {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
        Date date = null;
        try {
            date = format.parse(this.commandDate);
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
}
