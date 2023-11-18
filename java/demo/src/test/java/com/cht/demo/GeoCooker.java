package com.cht.demo;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

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
	
	CoordinateTransform twd97ToWgs84;
	
	public GeoCooker() {
		var builder = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS);
		client = builder.build();
		
		var crsFactory = new CRSFactory();
		
		var WGS84 = crsFactory.createFromName("EPSG:4326");	// lat, lng
		var TWD97 = crsFactory.createFromName("EPSG:3826");
				
		var ctFactory = new CoordinateTransformFactory();
		twd97ToWgs84 = ctFactory.createTransform(TWD97, WGS84);

//		var result = t.transform(new ProjCoordinate(lng, lat), new ProjCoordinate());
	}
	
	public ProjCoordinate fromTwd97ToWgs84(Double lng, Double lat) {
		return twd97ToWgs84.transform(new ProjCoordinate(lng, lat), new ProjCoordinate());
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
	
	/**
	 * 『里』轉換為『行政區』	
	 * @return
	 * @throws Exception
	 */
	Map<String, String> getLieToDistrict() throws Exception {
		var lieToDistrict = new HashMap<String, String>();
		
		try (var fis = new FileInputStream("data/臺北市鄰界圖.csv")) {
			var reader = new InputStreamReader(fis, "UTF-8");
			var parser = new CSVParser(reader, CSVFormat.EXCEL.builder().setHeader().setSkipHeaderRecord(true).build());
			
			for (var p : parser) {
				var district = p.get(0);
				var lie = p.get(2);
				
				lieToDistrict.put(lie, district);
			}			
		}
		
		return lieToDistrict;
	}
}
