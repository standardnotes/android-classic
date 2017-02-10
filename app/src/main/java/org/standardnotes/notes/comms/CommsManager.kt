package org.standardnotes.notes.comms

import android.accounts.Account
import com.google.gson.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import org.standardnotes.notes.SApplication
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

class CommsManager(account: Account?, var server: String?) {

    private val retrofit: Retrofit
    private val okHttpClient: OkHttpClient
    val api: ServerApi

    init {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor(logger)
                .addInterceptor { chain ->
                    // Add auth to header if we have a token
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                    if (account != null && SApplication.instance!!.valueStore(account).token != null) {
                        requestBuilder.header("Authorization", "Bearer " + SApplication.instance!!.valueStore(account).token)
                    }
                    val request = requestBuilder.build()
                    chain.proceed(request)
                }
                .build()
        if (account != null)
            server = SApplication.instance!!.valueStore(account).server
        retrofit = Retrofit.Builder()
                .baseUrl(server)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(SApplication.instance!!.gson))
                .build()
        api = retrofit.create(ServerApi::class.java)
    }

    class DateTimeDeserializer : JsonDeserializer<DateTime>, JsonSerializer<DateTime> {

        @Throws(JsonParseException::class)
        override fun deserialize(je: JsonElement, type: Type,
                                 jdc: JsonDeserializationContext): DateTime? {
            if (je.asString.isEmpty()) {
                return null
            } else {
                return DATE_TIME_FORMATTER.parseDateTime(je.asString)
            }
        }

        override fun serialize(src: DateTime?, typeOfSrc: Type,
                               context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(if (src == null) "" else DATE_TIME_FORMATTER.print(src))
        }

        companion object {
            val DATE_TIME_FORMATTER: org.joda.time.format.DateTimeFormatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC)
        }
    }
}
