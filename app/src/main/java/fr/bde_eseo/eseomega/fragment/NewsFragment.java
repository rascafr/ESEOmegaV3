package fr.bde_eseo.eseomega.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.utils.StringUtils;

import com.rascafr.test.matdesignfragment.R;
import fr.bde_eseo.eseomega.adapter.MyNewsAdapter;
import fr.bde_eseo.eseomega.model.NewsItem;
import fr.bde_eseo.eseomega.utils.Utilities;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by François on 13/04/2015.
 */
public class NewsFragment extends Fragment {

    public static final String NO_NEWS_ERROR_MESSAGE = "Il est mort Jim !\nAucune news n'a été trouvée : il peut s'agir de votre connexion au réseau, ou de l'état d'un de nos serveurs.";
    public static final String NO_NEWS_ERROR_MESSAGE_OFFLINE = "\n\nActivez votre connexion internet, puis tentez une nouvelle synchronisation.";

    public NewsFragment() {}

    private static NewsFragment instance;
    private RecyclerView recList;
    private TextView tempHeadTextView;
    private Random mRandom;
    private MyNewsAdapter mAdapter;
    private ProgressDialog prgDialog;
    private SharedPreferences prefs_Read;
    private SharedPreferences.Editor prefs_Write;
    private ProgressBar progressCircle;
    private static int errorMsg = 0;
    private static final String[] ERROR_STRINGS =  {"C'est mort on t'a dit", "Tu le fais exprès ?",
                                                    "L'espoir fait vivre", "C'est l'histoire de Toto ... sérieux ?", "Abandonne.",
                                                    "Lourd !", "Va faire un KFC plutôt ?"};

    private static final String TAG = "NewsDBG";

    private static final String newsURL_ESEOmega = "http://176.32.230.7/eseomega.com/blog/feed/";

    private static final String noInternetMessage = "Echec de la mise à jour des news.\nLors du premier affichage des news, " +
                                                    "un accès à Internet est nécessaire afin de pouvoir se connecter au serveur. " +
                                                    "Vérifiez vos réglages, puis réessayez.";

    private static final String DIALOG_OMEGA = "Les news qui suivent cet en-tête sont les news personnalisées de ESEOmega.<br>" +
            "Elles sont mises à jour régulièrement afin de vous tenir au courant de nos évenements respectifs, " +
            "maintenant que nous sommes le BDE de l'année à venir.";

    public static NewsFragment getInstance () {
        if (instance == null)
            instance = new NewsFragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_news_cards, container, false);
        tempHeadTextView = (TextView) rootView.findViewById(R.id.tvTemp);
        progressCircle = (ProgressBar) rootView.findViewById(R.id.progressNewsList);
        tempHeadTextView.setVisibility(View.GONE);
        mAdapter = new MyNewsAdapter(getActivity());
        mRandom = new Random();

        // Search for the listView, then set its adapter
        recList = (RecyclerView) rootView.findViewById(R.id.cardNews);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);

        // Set preferences objects
        prefs_Read = getActivity().getSharedPreferences(Constants.PREFS_NEWS_KEY, 0);
        prefs_Write = prefs_Read.edit();

        // Get file from cache directory
        String cachePath = getActivity().getCacheDir() + "/";
        final File cacheFileEseo = new File(cachePath + "news_eseomega.txt");

        // If we're online, then check news
        if (Utilities.isPingOnline(getActivity())) {

            // Do a long task
            DownloadNews dwn = new DownloadNews(getActivity());
            dwn.execute(newsURL_ESEOmega);

        } else { // Else, check if we already have downloaded news

            // Check if newsFile is present
            if (cacheFileEseo.exists()) {

                // Set temporary message
                tempHeadTextView.setVisibility(View.VISIBLE);
                tempHeadTextView.setText("Hors-ligne (Dernière mise à jour : " +
                                         prefs_Read.getString(Constants.PREFS_NEWS_LAST_DOWNLOAD_DATE, "jamais") +
                                         ")");
                /*final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tempHeadTextView.setVisibility(View.GONE);
                    }
                }, 2500);*/

                /* Load file from cache */

                // Prepare the array
                ArrayList<NewsItem> newsOmega = new ArrayList<>();

                // Set the header
                //newsOmega.add(new NewsItem("Flux d'actualités ESEOmega", true));

                // Get text for ESEOmega stream
                String result_omega = Utilities.getStringFromFile(cacheFileEseo);

                // Add data
                newsOmega = getNews(result_omega, newsOmega);
                if (newsOmega.size() == 1) { // Only header
                    newsOmega.add(new NewsItem(null, NO_NEWS_ERROR_MESSAGE + NO_NEWS_ERROR_MESSAGE_OFFLINE, null, null, null));
                }

                // Set the adapter
                mAdapter.setNewsArray(newsOmega);

            } else {
                // File doesn't exist, set an arbitrary Error message
                mAdapter.addErrorMessage(noInternetMessage);
                Log.d(TAG, "Error message addded");
            }

        }

        // On click custom listener
        recList.addOnItemTouchListener(
            new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {

                    NewsItem ni = mAdapter.getItem(position);
                    if (mAdapter.getItem(position).getName() == null) { // It's an error message
                        int r = mRandom.nextInt(ERROR_STRINGS.length);
                        Toast.makeText(getActivity(), ERROR_STRINGS[r], Toast.LENGTH_SHORT).show();
                    } else { // It's an article

                        ArticleFragment fragment = new ArticleFragment();
                        fragment.setNewsItem(ni);

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.slide_down, R.anim.slide_up, R.anim.slide_down, R.anim.slide_up)
                                .replace(R.id.frame_container, fragment, "FRAG_ARTICLE")
                                .addToBackStack("BACK")
                                .commit();

                    }
                }
            }));


        return rootView;
    }


    // Async Task Class
    class DownloadNews extends AsyncTask<String, String, String> {

        private Context context;
        String content_omega = "";

        public DownloadNews(Context context) {
            this.context = context;
        }

        // Show Progress bar before downloading
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressCircle.setVisibility(View.VISIBLE);

            /*
            prgDialog = new ProgressDialog(context);
            prgDialog.setMessage("Mise à jour des news.\nVeuillez patienter ...");
            prgDialog.setIndeterminate(false);
            prgDialog.setMax(100);
            prgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            prgDialog.setCancelable(false);
            prgDialog.show();*/
        }

        // Download File from Internet
        @Override
        protected String doInBackground(String... f_url) {

            content_omega = downloadFile(f_url[0]);

            return null;
        }

        // While Downloading File
        @Override
        protected void onProgressUpdate(String... progress) {
            // Set progress percentage
            //prgDialog.setProgress(Integer.parseInt(progress[0]));
        }

        // Once File is downloaded
        @Override
        protected void onPostExecute(String file_url) {
            // Dismiss the dialog after the file was downloaded
            //prgDialog.hide();
            progressCircle.setVisibility(View.GONE);
            Toast.makeText(context, "Mise à jour terminée !", Toast.LENGTH_SHORT).show();

            // Prepare array
            ArrayList<NewsItem> newsOmega = new ArrayList<>();

            // Prepare cache directory
            String cachePath = context.getCacheDir() + "/";
            File cacheFileEseo = new File(cachePath + "news_eseomega.txt");

            // Get text only for ESEOmega stream
            String result_omega = StringUtils.unescapeHtml3(content_omega);

            // Set the header
            //newsOmega.add(new NewsItem("Flux d'actualités ESEOmega", true));

            // Add data
            //newsOmega = getNewsFromXML(result_omega, newsOmega); // TODO
            newsOmega = getNews(result_omega, newsOmega);
            if (newsOmega.size() == 1) { // Only header
                newsOmega.add(new NewsItem(null, NO_NEWS_ERROR_MESSAGE, null, null, null));
            }

            // Then, write string to file into cache directory
            Utilities.writeStringToFile(cacheFileEseo, result_omega);

            // Save news download date
            prefs_Write.putString(Constants.PREFS_NEWS_LAST_DOWNLOAD_DATE, getCalendarAsString());
            prefs_Write.commit();

            //newsOmega.addAll(newsEldo);

            // Set the final array into listView
            mAdapter.setNewsArray(newsOmega);
            //for ...
            // mAdapter.addNewsCardItem(news.get(i).getName(), news.get(i).getContent(), news.get(i).getImageLink(), news.get(i).getAuthor());
        }
    }

    public ArrayList<NewsItem> getNewsFromXML (String content, ArrayList<NewsItem> news) {

        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(content));
            String title = "", description = "";

            try {
                parser.next();
                int eventType = parser.getEventType();

                while (parser.getEventType()!=XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType()==XmlPullParser.START_TAG) {
                        if (parser.getName().equals("title")) {
                            title = parser.nextText();
                            news.add(new NewsItem(title, "", "", "", ""));
                        }
                        if (parser.getName().equals("val")) {
                            description = parser.nextText();
                        }
                    }
                    parser.next();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return news;
    }

    // Get news objects
    // Ok ! V2.0
    public ArrayList<NewsItem> getNews (String content, ArrayList<NewsItem> news) {

        String title, description, author, imageLink, article, date, link;
        int st = 0, end;

        while ((st = content.indexOf("<item>", st)) != -1) {
        //for  (int i = 0;i<size;i++) {

            st += 5;

            /* Title */
            st = content.indexOf("<title>", st) + 7;
            end = content.indexOf("</title>", st);
            title = content.substring(st, end);

            st = content.indexOf("<link>", st) + 6;
            end = content.indexOf("</link>", st);
            link = content.substring(st, end);

            /* Date */
            st = content.indexOf("<pubDate>", st) + 9;
            end = content.indexOf("</pubDate>", st);
            date = content.substring(st, end);

            /* Author */
            st = content.indexOf("<dc:creator>", st) + 12;
            end = content.indexOf("</dc:creator>", st);
            author = content.substring(st, end);

            /* Image Link */

            // better :
            // Image it in the description header : so, get it, copy the string between <div></div> and remove it : it's description
            // The saved string contains <img> -> get the link : it's the image URL

            /*st = content.indexOf("<div class=\"entry-content\">", st) + 27;
            st = content.indexOf("<img", st) + 4;
            st = content.indexOf("src=\"", st) + 5;
            end = content.indexOf("\"", st+1);*/


            /* Description */
            st = content.indexOf("<description>", st) + 13;
            end = content.indexOf("</description>", st);
            description = content.substring(st, end);
            description = description.replace("[…]", "...");
            if (description.contains("<div>")) { // there is an image : split string into description an image link
                int stt = description.indexOf("src=\"") + 5;
                int endd = description.indexOf("\"", stt);
                int des = description.indexOf("</div>") + 7;
                imageLink = description.substring(stt, endd);
                description = "<![CDATA[" + description.substring(des);
            } else {
                imageLink = "http://web-hosting-blog.rackservers.com.au/wp-content/uploads/2012/08/internet-error-404-file-not-found.jpg"; // substitute, TODO
            }

            /* Content */
            st = content.indexOf("<content:encoded>", st) + 17;
            st = content.indexOf("<![CDATA[", st) + 9;
            end = content.indexOf("]]>", st);
            article = content.substring(st, end);
            if (article.contains("<div>")) {
                int des = article.indexOf("</div>") + 7;
                article = article.substring(des);
            }

            // Link to news object
            news.add(new NewsItem(title, description, article, imageLink, link, author, date));
        }

        return news;
    }

    public String downloadFile (String url) {

        String data_s = "";
        int count = 0;

        try {
            URL url_1 = new URL(url);
            URLConnection connection = url_1.openConnection();
            connection.connect();
            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url_1.openStream(),10*1024);
            InputStreamReader isr = new InputStreamReader(input);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(isr);
            String read = br.readLine();
            while (read != null) {
                sb.append(read);
                read = br.readLine();
            }

            data_s = sb.toString();

        } catch (Exception e) {
            e.printStackTrace(); //Log.e("Error: ", e.getMessage());
        }

        return data_s;
    }

    public static String getPlainText(String html) {
        String htmlBody = html.replaceAll("<hr>", ""); // one off for horizontal rule lines
        String plainTextBody = htmlBody.replaceAll("<[^<>]+>([^<>]*)<[^<>]+>", "$1");
        plainTextBody = plainTextBody.replaceAll("<br ?/>", "");
        return plainTextBody;
    }

    public String getCalendarAsString () {
        Calendar c  = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"), Locale.FRANCE);
        int day = c.get(Calendar.DATE), month = c.get(Calendar.MONTH) + 1, year = c.get(Calendar.YEAR);
        String c_s = ((day < 10) ? "0":"") + day + "/" + ((month < 10) ? "0":"") + month + "/" + year;

        return c_s;
    }
}
