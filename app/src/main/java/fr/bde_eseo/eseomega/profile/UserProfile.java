package fr.bde_eseo.eseomega.profile;

import android.content.Context;
import android.content.SharedPreferences;

import fr.bde_eseo.eseomega.R;

import fr.bde_eseo.eseomega.model.NavDrawerItem;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.StringUtils;
import fr.bde_eseo.eseomega.Constants;

/**
 * Created by François on 14/04/2015.
 */
public class UserProfile {

    public static final String RESEAU_ESEO_FR = "@reseau.eseo.fr";
    private String name;
    private String id;
    private String email;
    private String encodedPassword;
    private boolean isCreated;
    private String picturePath;
    private String pushToken;

    private final static int MAX_TEXT_LENGTH = 39;

    public UserProfile () {
        this.isCreated = false;
    }

    public UserProfile (Context ctx, String name, String id, String encodedPassword) {
        this.name = name;
        this.id = id;
        this.encodedPassword = EncryptUtils.sha256(ctx.getResources().getString(R.string.MESSAGE_PASS_USER) + encodedPassword);
        this.isCreated = true;
        this.picturePath = "";
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getName () {
        return this.name;
    }

    public String getId () {
        return this.id;
    }

    public String getPassword () {
        return this.encodedPassword;
    }

    public String getEmail () {
        return this.email;
    }

    public void setName (String name) {
        this.name = name;
    }

    public void setId (String id) {
        this.id = id;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public void setCreated () {
        this.isCreated = true;
    }

    public void setUnCreated () {
        this.isCreated = false;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public String getPicturePath() {

        return picturePath;
    }

    public boolean isCreated () {
        return this.isCreated;
    }

    public String getFirstName () {
        int sp = getLastSpaceInString(this.name);
        if (sp != -1 && this.name.length() >= sp+2) {
            return this.name.substring(0, sp);
        } else {
            return this.name;
        }
    }

    public String getFamilyName () {
        int sp = getLastSpaceInString(this.name);
        if (sp != -1 && this.name.length() >= sp+2) {
            return this.name.substring(sp+1);
        } else {
            return this.name;
        }
    }

    public void guessEmailAddress () {
        this.email = StringUtils.simpleString(getFirstName()) + "." + StringUtils.simpleString(getFamilyName()) + RESEAU_ESEO_FR;
    }

    public void reverseName () {
        int sp = getLastSpaceInString(this.name);
        if (sp != -1 && this.name.length() >= sp+2) {
            String firstName = this.name.substring(sp+1);
            String famName = this.name.substring(0, sp);
            this.name = firstName + " " + famName;
        }
    }

    // Returns the exact position of the last space in a String, -1 else
    // V2.0 -> ok
    private int getLastSpaceInString (String s) {
        int a = 0, b = -1;

        do {
            a = s.indexOf(" ", a);
            if (a != -1) {
                b = a;
                ++a;
            }
        } while (a != -1);

        return b;
    }

    // Use getShortNameV2 instead
    public String getShortName () {
        if (this.name.length() > MAX_TEXT_LENGTH)
            return this.name.substring(0, MAX_TEXT_LENGTH) + "...";
        else
            return this.name;
    }

    public String getShortNameV2 () {

        // search for space
        String s = getShortName();
        int sp = getLastSpaceInString(s);
        if (sp != -1) {
            // Get the family name first letter
            if (s.length() >= sp + 2)
                return s.substring(0, sp+2);
            else
                return s.substring(0, sp);
        } else
            return s;
    }

    public String getShortEmail () {
        if (this.email.length() > MAX_TEXT_LENGTH)
            return this.email.substring(0, MAX_TEXT_LENGTH) + "...";
        else
            return this.email;
    }

    public NavDrawerItem getDrawerProfile () {
        return new NavDrawerItem(this.name, getShortEmail());
    }

    public void registerProfileInPrefs (Context context) {

        SharedPreferences prefs_Read;
        SharedPreferences.Editor prefs_Write;

        prefs_Read = context.getSharedPreferences(Constants.PREFS_USER_PROFILE_KEY, 0);
        prefs_Write = prefs_Read.edit();

        prefs_Write.putString(Constants.PREFS_USER_PROFILE_NAME, this.name);
        prefs_Write.putString(Constants.PREFS_USER_PROFILE_MAIL, this.email);
        prefs_Write.putString(Constants.PREFS_USER_PROFILE_ID, this.id);
        prefs_Write.putString(Constants.PREFS_USER_PROFILE_PASSWORD, this.encodedPassword);
        prefs_Write.putBoolean(Constants.PREFS_USER_PROFILE_EXISTS, this.isCreated);
        prefs_Write.putString(Constants.PREFS_USER_PROFILE_PICTURE, this.picturePath);
        prefs_Write.putString(Constants.PREFS_USER_PROFILE_PUSH_TOKEN, this.pushToken);
        prefs_Write.apply();
    }

    public void readProfilePromPrefs (Context context) {

        SharedPreferences prefs_Read;

        prefs_Read = context.getSharedPreferences(Constants.PREFS_USER_PROFILE_KEY, 0);

        this.name = prefs_Read.getString(Constants.PREFS_USER_PROFILE_NAME, "");
        this.email = prefs_Read.getString(Constants.PREFS_USER_PROFILE_MAIL, "");
        this.encodedPassword = prefs_Read.getString(Constants.PREFS_USER_PROFILE_PASSWORD, "");
        this.id = prefs_Read.getString(Constants.PREFS_USER_PROFILE_ID, "");
        this.isCreated = prefs_Read.getBoolean(Constants.PREFS_USER_PROFILE_EXISTS, false);
        this.picturePath = prefs_Read.getString(Constants.PREFS_USER_PROFILE_PICTURE, "");
        this.pushToken = prefs_Read.getString(Constants.PREFS_USER_PROFILE_PUSH_TOKEN, "");
    }

    public void removeProfileFromPrefs (Context context) {
        // remove user name, id, mail and reset Profile_exists
        this.isCreated = false;
        this.name = "";
        this.id = "";
        this.email = "";
        this.encodedPassword = "";
        this.picturePath = "";
        registerProfileInPrefs(context);
    }

}
