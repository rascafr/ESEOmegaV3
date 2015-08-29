package fr.bde_eseo.eseomega.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.rascafr.test.matdesignfragment.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import fr.bde_eseo.eseomega.model.NewsItem;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr on 18/07/2015.
 */
public class ArticleFragment extends Fragment {

    public ArticleFragment () {}

    // The current reading news
    public NewsItem newsItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_article, container, false);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.solid_loading_background)
                .showImageForEmptyUri(R.drawable.solid_loading_background)
                .showImageOnFail(R.drawable.solid_loading_background)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        TextView tvArticle = (TextView) rootView.findViewById(R.id.tvArticle);
        TextView tvTitle = (TextView) rootView.findViewById(R.id.tvTitle);
        ImageView imageArticle = (ImageView) rootView.findViewById(R.id.imageArticle);
        /*
        tvArticle.setText(Html.fromHtml(newsItem.getContent() + "<br><br><i>Le " + newsItem.getStrDate() + " par " + newsItem.getAuthor() + "</i>"));
        ImageLoader.getInstance().displayImage(newsItem.getImageLink(), imageArticle, displayImageOptions);
        tvTitle.setText(newsItem.getName());
        tvTitle.setTypeface(Typeface.SERIF);

        tvArticle.setText(Html.fromHtml("<p>This is some <b>random text</b></p><p>Clic ? <a href=\"http://www.google.com\">Hello word, this is a link</a></p>" +
                "<p>And ... </p><p>This is <a href=\"http://stackoverflow.com\">another link</a></p><img src=\"" +
                "http://cdn-careers.sstatic.net/careers/gethired/img/companypageadfallback-leaderboard-2.png?v=59b591051ad7\"/>", new MyImageGetter(), null));*/

        // Test
        try {
            //Log.d("HTML", "-- Parse Start --");
            InputStream assetsPath = getActivity().getAssets().open("data.html");
            //testParsingHtml(Utilities.convertStreamToString(assetsPath));
            String html = Utilities.convertStreamToString(assetsPath);
            //html = html.replace("<img", "&nbsp;<br>&nbsp;<br>&nbsp;<br>&nbsp;<br><img");
            URLImageParser uip = new URLImageParser(tvArticle, getActivity());
            Spanned htmlSpan = Html.fromHtml(html, uip, null);
            tvArticle.setText(htmlSpan);
            //Log.d("HTML", "-- Parse End --");
        } catch (IOException e) {
            e.printStackTrace();
        }

        tvArticle.setMovementMethod(LinkMovementMethod.getInstance());

        imageArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Vous allez être redirigé vers l'article en question.", Toast.LENGTH_SHORT).show();
                // Go to website
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getLink()));
                startActivity(browserIntent);
            }
        });
/*
        String mContent = "", str;
        try {
            InputStream is = getActivity().getAssets().open("lorem.txt");

            String UTF8 = "utf8";
            int BUFFER_SIZE = 8192;
            BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF8), BUFFER_SIZE);

            while ((str = br.readLine()) != null) {
                mContent += str;
            }
        }
        catch (IOException ex) {
            mContent = "Seems like there is an error.";
        }

        tvArticle.setText(mContent);
*/

        return rootView;
    }

    private class MyImageGetter implements Html.ImageGetter {
        public Drawable getDrawable(String source) {
            Log.d("DRAW", "Image source : " + source);

            return new BitmapDrawable(ImageLoader.getInstance().loadImageSync(source));
        }
    }


    public void setNewsItem (NewsItem newsItem) {
        this.newsItem = newsItem;
    }

    // Assuming : <p> for text, <img> for image and <a> for link

    private static final String H_START_IMG = "<img";
    private static final String H_START_LINK = "<a";
    private static final String H_START_TEXT = "<p";
    private static final String H_END_IMG = "/>";
    private static final String H_END_LINK = "</a>";
    private static final String H_END_TEXT = "</p>";

    public void testParsingHtml (String data) {

        /*

        Basics :
        Get the nearest tag
        distance

         */
        int posImg, posLink, posText, posEnd;
        int back = 0;
        boolean htmlInside, hasBeenParsed;
        data = data.replaceAll("\n", "");

        do {

            posImg = data.indexOf(H_START_IMG, back);
            posLink = data.indexOf(H_START_LINK, back);
            posText = data.indexOf(H_START_TEXT, back);

            htmlInside = posText != -1 || posImg != -1 || posLink != -1;
            hasBeenParsed = false;

            // At least one html fragment
            if (htmlInside) {
                // Text
                if (posText != -1 && ((posLink != -1 && posText < posLink ) || (posImg != -1 && posText < posImg) || (posLink == -1 && posImg == -1))) {
                    posEnd = data.indexOf(H_END_TEXT, posText);
                    if (posEnd != -1 && posEnd > posText) { // Correct !
                        Log.d("HTML", "Text data founded : " + data.substring(posText, posEnd));
                        back = posText + 2; // update back integer decade
                        hasBeenParsed = true;
                    }
                }

                // Link (URL)
                if (!hasBeenParsed && posLink != -1 && ((posText != -1 && posLink < posText ) || (posImg != -1 && posLink < posImg) || (posText == -1 && posImg == -1))) {
                    posEnd = data.indexOf(H_END_LINK, posLink);
                    if (posEnd != -1 && posEnd > posLink) { // Correct !
                        Log.d("HTML", "URL Link data founded : " + data.substring(posLink, posEnd));
                        back = posLink + 2; // update back integer decade
                        hasBeenParsed = true;
                    }
                }

                // Image
                if (!hasBeenParsed && posImg != -1 && ((posText != -1 && posImg < posText ) || (posLink != -1 && posImg < posLink) || (posText == -1 && posLink == -1))) {
                    posEnd = data.indexOf(H_END_IMG, posImg);
                    if (posEnd != -1 && posEnd > posImg) { // Correct !
                        Log.d("HTML", "Image data founded : "  + data.substring(posImg, posEnd));
                        back = posImg + 4; // update back integer decade
                    }
                }
            }

        } while (htmlInside);
    }

    /*
     * Simple copy & paste
     * @see : http://stackoverflow.com/questions/15617210/android-html-fromhtml-with-images
     */
    public class URLDrawable extends BitmapDrawable {
        // the drawable that you need to set, you could set the initial drawing
        // with the loading image if you need to
        protected Drawable drawable;

        @Override
        public void draw(Canvas canvas) {
            // override the draw to facilitate refresh function later
            if(drawable != null) {
                drawable.draw(canvas);
            }
        }
    }

    public class URLImageParser implements Html.ImageGetter {
        Context c;
        View container;

        /***
         * Construct the URLImageParser which will execute AsyncTask and refresh the container
         * @param t
         * @param c
         */
        public URLImageParser(View t, Context c) {
            this.c = c;
            this.container = t;
        }

        public Drawable getDrawable(String source) {
            URLDrawable urlDrawable = new URLDrawable();

            // get the actual source
            ImageGetterAsyncTask asyncTask =
                    new ImageGetterAsyncTask( urlDrawable);

            asyncTask.execute(source);

            // return reference to URLDrawable where I will change with actual image from
            // the src tag
            return urlDrawable;
        }

        public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
            URLDrawable urlDrawable;

            public ImageGetterAsyncTask(URLDrawable d) {
                this.urlDrawable = d;
            }

            @Override
            protected Drawable doInBackground(String... params) {
                String source = params[0];
                return fetchDrawable(source);
            }

            @Override
            protected void onPostExecute(Drawable result) {
                // set the correct bound according to the result from HTTP call
                urlDrawable.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());

                // change the reference of the current drawable to the result
                // from the HTTP call
                urlDrawable.drawable = result;

                // redraw the image by invalidating the container
                URLImageParser.this.container.invalidate();
            }

            /***
             * Get the Drawable from URL
             * @param urlString
             * @return
             */
            public Drawable fetchDrawable(String urlString) {
                try {
                    InputStream is = fetch(urlString);
                    Drawable drawable = Drawable.createFromStream(is, "src");
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    return drawable;
                } catch (Exception e) {
                    return null;
                }
            }

            private InputStream fetch(String urlString) throws MalformedURLException, IOException {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet request = new HttpGet(urlString);
                HttpResponse response = httpClient.execute(request);
                return response.getEntity().getContent();
            }
        }
    }
}
