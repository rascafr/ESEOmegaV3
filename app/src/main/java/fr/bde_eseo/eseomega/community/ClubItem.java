package fr.bde_eseo.eseomega.community;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Rascafr on 31/08/2015.
 *
 */
public class ClubItem {

    private String name, desc, details, img;
    private String fb, linkedin, tel, twitter, snap, youtube, web, mail, instagram;
    private ArrayList<ModuleItem> modules;

    public ClubItem (JSONObject obj) throws JSONException {
        name = obj.getString("nom");
        desc = obj.getString("desc");
        details = obj.getString("detail");
        img = obj.getString("img");
        fb = obj.getString("fb");
        linkedin = obj.getString("linkedin");
        tel = obj.getString("tel");
        twitter = obj.getString("twitter");
        snap = obj.getString("snap");
        youtube = obj.getString("youtube");
        web = obj.getString("web");
        mail = obj.getString("mail");
        instagram = obj.getString("instagram");
        modules = new ArrayList<>();

        JSONArray modulesArray = obj.getJSONArray("modules");
        for (int i=0;i<modulesArray.length();i++) {
            modules.add(new ModuleItem(modulesArray.getJSONObject(i)));
        }
    }

    public ArrayList<ModuleItem> getModules() {
        return modules;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getDetails() {
        return details;
    }

    public String getImg() {
        return img;
    }

    public String getFb() {
        return fb;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public String getTel() {
        return tel;
    }

    public String getTwitter() {
        return twitter;
    }

    public String getSnap() {
        return snap;
    }

    public String getYoutube() {
        return youtube;
    }

    public String getWeb() {
        return web;
    }

    public String getMail() {
        return mail;
    }

    public String getInstagram() { return instagram; }

    public boolean hasFacebook () {
        return fb!=null && fb.length() > 0;
    }

    public boolean hasLinkedIn () {
        return linkedin!=null && linkedin.length() > 0;
    }

    public boolean hasPhone () {
        return tel!=null && tel.length() > 0;
    }

    public boolean hasTwitter () {
        return twitter!=null && twitter.length() > 0;
    }

    public boolean hasSnapchat () {
        return snap!=null && snap.length() > 0;
    }

    public boolean hasYoutube () {
        return youtube!=null && youtube.length() > 0;
    }

    public boolean hasWeb () {
        return web!=null && web.length() > 0;
    }

    public boolean hasMail () {
        return mail!=null && mail.length() > 0;
    }

    public boolean hasInsta () {
        return instagram!=null && instagram.length() > 0;
    }

    public class ModuleItem {

        private String name;
        private ArrayList<TeamItem> members;

        public ModuleItem (JSONObject obj) throws JSONException {
            name = obj.getString("nomModule");
            members = new ArrayList<>();
            JSONArray teams = obj.getJSONArray("membres");
            for (int i=0;i<teams.length();i++) {
                members.add(new TeamItem(teams.getJSONObject(i)));
            }
        }

        public String getName() {
            return name;
        }

        public ArrayList<TeamItem> getMembers() {
            return members;
        }

        public class TeamItem {
            private String name, detail, img;

            public TeamItem (JSONObject obj) throws JSONException {
                name = obj.getString("nom");
                detail = obj.getString("detail");
                img = obj.getString("img");
            }

            public String getName() {
                return name;
            }

            public String getDetail() {
                return detail;
            }

            public String getImg() {
                return img;
            }
        }
    }
}
