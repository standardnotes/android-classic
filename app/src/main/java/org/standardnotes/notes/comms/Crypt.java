package org.standardnotes.notes.comms;

import android.util.Base64;

import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;
import org.standardnotes.notes.SApplication;
import org.standardnotes.notes.comms.data.DirtyNote;
import org.standardnotes.notes.comms.data.EncryptedItem;
import org.standardnotes.notes.comms.data.Note;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import kotlin.text.Charsets;

/**
 * Created by carl on 14/01/17.
 */

public class Crypt {

    final static IvParameterSpec ivSpec = new IvParameterSpec(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

    public static byte[] generateKey(byte[] passphraseOrPin, byte[] salt, int iterations, int outputKeyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA512Digest());
        gen.init(passphraseOrPin, salt, iterations);
        byte[] dk = ((KeyParameter) gen.generateDerivedParameters(outputKeyLength)).getKey();
        return dk;
    }

    public static String generateKey(int size) throws Exception {
        byte[] key = new byte[size / 8];
        Random rand = new Random();
        rand.nextBytes(key);
        String keyHex = bytesToHex(key);
        return encrypt(keyHex, SApplication.Companion.getInstance().getValueStore().getMasterKey());
    }


    public static String[] getItemKeys(EncryptedItem item) throws Exception {
        String itemKey = decrypt(item.getEncItemKey(), SApplication.Companion.getInstance().getValueStore().getMasterKey());
        String[] val = new String[2];
        val[0] = itemKey.substring(0, itemKey.length() / 2);
        val[1] = itemKey.substring(itemKey.length() / 2);
        return val;
    }

    public static String decrypt(String base64Text, String hexKey) throws Exception {
        byte[] base64Data = Base64.decode(base64Text, Base64.NO_WRAP);
        Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] key = Hex.decode(hexKey);
        SecretKey sks = new SecretKeySpec(key, "AES");
        ecipher.init(Cipher.DECRYPT_MODE, sks, ivSpec);
        byte[] resultData = ecipher.doFinal(base64Data);
        return new String(resultData, Charsets.UTF_8);
    }

    public static String encrypt(String text, String hexKey) throws Exception {
        Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] key = Hex.decode(hexKey);
        SecretKey sks = new SecretKeySpec(key, "AES");
        ecipher.init(Cipher.ENCRYPT_MODE, sks, ivSpec);
        byte[] resultData = ecipher.doFinal(text.getBytes(Charsets.UTF_8));
        String base64Encr = Base64.encodeToString(resultData, Base64.NO_WRAP);
        return base64Encr;
    }



    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] fromHexString(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static EncryptedItem encrypt(DirtyNote note) {
        try {
            EncryptedItem item = note.getOriginal();
            String[] keys = Crypt.getItemKeys(item);
            String ek = keys[0];
            String ak = keys[1];

            Note justUnencContent = new Note();
            justUnencContent.setTitle(note.getTitle());
            justUnencContent.setText(note.getText());
            String contentJson = SApplication.Companion.getInstance().getGson().toJson(justUnencContent);
            String contentEnc = "001" + encrypt(contentJson, ek);
            String hash = createHash(contentEnc, ak);
            item.setAuthHash(hash);
            item.setContent(contentEnc);
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Note decrypt(EncryptedItem item) {
        try {

            if (item.getContent() != null) {
                String contentJson;
                String contentWithoutType = item.getContent().substring(3);
                if (item.getContent().startsWith("000")) {
                    contentJson = new String(Base64.decode(contentWithoutType, Base64.NO_PADDING), Charsets.UTF_8);
                } else {
                    String[] keys = Crypt.getItemKeys(item);
                    String ek = keys[0];
                    String ak = keys[1];

                    // authenticate
                    String hash = createHash(item.getContent(), ak);
                    if (!hash.equals(item.getAuthHash())) {
                        throw new Exception("could not authenticate item");
                    }
                    // TODO make above use spongycastle

                    contentJson = Crypt.decrypt(contentWithoutType, ek);
                }

                Note note = SApplication.Companion.getInstance().getGson().fromJson(contentJson, Note.class);
                note.setOriginal(item);
                return note;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String createHash(String text, String ak) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] contentData = text.getBytes(Charsets.UTF_8);
        byte[] akHexData = Hex.decode(ak);
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKey secret_key = new SecretKeySpec(akHexData, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Crypt.bytesToHex(sha256_HMAC.doFinal(contentData));
    }

//                try {
//                    byte[] kk = generateKey("password".getBytes(Charsets.UTF_8),
//                            "f462222159791bd70be5fac46d6d5073ec9c15a8".getBytes(Charsets.UTF_8),
//                            5000,
//                            512);
//                    byte[] ss = Arrays.copyOfRange(kk, 0, kk.length/2);
//                    String hh = bytesToHex(ss);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


}
