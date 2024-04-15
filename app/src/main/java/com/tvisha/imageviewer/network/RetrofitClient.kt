import com.tvisha.imageviewer.network.NetworkApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitClient {

    companion object {
        const val BASE_URL = "https://api.unsplash.com/"
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor()
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)).build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val networkService: NetworkApi by lazy {
        retrofit.create(NetworkApi::class.java)
    }

}