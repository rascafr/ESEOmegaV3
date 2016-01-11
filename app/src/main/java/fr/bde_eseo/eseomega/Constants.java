package fr.bde_eseo.eseomega;

/**
 * Created by François on 16/04/2015.
 */
public class Constants {

    // Register all constants here
    // Try to get the writing order like this :
    // package.class.NAME
    public static final String PLAY_STORE_APP_ID = "com.android.vending";

    // Store app verification
    public final static String SIGNATURE = "mM6h5mqKyeuhXgBF8SLnMBKc7BE=";

    // Timezoone : CET ou GMT+01:00
    // ID Timezone : Europe/Paris
    public static final String TZ_ID_PARIS = "Europe/Paris";

    // Preferences
    public static final String PREFS_NEWS_KEY = "fragment.NewsFragment.KEY";
    public static final String PREFS_APP_WELCOME = "mainactivity.welcome";
    public static final String PREFS_APP_VERSION = "mainactivity.appversion";
    public static final String PREFS_NEWS_LAST_DOWNLOAD_DATE = "fragment.NewsFragment.LAST_DOWNLOAD_DATE";
    public static final String PREFS_APP_WELCOME_DATA = "mainactivity.welcome.DATA";

    public static final String PREFS_USER_PROFILE_KEY = "model.UserProfile.KEY";
    public static final String PREFS_USER_PROFILE_NAME = "model.UserProfile.NAME";
    public static final String PREFS_USER_PROFILE_ID = "model.UserProfile.ID";
    public static final String PREFS_USER_PROFILE_PASSWORD = "model.UserProfile.PASSWORD";
    public static final String PREFS_USER_PROFILE_MAIL = "model.UserProfile.MAIL";
    public static final String PREFS_USER_PROFILE_EXISTS = "model.UserProfile.EXISTS";
    public static final String PREFS_USER_PROFILE_PICTURE = "model.UserProfile.PICTURE";
    public static final String PREFS_USER_PROFILE_PUSH_TOKEN = "model.UserProfile.PUSHTOKEN";
    public static final String PREFS_USER_PROFILE_PHONE = "model.UserProfile.PHONE";

    // SharedPref class
    public static final String PREFS_GENERAL_HOMESCREEN = "settings.general.homescreen";
    public static final String PREFS_GENERAL_UPDATE = "settings.general.autoupdate";
    public static final String PREFS_LYDIA_PHONE = "settings.lydia.phone";

    // Notifications
    public static final int NOTIF_GENERAL = 0;
    public static final int NOTIF_NEWS = 1;
    public static final int NOTIF_EVENTS = 2;
    public static final int NOTIF_CLUBS = 3;
    public static final int NOTIF_CAFET = 4;
    public static final int NOTIF_TIPS = 5;
    public static final int NOTIF_UPDATE = 21;
    public static final int NOTIF_GANTIER = 42;
    public static final int NOTIF_CONNECT = 99;
    public static final double NOTIF_VERSION = 1.0; // Notification is valid if V_Push_App >= V_Push_Server
    public static final String NOTIF_UPDATE_TITLE = "Impossible de recevoir les notifications";
    public static final String NOTIF_UPDATE_TEXT = "Merci de mettre l'application à jour depuis le Play Store.";
    public static final int NOTIF_UPDATE_FORCE= 147; // local only

    // Fragments ID
    public static final String TAG_FRAGMENT_ORDER_TABS = "fragment.tabs.order";

    // URL's
    public static final String URL_SERVER = "http://217.199.187.59/francoisle.fr/";
    public static final String URL_ASSETS = URL_SERVER + "lacommande/assets/";

    /**
     * API V3.0 - Since December 2015 ↔ January 2016
     */

    // Base URL
    private static final String URL_API_BASE = URL_SERVER + "lacommande/api/";

    // Connexion
    public static final String URL_API_CLIENT_CONNECT = URL_API_BASE + "client/connect.php";

    // Gantier game
    public static final String URL_API_GANTIER_SCORES = URL_API_BASE + "gantier/scores.php";

    // Push notifications
    public static final String URL_API_PUSH_REGISTER = URL_API_BASE + "push/register.php";
    public static final String URL_API_PUSH_UNREGISTER = URL_API_BASE + "push/unregister.php";

    // LaCommande
    public static final String URL_API_ORDER_ITEMS = URL_API_BASE + "order/items.php";
    public static final String URL_API_ORDER_LIST = URL_API_BASE + "order/list.php";
    public static final String URL_API_ORDER_RESUME = URL_API_BASE + "order/resume.php";
    public static final String URL_API_ORDER_PREPARE = URL_API_BASE + "order/prepare.php";
    public static final String URL_API_ORDER_SEND = URL_API_BASE + "order/send.php";

    // General & Specific Informations
    public static final String URL_API_INFO_SERVICE = URL_API_BASE + "info/service.php";
    public static final String URL_API_INFO_VERSION = URL_API_BASE + "info/version.php";

    // Lydia
    public static final String URL_API_LYDIA_ASK = URL_API_BASE + "lydia/ask.php";
    public static final String URL_API_LYDIA_CHECK = URL_API_BASE + "lydia/check.php";

    // Events
    public static final String URL_API_EVENT_LIST = URL_API_BASE + "event/list.php";
    public static final String URL_API_EVENT_PREPARE = URL_API_BASE + "event/prepare.php";


    // Data from Naudet-Sonasi
    public static final String URL_SERVERBIS = "http://79.170.44.147/eseonews.fr/";
    public static final String URL_JSON_SPONSORS = URL_SERVERBIS + "jsondata/sponsors_data/sponsors.json";
    public static final String URL_JSON_EVENTS = URL_SERVERBIS + "jsondata/events_data/events.json";
    public static final String URL_JSON_CLUBS = URL_SERVERBIS + "jsondata/clubs_data/clubs.json";
    public static final String URL_NEWS_ANDROID = URL_SERVERBIS + "getData.php?smtype=ANDROID&";
    public static final String URL_JSON_INGENEWS = URL_SERVERBIS + "jsondata/ingenews_data/menu_empty.php";

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
                                                    "Bien tenté, mais vous ne pouvez pas tricher pour commander à la cafet avant les autres.\n(Et puis de toute manière ça sert à rien)";

    public static final int ERROR_USERREGISTER = 2;
    public static final String ERROR_USERREGISTER_STR = "Votre mot de passe est incorrect.\n" +
                                                    "Si vous l'avez changé depuis les services ESEO, merci de bien vouloir vous déconnecter puis reconnecter depuis l'onglet \"Mon profil\" de l'application";

    public static final int ERROR_SERVICE_OUT = 3;
    public static final String ERROR_SERVICE_OUT_STR = "Désolé ! On sait que vous avez faim. Mais la cafétéria n'est ouverte que pendant les périodes scolaires de 12h à 13h.";


    public static final int ERROR_UNPAID = 4;
    public static final String ERROR_UNPAID_STR = "Hey, vous êtes interdits de commande ! En effet, vous avez un repas (ou plus) de non réglé(s). \n" +
                                                    "Venez nous voir le plus rapidement possible afin de nous rembourser.";

    public static final int ERROR_APP_PB = 6;
    public static final String ERROR_APP_PB_STR = "Le système est en maintenance pour la version Android, nous sommes en train de corriger ça.\n" +
            "En attendant, vous pouvez toujours commander au comptoir.\nMerci !";

    public static final int ERROR_USER_BAN = 7;
    public static final String ERROR_USER_BAN_STR = "Vous ne pouvez pas accéder au service.\n\n(Raison : ";

    public static final int ERROR_BAD_VERSION = 8;
    public static final String ERROR_BAD_VERSION_STR = "Vous ne pouvez pas accéder au service car votre application n'est pas à jour.\n" +
            "Téléchargez la nouvelle version depuis le Play Store.";

    public static final int ERROR_HOTSPOT = -2;
    public static final String ERROR_HOTSPOT_STR = "La connexion semble passer par un hotspot : avez vous bien renseigné vos identifiants de connexion Wifi ?";

    public static final int ERROR_NETWORK = 0;
    public static final String ERROR_NETWORK_STR = "Impossible de se connecter au serveur. Veuillez vérifier votre connexion ou réessayer plus tard.";

    public static final String ERROR_UNKNOWN = "Veuillez réessayer plus tard, le service semble indisponible.";

    // APP ID
    public static final String APP_ID = "ANDROID";

    // Bundled intents & data
    public static final String KEY_ELEMENT_ID = "lacommande.key.element.id";
    public static final String KEY_ORDER_ID = "lacommande.order_id";
    public static final String KEY_MENU_ID = "lacommande.key.menu.id";
    public static final String KEY_ELEMENT_POSITION = "lacommande.key.element.position";
    public static final String KEY_NEWS_JSON_DATA = "news.json.data";
    public static final String KEY_NEWS_TITLE = "news.title";
    public static final String KEY_NEWS_AUTHOR = "news.author";
    public static final String KEY_NEWS_LINK = "news.link";
    public static final String KEY_NEWS_IMGARRAY = "news.imgarray";
    public static final String KEY_NEWS_HTML = "news.html";
    public static final String KEY_IMG = "activity.wide.img";
    public static final String KEY_CLUB_VIEW = "clubs.viewitem";
    public static final String KEY_MAIN_INTENT = "main.intent.start";
    public static final String KEY_MAIN_TITLE = "main.intent.title";
    public static final String KEY_MAIN_MESSAGE = "main.intent.message";
    public static final String KEY_GANTIER_INTENT = "gp.intent.start";

    // LYDIA
    public static final String KEY_LYDIA_ORDER_ID = "lydia.order_id";
    public static final String KEY_LYDIA_ORDER_TYPE = "lydia.order_type";
    public static final String KEY_LYDIA_ORDER_ASKED = "lydia.order_status";
    public static final String TYPE_LYDIA_CAFET = "CAFET";
    public static final String TYPE_LYDIA_EVENT = "EVENT";

    // External Intents
    // public static final String EXTERNAL_INTENT_LYDIA_CAFET = "fr.bde_eseomega.eseomega.LYDIA_CAFET"; // Android VIEW now

}
