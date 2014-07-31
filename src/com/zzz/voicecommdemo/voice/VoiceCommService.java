package com.zzz.voicecommdemo.voice;

import com.zzz.voicecommdemo.ui.MainActivity;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

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

	double freq1 = 1000;
	double freq2 = freq1;

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
						handler.post(new Runnable() {
							@Override
							public void run() {
								Message msg = Message.obtain(null,
										MainActivity.MSG_STATE_GENERATEREADY,
										0, 0);
								msg.obj = genVoice();
								try {
									VoiceCommService.this.client.send(msg);
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
		Voice voice = new Voice();

		double instfreq = 0, numerator;
		for (int i = 0; i < voice.getNumSample(); i++) {
			numerator = (double) (i) / (double) voice.getNumSample();
			instfreq = freq1 + (numerator * (freq2 - freq1));
			if ((i % 1000) == 0) {
				Log.v("Current Freq:", String.format(
						"Freq is:  %f at loop %d of %d", instfreq, i,
						voice.getNumSample()));
			}
			voice.sample[i] = Math.sin(2 * Math.PI * i
					/ (voice.sampleRate / instfreq));

		}
		int idx = 0;
		for (final double dVal : voice.sample) {
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767)); // max positive sample
														// for signed 16 bit
														// integers is 32767
			// in 16 bit wave PCM, first byte is the low order byte (pcm: pulse
			// control modulation)
			voice.generatedVoice[idx++] = (byte) (val & 0x00ff);
			voice.generatedVoice[idx++] = (byte) ((val & 0xff00) >>> 8);
		}
		return voice;
	}

}
