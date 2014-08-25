package com.zzz.voicecommdemo.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import com.zzz.voicecommdemo.voice.VoiceCommService;

/**
 * MainActivity
 * 
 * @author zzz
 *
 */
public class MainActivity extends Activity implements OnClickListener {
    public static final String TAG = "MainActivity";

    // private TextView textviewHello;
    private EditText edittextCode;
    private Button buttonPlay;
    private Button buttonRecord;
    private Button buttonStop;
    // private Button buttonClear;
    // private Button buttonJump;
    private TextView textviewReceived;

    public static final int MSG_STATE_GENERATEREADY = 1;
    public static final int MSG_CHAR_RECEIVED = 2;
    private Messenger service;

    private final Messenger messenger = new Messenger(new Handler(
            new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    Log.v(TAG, "IncomingHandler.handleMessage");
                    switch (msg.what) {
                    case MSG_CHAR_RECEIVED:
                        textviewReceived.append((String) msg.obj);
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

        // textviewHello = (TextView) findViewById(R.id.textview_hello);
        edittextCode = (EditText) findViewById(R.id.edittext_code);
        buttonPlay = (Button) findViewById(R.id.button_play);
        buttonRecord = (Button) findViewById(R.id.button_record);
        buttonStop = (Button) findViewById(R.id.button_stop);
        textviewReceived = (TextView) findViewById(R.id.textview_received);
        // buttonClear = (Button) findViewById(R.id.button_clear);
        // buttonJump = (Button) findViewById(R.id.button_jump);
        buttonPlay.setOnClickListener(this);
        buttonRecord.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        // buttonClear.setOnClickListener(this);
        // buttonJump.setOnClickListener(this);

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
        int messageWhat = -1;
        switch (v.getId()) {
        case R.id.button_play:
            Log.v(TAG, "button_play clicked");
            messageWhat = VoiceCommService.MSG_SEND;
            break;
        case R.id.button_record:
            Log.v(TAG, "button_record clicked");
            textviewReceived.setText("");
            messageWhat = VoiceCommService.MSG_RECV;
            break;
        case R.id.button_stop:
            Log.v(TAG, "button_stop clicked");
            messageWhat = VoiceCommService.MSG_STOP;
            break;
        // case R.id.button_clear:
        // Log.v(TAG, "button_clear clicked");
        // textviewReceived.setText("");
        // messageWhat = VoiceCommService.MSG_CLEAR;
        // break;
        // case R.id.button_jump:
        // Log.v(TAG, "button_jump clicked");
        // messageWhat = VoiceCommService.MSG_JUMP;
        // break;
        default:
            Log.v(TAG, "unknown clicked");
        }

        if (bound && messageWhat > 0) {
            Message msg = Message.obtain(null, messageWhat, 0, 0);
            msg.obj = edittextCode.getText().toString();
            try {
                service.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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