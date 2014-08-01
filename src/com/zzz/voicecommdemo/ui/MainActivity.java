package com.zzz.voicecommdemo.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zzz.voicecommdemo.R;
import com.zzz.voicecommdemo.voice.VoiceSignal;
import com.zzz.voicecommdemo.voice.VoiceCommService;

/**
 * MainActivity
 * 
 * @author zzz
 *
 */
public class MainActivity extends Activity implements OnClickListener {
    public static final String TAG = "MainActivity";

    private TextView textviewHello;
    private EditText edittextCode;
    private Button buttonPlay;
    private Button buttonRecord;

    public static final int MSG_STATE_GENERATEREADY = 1;
    private Messenger service;

    private final Messenger messenger = new Messenger(new Handler(
            new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    Log.v(TAG, "IncomingHandler.handleMessage");
                    switch (msg.what) {
                    case MSG_STATE_GENERATEREADY:
                        break;
                    }
                    return false;
                }
            }));

    private boolean bound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textviewHello = (TextView) findViewById(R.id.textview_hello);
        edittextCode = (EditText) findViewById(R.id.edittext_code);
        buttonPlay = (Button) findViewById(R.id.button_play);
        buttonRecord = (Button) findViewById(R.id.button_record);
        buttonPlay.setOnClickListener(this);
        buttonRecord.setOnClickListener(this);

        Intent intent = new Intent(this.getApplicationContext(),
                VoiceCommService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        if (bound) {
            unbindService(connection);
            bound = false;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_play:
            Log.v(TAG, "button_play clicked");
            if (bound) {
                Message msg = Message.obtain(null, VoiceCommService.MSG_SEND,
                        0, 0);
                msg.obj = edittextCode.getText().toString();
                try {
                    service.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            break;
        case R.id.button_record:
            Log.v(TAG, "button_record clicked");
            if (bound) {
                Message msg = Message.obtain(null, VoiceCommService.MSG_RECV,
                        0, 0);
                try {
                    service.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            break;
        default:
            Log.v(TAG, "unknown clicked");
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "onServiceConnected");
            MainActivity.this.service = new Messenger(service);
            bound = true;
            Message msg = Message.obtain(null, VoiceCommService.MSG_CONNECTED,
                    0, 0);
            msg.replyTo = messenger;
            try {
                MainActivity.this.service.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MainActivity.this.service = null;
            bound = false;
            Log.v(TAG, "onServiceDisconnected");
        }
    };

}