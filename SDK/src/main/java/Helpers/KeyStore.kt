package Helpers

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


class SecureDataManager(private val context: Context) {

    private val aliasPrefix = "optimuspass_"
    private lateinit var secretKey: SecretKey

    fun encryptCustomerData(
        customerNo: String, title: String, secretKeyToStore: String, creationDate: Int
    ) {
        val alias = aliasPrefix + customerNo

        // KeyStore'dan anahtarı al veya oluştur
        secretKey = generateAndStoreSecretKeyIfNotExists(alias)

        // Şifrelemek için Cipher oluştur
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedSecretKeyToStore = cipher.doFinal(secretKeyToStore.toByteArray())
        saveToSharedStorage(
            customerNo,
            context,
            encryptedSecretKeyToStore,
            title,
            creationDate,
            cipher.iv
        )
    }

    //  bu tek bir müşterinin secretkeyine ulaşmak için
    fun getCustomerSecretKey(customerNo: String): String? {

        val encryptedData = getAccount(customerNo)

        if (encryptedData != null) {
            val alias = aliasPrefix + customerNo
            val secretKey = generateAndStoreSecretKeyIfNotExists(alias)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val ivParameterSpec = IvParameterSpec(encryptedData.iv)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

            val decryptedData = cipher.doFinal(encryptedData.secretKey)
            val decryptedKey = String(decryptedData)

            println(decryptedKey)

            return decryptedKey
        }
        return null
    }


    private fun generateAndStoreSecretKeyIfNotExists(alias: String): SecretKey {
        // Anahtarın zaten var olup olmadığını kontrol et
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (keyStore.containsAlias(alias)) {
            // Anahtar zaten varsa, mevcut anahtarı döndür
            val keyEntry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
            return keyEntry.secretKey
        } else {
            // Anahtar yoksa yeni bir anahtar üret
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(
                alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(BLOCK_MODE).setEncryptionPaddings(PADDING)

            keyGenerator.init(builder.build())
            val secretKey: SecretKey = keyGenerator.generateKey()

            return secretKey
        }
    }

    private fun saveToSharedStorage(
        customerNo: String,
        context: Context,
        secretKeyToStore: ByteArray,
        title: String,
        creationDate: Int,
        iv: ByteArray
    ) {
        // Yeni hesabı oluştur
        val account = CustomerItem(customerNo, secretKeyToStore, title, creationDate, iv)

        // Eski hesap listesini al
        var accountList = getAccountlist()

        // Yeni hesabı listeye ekle
        if (accountList.none { it.customerNo == customerNo }) {
            val updatedAccountList = accountList.toMutableList().apply {
                add(account)
            }

            // Hesap listesini JSON formatına dönüştür
            val jsonAccountList = convertAccountListToJson(updatedAccountList)

            // JSON verisini SharedPreferences içinde sakla
            val sharedPreferences =
                context.getSharedPreferences("AccountPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("accountList", jsonAccountList)
            editor.apply()

        } else {

            println("++++" + customerNo.toString() + " nolu müşteri zaten kayıt yapılmıs ")
        }
    }

    private fun convertAccountListToJson(accountList: List<CustomerItem>): String {
        val jsonArray = JSONArray()
        for (account in accountList) {
            val jsonObject = JSONObject().apply {
                put("customerNo", account.customerNo)
                put("secretKey", Base64.encodeToString(account.secretKey, Base64.DEFAULT))
                put("title", account.title)
                put("creationDate", account.creationDate)
                put("iv", Base64.encodeToString(account.iv, Base64.DEFAULT))
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }


    fun getAccountlist(): List<CustomerItem> {

        // SharedPreferences'tan JSON verisini alın
        val sharedPreferences = context.getSharedPreferences("AccountPrefs", Context.MODE_PRIVATE)
        val jsonAccountList = sharedPreferences.getString("accountList", null)

        if (jsonAccountList != null) {
            // JSON verisini CustomerItem listesine dönüştürün
            val jsonArray = JSONArray(jsonAccountList)
            val accountList = mutableListOf<CustomerItem>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val customerNo = jsonObject.getString("customerNo")
                val secretKeyStr = jsonObject.getString("secretKey")
                val title = jsonObject.optString("title", null)
                val creationDate = jsonObject.getInt("creationDate")
                val ivStr = jsonObject.getString("iv")

                val secretKey = Base64.decode(secretKeyStr, Base64.DEFAULT)
                val iv = Base64.decode(ivStr, Base64.DEFAULT)

                val account = CustomerItem(customerNo, secretKey, title, creationDate, iv)
                accountList.add(account)
            }

            return accountList
        } else {
            return emptyList()
        }
    }


    fun getAccount(customerNo: String): CustomerItem? {


        var accountList = getAccountlist()

        // Hesabı aramak için bir döngü kullanabilirsiniz
        for (account in accountList) {
            if (account.customerNo == customerNo) {
                // Eşleşen hesabı bulduk, bu hesabı return edebiliriz
                return account
            }
        }
        // Eşleşen hesap bulunamadı
        return null
    }

    fun removeAcount(customerNo: String) {

        val account = getAccount(customerNo)

        if (account != null) {
            // Hesap bulundu, bu hesabı hesap listesinden kaldır
            val accountList = getAccountlist()
            val updatedAccountList = accountList.filterNot { it.customerNo == account.customerNo }

            // Güncellenmiş hesap listesini kaydet
            saveAccountList(updatedAccountList)

            // Hesap başarıyla kaldırıldı
            println("Hesap kaldırıldı: ${account.customerNo}")
        } else {
            // Hesap bulunamadı
            println("Hesap bulunamadı: $customerNo")
        }
    }

    private fun saveAccountList(accountList: List<CustomerItem>) {
        // Hesap listesini JSON formatına dönüştür
        val jsonAccountList = convertAccountListToJson(accountList)

        // JSON verisini SharedPreferences içinde sakla
        val sharedPreferences = context.getSharedPreferences("AccountPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("accountList", jsonAccountList)
        editor.apply()
    }

    companion object {
        // bunların hepsi private olacak sonra düzelt
         const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
         const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
         const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
         const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}

data class CustomerItem(
    val customerNo: String,
    val secretKey: ByteArray,
    val title: String?,
    val creationDate: Int,
    val iv: ByteArray,
)