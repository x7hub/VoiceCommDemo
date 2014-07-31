package com.zzz.voicegeneratedemo.ui;

import java.io.IOException;

import com.zzz.voicegeneratedemo.R;
import com.zzz.voicegeneratedemo.R.layout;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * MainActivity
 * 
 * from http://stackoverflow.com/questions/23154609/generate-chirp-signals-in-android
 *
 */
public class MainActivity extends Activity {

	int duration = 1;
	int sampleRate = 44100;
	int numSample = duration * sampleRate;
	double sample[] = new double[numSample];
	double freq1 = 1000;
	double freq2 = freq1;
	byte[] generatedSnd = new byte[2 * numSample];
	Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					genTone();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.post(new Runnable() {
					public void run() {
						playSound();
					}

				});

			}
		});

		thread.start();

	}

	protected void onResume() {
		super.onResume();

	}

	void genTone() throws IOException {

		double instfreq = 0, numerator;
		for (int i = 0; i < numSample; i++) {
			numerator = (double) (i) / (double) numSample;
			instfreq = freq1 + (numerator * (freq2 - freq1));
			if ((i % 1000) == 0) {
				Log.e("Current Freq:", String
						.format("Freq is:  %f at loop %d of %d", instfreq, i,
								numSample));
			}
			sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / instfreq));

		}
		int idx = 0;
		for (final double dVal : sample) {
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767)); // max positive sample
														// for signed 16 bit
														// integers is 32767
			// in 16 bit wave PCM, first byte is the low order byte (pcm: pulse
			// control modulation)
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
		}

	}

	void playSound() {
		AudioTrack audioTrack = null;
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
				AudioTrack.MODE_STATIC);
		audioTrack.write(generatedSnd, 0, generatedSnd.length);
		audioTrack.play();
	}
}