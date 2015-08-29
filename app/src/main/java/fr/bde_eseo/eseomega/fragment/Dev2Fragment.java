package fr.bde_eseo.eseomega.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rascafr.test.matdesignfragment.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EncodingUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr on 19/07/2015.
 */
public class Dev2Fragment extends Fragment {

    public Dev2Fragment() {}

    private TextView textView, tvConsole;
    private WebView webView;
    private ScrollView scrollView;
    private EditText etLogin, etPassword, etPing;
    private Button bpMagic, bpSave, bpLogout;
    private String postLogin;
    private String postPassword;
    private int pingDelay;
    private String htmlPage;
    private int nbLoad = 0;
    private int stepID = 0;
    private boolean go = false;
    private MaterialDialog mdProgress;

    /**
     * Protocol :
     * 1 - load http://campus.eseo.fr/ -> create cookies
     * 2 - load +webvpn+ injecting POST data
     * 3 - load bandeau.aspx
     * 4 - inject Javascript into bandeau.aspx to get source code with associated interface
     */

    private static final String URL_HOME = "http://campus.eseo.fr/";
    private static final String URL_VPN = "https://portail.eseo.fr/+webvpn+/index.html";
    private static final String URL_ASPX = "https://portail.eseo.fr/OpDotNet/Noyau/Bandeau.aspx?hideMenu=true";
    private static final String URL_LOGOUT = "https://portail.eseo.fr/+webvpn+/webvpn_logout.html";
    private static final String URL_CSC = "https://portail.eseo.fr/+CSCO+00756767633A2F2F726672622D617267++/OpDotNet/Specifique/ADAUTHENTIF/default.aspx?";
    private static final String DATA_POST1 = "tgroup=&next=&tgcookieset=&Login=Connexion&username=";
    private static final String DATA_POST2 = "&password=";
    private static final String DATA_POST3 = "&group_list=Student_ssl";

    private static final String DATA_POST4 = "CSCO_WRAPPED=1&proxy=0&handler=2&req_method=GET&realm=&ucte_headers=R0VUIC9PcERvdE5ldC9TcGVjaWZpcXVlL0FEQVVUSEVOVElGL2RlZmF1bHQuYXNweD8gSFRUUC8xLjENCkhvc3Q6IGVzZW8tbmV0DQpBY2NlcHQ6IHRleHQvaHRtbCxhcHBsaWNhdGlvbi94aHRtbCt4bWwsYXBwbGljYXRpb24veG1sO3E9MC45LGltYWdlL3dlYnAsKi8qO3E9MC44DQpVc2VyLUFnZW50OiBNb3ppbGxhLzUuMCAoV2luZG93cyBOVCA2LjM7IFdPVzY0KSBBcHBsZVdlYktpdC81MzcuMzYgKEtIVE1MLCBsaWtlIEdlY2tvKSBDaHJvbWUvNDMuMC4yMzU3LjEzNCBTYWZhcmkvNTM3LjM2DQpSZWZlcmVyOiBodHRwczovL3BvcnRhaWwuZXNlby5mci8rQ1NDT0UrL3BvcnRhbC5odG1sDQpBY2NlcHQtTGFuZ3VhZ2U6IGZyLUZSLGZyO3E9MC44LGVuLVVTO3E9MC42LGVuO3E9MC40DQo%3D=&type=NTLM&ucte_body=&auth_attempt=1&username=leparofr&password=ff2e2w6n93&Continue=Continuer";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dev, container, false);

        textView = (TextView) rootView.findViewById(R.id.tvDebug);
        tvConsole = (TextView) rootView.findViewById(R.id.tvConsole);
        webView = (WebView) rootView.findViewById(R.id.webviewDebug);
        bpMagic = (Button) rootView.findViewById(R.id.buttonMagic);
        bpSave = (Button) rootView.findViewById(R.id.buttonSave);
        bpLogout = (Button) rootView.findViewById(R.id.buttonLogout);
        etLogin = (EditText) rootView.findViewById(R.id.etLogin);
        etPassword = (EditText) rootView.findViewById(R.id.etPassword);
        etPing = (EditText) rootView.findViewById(R.id.etTime);
        scrollView = (ScrollView) rootView.findViewById(R.id.consScroll);

        tvConsole.setText(tvConsole.getText() + (" - Init console [OK] - \n"));

        postLogin = "leparofr";
        postPassword = "ff2e2w6n93";
        pingDelay = 20000;

        //mdProgress = new MaterialDialog.Builder(getActivity()).

        CookieManager.getInstance().setAcceptCookie(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(getActivity()), "HTMLOUT");
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, final String url) {
                //Toast.makeText(getActivity(), "Finished load for \"" + url + "\"", Toast.LENGTH_SHORT).show();
                Log.d("URL DEV", url);
                tvConsole.setText(tvConsole.getText() + ("Loaded : " + url + "\n"));
                scrollView.fullScroll(View.FOCUS_DOWN);

                if (go) {
                    tvConsole.setText(tvConsole.getText() + ("Step " + stepID + " - Wait " + pingDelay + " ms ...\n"));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (stepID == 0 && url.contains("https://portail.eseo.fr/+CSCOE+/logon.html")) {
                                webView.postUrl(URL_VPN, EncodingUtils.getBytes(DATA_POST1 + postLogin + DATA_POST2 + postPassword + DATA_POST3, "utf-8"));
                                tvConsole.setText(tvConsole.getText() + "Loading : " + URL_VPN + " (POST)\n");

                                mdProgress.incrementProgress(1);
                                mdProgress.setContent("POST data to server ...");
                                stepID++;
                            } else if (stepID == 1 && url.contains("https://portail.eseo.fr/+CSCOE+/portal.html")) {
                                webView.loadUrl(URL_ASPX);
                                tvConsole.setText(tvConsole.getText() + "Loading : " + URL_ASPX + "\n");
                                mdProgress.incrementProgress(1);
                                mdProgress.setContent("Preparing for Javascript injection ...");
                                stepID++;
                            } else if (stepID == 2 && url.contains("https://portail.eseo.fr/+CSCO+00756767633A2F2F726672622D617267++/OpDotNet/Noyau/Bandeau.aspx?hideMenu=true")) {
                                webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                                tvConsole.setText(tvConsole.getText() + "Injecting Javascript ...\n");
                                mdProgress.incrementProgress(1);
                                mdProgress.setContent("Injecting Javascript ...");
                                Handler handlerWindow = new Handler();
                                handlerWindow.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvConsole.setText(tvConsole.getText() + "Done ! Results :\n");
                                        int index = -1;
                                        if (htmlPage != null)
                                            index = htmlPage.indexOf("lblBonjour");

                                        if (index != -1) {

                                            String userName = htmlPage.substring(index + 25, htmlPage.indexOf("</span>", index));
                                            tvConsole.setText(tvConsole.getText() + "Found index != -1 for user \"" + userName + "\" [OK]\n\n");

                                            MaterialDialog md = new MaterialDialog.Builder(getActivity())
                                                    .title("Portail ESEO access")
                                                    .content("Hey, coucou \"" + userName + "\" !\n(wow, ça a marché)")
                                                    .negativeText("Close")
                                                    .show();


                                        } else {
                                            tvConsole.setText(tvConsole.getText() + "Error, no username founded.\n\n");
                                            MaterialDialog md = new MaterialDialog.Builder(getActivity())
                                                    .title("Portail ESEO access")
                                                    .content(htmlPage)
                                                    .negativeText("Close")
                                                    .show();
                                        }

                                        mdProgress.hide();
                                    }
                                }, pingDelay);
                                stepID++;
                            } else if (stepID == 3) {
                                webView.loadUrl(URL_LOGOUT);
                                textView.setText("Login' out ...");
                                go = false;
                            }
                        }
                    }, pingDelay);
                }

            }

        });
        webView.setVisibility(View.GONE);
        //webView.setBackgroundColor(Color.parseColor("#7f000000"));

        bpMagic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                postLogin = etLogin.getText().toString();
                postPassword = etPassword.getText().toString();

                AsyncLogin asyncLogin = new AsyncLogin(getActivity());
                asyncLogin.execute(URL_HOME + "login/index.php");
                tvConsole.setText("executing request " + URL_HOME + "login/index.php" + "\n\n");

                /*mdProgress = new MaterialDialog.Builder(getActivity())
                        .title("Connecting ...")
                        .content("Creating cookies ...")
                        .progress(false, 4, true)
                        .show();


                pingDelay = Integer.parseInt(etPing.getText().toString());
                Toast.makeText(getActivity(), "Try with \"" + postLogin + "\" at " + pingDelay + " ms delay", Toast.LENGTH_SHORT).show();

                webView.loadUrl(URL_HOME);
                tvConsole.setText(tvConsole.getText() + "Loading : " + URL_HOME + "\n");

                go = true;
                stepID = 0;*/

                //webView.postUrl(URL_HOME + "login/index.php", EncodingUtils.getBytes("username=" + postLogin + "&password=" + postPassword + "&rememberusername=1", "utf-8"));
            }
        });

        bpSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int index = -1;
                if (htmlPage != null) index = htmlPage.indexOf("lblBonjour");

                if (index != -1) {
                    String userName = htmlPage.substring(index + 25, htmlPage.indexOf("</span>", index));

                    MaterialDialog md = new MaterialDialog.Builder(getActivity())
                            .title("Portail ESEO access")
                            .content("Hey, coucou \"" + userName + "\" !")
                            .negativeText("Close")
                            .show();


                } else {
                    MaterialDialog md = new MaterialDialog.Builder(getActivity())
                            .title("Portail ESEO access")
                            .content(htmlPage)
                            .negativeText("Close")
                            .show();
                }
                //CookieManager.getInstance().removeSessionCookie();

            }
        });

        bpLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(URL_LOGOUT);
                //CookieManager.getInstance().removeSessionCookie();
            }
        });

        return rootView;
    }

    class MyJavaScriptInterface {

        private Context ctx;

        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML (String html) {
            Toast.makeText(ctx, "Data loaded", Toast.LENGTH_SHORT).show();
            //tvConsole.setText(tvConsole.getText() + "JS injected\n");
            /*
            String res;
            if (htmlPage.contains("Authentification du serveur Web requise")) {
                res = "Connect : [OK]";
            } else {
                res = "Connect : [ERROR]";
            }
            MaterialDialog md = new MaterialDialog.Builder(ctx)
                    .title("Result")
                    .content(res)
                    .negativeText("Close")
                    .show();
            //CookieManager.getInstance().removeSessionCookie();
            stepID = 0;
            */
            htmlPage = html;


        }
    }

    class AsyncLogin extends AsyncTask<String, String, String> {

        private Context ctx;
        private boolean errorsHasOccured = false;

        public AsyncLogin (Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... urls) {

            String result = "";

            // Create a local instance of cookie store
            CookieStore cookieStore = new BasicCookieStore();

            // Create local HTTP context
            HttpContext localContext = new BasicHttpContext();
            // Create our client
            HttpClient httpclient = new DefaultHttpClient();
            // Bind custom cookie store to the local context
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            HttpPost httpPost = new HttpPost(urls[0]);


            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", postLogin));
            params.add(new BasicNameValuePair("password", postPassword));
            params.add(new BasicNameValuePair("rememberusername", "0"));
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                Log.d("HTTPPOST", "executing request " + httpPost.getURI());

                // Pass local context as a parameter
                HttpResponse response = httpclient.execute(httpPost, localContext);
                HttpEntity entity = response.getEntity();

                if (entity != null) {

                    Log.d("HTTP", "Response [OK]");

                    // A Simple Response Read
                    InputStream instream = entity.getContent();
                    result = Utilities.convertStreamToString(instream);
                    // now you have the string representation of the HTML request
                    instream.close();

                    // now disconnect
                    String sCampus;
                    int id = result.indexOf("http://campus.eseo.fr/login/logout.php?sesskey=");
                    int ed = result.indexOf("\"", id);


                    if (id != -1 && ed != -1) {
                        sCampus = result.substring(id, ed);
                        Log.d("HTTP", "Sessionkey : " + sCampus);
                        HttpGet httpGet = new HttpGet(sCampus);
                        httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
                        httpclient.execute(httpGet, localContext);
                        Log.d("HTTP", "Disconnected [OK]");
                    } else {
                        errorsHasOccured = true;
                    }
                } else {
                    errorsHasOccured = true;
                }

            } catch (IOException e) {
                errorsHasOccured = true;
                e.printStackTrace();
            }

            return result;
        }

        // Once connexion is done
        @Override
        protected void onPostExecute(String result) {
            //Log.d("HTTPPOST", result.length() + " bytes of data : " + result);
            tvConsole.setText(result.length() + " bytes of data : \n\n" + result);

            int ind = result.indexOf("title=\"Consulter le profil\">");
            int end = result.indexOf("</a>", ind);
            String res = "Erreur de connexion";
            if (ind != -1 && end != -1) {
                Log.d("HTTP", "Username [OK]");
                res = "Hey, salut, " + result.substring(ind + 28, end) + " !";
            } else {
                errorsHasOccured = true;
            }

            if (errorsHasOccured) {
                res = "Erreur de connexion. Veuillez vérifier vos informations, puis réessayer.";
            }

            MaterialDialog md = new MaterialDialog.Builder(ctx)
                    .title("Oups ...")
                    .content(res)
                    .negativeText("Close")
                    .iconRes(R.drawable.apple)
                    .show();
        }
    }


}
