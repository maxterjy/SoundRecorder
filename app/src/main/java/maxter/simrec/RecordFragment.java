package maxter.simrec;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;


public class RecordFragment extends Fragment {

    final int REQUEST_PERMISSION_TO_RECORD = 1;
    final String REQUIRE_PERMISSIONS[] = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    Chronometer mChronometer = null;
    FloatingActionButton mFabRecord = null;
    boolean mIsRecording = false;
    CoordinatorLayout mCoordinatorLayout = null;
    TextView mTvStatus;
    Animation mRotateAnim;
    Animation mFadeAnim;

    public RecordFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View outputView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = outputView.findViewById(R.id.recordChronometer);
        mCoordinatorLayout = outputView.findViewById(R.id.coordinatorLayout);
        mFabRecord = outputView.findViewById(R.id.fabRecord);
        mFabRecord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onFabRecordClick();
            }
        });
        mTvStatus = outputView.findViewById(R.id.recordingStatusTv);

        mRotateAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_rotate);

        mFadeAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_fade_in);
        final Animation fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_fade_out);
        mFadeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mIsRecording) {
                    mTvStatus.setText("Recording...");
                }
                else {
                    mTvStatus.setText("Tap the button to start recording");
                }

                mTvStatus.startAnimation(fadeOutAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        return outputView;
    }

    private void onFabRecordClick() {
        if (!isPermissionGranted()) {
            requestNeededPermissions();
            return;
        }

        mIsRecording = !mIsRecording;

        if (mIsRecording) {
            startRecording();
        }
        else {
            stopRecording();
        }
    }

    public void startRecording() {
        mFabRecord.setImageResource(R.drawable.ic_media_stop);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();

        //mTvStatus.setText("Recording...");
        mTvStatus.clearAnimation();
        mTvStatus.startAnimation(mFadeAnim);

        Intent intent = new Intent(getActivity(), RecordingService.class);
        getActivity().startService(intent);
    }

    public void stopRecording() {
        mChronometer.stop();
        mFabRecord.setImageResource(R.drawable.ic_mic_white_36dp);

//        Snackbar snackBar = Snackbar.make(mCoordinatorLayout, "Record Saved", Snackbar.LENGTH_LONG);
//        snackBar.setAction("Open", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PlaybackFragment fragment = new PlaybackFragment();
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                fragment.show(transaction, "dialog_playback");
//            }
//        });
//        snackBar.show();

//        Toast.makeText(getActivity(), "Record saved", Toast.LENGTH_SHORT).show();

        mTvStatus.clearAnimation();
        mTvStatus.startAnimation(mFadeAnim);

        Intent intent = new Intent(getActivity(), RecordingService.class);
        getActivity().stopService(intent);
    }

    boolean isPermissionGranted() {
        for(String permission: REQUIRE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    //ref: https://android--code.blogspot.com/2017/08/android-request-multiple-permissions.html
    void requestNeededPermissions() {
        if (!isPermissionGranted()) { //do something, when permissions not granted
            boolean shouldShowPermissionRational = false;
            for(String permission: REQUIRE_PERMISSIONS) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    shouldShowPermissionRational = true;
                    break;
                }
            }

            if (shouldShowPermissionRational) { //show explanation of request permissions
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Permission Require");

                if (Build.VERSION.SDK_INT >= 24) {
                    builder.setMessage(Html.fromHtml("<b>Microphone</b> and <b>Storage</b><br/>are required to do the tasks", 0));
                }
                else {
                    builder.setMessage(Html.fromHtml("<b>Microphone</b> and <b>Storage</b><br/>are required to do the tasks"));
                }

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(REQUIRE_PERMISSIONS, REQUEST_PERMISSION_TO_RECORD);
                    }
                });
                builder.setNeutralButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else { //directly request for permission without explanation
                requestPermissions(REQUIRE_PERMISSIONS, REQUEST_PERMISSION_TO_RECORD);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_TO_RECORD) {
            if (!isPermissionGranted()) {
                Toast.makeText(getActivity(), "Permissions are denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
