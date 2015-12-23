package fr.bde_eseo.eseomega.news;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;

/**
 * Created by Rascafr on 30/08/2015.
 */
public class ViewNewsActivityWV extends AppCompatActivity {

    private Toolbar toolbar;
    private NewsItem newsItem;
    private MyImageAdapter mAdapter;
    private RecyclerView recList;
    private TextView tvName, tvAuthor;
    private WebViewTitle webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_wv);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(ViewNewsActivityWV.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                newsItem = new NewsItem(
                        extras.getString(Constants.KEY_NEWS_TITLE),
                        extras.getString(Constants.KEY_NEWS_AUTHOR),
                        extras.getString(Constants.KEY_NEWS_HTML),
                        extras.getString(Constants.KEY_NEWS_LINK),
                        extras.getStringArrayList(Constants.KEY_NEWS_IMGARRAY));

                getSupportActionBar().setTitle("Lecture news");
            }
        }

        if (newsItem != null) {
            mAdapter = new MyImageAdapter();
            recList = (RecyclerView) findViewById(R.id.recyList);
            recList.setAdapter(mAdapter);
            recList.setVisibility(View.VISIBLE);
            recList.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.HORIZONTAL);
            recList.setLayoutManager(llm);
            tvName = (TextView) findViewById(R.id.tvTitle);
            tvName.setText(newsItem.getName());
            tvAuthor = (TextView) findViewById(R.id.tvAuthor);
            tvAuthor.setText(newsItem.getAuthor());
            /*
            tvHtml = (TextView) findViewById(R.id.tvHtml);
            tvHtml.setText(Html.fromHtml(newsItem.getData()));
            tvHtml.setMovementMethod(LinkMovementMethod.getInstance());*/
            webView = (WebViewTitle) findViewById(R.id.webview);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setHapticFeedbackEnabled(false);
            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
            webView.setLongClickable(false);
            webView.loadData(newsItem.getData(), "text/html; charset=UTF-8", null);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_share:

                Intent intent2 = new Intent(); intent2.setAction(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.putExtra(Intent.EXTRA_TEXT, "\"" + newsItem.getName() + "\"\n" + newsItem.getLink());
                startActivity(Intent.createChooser(intent2, "Partager ..."));
                //NavUtils.navigateUpFromSameTask(this);
                return true;

            case android.R.id.home:
                this.onBackPressed();
                return true;

            case R.id.action_browser:
                String url = newsItem.getLink();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));

                if (i.resolveActivity(getPackageManager()) != null)
                    startActivity(i);
                else
                    Toast.makeText(ViewNewsActivityWV.this, "Pas d'application installée pour ça", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Image list adapter
     */
    private class MyImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NewsImageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_image, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final String link = newsItem.getImgLinks().get(position);
            NewsImageHolder nih = (NewsImageHolder) holder;
            ImageLoader.getInstance().displayImage(link, nih.img);
            nih.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(ViewNewsActivityWV.this, ImageViewActivity.class);
                    myIntent.putExtra(Constants.KEY_IMG, link);
                    startActivity(myIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return (newsItem != null && newsItem.getImgLinks() != null)?newsItem.getImgLinks().size():0;
        }

        private class NewsImageHolder extends RecyclerView.ViewHolder {
            protected ImageView img;
            public NewsImageHolder (View itemView) {
                super(itemView);
                img = (ImageView) itemView.findViewById(R.id.imgItem);
            }
        }
    }
}
