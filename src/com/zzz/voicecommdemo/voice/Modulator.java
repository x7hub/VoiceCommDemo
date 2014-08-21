package com.zzz.voicecommdemo.voice;

import android.util.Log;

/**
 * Modulator
 * 
 * @author zzz
 *
 */
public class Modulator {
    private static final String TAG = "VoiceSignal";

    private String data;
    public byte[] generatedVoice; // result to play

    public Modulator(String data) {
        this.data = data;
    }

    // create voice from message
    public byte[] perform() {
        Log.v(TAG, "data - " + data);
        String pattern = Codec.encode(data);
        Log.v(TAG, "pattern - " + pattern);

        this.generatedVoice = new byte[pattern.length() * Constants.DATASIZE
                * 2]; // two short for one sample

        char lastBit = pattern.charAt(pattern.length() - 1);
        char nextBit;
        int offset = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char bit = pattern.charAt(i);
            // Log.v(TAG, "bit - " + bit);

            if (i < pattern.length() - 1) {
                nextBit = pattern.charAt(i + 1);
            } else {
                nextBit = pattern.charAt(0);
            }

            int freq = bit == '1' ? Constants.FREQ : Constants.FREQ_OFF;

            // tone
            double[] tone = new double[Constants.DATASIZE];
            for (int j = 0; j < Constants.DATASIZE; j++) {
                tone[j] = Math.sin(2 * Math.PI * freq
                        * ((double) (j + offset) / Constants.RATE));
            }

            // envelope
            // TODO
            double half = (double) tone.length / 2;
            double f = Math.PI / (tone.length / 2);
            double[] output = new double[Constants.DATASIZE];
            for (int j = 0; j < tone.length; j++) {
                double samp = tone[j];
                if ((j < half && lastBit == '0')
                        || (j > half && nextBit == '0')) {
                    samp = samp * (1 + Math.sin(f * j - (Math.PI / 2))) / 2;
                }
                output[j] = samp;
            }

            // transform sample into generatedVoice
            int idx = offset * 2;
            for (final double dVal : output) {
                // scale to maximum amplitude
                final short val = (short) ((dVal * 32767)); // max positive
                                                            // sample
                                                            // for signed 16 bit
                                                            // integers is 32767
                // in 16 bit wave PCM, first byte is the low order byte (pcm:
                // pulse
                // control modulation)
                this.generatedVoice[idx++] = (byte) (val & 0x00ff);
                this.generatedVoice[idx++] = (byte) ((val & 0xff00) >>> 8);
            }

            offset += Constants.DATASIZE;
            lastBit = bit;
        }

        return this.generatedVoice;
    }

}
