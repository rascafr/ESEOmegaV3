package fr.bde_eseo.eseomega.lacommande;

import android.sax.RootElement;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.lacommande.model.LacmdCategory;
import fr.bde_eseo.eseomega.lacommande.model.LacmdElement;
import fr.bde_eseo.eseomega.lacommande.model.LacmdIngredient;
import fr.bde_eseo.eseomega.lacommande.model.LacmdMenu;
import fr.bde_eseo.eseomega.lacommande.model.LacmdRoot;

/**
 * Created by Rascafr on 22/07/2015.
 * Used to store data with prices, images, etc.
 */
public class DataManager {

    private static DataManager instance;
    private String token;
    private int nbCartItems = 0;

    private DataManager (){}

    public static DataManager getInstance() {
        if (instance == null)
            instance = new DataManager();
        return instance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void reset() {
        this.token = null;
        this.nbCartItems = 0;
        if (cartArray == null) this.cartArray = new ArrayList<>();
        this.cartArray.clear();
        if (elements == null) this.elements = new ArrayList<>();
        this.elements.clear();
        if (menus == null) this.menus = new ArrayList<>();
        this.menus.clear();
        if (ingredients == null) this.ingredients = new ArrayList<>();
        this.ingredients.clear();
        if (categories == null) this.categories = new ArrayList<>();
        this.categories.clear();
        this.instructions = "";
    }

    /**
     * LOOK AT HERE BABY IT'S READY
     * 17/08/2015
     */
    // Get menus, elements and ingredients from JSON Array (syncData.php)
    private ArrayList<LacmdIngredient> ingredients;
    private ArrayList<LacmdMenu> menus;
    private ArrayList<LacmdElement> elements;
    private ArrayList<LacmdCategory> categories;
    private final static String ENTRY_MENU_ID = "cat_menus";
    private final static String JSON_KEY_CATEGORIES = "lacmd-categories";
    private final static String JSON_KEY_MENUS = "lacmd-menus";
    private final static String JSON_KEY_ELEMENTS = "lacmd-elements";
    private final static String JSON_KEY_INGREDIENTS = "lacmd-ingredients";

    public void fillData (JSONArray array) {

        // Check initialization
        if (array != null && ingredients != null && menus != null && elements != null && categories != null) {

            try {

                //JSONArray array = new JSONArray(strArray);

                // Loop for menus - elements - etc ...
                for (int a=0;a<array.length();a++) {

                    JSONObject lacmdObj = array.getJSONObject(a); // object like "menu", "elements"...
                    if (lacmdObj.has(JSON_KEY_MENUS)) {
                        JSONArray lacmdArray = lacmdObj.getJSONArray(JSON_KEY_MENUS);
                        //menus = new ArrayList<>(); -> Now in RESET !
                        for (int i=0;i<lacmdArray.length();i++) {
                            menus.add(new LacmdMenu(lacmdArray.getJSONObject(i)));
                        }
                    } else if (lacmdObj.has(JSON_KEY_ELEMENTS)) {
                        JSONArray lacmdArray = lacmdObj.getJSONArray(JSON_KEY_ELEMENTS);
                        //elements = new ArrayList<>();
                        for (int i = 0; i < lacmdArray.length(); i++) {
                            elements.add(new LacmdElement(lacmdArray.getJSONObject(i)));
                        }
                    } else if (lacmdObj.has(JSON_KEY_INGREDIENTS)) {
                        JSONArray lacmdArray = lacmdObj.getJSONArray(JSON_KEY_INGREDIENTS);
                        //ingredients = new ArrayList<>();
                        for (int i=0;i<lacmdArray.length();i++) {
                            ingredients.add(new LacmdIngredient(lacmdArray.getJSONObject(i)));
                        }
                    } else if (lacmdObj.has(JSON_KEY_CATEGORIES)) {
                        JSONArray lacmdArray = lacmdObj.getJSONArray(JSON_KEY_CATEGORIES);
                        //categories = new ArrayList<>();
                        for (int i=0;i<lacmdArray.length();i++) {
                            categories.add(new LacmdCategory(lacmdArray.getJSONObject(i)));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<LacmdIngredient> getIngredients() {
        return ingredients;
    }

    public ArrayList<LacmdMenu> getMenus() {
        return menus;
    }

    public ArrayList<LacmdElement> getElements() {
        return elements;
    }

    public ArrayList<LacmdCategory> getCategories() {
        return categories;
    }

    // Convert Menus Array into charsequence []
    @Deprecated
    public CharSequence[] menusToCharSequences() {
        CharSequence cs[] = new CharSequence[menus.size()];
        for (int i=0;i<menus.size();i++) {
            cs[i] = menus.get(0).getName();
        }
        return cs;
    }

    // Convert Elements Array into charsequence []
    @Deprecated
    public CharSequence[] elementsToCharSequences() {
        CharSequence cs[] = new CharSequence[elements.size()];
        for (int i=0;i<elements.size();i++) {
            cs[i] = elements.get(0).getName();
        }
        return cs;
    }

    // Convert Array into charsequence [] associated with category ID
    public CharSequence[] arrayToCharSequences(String catID) {

        ArrayList<String> names = new ArrayList<>();

        // If cat_menu : Disp all of the menus
        if (catID.equals(ENTRY_MENU_ID))
            for (int i=0;i<menus.size();i++)
                names.add(menus.get(i).getName());

        // Else, it could be any kind of element
        else
            for (int i=0;i<elements.size();i++)
                if (elements.get(i).getIdcat().equals(catID))
                    names.add(elements.get(i).getName());

        return names.toArray(new CharSequence[names.size()]);
    }

    // Return an array in relationship with category ID
    public ArrayList<LacmdRoot> arrayToCatArray (String catID) {
        ArrayList<LacmdRoot> roots = new ArrayList<>();

        // If cat_menu : compose with all of the menus
        if (catID.equals(ENTRY_MENU_ID))
            for (int i=0;i<menus.size();i++)
                roots.add(menus.get(i));

        // Else, it could be any kind of element
        else
            for (int i=0;i<elements.size();i++)
                if (elements.get(i).getIdcat().equals(catID))
                    roots.add(elements.get(i));

        return roots;
    }

    /**
     * Cart manager objects ! 19/08/2015
     * Uses LacmdRoot element for the moment (to store name, price, identifier ...)
     */
    private ArrayList<LacmdRoot> cartArray;

    // Returns the cart array (Usage : for Adapter)
    public ArrayList<LacmdRoot> getCartArray() {
        return cartArray;
    }

    // Get number of items in cart
    public int getNbCartItems () {
        return cartArray == null ? 0 : cartArray.size();
    }

    // Increments number of items
    public void addCartItem (LacmdRoot root) {
        cartArray.add(new LacmdRoot(root)); // HARDWARE COPY -> TODO
    }

    // Removes an item
    public void removeCartItem (int index) {
        cartArray.remove(index);
    }

    // Calculates the cart's price
    public double getCartPrice () {
        double tot = 0.0;
        for (int i=0;i<cartArray.size();i++)
            tot += cartArray.get(i).calcRealPrice(false);

        return tot;
    }

    /**
     * For ingredients chooser
     * 20/08/2015
     */
    public LacmdElement getElementFromID (String id) {
        for (int i=0;i<elements.size();i++) {
            if (elements.get(i).getIdstr().equals(id))
                return elements.get(i);
        }
        return null;
    }

    /**
     * For elements chooser
     */
    public LacmdMenu getMenuFromID (String id) {
        for (int i=0;i<menus.size();i++) {
            if (menus.get(i).getIdstr().equals(id))
                return menus.get(i);
        }
        return null;
    }

    /**
     * For Cart JSON output
     * 21/08/2015
     */
    public String outputJSON () {
        String json = "["; // Array begin

        for (int i=0;i<cartArray.size();i++) {
            if (i!=0) json += ", ";
            json += cartArray.get(i).toJSONString();
        }

        json += "]";

        return json;
    }

    /**
     * Curent menu's item list
     */
    private LacmdMenu menu;

    public void setMenu (LacmdMenu menu) {
        this.menu = new LacmdMenu(menu);
    }

    public LacmdMenu getMenu() {
        return menu;
    }

    /**
     * Instructions
     */
    private String instructions;

    public void setInstructions (String instructions) {
        this.instructions = instructions;
    }

    public String getInstructions () {
        return instructions;
    }
}
