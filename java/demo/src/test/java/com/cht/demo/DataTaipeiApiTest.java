package com.cht.demo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
public class DataTaipeiApiTest {

	@Test
	void get() throws Exception {
		var u = "https://data.taipei/api/v1/dataset/82f3f379-b17c-41ff-aff2-1f3c0d16bb28?scope=resourceAquire";
		OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(5,
				TimeUnit.SECONDS);
		OkHttpClient client = builder.build();

		HttpUrl url = HttpUrl.parse(u).newBuilder().build();

		Request request = new Request.Builder().url(url).get().build();
		try (Response response = client.newCall(request).execute()) {
			String responseBody = response.body().string();
			log.info("{}", responseBody);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
