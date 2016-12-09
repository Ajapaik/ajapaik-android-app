package ee.ajapaik.android.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {
    private static final String UTF8 = "UTF-8";

    public static String encode(String input) {
        return encode(input.getBytes(Charset.forName(UTF8)));
    }

    public static String encode(byte[] bytes) {
        StringBuilder output = new StringBuilder();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            bytes = new byte[0];
        }

        for(int i = 0, c = bytes.length; i < c; i++) {
            output.append(Strings.toBase16(bytes[i]));
        }

        return output.toString();
    }
}
