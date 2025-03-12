package com.example.mobile_app;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApiService {
    @GET("users")
    Call<List<User>> getUsers();

    @GET("users/{id}")
    Call<User> getUser(@Path("id") int userId);

    @POST("users")
    Call<User> createUser(@Body User user);

    @PUT("users/{id}")
    Call<User> updateUser(@Path("id") int userId, @Body User user);

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") int userId);
}