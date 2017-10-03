package in.sdev.android.inshorts.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIService {

    @GET ("/newsjson")
    Call<ResponseBody> getNews(@Query ("category") String category, @Query ("publisher") String publisher,
                               @Query ("page") String page, @Query ("limit") String limit);


}
