package fi.aalti.mobcompoffloading;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;


public interface backendApplicationService {

    //Different API endpoints of the backend

    @POST("/api/users/auth")
    Call<UserLogin> auth(@Body UserLogin userLoginData);
    @POST("/api/users/facebook")
    Call<fb_login> fb_auth(@Body fb_login userLoginData);
    @POST("/api/ocr/text")
    Call<ocrRes> getOcr(@Body ocrreq ocrResData);
    @GET("/api/ocr/history")
    Call<List<History>> getHistory();

}






