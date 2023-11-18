package com.cht.demo;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.cht.demo.bean.MapEntity;
import com.cht.demo.util.JsonUtils;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeoCooker {

	protected Optional<MapEntity> findByAddress(String address) throws IOException {
		var u = "https://nominatim.openstreetmap.org/search";
		OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(5,
				TimeUnit.SECONDS);
		OkHttpClient client = builder.build();

		HttpUrl url = HttpUrl.parse(u).newBuilder()
				.addQueryParameter("q", address)
				.addQueryParameter("format", "json")
				.addQueryParameter("polygon", "1")
				.addQueryParameter("addressdetails", "1")				
				.build();

		Request request = new Request.Builder().url(url).get().build();
		try (Response response = client.newCall(request).execute()) {
			String responseBody = response.body().string();
			
			var mes = JsonUtils.fromJson(responseBody, MapEntity[].class);
			if (mes.length > 0) {
				return Optional.of(mes[0]);
			}
		}
		
		return Optional.empty();
	}
}
