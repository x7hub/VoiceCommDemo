package com.zzz.voicecommdemo.voice;

/**
 * Constants
 * 
 * @author zzz
 *
 */
public class Constants {

    public final static int FREQ = 19100;
    public final static int FREQ_OFF = 0;
    public final static int RATE = 44100;
    public final static int CHANNELS = 1;
    public final static int FRAME_LENGTH = 3;
    public final static int CHUNK = 256;
    public final static int DATASIZE = CHUNK * FRAME_LENGTH;
    public final static String SIGIL = "00";
    public final static int IN_QUEUE_LENGTH = 4000;
    public final static int BUTTOM_THRESHOLD = 500;
    public final static String ERROR_CHAR = ".";
}