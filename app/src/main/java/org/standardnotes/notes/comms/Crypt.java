package org.standardnotes.notes.comms;

import android.util.Base64;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;
import org.standardnotes.notes.BuildConfig;
import org.standardnotes.notes.SApplication;
import org.standardnotes.notes.comms.data.AuthParamsResponse;
import org.standardnotes.notes.comms.data.ContentType;
import org.standardnotes.notes.comms.data.EncryptableItem;
import org.standardnotes.notes.comms.data.EncryptedItem;
import org.standardnotes.notes.comms.data.Note;
import org.standardnotes.notes.comms.data.NoteContent;
import org.standardnotes.notes.comms.data.Reference;
import org.standardnotes.notes.comms.data.SigninResponse;
import org.standardnotes.notes.comms.data.Tag;
import org.standardnotes.notes.comms.data.TagContent;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import kotlin.text.Charsets;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Crypt {

    public static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    public static final String AES = "AES";
    public static final String HMAC_SHA_256 = "HmacSHA256";
    private static ContentDecryptor<Note> noteDecryptor = new ContentDecryptor<>(Note.class);
    private static ContentDecryptor<Tag> tagDecryptor = new ContentDecryptor<>(Tag.class);

    public static final String ENCRYPTION_VERSION = "001";

    @NotNull
    public static boolean isParamsSupported(AuthParamsResponse params) {
        if (!"sha512".equals(params.getPwAlg())) {
            return false;
        }
        return true;
    }

    public static void doLogin(final String email, final String password, final AuthParamsResponse params, final Callback<SigninResponse> callback) {
        Thread loginThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] key = Crypt.generateKey(
                            password.getBytes(Charsets.UTF_8),
                            params.getPwSalt().getBytes(Charsets.UTF_8),
                            params.getPwCost(),
                            params.getPwKeySize());
                    String fullHashedPassword = Crypt.bytesToHex(key);
                    String serverHashedPassword = fullHashedPassword.substring(0, fullHashedPassword.length() / 2);
                    final String mk = fullHashedPassword.substring(fullHashedPassword.length() / 2);
                    SApplication.Companion.getInstance().getComms().getApi().signin(email, serverHashedPassword).enqueue(new Callback<SigninResponse>() {
                        @Override
                        public void onResponse(Call<SigninResponse> call, Response<SigninResponse> response) {
                            if (response.isSuccessful()) {
                                SApplication.Companion.getInstance().getValueStore().setTokenAndMasterKey(response.body().getToken(), mk);
                                SApplication.Companion.getInstance().getValueStore().setEmail(email);
                            }
                            callback.onResponse(call, response);
                        }

                        @Override
                        public void onFailure(Call<SigninResponse> call, Throwable t) {
                            callback.onFailure(call, t);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(null, e);
                }
            }
        });
        loginThread.start();
    }

    public static void doRegister(final String email, final String password, final Callback<SigninResponse> callback) {
        Thread loginThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AuthParamsResponse params = Crypt.getDefaultAuthParams(email);
                    byte[] key = Crypt.generateKey(
                            password.getBytes(Charsets.UTF_8),
                            params.getPwSalt().getBytes(Charsets.UTF_8),
                            params.getPwCost(),
                            params.getPwKeySize());
                    String fullHashedPassword = Crypt.bytesToHex(key);
                    String serverHashedPassword = fullHashedPassword.substring(0, fullHashedPassword.length() / 2);
                    final String mk = fullHashedPassword.substring(fullHashedPassword.length() / 2);
                    SApplication.Companion.getInstance().getComms().getApi().register(email, serverHashedPassword,
                            params.getPwSalt(), params.getPwNonce(), params.getPwFunc(), params.getPwAlg(), params.getPwCost(), params.getPwKeySize()).enqueue(new Callback<SigninResponse>() {
                        @Override
                        public void onResponse(Call<SigninResponse> call, Response<SigninResponse> response) {
                            if (response.isSuccessful()) {
                                SApplication.Companion.getInstance().getValueStore().setTokenAndMasterKey(response.body().getToken(), mk);
                                SApplication.Companion.getInstance().getValueStore().setEmail(email);
                            }
                            callback.onResponse(call, response);
                        }

                        @Override
                        public void onFailure(Call<SigninResponse> call, Throwable t) {
                            callback.onFailure(call, t);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(null, e);
                }
            }
        });
        loginThread.start();
    }

    private static class Keys {
        Keys(String ek, String ak) {
            this.ek = ek;
            this.ak = ak;
        }
        String ek;
        String ak;
    }

    final static IvParameterSpec emptyIvSpec = new IvParameterSpec(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

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
        return keyHex;
    }

    private static Keys generate002KeysFromMasterKey() throws InvalidKeyException, NoSuchAlgorithmException {
        String masterKey = SApplication.Companion.getInstance().getValueStore().getMasterKey();
        String encryptionKey = createHash(masterKey, Hex.toHexString("e".getBytes()));
        String authKey = createHash(masterKey, Hex.toHexString("a".getBytes()));
        return new Keys(encryptionKey, authKey);
    }

    public static String generateEncryptedKey(int size, String version) throws Exception {
        String itemKey = generateKey(size);
        if (version.equals("001")) {
            return encrypt(itemKey, SApplication.Companion.getInstance().getValueStore().getMasterKey(), null);
        } else if (version.equals("002")) {
            String ivHex = generateKey(128);
            Keys keys = generate002KeysFromMasterKey();
            String cipherText = encrypt(itemKey, keys.ek, ivHex);
            String stringToAuth = version + ":" + ivHex + ":" + cipherText;
            String authHash = createHash(stringToAuth, keys.ak);
            return version + ":" + authHash + ":" + ivHex + ":" + cipherText;
        }
        throw new RuntimeException("Encryption version " + version + " not supported");
    }

    public static Keys getItemKeys(EncryptedItem item, String version) throws Exception {
        if (version.equals("001")) {
            String itemKey = decrypt(item.getEncItemKey(), SApplication.Companion.getInstance().getValueStore().getMasterKey(), null);
            Keys val = new Keys(itemKey.substring(0, itemKey.length() / 2), itemKey.substring(itemKey.length() / 2));
            return val;
        } else if (version.equals("002")) {
            String[] keyBits = item.getEncItemKey().split(":");
            String authHash = keyBits[1];
            String ivHex = keyBits[2];
            String keyCipherText = keyBits[3];
            Keys keys = generate002KeysFromMasterKey();
            String stringToAuth = version + ":" + ivHex + ":" + keyCipherText;

            String localAuthHash = createHash(stringToAuth, keys.ak);
            if (!localAuthHash.equals(authHash)) {
                Log.d("Crypt", "Auth hash does not match.");
                return null;
            }
            String decryptedKey = decrypt(keyCipherText, keys.ek, ivHex);
            if (decryptedKey == null)
                return null;
            return new Keys(decryptedKey.substring(0, decryptedKey.length() / 2), decryptedKey.substring(decryptedKey.length() / 2));
        }
        throw new RuntimeException("Encryption version " + version + " not supported");
    }

    public static String decrypt(String base64Text, String hexKey, String hexIv) throws Exception {
        byte[] base64Data = Base64.decode(base64Text, Base64.NO_WRAP);
        Cipher ecipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        byte[] key = Hex.decode(hexKey);
        SecretKey sks = new SecretKeySpec(key, AES);
        ecipher.init(Cipher.DECRYPT_MODE, sks, hexIv == null ? emptyIvSpec : new IvParameterSpec(Hex.decode(hexIv)));
        byte[] resultData = ecipher.doFinal(base64Data);
        return new String(resultData, Charsets.UTF_8);
    }

    public static String encrypt(String text, String hexKey, String hexIv) throws Exception {
        Cipher ecipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        byte[] key = Hex.decode(hexKey);
        SecretKey sks = new SecretKeySpec(key, AES);
        ecipher.init(Cipher.ENCRYPT_MODE, sks, hexIv == null ? emptyIvSpec : new IvParameterSpec(Hex.decode(hexIv)));
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


    public static EncryptedItem encrypt(EncryptableItem thing, String version) {
        try {
            EncryptedItem item = new EncryptedItem();
            copyInEncryptableItemFields(thing, item);
            String contentJson = null;
            Keys keys = Crypt.getItemKeys(item, version);
            if (thing instanceof Note) {
                item.setContentType(ContentType.Note.toString());
                NoteContent justUnencContent = new NoteContent();
                Note note = (Note) thing;
                justUnencContent.setTitle(note.getTitle());
                justUnencContent.setText(note.getText());
                List<Tag> tags = SApplication.Companion.getInstance().getNoteStore().getTagsForNote(thing.getUuid());
                List<Reference> refs = new ArrayList<>(tags.size());
                for (EncryptableItem tag : tags) {
                    Reference ref = new Reference();
                    ref.setContentType(ContentType.Tag.toString());
                    ref.setUuid(tag.getUuid());
                    refs.add(ref);
                }
                justUnencContent.setReferences(refs);
                contentJson = SApplication.Companion.getInstance().getGson().toJson(justUnencContent);
            } else if (thing instanceof Tag) {
                item.setContentType(ContentType.Tag.toString());
                Tag tag = (Tag) thing;
                TagContent justUnencContent = new TagContent();
                justUnencContent.setTitle(tag.getTitle());
                List<Note> notes = SApplication.Companion.getInstance().getNoteStore().getNotesForTag(thing.getUuid());
                List<Reference> refs = new ArrayList<>(notes.size());
                for (EncryptableItem note : notes) {
                    Reference ref = new Reference();
                    ref.setContentType(ContentType.Note.toString());
                    ref.setUuid(note.getUuid());
                    refs.add(ref);
                }
                justUnencContent.setReferences(refs);
                contentJson = SApplication.Companion.getInstance().getGson().toJson(justUnencContent);
            }
            if (BuildConfig.DEBUG) {
                Log.d("Crypt", "Encrypting " + item.getContentType() + " " + item.getUuid() + ": " + contentJson);
            }
            item.setContent(createContentEncrypted(contentJson, keys, version));
            item.setAuthHash(createItemAuthHash(item.getContent(), keys.ak, version));
            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String createContentEncrypted(String contentJson, Keys keys, String version) throws Exception {
        if (version.equals("001")) {
            return version + encrypt(contentJson, keys.ek, null);
        } else if (version.equals("002")) {
            String hexIv = generateKey(128);
            String cipherText = encrypt(contentJson, keys.ek, hexIv);
            String stringToAuth = version + ":" + hexIv + ":" + cipherText;
            String authHash = createHash(stringToAuth, keys.ak);
            return version + ":" + authHash + ":" + hexIv + ":" + cipherText;
        }
        throw new RuntimeException("Encryption version " + version + " not supported");
    }

    private static String createItemAuthHash(String contentEnc, String key, String version) throws Exception {
        if (version.equals("001")) {
            return createHash(contentEnc, key);
        } else if (version.equals("002")) {
            return null;
        }
        throw new RuntimeException("Encryption version " + version + " not supported");
    }

    private static List<Reference> generateReferences(Collection<EncryptableItem> items, ContentType type) {
        List<Reference> refs = new ArrayList<>(items.size());
        for (EncryptableItem item : items) {
            Reference ref = new Reference();
            ref.setContentType(type.toString());
            ref.setUuid(item.getUuid());
            refs.add(ref);
        }
        return refs;
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

    public static String hashSha1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes(Charsets.UTF_8);
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return bytesToHex(sha1hash);
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
                    String version = item.getContent().substring(0, 3);
                    String contentToDecrypt = item.getContent().substring(3);
                    if (version.equals("000")) {
                        contentJson = new String(Base64.decode(contentToDecrypt, Base64.NO_PADDING), Charsets.UTF_8);
                    } else if (version.equals("001") || version.equals("002")) {
                        Keys keys = Crypt.getItemKeys(item, version);

                        if (version.equals("002")) {
                            String[] contentBits = contentToDecrypt.split(":");
                            String authHash = contentBits[1];
                            String ivHex = contentBits[2];
                            String cipherText = contentBits[3];
                            String stringToAuth = version + ":" + ivHex + ":" + cipherText;

                            // authenticate
                            String hash = createHash(stringToAuth, keys.ak);
                            if (!hash.equals(authHash)) {
                                throw new Exception("could not authenticate item");
                            }

                            contentJson = Crypt.decrypt(cipherText, keys.ek, ivHex);
                        } else { // "001"
                            // authenticate
                            String hash = createHash(item.getContent(), keys.ak);
                            if (!hash.equals(item.getAuthHash())) {
                                throw new Exception("could not authenticate item");
                            }
                            contentJson = Crypt.decrypt(contentToDecrypt, keys.ek, null);
                        }
                    } else {
                        throw new RuntimeException("Encryption version " + version + " not supported");
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

    public static AuthParamsResponse getDefaultAuthParams(String email) throws Exception {
        AuthParamsResponse defaultAuthParams = new AuthParamsResponse();
        defaultAuthParams.setPwCost(60000);
        defaultAuthParams.setPwKeySize(512);
        defaultAuthParams.setPwFunc("pbkdf2");
        defaultAuthParams.setPwAlg("sha512");
        String nonce = generateKey(256);
        defaultAuthParams.setPwNonce(nonce);
        defaultAuthParams.setPwSalt(hashSha1(email + "SN" + nonce));
        return defaultAuthParams;
    }
}

