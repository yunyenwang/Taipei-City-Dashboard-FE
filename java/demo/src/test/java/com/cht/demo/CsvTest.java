package com.cht.demo;

import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.geojson.FeatureCollection;
import org.junit.jupiter.api.Test;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import com.cht.demo.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Slf4j
public class CsvTest {

	@Test
	void test() throws Exception {
		var builder = new OkHttpClient.Builder()
				.connectTimeout(5, TimeUnit.SECONDS)
				.readTimeout(5, TimeUnit.SECONDS);
		
		var client = builder.build();
		
		var url = HttpUrl.parse("https://raw.githubusercontent.com/tpe-doit/Taipei-Codefest-2023-Workshop/3-ETL/Datasets/Processed/%E8%BA%AB%E9%9A%9C%E5%8F%8B%E5%96%84%E6%A9%9F%E6%A7%8B.csv").newBuilder()
				.build();
		
		var req = new Request.Builder()
				.url(url)
				.get()
				.build();
		
		try (var res = client.newCall(req).execute()) {
			var reader = res.body().charStream();
			var parser = new CSVParser(reader, CSVFormat.EXCEL.builder().setHeader().build());
			
			log.info("{}", parser.getHeaderNames());
			
			for (var r : parser) {
				log.info("{}", r);
			}			
		}
	}	
}
