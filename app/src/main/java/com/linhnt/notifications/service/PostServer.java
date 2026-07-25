package com.linhnt.notifications.service;

import com.google.gson.Gson;
import com.linhnt.notifications.model.PostData;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * {
 * integrity: 'qeaoisdoiw',
 * device_id: 'qeaoisdoiw',
 * app: 'VCB',
 * content: 'Số dư TK VCB 0011004199711 +02 VND lúc 30-10-2022 23:07:31. Số dư 1,256,522 VND. Ref 883598.301022.230730.9dmanga taivm1',
 * source: 'accfifa',
 * amount: '233000 ',
 * account: 'sangdeptrai11'
 * }
 */

public class PostServer {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    final OkHttpClient client = new OkHttpClient();

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    String convertJson(PostData data) {
        return new Gson().toJson(data);
    }
}