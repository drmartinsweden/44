package pl.ss.capstone.atmprotocol.common.utils;

import org.bouncycastle.util.encoders.Base64;
import pl.ss.capstone.atmprotocol.common.Default;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Created by nulon on 12.10.15.
 */
public class CryptoTool {

    public static String encrypt(String string, String encryptedKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, decodeKeyBase64(encryptedKey),new IvParameterSpec(new byte[16]));
        byte[] stringBytes = string.getBytes("UTF-8");
        byte[] encryptedBytes = cipher.doFinal(stringBytes);
        return new String(Base64.encode(encryptedBytes));
    }

    public static String decrypt(String string, String encryptedKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, decodeKeyBase64(encryptedKey),new IvParameterSpec(new byte[16]));
        byte[] decryptedBytes = Base64.decode(string.getBytes());
        byte[] encryptedBytes = cipher.doFinal(decryptedBytes);
        return new String(encryptedBytes,"UTF-8");
    }

    public static String generateKey() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        Key key = generator.generateKey();
        return encodeKeyBase64(key);
    }

    public static String encodeKeyBase64(Key key) {
        return new String(Base64.encode(key.getEncoded()));
    }

    public static Key decodeKeyBase64(String encodedKey) {
        byte[] keyBytes = Base64.decode(encodedKey);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] createSig(String data, String authfile) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException, InvalidKeyException, SignatureException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(authfile), Default.STORE_PASS.toCharArray());
        PrivateKey key = (PrivateKey) ks.getKey(Default.KEY_ALIAS, Default.STORE_PASS.toCharArray());
        return createSig(data,key);
    }

    public static byte[] createSig(String data, PrivateKey key) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        Signature instance = Signature.getInstance("SHA1withRSA");
        instance.initSign(key);
        instance.update(data.getBytes());
        return instance.sign();
    }
}
