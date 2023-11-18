package com.cht.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.cht.demo.bean.MapEntity;
import com.cht.demo.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class GeoCooker {
	
	OkHttpClient client;
	
	public GeoCooker() {
		var builder = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS);
		client = builder.build();
	}

	protected Optional<MapEntity> findByAddress(String address)  {
		var u = "https://nominatim.openstreetmap.org/search";		

		HttpUrl url = HttpUrl.parse(u).newBuilder()
				.addQueryParameter("q", address)
				.addQueryParameter("format", "json")
				.addQueryParameter("polygon", "1")
				.addQueryParameter("addressdetails", "1")				
				.build();

		try {		
			Request request = new Request.Builder().url(url).get().build();
			try (Response response = client.newCall(request).execute()) {
				String responseBody = response.body().string();
				
				var mes = JsonUtils.fromJson(responseBody, MapEntity[].class);
				if (mes.length > 0) {
					return Optional.of(mes[0]);
				}
			}
			
		} catch (Exception e) {
			log.error("Error", e);
		}
		
		return Optional.empty();
	}
	
	protected Optional<MapEntity> findByLatLon(Double latitude,Double longitude)  {
		var u = "https://nominatim.openstreetmap.org/search";		

		HttpUrl url = HttpUrl.parse(u).newBuilder()
				.addQueryParameter("q", String.format("%f, %f", latitude,longitude))
				.addQueryParameter("format", "json")
				.addQueryParameter("polygon", "1")
				.addQueryParameter("addressdetails", "1")				
				.build();

		try {		
			Request request = new Request.Builder().url(url).get().build();
			try (Response response = client.newCall(request).execute()) {
				String responseBody = response.body().string();
				
				var mes = JsonUtils.fromJson(responseBody, MapEntity[].class);

				if (mes.length > 0) {
			
					return Optional.of(mes[0]);
				}
			}
			
		} catch (Exception e) {
			log.error("Error", e);
		}
		
		return Optional.empty();
	}
}
