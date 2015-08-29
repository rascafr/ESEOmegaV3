package fr.bde_eseo.eseomega.hintsntips;

import java.util.ArrayList;

/**
 * Created by Rascafr on 11/08/2015.
 */
public class SponsorItem {

    private String name, detail, img, url, adr;
    private ArrayList<String> avantages;

    public SponsorItem(String name, String detail, String img, String url, String adr, ArrayList<String> avantages) {
        this.name = name;
        this.detail = detail;
        this.img = img;
        this.url = url;
        this.adr = adr;
        this.avantages = avantages;
    }

    public String getAdr() {
        return adr;
    }

    public String getDetail() {
        return detail;
    }

    public String getImg() {
        return img;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getAvantages() {
        return avantages;
    }

    @Override
    public String toString() {
        return "SponsorItem{" +
                "name='" + name + '\'' +
                ", detail='" + detail + '\'' +
                ", img='" + img + '\'' +
                ", url='" + url + '\'' +
                ", adr='" + adr + '\'' +
                ", avantages=" + avantages +
                '}';
    }
}
