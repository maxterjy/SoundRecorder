package maxter.simrec;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RecordingService extends Service {

    MediaRecorder mRecorder = null;
    DBHelper mDBHelper = null;

    String mOutputPath = null;
    String mOutputName = null;

    long mStartRecTime = 0;

    public RecordingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = DBHelper.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRecorder != null) {
            stopRecording();
        }
    }

    void calculateOutputPath() {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SimpleRecorder/";

        File dir = new File(dirPath);
        if (!dir.exists())
            dir.mkdir();

        mOutputName =  "Record";

        try {
            SharedPreferences pref = getSharedPreferences("simple_sound_pref", MODE_PRIVATE);
            int n = pref.getInt("latest_record_index", 1);

            String index = String.format("%02d", n);
            mOutputName = "Record " + index;

            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("latest_record_index", n+1);
            editor.commit();
        }
        catch (Exception ex) {
            Log.i("RecordingService", "calculateOutputPath failed: " + ex.getMessage());
        }

        mOutputPath =  dirPath + mOutputName + ".mp4";
    }

    public void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);

        calculateOutputPath();
        mRecorder.setOutputFile(mOutputPath);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException ex){
            Log.e("RecordingService", "prepare failed: " + ex.getMessage());
        }

        mStartRecTime = System.currentTimeMillis();
    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        final long recLength = System.currentTimeMillis() - mStartRecTime;

        try {
            mDBHelper.addRecording(mOutputName, mOutputPath, recLength);
            Toast.makeText(this, "Saved to " + mOutputName, Toast.LENGTH_SHORT).show();
        }
        catch (Exception ex) {
            Log.i("RecordingService", "stopRecording failed: " + ex.getMessage());
        }
    }
}
