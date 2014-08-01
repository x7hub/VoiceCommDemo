package com.zzz.voicecommdemo.voice;

import android.util.Log;

/**
 * VoiceSignal
 * 
 * @author zzz
 *
 */
public class VoiceSignal {
    private static final String TAG = "VoiceSignal";

    public int sampleRate; // voice params
    // private int sliceDuration;
    private int sliceNum;
    private int sliceLength; // points in one slice
    private double sample[]; // intermediate variable
    public byte[] generatedVoice; // result to play

    private VoiceSignal(int sliceNum) {
        this.sampleRate = Constants.DEFAULT_SAMPLE_RATE;
        this.sliceLength = Constants.DEFAULT_SLICE_LENGTH;
        this.sliceNum = sliceNum;
        this.sample = new double[getTotalPointsCount()];
        this.generatedVoice = new byte[2 * getTotalPointsCount()];
    }

    // items in total
    private int getTotalPointsCount() {
        return sliceLength * sliceNum;
    }

    // total duration
    private int getDuration() {
        return getTotalPointsCount() / sampleRate;
    }

    // create voice from message
    public static VoiceSignal createFrom(String data) {

        // get freqs from input code
        double[] freqs = new double[data.length()];
        for (int i = 0; i < data.length(); i++) {
            try {
                int index = data.charAt(i) - '0';
                freqs[i] = Constants.CODE_BOOK[index];
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        return VoiceSignal.createFrom(freqs);
    }

    // create voice from freqs
    public static VoiceSignal createFrom(double[] freqs) {
        VoiceSignal voice = new VoiceSignal(freqs.length);

        // create sample array into voice
        for (int sliceIndex = 0; sliceIndex < voice.sliceNum; sliceIndex++) {
            double freq = freqs[sliceIndex];
            Log.v(TAG, "freq - " + freq);
            try {
                for (int i = 0; i < voice.sliceLength; i++) {
                    int p = i + voice.sliceLength * sliceIndex;
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
