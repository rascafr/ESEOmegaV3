package fr.bde_eseo.eseomega.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rascafr.test.matdesignfragment.R;

import fr.bde_eseo.eseomega.interfaces.OnUserProfileChange;
import fr.bde_eseo.eseomega.model.UserProfile;

/**
 * Created by Rascafr on 29/07/2015.
 */
public class ViewProfileFragment extends Fragment {

    private UserProfile profile;
    private TextView tvUserName;
    private Button bpDisconnect, bpChangePicture;
    private String userName;
    private OnUserProfileChange mOnUserProfileChange;
    private String userFirst;
    private static final int INTENT_GALLERY_ID = 0x42; // quarantdeuuux t'as vu
    private static final int RESULT_OK = -1;

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
        bpDisconnect = (Button) rootView.findViewById(R.id.button_disconnect);
        bpChangePicture = (Button) rootView.findViewById(R.id.button_change);

        // Get current profile
        profile = new UserProfile();
        profile.readProfilePromPrefs(getActivity());
        userName = profile.getName();
        tvUserName.setText(userName);
        userFirst = profile.getFirstName();
        
        // If user want to change its profile picture, call Intent to gallery
        bpChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, INTENT_GALLERY_ID);
            }
        });

        // If disconnects, reset profile and says bye-bye
        bpDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                        profile.removeProfileFromPrefs(getActivity());

                        MaterialDialog md = new MaterialDialog.Builder(getActivity())
                                .title("Au revoir, " + userFirst + ".")
                                .content("Votre profil a été déconnecté de nos services.")
                                .negativeText("Fermer")
                                .cancelable(false)
                                .iconRes(R.drawable.ic_oppress)
                                .limitIconToDefaultSize()
                                .show();

                        mOnUserProfileChange.OnUserProfileChange(profile);

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                .replace(R.id.frame_container, new ConnectProfileFragment(), "FRAG_CONNECT_PROFILE")
                                .commit();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("INTENT", "Result is " + resultCode);
        if (requestCode == INTENT_GALLERY_ID && resultCode == RESULT_OK && data != null) {
            Uri profPicture = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getActivity().getContentResolver().query(profPicture, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Log.d("INTENT", "Image path is " + picturePath);
            if (profile != null) { // cas impossible, mais au cas où ...
                profile.setPicturePath(picturePath);
                profile.registerProfileInPrefs(getActivity());
                mOnUserProfileChange.OnUserProfileChange(profile);
            }
        }
    }
}
