package fr.bde_eseo.eseomega;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.util.ArrayList;

import fr.bde_eseo.eseomega.settings.SettingsFragment;
import fr.bde_eseo.eseomega.slidingmenu.NavDrawerListAdapter;
import fr.bde_eseo.eseomega.community.CommunityFragment;
import fr.bde_eseo.eseomega.events.EventsFragment;
import fr.bde_eseo.eseomega.gcmpush.QuickstartPreferences;
import fr.bde_eseo.eseomega.hintsntips.TipsFragment;
import fr.bde_eseo.eseomega.ingenews.IngeListActivity;
import fr.bde_eseo.eseomega.interfaces.OnItemAddToCart;
import fr.bde_eseo.eseomega.interfaces.OnUserProfileChange;
import fr.bde_eseo.eseomega.lacommande.DataManager;
import fr.bde_eseo.eseomega.lacommande.OrderHistoryFragment;
import fr.bde_eseo.eseomega.lacommande.OrderTabsFragment;
import fr.bde_eseo.eseomega.slidingmenu.NavDrawerItem;
import fr.bde_eseo.eseomega.news.NewsListFragment;
import fr.bde_eseo.eseomega.profile.ConnectProfileFragment;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.profile.ViewProfileFragment;
import fr.bde_eseo.eseomega.utils.ImageUtils;
import fr.bde_eseo.eseomega.utils.Utilities;
import fr.bde_eseo.eseomega.version.AsyncCheckVersion;

/**
 * Main Activity for ESEOmega app
 * Too much things to describe here, check code please ...
 */
public class MainActivity extends AppCompatActivity implements OnUserProfileChange, OnItemAddToCart {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    // Developer mail
    private static final String MAIL_DIALOG = "developers.eseomega@gmail.com";

    // Others constant values
    public static final int MAX_PROFILE_SIZE = 256; // seems good

    // used to store app title
    private CharSequence mTitle;

    // Profile item
    UserProfile profile = new UserProfile();

    // Preferences
    private SharedPreferences prefs_Read;
    private SharedPreferences.Editor prefs_Write;
    private SharedPreferences prefsUser;

    // Latch to remember position of fragment
    private int fragPosition = 0;

    // GCM
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    // slide menu items
    private String[] navMenuTitles, navMenuOptions;
    private TypedArray navMenuIcons;
    private ListView mDrawerList;
    private NavDrawerListAdapter navAdapter;
    private ArrayList<NavDrawerItem> navDrawerItems;

    // Material Toolbar
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Global UI View
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setPadding(0, Utilities.getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        mTitle = getTitle();

        // Check app auth
        boolean installPlayStore = Utilities.verifyInstaller(this);
        boolean installSigned = Utilities.checkAppSignature(this);

        if (!BuildConfig.DEBUG && (!installPlayStore || !installSigned)) {
            new MaterialDialog.Builder(this)
                .title(R.string.bad_signature_title)
                .content(R.string.bad_signature_content)
                .negativeText(R.string.bad_signature_button)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        MainActivity.this.finish();
                    }
                }).show();
        }

        // GCM Receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                ConnectProfileFragment mFragment = (ConnectProfileFragment) getSupportFragmentManager().findFragmentByTag("frag0");
                if (mFragment != null) {
                    mFragment.setPushRegistration(sentToken);
                }
            }
        };

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // load slide menu options
        navMenuOptions = getResources().getStringArray(R.array.nav_drawer_options);

        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<>();

        // add profile view item
        profile.readProfilePromPrefs(this);
        navDrawerItems.add(profile.getDrawerProfile());

        // adding nav drawer items to array
        for (int it=1;it<navMenuTitles.length;it++)
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[it], navMenuIcons.getResourceId(it, -1)));

        // add divider
        navDrawerItems.add(new NavDrawerItem());
        // ↑ Yes it'll be better to implement an algorithm to detect end of first list and put a divider
        //   But LA FLEMME

        // adding nav drawer options to array
        for (String navMenuOption : navMenuOptions) navDrawerItems.add(new NavDrawerItem(navMenuOption));

        // Recycle the typed array
        navMenuIcons.recycle();

        // Listen events
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        navAdapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        // set picture
        // WARNING ! Bitmap could be null if picture is removed from storage !
        // EDIT : It's ok, getResizedBitmap has been modified to survive to that kind of mistake
        // TODO : correct photo orientation
        // @see http://stackoverflow.com/questions/7286714/android-get-orientation-of-a-camera-bitmap-and-rotate-back-90-degrees
        File fp = new File(profile.getPicturePath());
        if (fp.exists())
            navAdapter.setBitmap(ImageUtils.getResizedBitmap(BitmapFactory.decodeFile(profile.getPicturePath()), MAX_PROFILE_SIZE));

        // set data adapter to our listview
        mDrawerList.setAdapter(navAdapter);

        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Initialize preference objects
        prefs_Read = getSharedPreferences(Constants.PREFS_APP_WELCOME, 0);
        prefs_Write = prefs_Read.edit();
        prefsUser = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // Receive Intent from notification
        Bundle extras = getIntent().getExtras();
        String message, title;

        // Set the user's preferred theme for menu
        // Keep clear theme for now
        //mDrawerList.setBackgroundColor(getResources().getColor(R.color.drawer_background_dark));

        // Get the user's preferred homescreen
        int intendID = Integer.parseInt(prefsUser.getString(Constants.PREFS_GENERAL_HOMESCREEN, "1")); // default if news

        boolean passInstance = false;

        // If we've just received intent from push notification event, we handle it
        if (extras != null) {
            title = extras.getString(Constants.KEY_MAIN_TITLE);
            message = extras.getString(Constants.KEY_MAIN_MESSAGE);
            intendID = extras.getInt(Constants.KEY_MAIN_INTENT);
            passInstance = true;
            if (intendID == Constants.NOTIF_CONNECT) {
                intendID = 0;
            } else if (intendID == Constants.NOTIF_GENERAL) {
                intendID++;
                if (title != null && title.length() > 0 && message != null && message.length() > 0) {
                    new MaterialDialog.Builder(this)
                            .title(title)
                            .content(message)
                            .negativeText(R.string.dialog_close)
                            .show();
                }
            }
        }

        if (savedInstanceState == null || passInstance) {
            // on first time display view for first nav item
            displayView(intendID); // Note : 0 is profile
        }

        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP

        // If needed, show a welcome message
        // For V2.1.1, prevent profile suppression
        prefs_Write.putString(Constants.PREFS_APP_VERSION, BuildConfig.VERSION_NAME);
        prefs_Write.apply();

        if (prefs_Read.getBoolean(Constants.PREFS_APP_WELCOME_DATA, true)) {
            new MaterialDialog.Builder(this)
                    .title("Bienvenue !")
                    .content("Merci d'avoir téléchargé notre application !\n" +
                            "Prenez le temps de la découvrir, et n'oubliez pas de vous y connecter à l'aide de votre profil ESEO !\n\nL'équipe ESEOmega Ω")
                    .negativeText("Allons-y !")
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            mDrawerLayout.openDrawer(mDrawerList);
                            prefs_Write.putBoolean(Constants.PREFS_APP_WELCOME_DATA, false);
                            prefs_Write.putString(Constants.PREFS_APP_VERSION, BuildConfig.VERSION_NAME); // prevent next message
                            prefs_Write.apply();
                        }
                    })
                    .show();
        } else {

            // App already installed
            // Check if app is updated
            if ((!(prefs_Read.getString(Constants.PREFS_APP_VERSION, "").equals(BuildConfig.VERSION_NAME))) && profile.isCreated()) {
                new MaterialDialog.Builder(this)
                        .title("Re-bonjour !")
                        .content("Merci d'avoir pris le temps de faire cette mise à jour ! " +
                                "Elle apporte des correctifs de sécurité aux différentes fonctions de l'application, ainsi que la possibilité de recevoir les notifications liées à la caféteria et aux news.\n" +
                                "Pour accéder à l'ensemble de ces services, nous vous demandons de bien vouloir vous reconnecter avec votre profil ESEO.\n\nL'équipe ESEOmega Ω")
                        .negativeText("OK")
                        .cancelable(false)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                profile.removeProfileFromPrefs(MainActivity.this);
                                profile.readProfilePromPrefs(MainActivity.this);
                                OnUserProfileChange(profile);
                                prefs_Write.putBoolean(Constants.PREFS_APP_WELCOME_DATA, false); // prevents from previous message
                                prefs_Write.putString(Constants.PREFS_APP_VERSION, BuildConfig.VERSION_NAME);
                                prefs_Write.apply();
                            }
                        })
                        .show();
            }
        }

        // If users asks for update check
        if (prefsUser.getBoolean(Constants.PREFS_GENERAL_UPDATE, false)) {
            AsyncCheckVersion asyncCheckVersion = new AsyncCheckVersion(MainActivity.this);
            asyncCheckVersion.execute();
        }
    }

    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (fragPosition == 1) getMenuInflater().inflate(R.menu.menu_ingenews, menu); // with Ingenews option
        else if (fragPosition == 2) getMenuInflater().inflate(R.menu.menu_event, menu); // with Event buy option
        else getMenuInflater().inflate(R.menu.main_less, menu); // without Ingenews
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {

            // Info : "A Propos" de l'application
            case R.id.action_info:
                new MaterialDialog.Builder(this)
                        .title(R.string.about_title)
                        .content(R.string.about_content)
                        .positiveText(R.string.dialog_contact)
                        .negativeText(R.string.dialog_close)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", MAIL_DIALOG, null));
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[APP] Questions / Problèmes");
                                emailIntent.putExtra(Intent.EXTRA_TEXT, "Version de l'application : " + BuildConfig.VERSION_NAME + "\n\n" + "...");
                                startActivity(Intent.createChooser(emailIntent, "Contacter les développeurs ..."));
                            }
                        })
                        .show();
                return true;

            // Ingénews : news du club du même nom, m'voyez
            case R.id.action_ingenews:
                Intent i = new Intent(MainActivity.this, IngeListActivity.class);
                startActivity(i);
                return true;

            // Event : on passe à la vue de l'historique d'achat des place events
            case R.id.action_ticketevent:

                return true;

            // Action par défaut, aucune
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        // boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList); // not used
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Displaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;

        switch (position) {
            case 0: // Edit profile
                if (!profile.isCreated()) fragment = new ConnectProfileFragment();
                else fragment = new ViewProfileFragment();
                break;
            case 1: // News
                fragment = new NewsListFragment();
                break;
            case 2: // Events
                fragment = new EventsFragment();
                break;
            case 3: // Clubs & Vie Asso
                fragment = new CommunityFragment();
                break;
            case 4: // Commande Cafet
                fragment = new OrderHistoryFragment();
                break;
            case 5: // Bons plans
                fragment = new TipsFragment();
                break;
            case 7: // Réglages
                fragment = new SettingsFragment();
                break;

            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment, "frag" + position).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerList.setItemsCanFocus(true);

            // Invalidate Menu to redraw it : Ingenews only visible from fragment n°1 (news)
            invalidateOptionsMenu();
            fragPosition = position;

            if (position < navMenuTitles.length) {
                setTitle(navMenuTitles[position]);
            } else {
                setTitle(navMenuOptions[position-navMenuTitles.length-1]);
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
            }, 100);
        }
    }

    /**
     * Sets the title of the current app window
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * INTERFACE : Synchronises the new profile with the old one in drawer
     */
    @Override
    public void OnUserProfileChange (UserProfile profile) {
        if (profile != null && navDrawerItems != null && navAdapter != null) {
            this.profile = profile;
            navDrawerItems.set(0, profile.getDrawerProfile());
            navAdapter.notifyDataSetChanged();
            mDrawerLayout.openDrawer(mDrawerList);
            navAdapter.setBitmap(profile.getPicturePath().length() == 0 ? null :
                    ImageUtils.getResizedBitmap(BitmapFactory.decodeFile(profile.getPicturePath()), MAX_PROFILE_SIZE));
        }
    }

    /**
     * INTERFACE : On item added to cart, refresh the content and title of order tab's title
     */
    @Override
    public void OnItemAddToCart() {
        OrderTabsFragment mFragment = (OrderTabsFragment) getSupportFragmentManager().findFragmentByTag(Constants.TAG_FRAGMENT_ORDER_TABS);
        mFragment.refreshCart();
    }

    /**
     * On back pressed : asks user if he really want to loose the cart's content (if viewing OrderTabsFragment)
     */
    @Override
    public void onBackPressed() {
        OrderTabsFragment fragment = (OrderTabsFragment) getSupportFragmentManager().findFragmentByTag(Constants.TAG_FRAGMENT_ORDER_TABS);
        if (fragment != null && fragment.isVisible() && DataManager.getInstance().getNbCartItems() > 0) {
            new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.order_cancel_title)
                    .content(R.string.order_cancel_message)
                    .positiveText(R.string.dialog_yes)
                    .negativeText(R.string.dialog_no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            MainActivity.super.onBackPressed();
                        }
                    })
                    .show();
        } else { // Another fragment, we don't care
            MainActivity.super.onBackPressed();
        }
    }

    /**
     * onStop : we don't care
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * onResume : Register listener for BroadcastReceiver
     */
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    /**
     * onPause : Unregister listener for BroadcastReceiver
     */
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
