package com.zzz.voicecommdemo.voice;

import android.util.Log;

/**
 * Recognizer
 * 
 * @author zzz
 *
 */
public class Recognizer {
    public static final String TAG = "Recognizer";

    private StringBuilder receivedChars;
    private OnEndingReceivedListener listener;

    public Recognizer(OnEndingReceivedListener listener) {
        this.receivedChars = new StringBuilder();
        this.listener = listener;
    }

    public Recognizer add(String data) {
        // add to StringBuilder
        if (receivedChars.length() > 0) {
            // start with B
            receivedChars.append(data);
        }

        // judge beginning B
        if (data.equals("B")) {
            this.clear();
            receivedChars.append(data);
        }

        // judge ending E
        if (data.equals("E")) {
            Log.v(TAG, "receivedChars - " + receivedChars.toString());
            // check
            if (receivedChars.length() > 0
                    && receivedChars.toString().matches("B[0-9]+E")) {
                // get !
                Log.d(TAG, "matched");
                String uid = receivedChars.substring(1,
                        receivedChars.length() - 1).toString();
                listener.onEndingReceived(uid);
            }
        }
        return this;
    }

    public void clear() {
        receivedChars.delete(0, receivedChars.length());
    }

    /**
     * OnEndingReceivedListener
     * 
     * @author zzz
     *
     */
    public static interface OnEndingReceivedListener {
        void onEndingReceived(String s);
    }
}
