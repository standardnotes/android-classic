package org.standardnotes.notes.comms;

import org.standardnotes.notes.comms.data.*;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Created by carl on 04/01/17.
 */

public interface ServerApi {

    @GET("/api/auth/params/")
    Call<AuthParamsResponse> getAuthParamsForEmail(@Query("email") String email);

    @FormUrlEncoded
    @POST("/api/auth/sign_in/")
    Call<SigninResponse> signin(@Field("email") String email, @Field("password") String hashedPassword);

    @POST("/api/items/sync/")
    Call<SyncItems> sync(@Body() Object data);

}
