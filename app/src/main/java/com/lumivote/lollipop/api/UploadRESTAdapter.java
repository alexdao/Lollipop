package com.lumivote.lollipop.api;

import android.util.Log;

import com.lumivote.lollipop.bus.BusProvider;
import com.lumivote.lollipop.bus.ImageUploadEvent;
import com.squareup.otto.Bus;

import java.io.File;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by alex on 9/20/15.
 */
public class UploadRESTAdapter {

    private static final String API_URL = "http://host_base_url";
    private static final UploadRESTAdapter restClient = new UploadRESTAdapter();

    private Bus eventBus;

    private UploadRESTAdapter() {
    }

    public static UploadRESTAdapter getInstance() {
        restClient.eventBus = BusProvider.getInstance();
        restClient.eventBus.register(restClient);
        return restClient;
    }

    public interface UploadService {
        @Multipart
        @POST("/tag")
        void upload(@Part("myfile")TypedFile file,
                    @Part("description") String description,
                    Callback<String> cb);
    }

    public void uploadImage(String path) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .build();

        UploadService service = restAdapter.create(UploadService.class);

        TypedFile typedFile = new TypedFile("multipart/form-data", new File(path));
        String description = "hello, this is description speaking";

        service.upload(typedFile, description, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                eventBus.post(new ImageUploadEvent(ImageUploadEvent.Type.COMPLETED, s));
                Log.e("Upload", "success");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Upload", "error");
            }
        });
    }
}