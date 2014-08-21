package com.zzz.voicecommdemo.voice;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class Mixer {
    public static final String TAG = "Mixer";
    private final int wavHeader = 44;
    private final int bufferSize = 4096;

    private Context context;
    private Modulator mod;
    public byte[] mixedVoice; // result to play

    public Mixer(Context context, Modulator mod) {
        this.context = context;
        this.mod = mod;
    }

    public byte[] perform() {
        AssetManager assetManager = context.getAssets();
        InputStream stream = null;
        try {
            stream = assetManager.open("music.wav");
            stream.skip(wavHeader); // skip wav file header
            mixedVoice = readToByteArray(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        short[] backgroud = byteArray2ShortArray(mixedVoice);
        short[] data = byteArray2ShortArray(mod.generatedVoice);

        try {
            for (int i = 0; i < backgroud.length; i++) {
                backgroud[i] = (short) (0.2 * backgroud[i] + 0.8 * data[i]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        mixedVoice = shortArray2ByteArray(backgroud);

        return mixedVoice;
    }

    private byte[] readToByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        BufferedInputStream bis = new BufferedInputStream(stream);
        byte[] buffer = new byte[bufferSize];
        while (bis.read(buffer) != -1) {
            os.write(buffer);
        }
        byte[] outputByteArray = os.toByteArray();
        bis.close();
        os.close();

        return outputByteArray;
    }

    public short[] byteArray2ShortArray(byte[] input) {
        short[] output = new short[input.length >> 1];
        for (int i = 0; i < output.length; i++) {
            output[i] = (short) ((input[i * 2] & 0xff) | (input[i * 2 + 1] & 0xff) << 8);
        }
        return output;
    }

    public byte[] shortArray2ByteArray(short[] input) {
        byte[] output = new byte[input.length << 1];
        for (int i = 0; i < input.length; i++) {
            output[2 * i] = (byte) (input[i] & 0xff);
            output[2 * i + 1] = (byte) ((input[i] & 0xff00) >> 8);
            // Log.v(TAG, "input - " + input[i]);
            // Log.v(TAG, "output - " + output[2 * i]);
            // Log.v(TAG, "output - " + output[2 * i + 1]);
        }
        return output;
    }
}
