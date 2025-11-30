package encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * This {@code RSAKeyManager} class manages the creation of the RSA pair keys for encryption and decryption
 * of passwords in the admin interface.
 * The algorithm used for encryption and decryption is RSA. The class defines:
 * <ul>
 *     <li> A filename that will contain the public key</li>
 *     <li> A filename that will contain the private key</li>
 * </ul>
 *
 *
 * @author pblan
 */
public class RSAKeyManager {
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        generator.initialize(2048, secureRandom);
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }
    public static void saveKey (KeyPair pair, String filename){
        //Create a file object with reference to a file path
        File publicFile = new File(filename + "_public_key");
        File privateFile = new File (filename + "_private_key");

        if (publicFile.exists() || privateFile.exists()){
            System.out.println("The key files already exist. No overwriting.");
            return;
        }

        try (FileOutputStream publicOut = new FileOutputStream(publicFile);
             FileOutputStream privateOut = new FileOutputStream(privateFile) ){ //To automatically close resources at the end of the try block
            publicOut.write(pair.getPublic().getEncoded()); //writes the public key in bytes
            privateOut.write(pair.getPrivate().getEncoded()); //writes the private key in bytes

            System.out.println("Public and private keys saved successfully.");

        } catch (Exception e) {
            //throw new RuntimeException("Error saving keys: "+e.getMessage(),e);
            throw new KeyErrorException("Error saving keys");

        }

    }

    public static PrivateKey retrievePrivateKey (String filename) throws KeyErrorException{
        File privateKeyFile = new File(filename + "_private_key");
        if (!privateKeyFile.exists()){
            //throw new RuntimeException("Private key file not found: " +privateKeyFile.getAbsolutePath());
            throw new KeyErrorException("Private key file not found");
        }

        try(FileInputStream privateIn = new FileInputStream(privateKeyFile)){
            byte[] privateKeyBytes = privateIn.readAllBytes();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);

        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            //throw new RuntimeException("Error retrieving private key: "+e.getMessage(), e);
            throw new KeyErrorException("Error retrieving private key");

        }
    }

    public static PublicKey retrievePublicKey (String filename) throws KeyErrorException{
        File publicKeyFile = new File(filename + "_public_key");
        if (!publicKeyFile.exists()){
            //throw new RuntimeException("Public key file not found: " +publicKeyFile.getAbsolutePath());
            throw new KeyErrorException("Public key file not found");
        }

        try(FileInputStream publicIn = new FileInputStream(publicKeyFile)){
            byte[] publicKeyBytes = publicIn.readAllBytes();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            return keyFactory.generatePublic(publicKeySpec);

        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            //throw new RuntimeException("Error retrieving private key: "+e.getMessage(), e);
            throw new KeyErrorException("Error retrieving private key");
        }
    }
}
