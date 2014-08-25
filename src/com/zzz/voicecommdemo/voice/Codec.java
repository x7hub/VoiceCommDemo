package com.zzz.voicecommdemo.voice;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Codec
 * 
 * @author zzz
 *
 */
public class Codec {
    // private static final String TAG = "Codec";

    private static final HashMap<String, String> codeMap = new HashMap<String, String>();
    static {
        codeMap.put(" ", "1");
        codeMap.put("!", "111111111");
        codeMap.put("\"", "101011111");
        codeMap.put("#", "111110101");
        codeMap.put("$", "111011011");
        codeMap.put("%", "1011010101");
        codeMap.put("&", "1010111011");
        codeMap.put("'", "101111111");
        codeMap.put("(", "11111011");
        codeMap.put(")", "11110111");
        codeMap.put("*", "101101111");
        codeMap.put("+", "111011111");
        codeMap.put(",", "1110101");
        codeMap.put("-", "110101");
        codeMap.put(".", "1010111");
        codeMap.put("/", "110101111");
        codeMap.put("0", "10110111");
        codeMap.put("1", "10111101");
        codeMap.put("2", "11101101");
        codeMap.put("3", "11111111");
        codeMap.put("4", "101110111");
        codeMap.put("5", "101011011");
        codeMap.put("6", "101101011");
        codeMap.put("7", "110101101");
        codeMap.put("8", "110101011");
        codeMap.put("9", "110110111");
        codeMap.put(":", "11110101");
        codeMap.put(";", "110111101");
        codeMap.put("<", "111101101");
        codeMap.put("=", "1010101");
        codeMap.put(">", "111010111");
        codeMap.put("?", "1010101111");
        codeMap.put("@", "1010111101");
        codeMap.put("A", "1111101");
        codeMap.put("B", "11101011");
        codeMap.put("C", "10101101");
        codeMap.put("D", "10110101");
        codeMap.put("E", "1110111");
        codeMap.put("F", "11011011");
        codeMap.put("G", "11111101");
        codeMap.put("H", "101010101");
        codeMap.put("I", "1111111");
        codeMap.put("J", "111111101");
        codeMap.put("K", "101111101");
        codeMap.put("L", "11010111");
        codeMap.put("M", "10111011");
        codeMap.put("N", "11011101");
        codeMap.put("O", "10101011");
        codeMap.put("P", "11010101");
        codeMap.put("Q", "111011101");
        codeMap.put("R", "10101111");
        codeMap.put("S", "1101111");
        codeMap.put("T", "1101101");
        codeMap.put("U", "101010111");
        codeMap.put("V", "110110101");
        codeMap.put("W", "101011101");
        codeMap.put("X", "101110101");
        codeMap.put("Y", "101111011");
        codeMap.put("Z", "1010101101");
        codeMap.put("[", "111110111");
        codeMap.put("\\", "111101111");
        codeMap.put("]", "111111011");
        codeMap.put("^", "1010111111");
        codeMap.put("_", "101101101");
        codeMap.put("`", "1011011111");
        codeMap.put("a", "1011");
        codeMap.put("b", "1011111");
        codeMap.put("c", "101111");
        codeMap.put("d", "101101");
        codeMap.put("e", "11");
        codeMap.put("f", "111101");
        codeMap.put("g", "1011011");
        codeMap.put("h", "101011");
        codeMap.put("i", "1101");
        codeMap.put("j", "111101011");
        codeMap.put("k", "10111111");
        codeMap.put("l", "11011");
        codeMap.put("m", "111011");
        codeMap.put("n", "1111");
        codeMap.put("o", "111");
        codeMap.put("p", "111111");
        codeMap.put("q", "110111111");
        codeMap.put("r", "10101");
        codeMap.put("s", "10111");
        codeMap.put("t", "101");
        codeMap.put("u", "110111");
        codeMap.put("v", "1111011");
        codeMap.put("w", "1101011");
        codeMap.put("x", "11011111");
        codeMap.put("y", "1011101");
        codeMap.put("z", "111010101");
        codeMap.put("{", "1010110111");
        codeMap.put("|", "110111011");
        codeMap.put("}", "1010110101");
        codeMap.put("~", "1011010111");
    }

    private static final HashMap<String, String> decodeMap = new HashMap<String, String>();

    static {
        for (Entry<String, String> e : codeMap.entrySet()) {
            decodeMap.put(e.getValue(), e.getKey());
        }
    }

    public static String encode(String input) {
        StringBuilder output = new StringBuilder(Constants.SIGIL);
        for (int i = 0; i < input.length(); i++) {
            String key = String.valueOf(input.charAt(i));
            output.append(codeMap.get(key)).append(Constants.SIGIL);
        }

        return output.toString();
    }

    public static String decode(String input) {
        if (decodeMap.containsKey(input)) {
            return decodeMap.get(input);
        } else {
            return Constants.ERROR_CHAR;
        }
    }
}
