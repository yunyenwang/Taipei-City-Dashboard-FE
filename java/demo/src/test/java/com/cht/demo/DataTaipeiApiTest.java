package com.cht.demo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.geojson.FeatureCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import com.cht.demo.bean.PointEntity;
import com.cht.demo.util.GeoUtils;
import com.cht.demo.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class DataTaipeiApiTest {

	CRSFactory crsFactory;
	CoordinateTransformFactory ctFactory;
	CoordinateTransform t;
	XmlMapper xmlMapper;

	@BeforeEach
	void init() throws IOException {

		crsFactory = new CRSFactory();

		var WGS84 = crsFactory.createFromName("EPSG:4326"); // lat, lng
		var TWD97 = crsFactory.createFromName("EPSG:3826");
		var TWD67 = crsFactory.createFromName("EPSG:3828");

		ctFactory = new CoordinateTransformFactory();
		t = ctFactory.createTransform(TWD97, WGS84);

		xmlMapper = new XmlMapper();
		// 設置Jackson物件映射器以忽略未知的屬性
		xmlMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

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

	@Test
	void XMLToGeojson() throws Exception {

		try {
			var file = "data/配電系統及其他設施.xml";
			String name = file.substring(0, file.lastIndexOf("."));

			File xmlFile = new File(file);

			// 將XML轉換為JsonNode
			JsonNode jsonNode = xmlMapper.readTree(xmlFile);

			// 提取featureMember的數據
			JsonNode features = jsonNode.get("featureMember");

			var fc = GeoUtils.newFeatureCollection();
			// 遍歷featureMember
			for (JsonNode feature : features) {
				// 提取座標數據
				String coordinates = feature.get("UTL_其他設施").get("geometry").get("Point").get("coordinates").asText();
				String[] coordinateArray = coordinates.split(" ");
				double longitude = Double.parseDouble(coordinateArray[0]);
				double latitude = Double.parseDouble(coordinateArray[1]);
				var result = t.transform(new ProjCoordinate(longitude, latitude), new ProjCoordinate());

				var lon = result.x;
				var lan = result.y;

//				log.info("{}, {}", result.x, result.y); // 250000.0 2544283.12479424

				Map<String, Object> properties = new HashMap<String, Object>();
				Iterator<String> fieldNames = feature.get("UTL_其他設施").fieldNames();
				while (fieldNames.hasNext()) {
					String fieldName = fieldNames.next();
					if (!fieldName.equals("geometry")) {
						JsonNode fieldValue = feature.get("UTL_其他設施").get(fieldName);
						properties.put(fieldName, fieldValue.asText());
					}
				}

				var pe = new PointEntity(properties, lon, lan);
				GeoUtils.addPoint(fc, pe);
			}

			// 將GeoJSON寫入文件
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			objectMapper.writeValue(new File(String.format("%s.geojson", name)), fc);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
