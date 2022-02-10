
package com.sprd.validationtools.itemstest.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.EnvironmentEx;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MelodyTest extends BaseActivity {
    private final static String TAG = "MelodyTest";
    TextView mContent;
    TextView mCurrentActionText;
    private RadioButton mRingButton;
    private RadioButton mVbrateButton;
    private int backupMode = 0;
    private List<String> mFilePaths;
    private File mFile;
    MediaPlayer mPlayer = null;
    private Vibrator mVibrator = null;
    private static final long V_TIME = 100000;
    private static final long DIALOG_TIME = 3000;
    private static final long RECYCLE_TIME = 1000;
    private static final String DEFAULT_AUDIO = "Orion.ogg";
    private boolean isSearchFinished = false;
    private boolean isRingTested = false;
    private boolean isVibrateTested = false;

    private Runnable mR = new Runnable() {
        public void run() {
            if (mPlayer != null) {
                //showResultDialog(getString(R.string.melody_play_info));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //mContent = new TextView(this);
        setContentView(R.layout.melody_test);
        setTitle(R.string.phone_loopback_test);
	
        mCurrentActionText = (TextView) findViewById(R.id.current_action_textView);
        mRingButton = (RadioButton) findViewById(R.id.ring_button);
        mVbrateButton = (RadioButton) findViewById(R.id.vbrate_button);

        mRingButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
		if (mVibrator != null) {
		    mVibrator.cancel();
		}

		mRingButton.setEnabled(false);
		mVbrateButton.setEnabled(true);

		AudioManager audioManager = (AudioManager) MelodyTest.this.getSystemService(Context.AUDIO_SERVICE);
		backupMode = audioManager.getMode();
		
		if (mPlayer == null) {
		    mPlayer = new MediaPlayer();
		}
		
		//if (isSearchFinished) {
		    doPlay();
		//    mHandler.postDelayed(mR, DIALOG_TIME);
		//}
                isRingTested = true;
            }
        });

	mVbrateButton.setOnClickListener(new OnClickListener() {
	public void onClick(View v) {
		mRingButton.setEnabled(true);
		mVbrateButton.setEnabled(false);

		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}

		//AudioManager audioManager = (AudioManager) MelodyTest.this.getSystemService(Context.AUDIO_SERVICE);
		//audioManager.setMode(backupMode);

		if(mVibrator == null)
			mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		dovbrate();
                isVibrateTested = true;
            }
	});

        //mContent.setGravity(Gravity.CENTER);
        //mContent.setTextSize(25);
        //setContentView(mContent);
        setTitle(R.string.melody_test);
        mFilePaths = new ArrayList<String>();
        mPlayer = new MediaPlayer();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //mContent.setText(R.string.start_searching);
        new Thread() {
            public void run() {
                if (false){//(checkSDCard()) {
                    mFile = EnvironmentEx.getExternalStoragePath();
                    toSearchFiles(mFile);

                    if (mFilePaths.size() != 0) {
                        mHandler.sendEmptyMessage(SEARCH_FINISHED);
                        return;
                    }
                }

                File firstAudio = new File("/system/media/audio/ringtones", DEFAULT_AUDIO);
                if (firstAudio.exists()) {
                    mFilePaths.add(firstAudio.getPath());
                } else {
                    mFile = new File("/system/media/audio/ringtones");
                    toSearchFiles(mFile);
                }

                mHandler.sendEmptyMessage(SEARCH_FINISHED);
            }
        }.start();
	
	setAudio();
        mPassButton.setEnabled(false);
	mPassButton.setBackgroundColor(Color.GRAY);
	mHandler.sendMessageDelayed(mHandler.obtainMessage(RECYCLE_MSG), RECYCLE_TIME);
    }

    private final int SEARCH_FINISHED = 0;
    private final int RECYCLE_MSG = 1;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEARCH_FINISHED:
                    isSearchFinished = true;
                    //doPlay();
                    //mHandler.postDelayed(mR, DIALOG_TIME);
                    break;
                case RECYCLE_MSG:
		    if( isRingTested && isVibrateTested ){
			mPassButton.setEnabled(true);
                        mPassButton.setBackgroundColor(Color.GREEN);
                    }else{
			mHandler.sendMessageDelayed(mHandler.obtainMessage(RECYCLE_MSG), RECYCLE_TIME);
		    }
		    break;		
            }
        }
    };

    private void doPlay() {

        int audioNumber = getRandom(mFilePaths.size());
        if (mPlayer == null) {
            return;
        }
        try {
            //mPlayer.setDataSource(mFilePaths.get(audioNumber));
	    //setDataSourceFromResource(this.getResources(), mPlayer, R.raw.autumn);
	    setDataSourceFromResource(this.getResources(), mPlayer, R.raw.qiku);
            mPlayer.prepare();
        } catch (IllegalArgumentException e) {
            /*SPRD: fix bug350197 setDataSource fail due to crash @{*/
//            mPlayer = null;
            /* @}*/
            e.printStackTrace();
        } catch (IllegalStateException e) {
//            mPlayer = null;
            e.printStackTrace();
        } catch (IOException e) {
//            mPlayer = null;
            e.printStackTrace();
        }
        mPlayer.start();
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (AudioSystem.DEVICE_STATE_AVAILABLE == AudioSystem.getDeviceConnectionState(
                AudioManager.DEVICE_OUT_EARPIECE, "")) {
            audioManager.setMode(AudioManager.MODE_RINGTONE);//modi for SPCSS00160783
        }
        mPlayer.setVolume(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mPlayer.setLooping(true);
        //mVibrator.vibrate(V_TIME);
        mCurrentActionText.setText(getResources().getText(R.string.melody_play_tag));
    }

    private void dovbrate() {
	//long[] pattern = {500,3000,1000,3000,1000,3000}; // OFF/ON/OFF/ON   

	//mVibrator.vibrate(V_TIME);
	mVibrator.vibrate(new long[]{100,3000,1000},0);

        mCurrentActionText.setText(getResources().getText(R.string.vibrate_tag));
    }

    @Override
    protected void onResume() {
        super.onResume();

	mRingButton.setEnabled(true);
	mVbrateButton.setEnabled(true);

        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        backupMode = audioManager.getMode();
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        //if (isSearchFinished) {
        //    doPlay();
        //    mHandler.postDelayed(mR, DIALOG_TIME);
        //}
    }

    @Override
    protected void onPause() {
        super.onPause();

        //if (mVibrator != null) {
            mVibrator.cancel();
        //}

        if (mPlayer == null) {
            return;
        }
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;

        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(backupMode);
    }

    private boolean checkSDCard() {
        boolean hasSDCard = false;
        
        hasSDCard = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        return hasSDCard;
    }

    public void toSearchFiles(File file) {
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File tf : files) {
            if (tf.isDirectory()) {
                toSearchFiles(tf);
            } else {
                try {
                    if (tf.getName().indexOf(".mp3") > -1) {
                        mFilePaths.add(tf.getPath());
                    }
                    if (tf.getName().indexOf(".ogg") > -1) {
                        mFilePaths.add(tf.getPath());
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "pathError", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private int getRandom(int max) {
        double random = Math.random();
        int result = (int) Math.floor(random * max);
        return result;
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mR);
        super.onDestroy();
    }

    private static void setDataSourceFromResource(Resources resources,
            MediaPlayer player, int res) throws java.io.IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
            afd.close();
        }
    }

    private void setAudio() {
	AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_DTMF), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_SYSTEM), 0);
    }
}
