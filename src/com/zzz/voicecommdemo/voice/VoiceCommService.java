package com.zzz.voicecommdemo.voice;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
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
    private final String CODE_DEFAULT = "0";

    private boolean flagStop = false;

    private Handler handler;

    public static final int MSG_CONNECTED = 1;
    public static final int MSG_SEND = 2;
    public static final int MSG_RECV = 3;
    private Messenger client;

    private final Messenger messenger = new Messenger(new Handler(
            new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    Log.v(TAG, "IncomingHandler.handleMessage");
                    switch (msg.what) {
                    case MSG_CONNECTED:
                        client = msg.replyTo;
                        break;
                    case MSG_SEND:
                        final String data = (String) msg.obj;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                playVoice(data.isEmpty() ? CODE_DEFAULT : data);
                            }
                        });
                        break;
                    case MSG_RECV:
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                readVoice();
                            }
                        });
                        break;
                    default:
                        Log.w(TAG, "unknown msg");
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

    // generate voice signal
    private void playVoice(String data) {
        VoiceSignal v = VoiceSignal.createFrom(data);
        AudioTrack audioTrack = null;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, v.sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, v.generatedVoice.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(v.generatedVoice, 0, v.generatedVoice.length);
        audioTrack.play();
    }

    // read voice from AudioRecord
    private VoiceRecv readVoice() {
        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, Constants.DEFAULT_SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, Constants.DEFAULT_RECORD_BUFFER);
        audioRecord.startRecording();

        VoiceRecv voice = new VoiceRecv();

        while (audioRecord.read(voice.bufferRead, 0,
                Constants.DEFAULT_RECORD_POINT) > 0 && !flagStop) {
            voice.decode();
        }

        audioRecord.stop();
        return voice;
    }
}
