package com.cht.demo;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class OpenStreetMapApiTest {

	@Test
	void test() throws Exception {
		var u = "https://nominatim.openstreetmap.org/search";
		OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(5,
				TimeUnit.SECONDS);
		OkHttpClient client = builder.build();

		HttpUrl url = HttpUrl.parse(u).newBuilder()
				.addQueryParameter("q", "189號, 後港街, 士林區, 臺北市")
//				.addQueryParameter("q", "135 pilkington avenue, birmingham")
//				.addQueryParameter("q", "國雲停車泊車趣(D1停車場), 信義路五段14巷, 景新里, 信義區, 三張犁, 臺北市, 11049, 臺灣")
				.addQueryParameter("format", "json")
				.addQueryParameter("polygon", "1")
				.addQueryParameter("addressdetails", "1")				
				.build();

		Request request = new Request.Builder().url(url).get().build();
		try (Response response = client.newCall(request).execute()) {
			String responseBody = response.body().string();
			log.info("{}", responseBody);
		}
	}

}
