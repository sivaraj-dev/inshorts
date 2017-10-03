package in.sdev.android.inshorts.utilities;

import android.content.Context;

import in.sdev.android.inshorts.Constants;
import in.sdev.android.inshorts.interfaces.APIService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiUtils {
    public static final String BASE_URL = Constants.STATIC_NS_URL;

    private ApiUtils() {
    }

    public static APIService getAPIService(Context context) {
        //HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        //httpClient.addInterceptor(logging);
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()
                .create(APIService.class);
    }
}
