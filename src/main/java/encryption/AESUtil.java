package encryption;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {

    private static final int tag_length_bits = 128;
    private static final int iv_length_bytes = 12;

    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128, SecureRandom.getInstanceStrong());
        SecretKey AESkey = keyGenerator.generateKey();
        return AESkey;
    }


    /**
     * Encrypts the plain text using AES-GCM with a random IV. The putput is a Base64 string containing
     * the IV + cipher text
     *
     * @param text      The text that is going to be encrypted
     * @throws Exception
     */
    public static String encrypt(String text, SecretKey AESkey) throws Exception{
        byte[] iv = new byte[iv_length_bytes];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(tag_length_bits,iv);
        cipher.init(Cipher.ENCRYPT_MODE,AESkey,spec);

        byte[] encryptedText = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        //Use the IV for the encryption
        ByteBuffer buffer = ByteBuffer.allocate(iv.length + encryptedText.length);
        buffer.put(iv);
        buffer.put(encryptedText);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    public static String decrypt(String encryptedText, SecretKey AESkey) throws Exception{
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        ByteBuffer buffer = ByteBuffer.wrap(decoded);

        byte[] iv = new byte[iv_length_bytes];
        buffer.get(iv);

        byte[] restEncrypted = new byte[buffer.remaining()];
        buffer.get(restEncrypted);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(tag_length_bits, iv);
        cipher.init(Cipher.DECRYPT_MODE, AESkey,spec);

        byte[] decrypted = cipher.doFinal(restEncrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
