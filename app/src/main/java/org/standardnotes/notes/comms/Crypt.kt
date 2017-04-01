package org.standardnotes.notes.comms

import android.support.v4.util.ArrayMap
import android.util.Base64
import android.util.Log
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.CryptCompat.*
import org.standardnotes.notes.comms.data.EncryptableItem
import org.standardnotes.notes.comms.data.EncryptedItem
import java.util.*

/**
 * Created by carl on 30/03/17.
 */
object Crypt {

    val defaultPasswordGenerationParams: Map<String, Any>
    get() {
        val v = ArrayMap<String, Any>(4)
        v["pw_func"] = "pbkdf2"
        v["pw_alg"] = "sha512"
        v["pw_key_size"] = 512
        v["pw_cost"] = 5000
        return v
    }

//
//        func defaultPasswordGenerationParams() -> [String : Any] {
//            return [
//            "pw_func" : "pbkdf2",
//                    "pw_alg": "sha512",
//                    "pw_key_size": 512,
//                    "pw_cost": 5000
//        ]
//        }
//


    fun generateKeysFromMasterKey(masterKey: String): Keys {
        val encryptionKey = HMAC256(masterKey.toByteArray(Charsets.UTF_8), "e".toByteArray(Charsets.UTF_8))
        val authKey = HMAC256(masterKey.toByteArray(Charsets.UTF_8), "a".toByteArray(Charsets.UTF_8))
        return Keys(masterKey, encryptionKey.hexEncodedString(), authKey.hexEncodedString())
    }

//        func generateKeysFromMasterKey(masterKey: String) -> Keys {
//            let encryptionKey = HMAC256(masterKey.data(using: .utf8), "e".data(using: .utf8))
//            let authKey = HMAC256(masterKey.data(using: .utf8), "a".data(using: .utf8))
//            return Keys.init(mk: masterKey, encryptionKey: encryptionKey!.hexEncodedString(), authKey: authKey!.hexEncodedString())
//        }
//

//    fun pbkdf2(hash: CCPBKDFAlgorithm, password: String, salt: String, keyByteCount: Int, rounds: Int): String? {
//        val saltData = salt.toByteArray(Charsets.UTF_8)
//        val passwordData = password.toByteArray(Charsets.UTF_8)
//        var derivedKeyData = ByteArray(keyByteCount)
//
//        var derivationStatus = derivedKeyData.
//    }

//        func pbkdf2(hash :CCPBKDFAlgorithm, password: String, salt: String, keyByteCount: Int, rounds: Int) -> String? {
//            let saltData = salt.data(using: .utf8)!
//                    let passwordData = password.data(using:String.Encoding.utf8)!
//                    var derivedKeyData = Data(repeating:0, count:keyByteCount)
//
//            let derivationStatus = derivedKeyData.withUnsafeMutableBytes {derivedKeyBytes in
//                saltData.withUnsafeBytes { saltBytes in
//
//                    CCKeyDerivationPBKDF(
//                            CCPBKDFAlgorithm(kCCPBKDF2),
//                            password, passwordData.count,
//                            saltBytes, saltData.count,
//                            hash,
//                            UInt32(rounds),
//                            derivedKeyBytes, derivedKeyData.count)
//                }
//            }
//            if (derivationStatus != 0) {
//                print("Error: \(derivationStatus)")
//                return nil;
//            }
//
//            return derivedKeyData.hexEncodedString()
//        }
//

    fun sha1(message: String): String {
        val result = SHA1(message.toByteArray(Charsets.UTF_8))
        return result.hexEncodedString()
    }

//        func sha1(message: String) -> String {
//            let result = SHA1(message.data(using: .utf8))
//            return result!.hexEncodedString()
//        }
//

    fun base64(message: String): String {
        return message.toByteArray(Charsets.UTF_8).base64EncodedString()
    }

//        func base64(message: String) -> String {
//            return message.data(using: .utf8)!.base64EncodedString()
//        }

    fun base64decode(base64String: String): String? {
        try {
            return String(base64String.base64Decode())
        } catch (e: Exception) {
            return null
        }
    }

//        func base64decode(base64String: String) -> String? {
//            guard let data = Data(base64Encoded: base64String) else {
//                return nil
//            }
//            return String(data: data, encoding: .utf8)!
//        }
//

    fun decrypt(stringToAuth: String?, base64String: String, hexKey: String, hexIV: String?, authHash: String?, authKey: String?, authRequired: Boolean): String? {
        if (authRequired && authHash == null) {
            Log.d("Crypt", "Auth hash is required.")
            return null
        }

        if (authHash != null) {
            var localAuthHash = authHashString(stringToAuth!!, authKey!!)
            if (localAuthHash != authHash) {
                Log.d("Crypt", "Auth hash does not match.")
                return null
            }
        }

        val base64Data = base64String.base64Decode()
        val resultData = AES128CBCDecrypt(base64Data, hexKey.toHexadecimalData(), hexIV?.toHexadecimalData())
        val resultString = String(resultData, Charsets.UTF_8)
        return resultString
    }

//        func decrypt(stringToAuth: String?, message base64String: String, hexKey: String, iv hexIV: String?, authHash: String?, authKey: String?, authRequired: Bool) -> String? {
//            if(authRequired && authHash == nil) {
//                print("Auth hash is required.")
//                return nil
//            }
//
//            if(authHash != nil) {
//                let localAuthHash = authHashString(encryptedContent: stringToAuth!, authKey: authKey!)
//                if localAuthHash != authHash {
//                    print("Auth hash does not match.")
//                    return nil
//                }
//            }
//
//            let base64Data = Data(base64Encoded: base64String)
//            let resultData = AES128CBC("decrypt", base64Data, hexKey.toHexadecimalData(), hexIV?.toHexadecimalData())
//            let resultString = String(data: resultData!, encoding: .utf8)
//            return resultString
//        }
//

    fun decryptFromComponents(components: EncryptionComponents, keys: Keys): String? {
        return decrypt(components.stringToAuth, components.ciphertext, keys.encryptionKey, components.iv, components.authHash, keys.authKey, true)
    }

//        func decryptFromComponents(components: EncryptionComponents, keys: Keys) -> String? {
//            return decrypt(stringToAuth: components.stringToAuth, message: components.ciphertext, hexKey: keys.encryptionKey, iv: components.iv, authHash: components.authHash, authKey: keys.authKey, authRequired: true)
//        }
//

    fun encrypt(plainTextMessage: String, hexKey: String, hexIV: String?): String {
        val resultData = AES128CBCEncrypt(plainTextMessage.toByteArray(Charsets.UTF_8), hexKey.toHexadecimalData(), hexIV?.toHexadecimalData())
        val base64String = resultData.base64EncodedString()
        return base64String
    }

//        func encrypt(message plainTextMessage: String, key hexKey: String, iv hexIV: String?) -> String {
//            let resultData = AES128CBC("encrypt", plainTextMessage.data(using: .utf8), hexKey.toHexadecimalData(), hexIV?.toHexadecimalData())
//            let base64String = resultData!.base64EncodedString()
//            return base64String
//        }

    fun encryptionParams(item: EncryptableItem, version: String): Map<String, Any>? {
        val params = ArrayMap<String, Any>()
        val itemKey = generateRandomHexKey(512)
        if (version == "001") {
            // legacy
            item.encItemKey = encrypt(itemKey, SApplication.instance.valueStore.masterKey!!, null)
        } else {
            val iv = generateRandomHexKey(128)
            var keys = generateKeysFromMasterKey(SApplication.instance.valueStore.masterKey!!)
            val cipherText = encrypt(itemKey, keys.encryptionKey, iv)
            val stringToAuth = "$version:$iv:$cipherText"
            val authHash = authHashString(stringToAuth, keys.authKey)
            item.encItemKey = "$version:$authHash:$iv:$cipherText"
        }

        params["enc_item_key"] = item.encItemKey

        val ek = itemKey.firstHalf()
        val ak = itemKey.secondHalf()
        val message = SApplication.instance.gson.toJson(item)
        if (version == "001") {
            // legacy
            params["content"] = version + encrypt(message, ek, null)
            params["auth_hash"] = authHashString(params["content"] as String, ak)
        } else {
            val iv = generateRandomHexKey(128)
            val cipherText = encrypt(itemKey, ek, iv)
            val stringToAuth = "$version:$iv:$cipherText"
            val authHash = authHashString(stringToAuth, ak)
            params["content"] = "$version:$authHash:$iv:$cipherText"
            params["auth_hash"] = null
        }
        return params
    }

//        func encryptionParams(forItem item: Item, version: String) -> [String : Any]? {
//            var params = [String : Any]()
//            let itemKey = generateRandomHexKey(size: 512)
//            if(version == "001") {
//                // legacy
//                item.encItemKey = encrypt(message: itemKey, key: UserManager.sharedInstance.mk, iv: nil)
//            } else {
//                let iv = generateRandomHexKey(size: 128)
//                let cipherText = encrypt(message: itemKey, key: UserManager.sharedInstance.keys.encryptionKey, iv: iv)
//                let stringToAuth = [version, iv, cipherText].joined(separator: ":")
//                let authHash = authHashString(encryptedContent: stringToAuth, authKey: UserManager.sharedInstance.keys.authKey)
//                item.encItemKey = [version, authHash, iv, cipherText].joined(separator: ":")
//            }
//
//            params["enc_item_key"] = item.encItemKey!
//
//                    let ek = itemKey.firstHalf()
//            let ak = itemKey.secondHalf()
//            let message = item.createContentJSONFromProperties().rawString()!
//            if(version == "001") {
//                // legacy
//                params["content"] = version + encrypt(message: message, key: ek, iv: nil)
//                params["auth_hash"] = authHashString(encryptedContent: params["content"] as! String, authKey: ak)
//            } else {
//                let iv = generateRandomHexKey(size: 128)
//                let cipherText = encrypt(message: itemKey, key: ek, iv: iv)
//                let stringToAuth = [version, iv, cipherText].joined(separator: ":")
//                let authHash = authHashString(encryptedContent: stringToAuth, authKey: ak)
//                params["content"] = [version, authHash, iv, cipherText].joined(separator: ":")
//                params["auth_hash"] = NSNull()
//            }
//            return params
//        }

    fun authHashString(base64string: String, hexKey: String): String {
        val messageData = base64string.toByteArray(Charsets.UTF_8)
        val authKeyData = hexKey.toHexadecimalData()
        val result = HMAC256(messageData, authKeyData)
        return result.hexEncodedString()
    }
//
//        func authHashString(encryptedContent base64string: String, authKey hexKey: String) -> String {
//            let messageData = base64string.data(using: .utf8)
//            let authKeyData = hexKey.toHexadecimalData()
//            let result = HMAC256(messageData, authKeyData)
//            return result!.hexEncodedString()
//        }
//

    fun generateRandomHexKey(size: Int): String {
        return generateKey(size)
    }

//        func generateRandomHexKey(size: Int) -> String {
//            var data = Data(count: size/8)
//            let _ = data.withUnsafeMutableBytes { mutableBytes in
//                SecRandomCopyBytes(kSecRandomDefault, data.count, mutableBytes)
//            }
//            return data.hexEncodedString()
//        }
//
//        func generateAndSetNewEncryptionKey(forItem item: Item) {
//            // key required to be 512 bits
//            let hex = generateRandomHexKey(size: 512)
//            // encrypt key with master key
//            item.encItemKey = encrypt(message: hex, key: UserManager.sharedInstance.mk, iv: nil)
//        }
//

//        struct EncryptionComponents {
//            var version: String
//            var authHash: String
//            var iv: String?
//                    var ciphertext: String
//            var stringToAuth: String
//        }
//
//        func encryptionComponents(fromString string: String) -> EncryptionComponents {
//            let comps = string.components(separatedBy: ":")
//            let version = comps[0], authHash = comps[1], iv = comps[2], ciphertext = comps[3]
//            let stringToAuth = [version, iv, ciphertext].joined(separator: ":")
//            return EncryptionComponents.init(version: version, authHash: authHash, iv: iv, ciphertext: ciphertext, stringToAuth: stringToAuth)
//        }

    fun itemKeys(key: String): Keys? {
        var decryptedKey: String?
        if (!key.startsWith("002")) {
            // legacy
            decryptedKey = decrypt(null, key, SApplication.instance.valueStore.masterKey!!, null, null, null, false)
        } else {
            val components = EncryptionComponents.from002(key)
            val keys = generateKeysFromMasterKey(SApplication.instance.valueStore.masterKey!!)
            decryptedKey = decryptFromComponents(components, keys)
        }

        if (decryptedKey == null)
            return null

        return Keys(null, decryptedKey.firstHalf(), decryptedKey.secondHalf())
    }


//        func itemKeys(fromEncryptedKey key: String) -> Keys? {
//            var decryptedKey: String?
//            if(key.hasPrefix("002") == false) {
//                // legacy
//                decryptedKey = decrypt(stringToAuth: nil, message: key, hexKey: UserManager.sharedInstance.mk, iv: nil,
//                        authHash: nil, authKey: nil, authRequired: false)
//            } else {
//                let components = encryptionComponents(fromString: key)
//                let keys = UserManager.sharedInstance.keys
//                decryptedKey = decryptFromComponents(components: components, keys: keys)
//            }
//
//            if(decryptedKey == nil) {
//                return nil
//            }
//
//            let ek = decryptedKey!.firstHalf()
//            let ak = decryptedKey!.secondHalf()
//            return Keys.init(mk: nil, encryptionKey: ek, authKey: ak)
//        }




//
//    extension String {
//
//        func firstHalf() -> String {
//            return self.substring(to: self.index(self.startIndex, offsetBy: self.characters.count/2))
//        }
//
//        func secondHalf() -> String {
//            return self.substring(from: self.index(self.startIndex, offsetBy: self.characters.count/2))
//        }
//
//        /// Create `Data` from hexadecimal string representation
//        ///
//        /// This takes a hexadecimal representation and creates a `Data` object. Note, if the string has any spaces or non-hex characters (e.g. starts with '<' and with a '>'), those are ignored and only hex characters are processed.
//        ///
//        /// - returns: Data represented by this hexadecimal string.
//
//        func toHexadecimalData() -> Data? {
//                var data = Data(capacity: characters.count / 2)
//
//        let regex = try! NSRegularExpression(pattern: "[0-9a-f]{1,2}", options: .caseInsensitive)
//        regex.enumerateMatches(in: self, options: [], range: NSMakeRange(0, characters.count)) { match, flags, stop in
//            let byteString = (self as NSString).substring(with: match!.range)
//            var num = UInt8(byteString, radix: 16)!
//                    data.append(&num, count: 1)
//        }
//
//        guard data.count > 0 else {
//            return nil
//        }
//
//        return data
//    }
//    }
//
//    extension String {
//        func index(from: Int) -> Index {
//            return self.index(startIndex, offsetBy: from)
//        }
//
//        func substring(from: Int) -> String {
//            let fromIndex = index(from: from)
//            return substring(from: fromIndex)
//        }
//
//        func substring(to: Int) -> String {
//            let toIndex = index(from: to)
//            return substring(to: toIndex)
//        }
//
//        func substring(with r: Range<Int>) -> String {
//            let startIndex = index(from: r.lowerBound)
//            let endIndex = index(from: r.upperBound)
//            return substring(with: startIndex..<endIndex)
//        }
//    }
//
//
//    extension Data {
//        func hexEncodedString() -> String {
//            return map { String(format: "%02hhx", $0) }.joined()
//        }
//    }


    internal class ContentDecryptor<T : EncryptableItem>(private val type: Class<T>) {

        fun decrypt(item: EncryptedItem): T? {
            try {

                if (item.deleted) {
                    return null
                }

                val encryptionVersion = item.content.substring(0, 3)

                if (encryptionVersion == "001" || encryptionVersion == "002") {

                    val keys = itemKeys(item.encItemKey)
                    if (keys == null) {
                        Log.d("Crypt", "Error decrypting item, continuing.")
                        return null
                    }

                    val contentToDecrypt = item.content.substring(3)
                    var encryptionComps: EncryptionComponents
                    if (encryptionVersion == "001") {
                        encryptionComps = EncryptionComponents(encryptionVersion, item.authHash,
                                null, contentToDecrypt, item.content)
                    } else {
                        encryptionComps = EncryptionComponents.from002(item.content)
                    }

                    val decryptedContent = decryptFromComponents(encryptionComps, keys)
                    if (decryptedContent != null) {
                        val decryptedItem = SApplication.instance.gson.fromJson(decryptedContent, type)
                        copyInEncryptableItemFields(item, decryptedItem)
                        return decryptedItem
                    }
                    return null
                } else {
                    val contentToDecode = item.content.substring(3)
                    val decryptedItem = SApplication.instance.gson.fromJson(base64decode(contentToDecode), type)
                    copyInEncryptableItemFields(item, decryptedItem)
                    return decryptedItem
                }
            } catch (e: Exception) {
                return null
            }
        }
    }

}


//        func decryptItems(items: inout [JSON]){
//            for index in 0..<items.count {
//                var item = items[index]
//
//                if item["deleted"].boolValue == true {
//                    continue
//                }
//
//                let encryptionVersion = item["content"].string?.substring(to: 3)
//
//
//                if (encryptionVersion == "001" || encryptionVersion == "002"), let enc_key = item["enc_item_key"].string {
//
//                    let keys = itemKeys(fromEncryptedKey: enc_key)
//                    if(keys == nil) {
//                        print("Error decrypting item, continuing.")
//                        continue
//                    }
//
//                    let content = item["content"].string!
//                            let contentToDecrypt = content.substring(from: 3)
//
//                    var encryptionComps: EncryptionComponents
//                    if(encryptionVersion == "001") {
//                        encryptionComps = EncryptionComponents.init(version: encryptionVersion!, authHash: item["auth_hash"].string!,
//                                iv: nil, ciphertext: contentToDecrypt, stringToAuth: content)
//                    } else {
//                        encryptionComps = encryptionComponents(fromString: content)
//                    }
//
//                    let decryptedContent = decryptFromComponents(components: encryptionComps, keys: keys!)
//                    if(decryptedContent != nil) {
//                        items[index]["content"] = JSON(decryptedContent!)
//                    }
//                } else {
//                    if let contentToDecode = item["content"].string?.substring(from: 3) {
//                        if let decoded = Crypto.sharedInstance.base64decode(base64String: contentToDecode) {
//                            items[index]["content"] = JSON(decoded)
//                        }
//                    }
//                }
//            }
//        }
//    }

data class Keys (
    	    val mk: String?,
    	    val encryptionKey: String,
    	    val authKey: String
)

fun ByteArray.hexEncodedString(): String {
    return bytesToHex(this)
}

fun String.toHexadecimalData(): ByteArray {
    return fromHexString(this)
}

fun ByteArray.base64EncodedString(): String {
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

fun String.base64Decode(): ByteArray {
    return Base64.decode(this, Base64.NO_WRAP)
}

fun String.firstHalf(): String {
    return substring(0, length / 2)
}

fun String.secondHalf(): String {
    return substring(length / 2)
}



data class EncryptionComponents(
        val version: String,
        val authHash: String,
        val iv: String?,
        val ciphertext: String,
        val stringToAuth: String
) {
    companion object {
        fun from002(string: String): EncryptionComponents
        {
            val comps = string.split(":")
            val version = comps[0]
            val authHash = comps[1]
            val iv = comps[2]
            val cipherText = comps[3]
            val stringToAuth = "$version:$iv:$cipherText"
            return EncryptionComponents(version, authHash, iv, cipherText, stringToAuth)
        }

    }
}
