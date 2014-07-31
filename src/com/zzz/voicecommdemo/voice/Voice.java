package com.zzz.voicecommdemo.voice;

/**
 * Voice
 * 
 * @author zzz
 *
 */
public class Voice {
	public int duration = 1;
	public int sampleRate = 44100;
	public byte[] generatedVoice;
	public double sample[];

	public Voice() {
		sample = new double[getNumSample()];
		generatedVoice = new byte[2 * getNumSample()];
	}

	public int getNumSample() {
		return duration * sampleRate;
	}
}
