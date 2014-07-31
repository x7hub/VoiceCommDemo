package com.zzz.voicecommdemo.voice;

import android.util.Log;

/**
 * Voice
 * 
 * @author zzz
 *
 */
public class Voice {
    private static final String TAG = "Voice";

    public int sampleRate = 44100; // voice params
    private int sliceDuration = 100;
    private int sliceNum = 1;

    private double sample[]; // intermediate variable

    public byte[] generatedVoice; // result to play

    private Voice(int sliceNum) {
        this.sliceNum = sliceNum;
        this.sample = new double[getTotalPointsCount()];
        this.generatedVoice = new byte[2 * getTotalPointsCount()];
    }

    // items in one slice
    private int getSlicePointsCount() {
        return sliceDuration * sampleRate / 1000;
    }

    // items in total
    private int getTotalPointsCount() {
        return getDuration() * sampleRate / 1000;
    }

    // total duration
    private int getDuration() {
        return sliceDuration * sliceNum;
    }

    // create voice from freqs
    public static Voice createFrom(double[] freqs) {
        Voice voice = new Voice(freqs.length);

        // create sample array into voice
        for (int sliceIndex = 0; sliceIndex < voice.sliceNum; sliceIndex++) {
            double freq = freqs[sliceIndex];
            Log.v(TAG, "freq - " + freq);
            try {
                for (int i = 0; i < voice.getSlicePointsCount(); i++) {
                    int p = i + voice.getSlicePointsCount() * sliceIndex;
                    voice.sample[p] = Math.sin(2 * Math.PI * i
                            / (voice.sampleRate / freq));
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        }

        // transform sample into generatedVoice
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
