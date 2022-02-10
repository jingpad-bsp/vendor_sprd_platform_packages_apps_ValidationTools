package com.sprd.validationtools.itemstest.audio;

import android.app.Activity;
import android.app.Notification;
import android.graphics.Color;
import android.app.NotificationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.KeyEvent;
import android.view.Window;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.WindowManager;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;

public class SpeakerTestActivity extends BaseActivity
{
    private static final String TAG = "SpeakerTestActivity";
    private Button mRestButton = null;
    private int mCurrentMusicVolume = 0;
    private MediaPlayer mPlayer = null;
    private MediaPlayer mPlayerLeft = null;
    private MediaPlayer mPlayerRight = null;
    private int mPlayerId = 0;
    public RadioGroup mRadioGroup1;
    public RadioButton mRadioFemale112, mRadioMale112;
    private Handler mHandler;
    public boolean mSurpportLeftRightTest = true;
    private AudioManager mAudioManager;
    public Button mPlayLeft_bt, mPlayRight_bt,mStopLeft_bt,mStopRight_bt;
    public final int PLAY_LEFT = 1, PLAY_RIGHT = 2, STOP_LEFT = 3, STOP_RIGHT = 4;
    public boolean flag_bt_play_left,flag_bt_play_right,flag_bt_stop_left,flag_bt_stop_right;
    public final String PATH_AUDIO_LEFT = "/sys/bus/i2c/devices/4-005b/hwen";
    public final String PATH_AUDIO_RIGHT = "/sys/bus/i2c/devices/4-0058/hwen";


    private Handler mHandler1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG,"flag_bt_play_left = " + flag_bt_play_left + " flag_bt_play_right = " + flag_bt_play_right + " flag_bt_stop_left = " + flag_bt_stop_left + " flag_bt_stop_right = " + flag_bt_stop_right);
            if(flag_bt_play_left && flag_bt_play_right && flag_bt_stop_left && flag_bt_stop_right){
                mPassButton.setEnabled(true);
                mPassButton.setBackgroundColor(Color.GREEN);
                mFailButton.setEnabled(true);
                mFailButton.setBackgroundColor(Color.RED);
                mPlayRight_bt.setEnabled(false);
                mPlayLeft_bt.setEnabled(false);
                mStopLeft_bt.setEnabled(false);
                mStopRight_bt.setEnabled(false);
            }
            switch (msg.what){
                case PLAY_LEFT:
                    mPlayerLeft = MediaPlayer.create(getApplicationContext(), R.raw.orion);
                    mPlayerLeft.setLooping(false);
                    mPlayerLeft.setVolume(0,1);
                    mPlayerLeft.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mediaplayer) {
                            mPlayLeft_bt.setEnabled(true);
                            mPlayRight_bt.setEnabled(true);
                            mStopRight_bt.setEnabled(true);
                            flag_bt_stop_left = true;
                        }
                    });
                    try {
                        if(mPlayerLeft != null) {
                            mPlayerLeft.start();
                            mPlayerLeft.setLooping(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case STOP_LEFT:
                    try {
                        if(mPlayerLeft != null) {
                            mPlayerLeft.stop();
                            mPlayerLeft.release();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case PLAY_RIGHT:
                    mPlayerRight = MediaPlayer.create(getApplicationContext(), R.raw.speaktest);
                    mPlayerRight.setVolume(1,0);
                    mPlayerRight.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mediaplayer) {
                            mPlayRight_bt.setEnabled(true);
                            mPlayLeft_bt.setEnabled(true);
                            mStopLeft_bt.setEnabled(true);
                            flag_bt_stop_right = true;
                        }
                    });
                    try {
                        if(mPlayerRight != null) {
                            mPlayerRight.start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case STOP_RIGHT:
                    try {
                        if(mPlayerRight != null) {
                            mPlayerRight.stop();
                            mPlayerRight.release();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener radiogpchange = new RadioGroup.OnCheckedChangeListener() 
    {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) 
        {
            if (checkedId == mRadioFemale112.getId()) 
            {
                Log.d(TAG, " mRadioFemale112  mPlayerId  = " + mPlayerId );
                if(mPlayerId == 0)
                {
                    mPassButton.setEnabled(true);
                    mPassButton.setBackgroundColor(Color.GREEN);
                }
                else
                {
                    mPassButton.setEnabled(false);
                    mPassButton.setBackgroundColor(Color.RED);
                }
                mRadioFemale112.setEnabled(false);
                mRadioMale112.setEnabled(false);
            } 
            else if (checkedId == mRadioMale112.getId()) 
            {
                Log.d(TAG, " mRadioMale112  mPlayerId  = " + mPlayerId );
                if(mPlayerId == 1)
                {
                    mPassButton.setEnabled(true);
                    mPassButton.setBackgroundColor(Color.GREEN);
                }
                else
                {
                    mPassButton.setEnabled(false);
                    mPassButton.setBackgroundColor(Color.RED);
                }
                mRadioFemale112.setEnabled(false);
                mRadioMale112.setEnabled(false);
            }
            else
            {
                mPassButton.setEnabled(false);
                mPassButton.setBackgroundColor(Color.RED);
                mRadioFemale112.setEnabled(false);
                mRadioMale112.setEnabled(false);
            }
        }
    };

    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        setContentView(R.layout.speaker_test); 

        mRadioGroup1 = (RadioGroup) findViewById(R.id.gendergroup);
        mRadioFemale112 = (RadioButton) findViewById(R.id.female112);
        mRadioMale112 = (RadioButton) findViewById(R.id.male112);
        mRadioGroup1.setOnCheckedChangeListener(radiogpchange);

        ImageView icon = (ImageView)findViewById(R.id.iv_icon);
        TextView tips = (TextView)findViewById(R.id.tv_tips);
        tips.setText(getString(R.string.speaker_tips));
        icon.setImageDrawable(getResources().getDrawable(R.drawable.bottomspeaker));
        icon.setVisibility(View.GONE);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        if(mSurpportLeftRightTest) {
            mPassButton.setEnabled(false);
            mPassButton.setBackgroundColor(Color.GRAY);
            mFailButton.setEnabled(false);
            mFailButton.setBackgroundColor(Color.GRAY);
            mRadioFemale112.setVisibility(View.GONE);
            mRadioMale112.setVisibility(View.GONE);
            mRestButton = (Button) findViewById(R.id.retest_btn);
            mRestButton.setVisibility(View.GONE);
            icon.setVisibility(View.GONE);
            tips.setVisibility(View.GONE);

            mPlayLeft_bt = findViewById(R.id.play_left);
            mPlayLeft_bt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    flag_bt_play_left = true;
                    mPlayLeft_bt.setEnabled(false);
                    mPlayRight_bt.setEnabled(false);
                    mStopRight_bt.setEnabled(false);
                    mHandler1.sendEmptyMessage(PLAY_LEFT);
                }
            });
            mStopLeft_bt = findViewById(R.id.stop_left);
            mStopLeft_bt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(flag_bt_play_left)
                        flag_bt_stop_left = true;
                    mPlayLeft_bt.setEnabled(true);
                    mPlayRight_bt.setEnabled(true);
                    mStopRight_bt.setEnabled(true);
                    mHandler1.sendEmptyMessage(STOP_LEFT);
                }
            });
            mPlayRight_bt = findViewById(R.id.play_right);
            mPlayRight_bt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    flag_bt_play_right = true;
                    mPlayRight_bt.setEnabled(false);
                    mPlayLeft_bt.setEnabled(false);
                    mStopLeft_bt.setEnabled(false);
                    mHandler1.sendEmptyMessage(PLAY_RIGHT);
                }
            });
            mStopRight_bt = findViewById(R.id.stop_right);
            mStopRight_bt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(flag_bt_play_right)
                        flag_bt_stop_right = true;
                    mHandler1.sendEmptyMessage(STOP_RIGHT);
                    mPlayLeft_bt.setEnabled(true);
                    mPlayRight_bt.setEnabled(true);
                    mStopLeft_bt.setEnabled(true);
                }
            });

        }else {
            Random rng1 = new Random();
            mPlayerId = rng1.nextInt(2);
            Log.d(TAG, " onCreate  mPlayerId  = " + mPlayerId);

            mPassButton.setEnabled(false);
            mPassButton.setBackgroundColor(Color.GRAY);
            mRadioFemale112.setEnabled(false);
            mRadioMale112.setEnabled(false);

            if (mPlayerId == 0) {
                mPlayer = MediaPlayer.create(this, R.raw.female112);
            } else if (mPlayerId == 1) {
                mPlayer = MediaPlayer.create(this, R.raw.male112);
            }

            mPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mediaplayer) {
                    Log.d(TAG, "playMusic  0 onCompletion ");
                    mRadioFemale112.setEnabled(true);
                    mRadioMale112.setEnabled(true);

                }
            });
            mRestButton = (Button) findViewById(R.id.retest_btn);
            mRestButton.setOnClickListener(new android.view.View.OnClickListener() {
                public void onClick(View view) {
                    if (mPlayer != null) {
                        mPlayer.stop();
                        mPlayer.release();
                    }
                    mHandler.sendEmptyMessageDelayed(0, 4000);
                    if (mPlayerId == 0) {
                        mPlayer = MediaPlayer.create(SpeakerTestActivity.this, R.raw.female112);
                    } else if (mPlayerId == 1) {
                        mPlayer = MediaPlayer.create(SpeakerTestActivity.this, R.raw.male112);
                    }

                    mPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {

                        public void onCompletion(MediaPlayer mediaplayer) {
                            Log.d(TAG, "playMusic 1 onCompletion ");
                            mRadioFemale112.setEnabled(true);
                            mRadioMale112.setEnabled(true);
                        }
                    });

                    mPassButton.setEnabled(false);
                    mPassButton.setBackgroundColor(Color.GRAY);
                    mRadioFemale112.setEnabled(false);
                    mRadioMale112.setEnabled(false);
                    mRadioGroup1.clearCheck();

                    try {
                        if (mPlayer != null) {
                            mPlayer.start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            mHandler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    switch (msg.what) {
                        case 0:
                            mRadioFemale112.setEnabled(true);
                            mRadioMale112.setEnabled(true);
                            Log.d(TAG, "Handler() ");
                    }
                }
            };
        }
    }

    @Override
    protected void onDestroy() {
        if(mPlayerLeft != null){
            //if(mPlayerLeft.isPlaying())
            //    mPlayerLeft.stop();
            mPlayerLeft.release();
        }
        if(mPlayerRight != null){
            //if(mPlayerRight.isPlaying())
            //    mPlayerRight.stop();
            mPlayerRight.release();
        }
        super.onDestroy();
    }

    protected void onPause()
    {
        Log.d(TAG,"onPause() ");
        //WakeLock.releaseCpuLock();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(mPlayer != null)
        {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        restoreMusicVolume();
        super.onPause();
    }
    
    protected void onResume()
    {
        Log.d(TAG,"onResume() ");
        //WakeLock.acquireCpuWakeLock(this);

        setMaxMusicVolume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        try {
            if(mPlayer != null) {
                mPlayer.start();
                mHandler.sendEmptyMessageDelayed(0, 4000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onResume();
    }
        
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        boolean handled = false;
        Log.d(TAG,"onKeyDown() keyCode = " + keyCode );
        if(keyCode == KeyEvent.KEYCODE_BACK 
           ||keyCode == KeyEvent.KEYCODE_VOLUME_UP 
           ||keyCode == KeyEvent.KEYCODE_VOLUME_DOWN )
        {
            return true;
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void setMaxMusicVolume() {
        Log.d(TAG, "  setMaxMusicVolume");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        audioManager.setMode(AudioManager.MODE_NORMAL);

        mCurrentMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "  setMaxMusicVolume 0  mCurrentMusicVolume = " + mCurrentMusicVolume + " ;maxVolume = "+maxVolume);
        //maxVolume = maxVolume *80/100;
        //Log.d(TAG, "  setMaxMusicVolume 1  maxVolume = "+maxVolume);
        
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
    }
    
    private void restoreMusicVolume() {
    	Log.d(TAG, "  restoreMusicVolume");
    	AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if(mCurrentMusicVolume > 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentMusicVolume, AudioManager.FLAG_PLAY_SOUND);
        }
    }
}

