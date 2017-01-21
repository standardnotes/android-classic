package org.standardnotes.notes.comms;

import android.util.Base64;

import org.joda.time.DateTime;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;
import org.standardnotes.notes.SApplication;
import org.standardnotes.notes.comms.data.EncryptableItem;
import org.standardnotes.notes.comms.data.EncryptedItem;
import org.standardnotes.notes.comms.data.Note;
import org.standardnotes.notes.comms.data.Tag;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

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

    public static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    public static final String AES = "AES";
    public static final String HMAC_SHA_256 = "HmacSHA256";
    private static ContentDecryptor<Note> noteDecryptor = new ContentDecryptor<>(Note.class);
    private static ContentDecryptor<Tag> tagDecryptor = new ContentDecryptor<>(Tag.class);

    private static class Keys {
        String ek;
        String ak;
    }

    final static IvParameterSpec ivSpec = new IvParameterSpec(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

    public static byte[] generateKey(byte[] passphraseOrPin, byte[] salt, int iterations, int outputKeyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA512Digest());
        gen.init(passphraseOrPin, salt, iterations);
        byte[] dk = ((KeyParameter) gen.generateDerivedParameters(outputKeyLength)).getKey();
        return dk;
    }

    public static String generateKey(int size) throws Exception {
        byte[] key = new byte[size / 8];
        SecureRandom rand = new SecureRandom();
        rand.nextBytes(key);
        String keyHex = bytesToHex(key);
        return encrypt(keyHex, SApplication.Companion.getInstance().getValueStore().getMasterKey());
    }


    public static Keys getItemKeys(EncryptedItem item) throws Exception {
        String itemKey = decrypt(item.getEncItemKey(), SApplication.Companion.getInstance().getValueStore().getMasterKey());
        Keys val = new Keys();
        val.ek = itemKey.substring(0, itemKey.length() / 2);
        val.ak = itemKey.substring(itemKey.length() / 2);
        return val;
    }

    public static String decrypt(String base64Text, String hexKey) throws Exception {
        byte[] base64Data = Base64.decode(base64Text, Base64.NO_WRAP);
        Cipher ecipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        byte[] key = Hex.decode(hexKey);
        SecretKey sks = new SecretKeySpec(key, AES);
        ecipher.init(Cipher.DECRYPT_MODE, sks, ivSpec);
        byte[] resultData = ecipher.doFinal(base64Data);
        return new String(resultData, Charsets.UTF_8);
    }

    public static String encrypt(String text, String hexKey) throws Exception {
        Cipher ecipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        byte[] key = Hex.decode(hexKey);
        SecretKey sks = new SecretKeySpec(key, AES);
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

    public static EncryptedItem encrypt(Note note) {
        try {
            EncryptedItem item = new EncryptedItem();
            copyInEncryptableItemFields(note, item);
            item.setUpdatedAt(DateTime.now());
            item.setContentType("Note");
            Keys keys = Crypt.getItemKeys(item);
            Note justUnencContent = new Note();
            justUnencContent.setTitle(note.getTitle());
            justUnencContent.setText(note.getText());
            String contentJson = SApplication.Companion.getInstance().getGson().toJson(justUnencContent);
            String contentEnc = "001" + encrypt(contentJson, keys.ek);
            String hash = createHash(contentEnc, keys.ak);
            item.setAuthHash(hash);
            item.setContent(contentEnc);
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Note decryptNote(EncryptedItem item) {
        return noteDecryptor.decrypt(item);
    }

    public static Tag decryptTag(EncryptedItem item) {
        return tagDecryptor.decrypt(item);
    }



    private static String createHash(String text, String ak) throws NoSuchAlgorithmException, InvalidKeyException {
        // TODO make use spongycastle
        byte[] contentData = text.getBytes(Charsets.UTF_8);
        byte[] akHexData = Hex.decode(ak);
        Mac sha256_HMAC = Mac.getInstance(HMAC_SHA_256);
        SecretKey secret_key = new SecretKeySpec(akHexData, HMAC_SHA_256);
        sha256_HMAC.init(secret_key);
        return Crypt.bytesToHex(sha256_HMAC.doFinal(contentData));
    }


    static void copyInEncryptableItemFields(EncryptableItem source, EncryptableItem target) {
        target.setUuid(source.getUuid());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setEncItemKey(source.getEncItemKey());
        target.setPresentationName(source.getPresentationName());
        target.setDeleted(source.getDeleted());
    }

    static class ContentDecryptor<T extends EncryptableItem> {
        private Class<T> type;
        public ContentDecryptor(Class<T> type) {
            this.type = type;
        }

        public T decrypt(EncryptedItem item) {
            try {

                if (item.getContent() != null) {
                    String contentJson;
                    String contentWithoutType = item.getContent().substring(3);
                    if (item.getContent().startsWith("000")) {
                        contentJson = new String(Base64.decode(contentWithoutType, Base64.NO_PADDING), Charsets.UTF_8);
                    } else {
                        Keys keys = Crypt.getItemKeys(item);

                        // authenticate
                        String hash = createHash(item.getContent(), keys.ak);
                        if (!hash.equals(item.getAuthHash())) {
                            throw new Exception("could not authenticate item");
                        }

                        contentJson = Crypt.decrypt(contentWithoutType, keys.ek);
                    }

                    T thing = SApplication.Companion.getInstance().getGson().fromJson(contentJson, type);
                    copyInEncryptableItemFields(item, thing);
                    return thing;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}

