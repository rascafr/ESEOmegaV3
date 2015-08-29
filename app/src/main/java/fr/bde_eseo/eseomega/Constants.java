package fr.bde_eseo.eseomega;

/**
 * Created by François on 16/04/2015.
 */
public class Constants {

    // Register all constants here
    // Try to get the writing order like this :
    // package.class.NAME

    public static final String PREFS_NEWS_KEY = "fragment.NewsFragment.KEY";
    public static final String PREFS_NEWS_LAST_DOWNLOAD_DATE = "fragment.NewsFragment.LAST_DOWNLOAD_DATE";

    public static final String PREFS_USER_PROFILE_KEY = "model.UserProfile.KEY";
    public static final String PREFS_USER_PROFILE_NAME = "model.UserProfile.NAME";
    public static final String PREFS_USER_PROFILE_ID = "model.UserProfile.ID";
    public static final String PREFS_USER_PROFILE_MAIL = "model.UserProfile.MAIL";
    public static final String PREFS_USER_PROFILE_EXISTS = "model.UserProfile.EXISTS";
    public static final String PREFS_USER_PROFILE_PICTURE = "model.UserProfile.PICTURE";

    // Fragments ID
    public static final String TAG_FRAGMENT_ORDER_TABS = "fragment.tabs.order";

    // URL's
    public static final String URL_SERVER = "http://217.199.187.59/francoisle.fr/";
    public static final String URL_ASSETS = URL_SERVER + "lacommande/assets/";
    public static final String URL_JSON_LACMD_DATA = URL_SERVER + "lacommande/apps/syncData.php";
    public static final String URL_POST_TOKEN = URL_SERVER + "lacommande/apps/syncDate-token.php"; // post ok
    public static final String URL_SYNC_HISTORY = URL_SERVER + "lacommande/apps/syncClientHistory.php"; // post ok
    public static final String URL_CAMPUS_LOGIN = "http://campus.eseo.fr/login/index.php"; // post ok
    public static final String URL_END_LOGIN =  URL_SERVER + "lacommande/apps/finaliserConnex.php"; // post ok
    public static final String URL_GPGAME_POST_SCORES = URL_SERVER + "lacommande/apps/syncGPScores.php"; // post ok
    public static final String URL_POST_CART = URL_SERVER + "lacommande/apps/syncOrder-token.php"; // post ok
    public static final String URL_SYNC_SINGLE = URL_SERVER + "lacommande/apps/syncSingle.php"; // post ok

    public static final String URL_SERVERBIS = "http://79.170.44.147/eseonews.fr/";
    public static final String URL_JSON_SPONSORS = URL_SERVERBIS + "jsondata/sponsors_data/sponsors.json";
    public static final String URL_JSON_EVENTS = URL_SERVERBIS + "jsondata/events_data/events.json";
    public static final String URL_JSON_CLUBS = URL_SERVERBIS + "jsondata/clubs_data/clubs.json";
    public static final String URL_NEWS_ANDROID = URL_SERVERBIS + "getData.php?smtype=ANDROID&";

    // JSON TAG
    public static final String JSON_TAG_CAT = "categories";
    public static final String JSON_TAG_CAT_NAME = "name";
    public static final String JSON_TAG_CAT_PRICE = "firstPrice";
    public static final String JSON_TAG_CAT_IMGURL = "imgUrl";
    public static final String JSON_TAG_CAT_BRIEF = "briefText";
    public static final String JSON_TAG_ITEMS = "items";

    // Errors
    public static final int ERROR_TIMESTAMP = 1;
    public static final String ERROR_TIMESTAMP_STR = "On dirait que votre smartphone n'est pas à l'heure.\n" +
                                                    "Bien tenté, mais vous ne pouvez pas tricher pour commander à la cafet avant les autres.";

    public static final int ERROR_USERREGISTER = 2;
    public static final String ERROR_USERREGISTER_STR = "Il semblerait que vous n'existez pas à l'ESEO (du moins dans notre base de données).\n" +
                                                        "Venez nous voir dès que possible.";

    public static final int ERROR_SERVICE_OUT = 3;
    public static final String ERROR_SERVICE_OUT_STR = "Désolé ! On sait que vous avez faim. Mais la cafétéria n'est ouverte que pendant les périodes scolaires de 12h à 13h.";


    public static final int ERROR_UNPAID = 4;
    public static final String ERROR_UNPAID_STR = "Hey, vous êtes interdits de commande ! En effet, vous avez un repas (ou plus) de non réglé(s). \n" +
                                                    "Venez nous voir le plus rapidement possible afin de nous rembourser.";

    public static final int ERROR_APP_PB = 6;
    public static final String ERROR_APP_PB_STR = "Le système est en maintenance pour Android, nous sommes en train de corriger ça.\n" +
            "En attendant, vous pouvez toujours commander au comptoir.\nMerci !";

    public static final int ERROR_USER_BAN = 7;
    public static final String ERROR_USER_BAN_STR = "A trop jouer avec le feu, on finit par se bruler les doigts ...\nVous avez été banni du service.\n\n(Raison : ";


    public static final String ERROR_UNKNOWN = "Veuillez réessayer plus tard, le service semble indisponible.";

    public static final String ERROR_NETWORK = "Impossible de se connecter au serveur. Veuillez vérifier votre connexion ou réessayer plus tard.";

    // APP ID
    public static final String APP_ID = "ANDROID";

    // Bundled intents & data
    public static final String KEY_ELEMENT_ID = "lacommande.key.element.id";
    public static final String KEY_MENU_ID = "lacommande.key.menu.id";
    public static final String KEY_ELEMENT_POSITION = "lacommande.key.element.position";

}
