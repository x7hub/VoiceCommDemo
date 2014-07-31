package com.zzz.voicecommdemo.voice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.zzz.voicecommdemo.ui.MainActivity;

/**
 * VoiceCommService
 * 
 * reference:
 * http://stackoverflow.com/questions/23154609/generate-chirp-signals-in-android
 * 
 * @author zzz
 *
 */
public class VoiceCommService extends Service {
    public static final String TAG = "VoiceCommService";

    private Handler handler;

    public static final int MSG_CONNECTED = 1;
    public static final int MSG_PREPARE = 2;
    private Messenger client;

    private String CODE_DEFAULT = "012345678923456789456789678989";
    private double[] codeBook = { 300, 400, 500, 600, 700, 800, 900, 1000,
            1100, 1200 };

    private final Messenger messenger = new Messenger(new Handler(
            new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    Log.v(TAG, "IncomingHandler.handleMessage");
                    switch (msg.what) {
                    case MSG_CONNECTED:
                        client = msg.replyTo;
                        break;
                    case MSG_PREPARE:

                        final String code = (String) msg.obj;

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Message msgClient = Message.obtain(null,
                                        MainActivity.MSG_STATE_GENERATEREADY,
                                        0, 0);
                                msgClient.obj = genVoice(code.isEmpty() ? CODE_DEFAULT
                                        : code);
                                try {
                                    VoiceCommService.this.client
                                            .send(msgClient);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    }
                    return false;
                }
            }));

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("ServiceStartArguments");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.v(TAG, "onBind");
        return messenger.getBinder();
    }

    private Voice genVoice() {
        return genVoice(CODE_DEFAULT);
    }

    private Voice genVoice(String code) {

        // get freqs from input code
        double[] freqs = new double[code.length()];
        for (int i = 0; i < code.length(); i++) {
            try {
                int index = code.charAt(i) - '0';
                freqs[i] = codeBook[index];
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        Voice voice = Voice.createFrom(freqs);

        return voice;
    }
}
