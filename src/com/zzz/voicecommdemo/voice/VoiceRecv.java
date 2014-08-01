package com.zzz.voicecommdemo.voice;

import android.util.Log;

/**
 * VoiceSignal
 * 
 * @author zzz
 *
 */
public class VoiceRecv {
    private static final String TAG = "VoiceRecv";

    public short[] bufferRead;

    public VoiceRecv() {
        bufferRead = new short[Constants.DEFAULT_RECORD_POINT];
    }

    public int decode() {
        int index = 0;

        // find beginning index
        for (index = 0; index < bufferRead.length; index++) {
            if (bufferRead[index] > Short.MAX_VALUE >> 5) {
                boolean isPreviousNagtive = false;
                int upEdgeCount = 0;
                for (int j = index; j < bufferRead.length; j++) {
                    if (isPreviousNagtive && bufferRead[j] > 0) {
                        isPreviousNagtive = false;
                        upEdgeCount++;
                    } else if (!isPreviousNagtive && bufferRead[j] < 0) {
                        isPreviousNagtive = true;
                    }
                }

                double ratio = Double.valueOf(upEdgeCount)
                        / (bufferRead.length - index);
                if (ratio > 0.1) {
                    // judge
                    Log.v(TAG, "ratio - " + ratio);
                    Log.v(TAG, ""
                            + (Constants.CODE_BOOK[0] + Constants.CODE_BOOK[1])
                            / 2 / Constants.DEFAULT_SAMPLE_RATE);
                    if (ratio > 0.4) {
                        Log.i(TAG, "judge as 1");
                    } else {
                        Log.d(TAG, "judge as 0");
                    }
                }

                break;
            }
        }

        // for (; index < bufferRead.length; index++) {
        // Log.v(TAG, "bufferRead[index] - " + bufferRead[index]);
        // }

        return -1;
    }
}
