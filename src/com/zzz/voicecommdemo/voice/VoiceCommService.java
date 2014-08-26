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

    private boolean interruptRecording = false;

    private AudioTrack audioTrack;
    private AudioRecord audioRecord;
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
                        stopAll();
                        final String data = (String) msg.obj;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                playVoice(data.isEmpty() ? CODE_DEFAULT : data);
                            }
                        }).start();
                        break;

                    case MSG_RECV:
                        stopAll();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                readVoice();
                            }
                        }).start();
                        break;

                    case MSG_STOP:
                        stopAll();
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

    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.v(TAG, "onBind");
        return messenger.getBinder();
    }

    // generate voice signal
    private synchronized void playVoice(String data) {
        Modulator mod = new Modulator(data);
        mod.perform();

        Mixer mixer = new Mixer(this, mod);
        mixer.perform();

        byte[] buffer = mixer.mixedVoice;
        // byte[] buffer = mod.generatedVoice;

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffer.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(buffer, 0, buffer.length);
        audioTrack.setLoopPoints(0, buffer.length / 2, -1);
        audioTrack.play();
    }

    // read voice from AudioRecord
    private synchronized void readVoice() {
        interruptRecording = false;

        // prepare recognizer
        if (recognizer == null) {
            recognizer = new Recognizer(
                    new Recognizer.OnEndingReceivedListener() {
                        @Override
                        public void onEndingReceived(String s) {
                            Log.d(TAG, "onEndingReceived");
                            jumpToWeiboProfile(s);
                        }
                    });
        } else {
            recognizer.clear();
        }

        // prepare demodulator
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

        // prepare audio record
        int buffersize = AudioRecord.getMinBufferSize(Constants.RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                Constants.RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffersize);

        // start
        audioRecord.startRecording();

        while (audioRecord.read(frame, 0, Constants.CHUNK) > 0
                && !interruptRecording) {
            // Log.d(TAG, "audioRecord time " + System.currentTimeMillis());
            dem.addFrame(frame);
        }

        audioRecord.stop();
        audioRecord.release();
        dem.stop();
        Log.d(TAG, "Record stopped");
    }

    private void stopAll() {
        // stop AudioRecord
        interruptRecording = true;
        // stop AudioTrack
        if (audioTrack != null)
            Log.d(TAG,
                    "audioTrack.getPlayState() - " + audioTrack.getPlayState());
        if (audioTrack != null
                && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

    private void jumpToWeiboProfile(String uid) {
        // stop recording and playing
        stopAll();
        
        Log.i(TAG, "uid - " + uid);

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
