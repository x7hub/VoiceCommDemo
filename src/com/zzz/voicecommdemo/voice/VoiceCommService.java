package com.zzz.voicecommdemo.voice;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.zzz.voicecommdemo.R;
import com.zzz.voicecommdemo.ui.MainActivity;
import com.zzz.voicecommdemo.voice.Recognizer.OnEndingReceivedListener;

/**
 * VoiceCommService
 * 
 * reference:
 * http://stackoverflow.com/questions/23154609/generate-chirp-signals-in-android
 * https://github.com/Katee/quietnet
 * 
 * @author zzz
 *
 */
public class VoiceCommService extends Service {
    public static final String TAG = "VoiceCommService";
    private final String CODE_DEFAULT = ".";

    private boolean flagStop = false;

    private Recognizer recognizer; // parse received chars

    public static final int MSG_CONNECTED = 1;
    public static final int MSG_SEND = 2;
    public static final int MSG_RECV = 3;
    public static final int MSG_STOP = 4;
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
                        flagStop = true;
                        final String data = (String) msg.obj;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                playVoice(data.isEmpty() ? CODE_DEFAULT : data);
                            }
                        }).start();
                        break;

                    case MSG_RECV:
                        flagStop = false;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                readVoice();
                            }
                        }).start();
                        break;

                    case MSG_STOP:
                        flagStop = true;
                        break;

                    // case MSG_CLEAR:
                    // clearRecerivedChars();
                    // break;

                    // case MSG_JUMP:
                    // jumpToWeiboProfile();
                    // break;

                    default:
                        Log.w(TAG, "unknown msg");
                    }
                    return false;
                }

            }));

    @Override
    public void onCreate() {
        super.onCreate();
        recognizer = new Recognizer(new OnEndingReceivedListener() {
            @Override
            public void onEndingReceived(String s) {
                jumpToWeiboProfile(s);
            }
        });
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.v(TAG, "onBind");
        return messenger.getBinder();
    }

    // generate voice signal
    private void playVoice(String data) {
        Modulator mod = new Modulator(data);
        mod.perform();

        Mixer mixer = new Mixer(this, mod);
        mixer.perform();

        byte[] buffer = mixer.mixedVoice;
        // byte[] buffer = mod.generatedVoice;

        AudioTrack audioTrack = null;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffer.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(buffer, 0, buffer.length);
        audioTrack.play();
    }

    // read voice from AudioRecord
    private void readVoice() {
        recognizer.clear();

        int buffersize = AudioRecord.getMinBufferSize(Constants.RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, Constants.RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffersize);
        audioRecord.startRecording();

        Demodulator dem = new Demodulator(
                new Demodulator.OnCharReceivedListener() {
                    @Override
                    public void onCharReceived(String data) {
                        // send to activity
                        Message msg = Message.obtain(null,
                                MainActivity.MSG_CHAR_RECEIVED, 0, 0);
                        msg.obj = data;
                        try {
                            client.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                        // add to recognizer
                        recognizer.add(data);
                    }
                });
        dem.perform();
        short[] frame = new short[Constants.CHUNK];

        while (audioRecord.read(frame, 0, Constants.CHUNK) > 0 && !flagStop) {
            // Log.d(TAG, "audioRecord time " + System.currentTimeMillis());
            dem.addFrame(frame);
        }

        audioRecord.stop();
        audioRecord.release();
        dem.stop();
        Log.d(TAG, "Record stopped");
    }

    private void jumpToWeiboProfile(String uid) {
        // stop recording
        flagStop = true;

        // build uri
        StringBuilder stringUserInfo = new StringBuilder(
                getString(R.string.userinfo_scheme_prefix));
        Uri uriUserinfo = Uri.parse(stringUserInfo.append(uid).toString());

        // jump
        Intent intentShowUser = new Intent();
        intentShowUser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentShowUser.setData(uriUserinfo);
        startActivity(intentShowUser);

    }
}
