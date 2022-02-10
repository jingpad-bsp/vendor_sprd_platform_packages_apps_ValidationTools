package com.sprd.validationtools.itemstest.audio;

import android.app.Activity;
import android.app.Notification;
import android.graphics.Color;
import android.app.NotificationManager;
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
import android.content.res.AssetFileDescriptor;
import java.io.IOException;
import java.util.Random;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.os.Handler;
import android.os.Message;
import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

public class EarpieceTestActivity extends BaseActivity
{
    private static final String TAG = "EarpieceTest";
    private Button mRestButton = null;
    private int mCurrentMusicVolume = 0;
    private int mCurrentVoiceCallVolume = 0;
    private MediaPlayer mPlayer = null;
    private int mPlayerId = 0;
    public RadioGroup mRadioGroup1;
    public RadioButton mRadioFemale112, mRadioMale112;
    private Handler mHandler;

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

        mPlayer = new MediaPlayer();

        mRadioGroup1 = (RadioGroup) findViewById(R.id.gendergroup);
        mRadioFemale112 = (RadioButton) findViewById(R.id.female112);
        mRadioMale112 = (RadioButton) findViewById(R.id.male112);
        mRadioGroup1.setOnCheckedChangeListener(radiogpchange);

        ImageView icon = (ImageView)findViewById(R.id.iv_icon);
        TextView tips = (TextView)findViewById(R.id.tv_tips);
        tips.setText(getString(R.string.earpiece_tips));
        icon.setImageDrawable(getResources().getDrawable(R.drawable.nearear));
        icon.setVisibility(View.GONE);

        Random rng1 = new Random();
        mPlayerId = rng1.nextInt(2);
        Log.d(TAG, " onCreate  mPlayerId  = " + mPlayerId );

        mPassButton.setEnabled(false);
        mPassButton.setBackgroundColor(Color.GRAY);
        mRadioFemale112.setEnabled(false);
        mRadioMale112.setEnabled(false);

        try {
            AssetFileDescriptor afd = null;

            if(mPlayerId == 0)
            {
                afd = this.getResources().openRawResourceFd(R.raw.female112);
            }
            else if(mPlayerId == 1)
            {
                afd = this.getResources().openRawResourceFd(R.raw.male112);
            }

            mPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
                
                public void onCompletion(MediaPlayer mediaplayer)
                {
                    Log.d(TAG,"playMusic  0 onCompletion ");
                    mRadioFemale112.setEnabled(true);
                    mRadioMale112.setEnabled(true);
                    
                }
            });
            
            if (afd != null)
            {
                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                //mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                mPlayer.prepare();
            }
            else
            {
                Log.d(TAG, "create afd == null ");
            }
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "create failed:", ex);
        } catch (SecurityException ex) {
            Log.d(TAG, "create failed:", ex);
        }
           
        //mPlayer = MediaPlayer.create(this, R.raw.medieval_jaunt);

        mRestButton = (Button)findViewById(R.id.retest_btn);
        mRestButton.setOnClickListener(new android.view.View.OnClickListener() 
        {
            public void onClick(View view)
            {
                if(mPlayer != null)
                {
                    mPlayer.stop();
                    mPlayer.release();
                }
                mPlayer = new MediaPlayer();
                mHandler.sendEmptyMessageDelayed(0, 4000);

                try {
                    AssetFileDescriptor afd = null;

                    if(mPlayerId == 0)
                    {
                        afd = EarpieceTestActivity.this.getResources().openRawResourceFd(R.raw.female112);
                    }
                    else if(mPlayerId == 1)
                    {
                        afd = EarpieceTestActivity.this.getResources().openRawResourceFd(R.raw.male112);
                    }

                    mPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
                        
                        public void onCompletion(MediaPlayer mediaplayer)
                        {
                            Log.d(TAG,"playMusic 1 onCompletion ");
                            mRadioFemale112.setEnabled(true);
                            mRadioMale112.setEnabled(true);
                            
                        }
                    });
                    
                    mPassButton.setEnabled(false);
                    mPassButton.setBackgroundColor(Color.GRAY);
                    mRadioFemale112.setEnabled(false);
                    mRadioMale112.setEnabled(false);
                    mRadioGroup1.clearCheck();
                    
                    if (afd != null)
                    {
                        mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();
                        //mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                        mPlayer.prepare();
                    }
                    else
                    {
                        Log.d(TAG, "create afd == null ");
                    }
                } catch (IOException ex) {
                    Log.d(TAG, "create failed:", ex);
                } catch (IllegalArgumentException ex) {
                    Log.d(TAG, "create failed:", ex);
                } catch (SecurityException ex) {
                    Log.d(TAG, "create failed:", ex);
                }
                
                try {
                    if(mPlayer != null) {
                        mPlayer.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch(msg.what){
                    case 0:
                        mRadioFemale112.setEnabled(true);
                        mRadioMale112.setEnabled(true);
                        Log.d(TAG,"Handler() ");
                }
            }
        };
            
    }

    protected void onPause()
    {
        Log.d(TAG,"onPause() ");
        //WakeLock.releaseCpuLock();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //audioManager.setMode(AudioManager.MODE_NORMAL);
        
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
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //audioManager.setMode(AudioManager.MODE_IN_CALL);

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
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMode(AudioManager.MODE_IN_CALL);

        /* backup the volume for restore use */
        mCurrentMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mCurrentVoiceCallVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "  setMaxMusicVolume 0  mCurrentMusicVolume = " + mCurrentMusicVolume + " ;maxVolume = "+maxVolume);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,maxVolume,AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,maxVolume,AudioManager.FLAG_PLAY_SOUND);
    }
    
    private void restoreMusicVolume() {
    	Log.d(TAG, "  restoreMusicVolume");
    	AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        if(mCurrentMusicVolume > 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mCurrentMusicVolume,AudioManager.FLAG_PLAY_SOUND);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,mCurrentVoiceCallVolume,AudioManager.FLAG_PLAY_SOUND);
        }
        audioManager.setMode(AudioManager.MODE_NORMAL);
    }
}

