package org.standardnotes.notes.comms;

import org.standardnotes.notes.comms.data.AuthParamsResponse;
import org.standardnotes.notes.comms.data.SigninResponse;
import org.standardnotes.notes.comms.data.SyncItems;
import org.standardnotes.notes.comms.data.UploadSyncItems;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServerApi {

    @GET("/api/auth/params/")
    Call<AuthParamsResponse> getAuthParamsForEmail(@Query("email") String email);

    @FormUrlEncoded
    @POST("/api/auth/sign_in/")
    Call<SigninResponse> signin(@Field("email") String email, @Field("password") String hashedPassword);

    @FormUrlEncoded
    @POST("/api/auth/")
    Call<SigninResponse> register(@Field("email") String email, @Field("password") String hashedPassword,
                                  @Field("pw_salt") String pwSalt, @Field("version") String version,
                                  @Field("pw_cost") Integer pwCost);

    @POST("/api/items/sync/")
    Call<SyncItems> sync(@Body UploadSyncItems data);
}
