package com.zzz.voicecommdemo.voice;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

/**
 * Demodulator
 * 
 * @author zzz
 *
 */
public class Demodulator {
    private static final String TAG = "Demodulator";

    private BlockingQueue<Frame> frameQueue;
    private Thread thread;
    private OnCharReceivedListener listener;

    public Demodulator(OnCharReceivedListener listener) {
        frameQueue = new ArrayBlockingQueue<Frame>(Constants.IN_QUEUE_LENGTH);
        thread = new Thread(processFramesRunnable);
        this.listener = listener;
    }

    public void demodulate() {
        thread.start();
    }

    public void addFrame(short[] data) {
        Frame frame = new Frame(data);
        try {
            frameQueue.put(frame);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        thread.interrupt();
    }

    private Runnable processFramesRunnable = new Runnable() {
        @Override
        public void run() {

            try {
                int previousPoint = Integer.MAX_VALUE;
                int currentPoint = 0;

                // loop for every char
                while (true) {
                    // wait for a new char
                    while (true) {
                        currentPoint = frameQueue.take().getPointAtFreqency(
                                Constants.FREQ);
                        // Log.i(TAG, "currentPoint - " + currentPoint);
                        if (previousPoint < Constants.BUTTOM_THRESHOLD
                                && currentPoint > Constants.BUTTOM_THRESHOLD) {
                            // Log.i(TAG, "get a new char");
                            break;
                        }
                        previousPoint = currentPoint;
                    }

                    // start analyze the new char
                    // the queue restores 3 chunks for 1 char
                    Queue<Integer> pointQueue = new LinkedList<Integer>();
                    pointQueue.add(currentPoint);
                    StringBuilder code = new StringBuilder();
                    // loop for every bit
                    while (true) {
                        if (pointQueue.size() == Constants.FRAME_LENGTH) {
                            int bit = calQueueAvg(pointQueue) > Constants.BUTTOM_THRESHOLD ? 1
                                    : 0;
                            // Log.i(TAG, "bit - " + bit);
                            code.append(bit);
                            pointQueue.clear();
                            // judge the ending bits
                            if (code.length() > 1
                                    && code.substring(code.length() - 2,
                                            code.length()).equals(
                                            Constants.SIGIL)) {
                                // decode a char
                                // Log.i(TAG, "code - " + code.toString());
                                String data = Codec.decode(code.substring(0,
                                        code.length() - 2));
                                Log.i(TAG, "data - " + data);
                                listener.onCharReceived(data);

                                // prepare for recognizing next char
                                previousPoint = 0;
                                break; // break to listen next char
                            }
                        }
                        currentPoint = frameQueue.take().getPointAtFreqency(
                                Constants.FREQ);
                        pointQueue.add(currentPoint);
                        // Log.i(TAG, "currentPoint - " + currentPoint);
                    }
                }
            } catch (InterruptedException e) {
                // stopped
                e.printStackTrace();
            }
        }
    };

    private int calQueueAvg(Queue<Integer> q) {
        int sum = 0;
        for (int item : q) {
            sum += item;
            // Log.i(TAG, "item - " + item);
        }
        return sum / q.size();
    }

    /**
     * Complex
     * 
     * reference http://blog.csdn.net/ownwell/article/details/8179189
     *
     */
    public static class Complex {
        private final double re; // the real part
        private final double im; // the imaginary part

        public Complex(double real, double imag) {
            re = real;
            im = imag;
        }

        public String toString() {
            if (im == 0)
                return re + "";
            if (re == 0)
                return im + "i";
            if (im < 0)
                return re + " - " + (-im) + "i";
            return re + " + " + im + "i";
        }

        public double abs() {
            return Math.hypot(re, im);
        }

        public double phase() {
            return Math.atan2(im, re);
        }

        public Complex plus(Complex b) {
            Complex a = this;
            double real = a.re + b.re;
            double imag = a.im + b.im;
            return new Complex(real, imag);
        }

        public Complex minus(Complex b) {
            Complex a = this;
            double real = a.re - b.re;
            double imag = a.im - b.im;
            return new Complex(real, imag);
        }

        public Complex times(Complex b) {
            Complex a = this;
            double real = a.re * b.re - a.im * b.im;
            double imag = a.re * b.im + a.im * b.re;
            return new Complex(real, imag);
        }

        public Complex times(double alpha) {
            return new Complex(alpha * re, alpha * im);
        }
    }

    /**
     * Frame
     * 
     * @author zzz
     *
     */
    private static class Frame {
        public short[] data;

        public Frame(short[] data) {
            this.data = data.clone();

        }

        public int getPointAtFreqency(int freq) {
            // get top
            int peakIndex = (int) Math.round((double) freq / Constants.RATE
                    * Constants.CHUNK);

            // Log.d(TAG, "before fft - " + System.currentTimeMillis());
            int[] fftResult = fft(data);
            // Log.d(TAG, " - after fft - " + System.currentTimeMillis());

            return fftResult[peakIndex];
        }

        private int[] fft(short[] input) {
            int[] ret = new int[input.length];
            Complex[] dataComplex = new Complex[input.length];
            for (int i = 0; i < input.length; i++) {
                dataComplex[i] = new Complex(input[i], 0);
            }

            Complex[] fftResultComplex = fft(dataComplex);

            for (int i = 0; i < fftResultComplex.length; i++) {
                ret[i] = (int) Math.round(fftResultComplex[i].abs());
                // Log.v(TAG, "ret[" + i + "] - " + ret[i]);
            }

            return ret;
        }

        // reference:
        // http://www.cnblogs.com/yangzhenyu/archive/2012/03/22/java.html
        private Complex[] fft(Complex[] x) {
            int N = x.length;

            // base case
            if (N == 1)
                return new Complex[] { x[0] };

            // radix 2 Cooley-Tukey FFT
            if (N % 2 != 0) {
                throw new RuntimeException("N is not a power of 2");
            }

            // fft of even terms
            Complex[] even = new Complex[N / 2];
            for (int k = 0; k < N / 2; k++) {
                even[k] = x[2 * k];
            }
            Complex[] q = fft(even);

            // fft of odd terms
            Complex[] odd = even; // reuse the array
            for (int k = 0; k < N / 2; k++) {
                odd[k] = x[2 * k + 1];
            }
            Complex[] r = fft(odd);

            // combine
            Complex[] y = new Complex[N];
            for (int k = 0; k < N / 2; k++) {
                double kth = -2 * k * Math.PI / N;
                Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
                y[k] = q[k].plus(wk.times(r[k]));
                y[k + N / 2] = q[k].minus(wk.times(r[k]));
            }
            return y;
        }
    }

    /**
     * demodulate callback
     * 
     * @author zzz
     *
     */
    public static interface OnCharReceivedListener {
        public void onCharReceived(String data);
    }
}
