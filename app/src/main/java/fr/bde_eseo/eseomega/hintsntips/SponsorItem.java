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
        this.detail = detail.replace("\\n", "\n"); // Parse JSON newline code
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
