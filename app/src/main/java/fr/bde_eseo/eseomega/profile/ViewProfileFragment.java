package fr.bde_eseo.eseomega.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rascafr.test.matdesignfragment.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.MainActivity;
import fr.bde_eseo.eseomega.interfaces.OnUserProfileChange;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.ImageUtils;

/**
 * Created by Rascafr on 29/07/2015.
 */
public class ViewProfileFragment extends Fragment {

    private UserProfile profile;
    private TextView tvUserName, tvDisconnect;
    private String userName;
    private CircleImageView imageView;
    private OnUserProfileChange mOnUserProfileChange;
    private String userFirst;
    private static final int INTENT_GALLERY_ID = 0x42; // quarantdeuuux t'as vu
    private static final int RESULT_OK = -1;
    private MaterialDialog materialDialog;

    public ViewProfileFragment () {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mOnUserProfileChange = (OnUserProfileChange) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Find layout elements
        View rootView = inflater.inflate(R.layout.fragment_view_profile, container, false);
        tvUserName = (TextView) rootView.findViewById(R.id.tvUserName);
        tvDisconnect = (TextView) rootView.findViewById(R.id.tvDisconnect);
        imageView = (CircleImageView) rootView.findViewById(R.id.circleView);

        // Get current profile
        profile = new UserProfile();
        profile.readProfilePromPrefs(getActivity());
        Log.d("PROFILE", profile.getId() + ", " + profile.getPushToken());
        userName = profile.getName();
        tvUserName.setText(userName);
        userFirst = profile.getFirstName();
        setImageView();
        
        // If user want to change its profile picture, call Intent to gallery
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, INTENT_GALLERY_ID);
            }
        });

        // If disconnects, reset profile and says bye-bye
        tvDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            tvDisconnect.setBackgroundColor(0x2fffffff);

            MaterialDialog mdConfirm = new MaterialDialog.Builder(getActivity())
                .title("Déconnexion")
                .content("Hey, " + userFirst + ", en êtes-vous vraiment sûr ?\nVous ne pourrez plus accéder à nos services (et ça, c'est dommage).")
                .positiveText("Oui, au revoir")
                .negativeText("Non, je reste")
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        AsyncDisconnect asyncDisconnect = new AsyncDisconnect(getActivity(), profile);
                        asyncDisconnect.execute(profile);
/*
                        materialDialog = new MaterialDialog.Builder(getActivity())
                                .title("Au revoir, " + userFirst + ".")
                                .content("Votre profil a été déconnecté de nos services.")
                                .negativeText("Fermer")
                                .cancelable(false)
                                .iconRes(R.drawable.ic_oppress)
                                .limitIconToDefaultSize()
                                .show();*/
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        Toast.makeText(getActivity(), "Vous avez fait le bon choix.", Toast.LENGTH_SHORT).show();
                    }
                })
                .iconRes(R.drawable.ic_devil)
                .limitIconToDefaultSize()
                .show();

            }
        });

        return rootView;
    }

    // Class to disconnect profile from server
    private class AsyncDisconnect extends AsyncTask<UserProfile, String, String> {

        private Context ctx;
        private UserProfile profile;

        public AsyncDisconnect (Context ctx, UserProfile profile) {
            this.ctx = ctx;
            this.profile = profile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            materialDialog = new MaterialDialog.Builder(getActivity())
                    .title("Déconnexion ...")
                    .content("Veuillez patienter ...")
                    .negativeText("Annuler")
                    .cancelable(false)
                    .progress(true, 4, false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            AsyncDisconnect.this.cancel(true);
                        }
                    })
                    .show();
        }

        @Override
        protected String doInBackground(UserProfile ... params) {
            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("client", this.profile.getId()));
            pairs.add(new BasicNameValuePair("password", this.profile.getPassword()));
            pairs.add(new BasicNameValuePair("os", Constants.APP_ID));
            pairs.add(new BasicNameValuePair("token", this.profile.getPushToken()));
            pairs.add(new BasicNameValuePair("hash", EncryptUtils.sha256(
                    getResources().getString(R.string.SALT_DESYNC_PUSH) +
                            this.profile.getId() +
                            this.profile.getPassword() +
                            Constants.APP_ID +
                            this.profile.getPushToken())));

            return ConnexionUtils.postServerData(Constants.URL_DESYNC_PUSH, pairs);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("RES", s);

            materialDialog.hide();

            this.profile.removeProfileFromPrefs(ctx);
            this.profile.readProfilePromPrefs(ctx);
            mOnUserProfileChange.OnUserProfileChange(this.profile);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.frame_container, new ConnectProfileFragment(), "FRAG_CONNECT_PROFILE")
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_GALLERY_ID && resultCode == RESULT_OK && data != null) {
            Uri profPicture = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getActivity().getContentResolver().query(profPicture, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            if (profile != null) { // cas impossible, mais au cas où ...
                profile.setPicturePath(picturePath);
                profile.registerProfileInPrefs(getActivity());
                setImageView();
                mOnUserProfileChange.OnUserProfileChange(profile);
            }
        }
    }

    public void setImageView () {
        File fp = new File(profile.getPicturePath());
        if (fp.exists()) {
            Bitmap bmp = ImageUtils.getResizedBitmap(BitmapFactory.decodeFile(profile.getPicturePath()), MainActivity.MAX_PROFILE_SIZE);
            if (bmp != null)
                imageView.setImageBitmap(bmp);
        }
    }
}
