package maxter.simrec;

import android.app.AlertDialog;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PlaybackFragment extends DialogFragment {

    ImageView mBtnClose = null;
    FloatingActionButton mFabPlay = null;
    MediaPlayer mMediaPlayer = null;
    boolean mIsPlaying = false;

//For handling SeekBar
    SeekBar mSeekBar = null;
    Handler mHandler = new Handler();
    Runnable mUiUpdater = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null) {
                int progress = mMediaPlayer.getCurrentPosition();
                updateUI(progress);

                postUpdateUiRunnable();
            }
        }
    };

    void postUpdateUiRunnable() {
        mHandler.postDelayed(mUiUpdater, 1000);
    }
//For handling SeekBar end

    TextView mTvCurrentTime;
    TextView mTvAudioLength;
    TextView mTvAudioName;

    RecordInfo mRecord = null;
    long mAudioLengthMinute = 0;
    long mAudioLengthSecond = 0;

    public static PlaybackFragment newInstance(RecordInfo record) {
        Bundle args = new Bundle();
        args.putParcelable("record_info", record);

        PlaybackFragment fragment = new PlaybackFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecord = getArguments().getParcelable("record_info");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.PlayBackDialogTheme;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_playback, null);
        builder.setView(view);

        mFabPlay = view.findViewById(R.id.fabPlay);
        mFabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabPlayClicked();
            }
        });

        mBtnClose = view.findViewById(R.id.btnClose);
        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mSeekBar = view.findViewById(R.id.seekbar);
        mSeekBar.setMax((int)mRecord.mLength);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateUI(progress);
                }

                if (mMediaPlayer != null && fromUser) {
                    mHandler.removeCallbacks(mUiUpdater);

                    postUpdateUiRunnable();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mUiUpdater);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mUiUpdater);
                    mMediaPlayer.seekTo(seekBar.getProgress());

                    postUpdateUiRunnable();
                }
            }
        });


        mTvAudioName = view.findViewById(R.id.tvAudioName);
        mTvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        mTvAudioLength = view.findViewById(R.id.tvAudioLength);

        mAudioLengthMinute = TimeUnit.MILLISECONDS.toMinutes(mRecord.mLength);
        mAudioLengthSecond = TimeUnit.MILLISECONDS.toSeconds(mRecord.mLength) - TimeUnit.MINUTES.toSeconds(mAudioLengthMinute);
        mTvAudioLength.setText(String.format("%02d:%02d", mAudioLengthMinute, mAudioLengthSecond));

        mTvAudioName.setText(mRecord.mName);

        Dialog dialog = builder.create();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private void onFabPlayClicked() {
        if (mIsPlaying) {
            pauseAudio();
        }
        else {
            if (mMediaPlayer == null) {
                playAudio();
            }
            else {
                resumeAudio();
            }
        }
    }

    void playAudio() {
        mIsPlaying = true;
        mFabPlay.setImageResource(R.drawable.ic_media_pause);

        mMediaPlayer = new MediaPlayer();

        try{
            String path = mRecord.mPath;
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();

            mSeekBar.setMax(mMediaPlayer.getDuration());

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopAudio();
                }
            });

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mSeekBar.setProgress(0);
                    mMediaPlayer.start();
                    postUpdateUiRunnable();
                }
            });
        }
        catch (IOException ex) {
            Log.i("PlaybackFragment", "playAudio failed: " + ex.getMessage());
        }
    }

    void resumeAudio() {
        mIsPlaying = true;
        mFabPlay.setImageResource(R.drawable.ic_media_pause);
        mHandler.removeCallbacks(mUiUpdater);
        mMediaPlayer.start();
        postUpdateUiRunnable();
    }

    void pauseAudio() {
        mIsPlaying = false;
        mFabPlay.setImageResource(R.drawable.ic_media_play);

        mMediaPlayer.pause();

        mHandler.removeCallbacks(mUiUpdater);
    }

    void stopAudio() {
        mIsPlaying = false;
        mFabPlay.setImageResource(R.drawable.ic_media_play);
        mTvCurrentTime.setText(String.format("%02d:%02d", mAudioLengthMinute, mAudioLengthSecond));

        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        mSeekBar.setProgress(mSeekBar.getMax());
        mHandler.removeCallbacks(mUiUpdater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_playback, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mMediaPlayer != null) {
            stopAudio();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            stopAudio();
        }
    }

    void updateUI(int progress) {
        mSeekBar.setProgress(progress);

        //current time
        long minute = TimeUnit.MILLISECONDS.toMinutes(progress);
        long second = TimeUnit.MILLISECONDS.toSeconds(progress) % 60; //- TimeUnit.MILLISECONDS.toSeconds(minute);

        mTvCurrentTime.setText(String.format("%02d:%02d", minute, second));
        //current time end
    }
}
