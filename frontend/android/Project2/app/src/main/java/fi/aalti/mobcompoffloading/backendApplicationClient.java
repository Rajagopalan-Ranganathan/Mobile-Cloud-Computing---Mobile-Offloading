package fi.aalti.mobcompoffloading;


import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fi.aalti.mobilecompoffloading.R;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class backendApplicationClient {
    public static final String API_BASE_URL = "";



    private backendApplicationService backendApplicationService;
    public backendApplicationClient(Context context, final String token) {

        OkHttpClient client = new OkHttpClient();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //Create the interceptor to edit the Header field , since we need the token to communicate
        //with beackend - After the login auth is success use the same token received to communicate further
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request req = chain.request().newBuilder().addHeader("Authorization", token).build();
                return chain.proceed(req);

            }
        });

        client = builder.readTimeout(60, TimeUnit.SECONDS).connectTimeout(60, TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.server_url))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        backendApplicationService = retrofit.create(backendApplicationService.class);
    }

    public backendApplicationService getBackendApplicationService() {
        return backendApplicationService;
    }
}





