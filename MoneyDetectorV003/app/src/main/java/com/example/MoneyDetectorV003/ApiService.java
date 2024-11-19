// src/main/java/com/example/myapplication2/ApiService.java
package com.example.MoneyDetectorV003;

import com.example.MoneyDetectorV003.ServerResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


public interface ApiService {
    @Multipart
    @POST("/api/process-image")
    Call<ServerResponse> processImage(@Part MultipartBody.Part image);
}



