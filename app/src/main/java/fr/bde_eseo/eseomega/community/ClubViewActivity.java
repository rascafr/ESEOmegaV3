package fr.bde_eseo.eseomega.community;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import fr.bde_eseo.eseomega.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.bde_eseo.eseomega.Constants;

/**
 * Created by Rascafr on 31/08/2015.
 */
public class ClubViewActivity extends AppCompatActivity {

    public static final String COM_SNAPCHAT_ANDROID = "com.snapchat.android";
    private Toolbar toolbar;
    private ClubItem clubItem;
    private TextView tvDesc, tvNoMember;
    private ImageView imgClub, iWeb, iFb, iTw, iSnap, iMail, iLinked, iPhone, iYou, iInsta;
    private DisplayImageOptions options;
    private ArrayList<MixedItem> items;
    private MyMembersAdapter mAdapter;
    private RecyclerView recList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_view);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(ClubViewActivity.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                clubItem = ClubDataHolder.getInstance().getClubs().get(extras.getInt(Constants.KEY_CLUB_VIEW));
                getSupportActionBar().setTitle(clubItem.getName());
            }
        } else {
            clubItem = ClubDataHolder.getInstance().getClubs().get((int) savedInstanceState.getSerializable(Constants.KEY_CLUB_VIEW));
            getSupportActionBar().setTitle(clubItem.getName());
        }

        tvDesc = (TextView) findViewById(R.id.tvDescClub);
        tvNoMember = (TextView) findViewById(R.id.tvNoMember);
        imgClub = (ImageView) findViewById(R.id.imgClub);

        iWeb = (ImageView) findViewById(R.id.icoWeb);
        iFb = (ImageView) findViewById(R.id.icoFb);
        iTw = (ImageView) findViewById(R.id.icoTwitter);
        iSnap = (ImageView) findViewById(R.id.icoSnap);
        iMail = (ImageView) findViewById(R.id.icoMail);
        iLinked = (ImageView) findViewById(R.id.icoLinkedIn);
        iYou = (ImageView) findViewById(R.id.icoYoutube);
        iPhone = (ImageView) findViewById(R.id.icoPhone);
        iInsta = (ImageView) findViewById(R.id.icoInsta);

        tvDesc.setText(clubItem.getDetails());
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.solid_loading_background)
                .showImageForEmptyUri(R.drawable.solid_loading_background)
                .showImageOnFail(R.drawable.solid_loading_background)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().displayImage(clubItem.getImg(), imgClub, options);

        // Image visibility
        if (clubItem.hasWeb()) iWeb.setVisibility(View.VISIBLE);
        else iWeb.setVisibility(View.GONE);

        if (clubItem.hasFacebook()) iFb.setVisibility(View.VISIBLE);
        else iFb.setVisibility(View.GONE);

        if (clubItem.hasTwitter()) iTw.setVisibility(View.VISIBLE);
        else iTw.setVisibility(View.GONE);

        if (clubItem.hasSnapchat()) iSnap.setVisibility(View.VISIBLE);
        else iSnap.setVisibility(View.GONE);

        if (clubItem.hasInsta()) iInsta.setVisibility(View.VISIBLE);
        else iInsta.setVisibility(View.GONE);

        if (clubItem.hasMail()) iMail.setVisibility(View.VISIBLE);
        else iMail.setVisibility(View.GONE);

        if (clubItem.hasLinkedIn()) iLinked.setVisibility(View.VISIBLE);
        else iLinked.setVisibility(View.GONE);

        if (clubItem.hasYoutube()) iYou.setVisibility(View.VISIBLE);
        else iYou.setVisibility(View.GONE);

        if (clubItem.hasPhone()) iPhone.setVisibility(View.VISIBLE);
        else iPhone.setVisibility(View.GONE);

        // Create data array
        items = new ArrayList<>();

        // Adapter & recycler view
        mAdapter = new MyMembersAdapter();
        recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        // Fill data array with Data Holder's objects
        for (int m=0;m<clubItem.getModules().size();m++) {
            ClubItem.ModuleItem mi = clubItem.getModules().get(m);
            items.add(new MixedItem(mi.getName()));

            for (int mm=0;mm<mi.getMembers().size();mm++) {
                ClubItem.ModuleItem.TeamItem ti = mi.getMembers().get(mm);
                items.add(new MixedItem(ti.getName(), ti.getDetail(), ti.getImg()));
            }
        }

        if (items.size() > 0)
            tvNoMember.setVisibility(View.GONE);
        else
            tvNoMember.setVisibility(View.VISIBLE);

        mAdapter.notifyDataSetChanged();

        // Prepare intents

        // Web (browser)
        iWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToBrowser(clubItem.getWeb());
            }
        });

        // Facebook (browser or app)
        iFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToBrowser(clubItem.getFb());
            }
        });

        // Twitter (browser or app)
        iTw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name="+clubItem.getTwitter()));
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
                else
                    intentToBrowser("https://twitter.com/" + clubItem.getTwitter());
            }
        });

        // Snapchat (dialog or app)
        iSnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialDialog.Builder mdb = new MaterialDialog.Builder(ClubViewActivity.this)
                        .title(clubItem.getSnap())
                        .content("Ajoutez le pseudo ci-dessus sur Snapchat !")
                        .negativeText("Fermer")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }

                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                openApp(ClubViewActivity.this, COM_SNAPCHAT_ANDROID);
                            }
                        });

                if (isPackageExisted(COM_SNAPCHAT_ANDROID))
                    mdb.positiveText("Ouvrir Snapchat");

                mdb.show();
            }
        });

        // Instagram (browser or app)
        iInsta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("instagram://user?username="+clubItem.getInstagram()));
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
                else
                    intentToBrowser("https://instagram.com/" + clubItem.getInstagram() + "/");
            }
        });

        // Youtube (browser or app)
        iYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToBrowser(clubItem.getYoutube());
            }
        });

        // Mail (app)
        iMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", clubItem.getMail(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Rensignements] ... ");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Bonjour ...");
                startActivity(Intent.createChooser(emailIntent, "Contacter " + clubItem.getName() + " ..."));
            }
        });

        // Phone call (app)
        iPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clubItem.getTel()));
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(ClubViewActivity.this, "Pas d'application installée pour ça !", Toast.LENGTH_SHORT).show();
            }
        });

        // LinkedIn (website or app)
        iLinked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToBrowser(clubItem.getLinkedin());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {

            case android.R.id.home:
                this.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Custom class for Member + Module
     */
    private class MixedItem {
        private boolean isModule;
        private String name, dec, imgLink;

        public MixedItem(String name) {
            this.name = name;
            isModule = true;
        }

        public MixedItem(String name, String dec, String imgLink) {
            this.name = name;
            this.dec = dec;
            this.imgLink = imgLink;
            isModule = false;
        }

        public boolean isModule() {
            return isModule;
        }

        public String getImgLink() {
            return imgLink;
        }

        public String getDesc() {
            return dec;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Custom adapter for members + module header
     */
    public class MyMembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int TYPE_MODULE = 0;
        private final static int TYPE_MEMBER = 1;
        private DisplayImageOptions options;

        public MyMembersAdapter () {
            this.options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MixedItem mi = items.get(position);

            if (mi.isModule()) {
                ModuleViewHolder mvh = (ModuleViewHolder) holder;
                mvh.name.setText(mi.getName());
            } else {
                MemberViewHolder mvh = (MemberViewHolder) holder;
                mvh.name.setText(mi.getName());
                //String s = mi.getDesc();
                //mvh.desc.setText(s!=null&&s.length()>0?s:"Membre");
                mvh.desc.setText(Html.fromHtml(mi.getDesc()));
                ImageLoader.getInstance().displayImage(mi.getImgLink(), mvh.img, options);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_MODULE)
                return new ModuleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_club_header, parent, false));
            else
                return new MemberViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_member, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).isModule()?TYPE_MODULE:TYPE_MEMBER;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // Classic View Holder for Module (header)
        public class ModuleViewHolder extends RecyclerView.ViewHolder {

            protected TextView name;

            public ModuleViewHolder(View v) {
                super(v);
                name = (TextView) v.findViewById(R.id.tvModuleHeader);
            }
        }

        // Classic View Holder for Member
        public class MemberViewHolder extends RecyclerView.ViewHolder {

            protected TextView name, desc;
            protected CircleImageView img;

            public MemberViewHolder(View v) {
                super(v);
                name = (TextView) v.findViewById(R.id.tvNameMember);
                desc = (TextView) v.findViewById(R.id.tvDescMember);
                img = (CircleImageView) v.findViewById(R.id.circleMember);
            }
        }
    }

    // Intent to browser
    public void intentToBrowser (String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    // Package exists ?
    public boolean isPackageExisted(String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }

    /** Open another app.
     * @param context current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();

        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
            //throw new PackageManager.NameNotFoundException();
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(i);
        return true;
    }

}
