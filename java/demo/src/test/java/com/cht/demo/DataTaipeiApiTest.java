package com.cht.demo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import com.cht.demo.bean.PointEntity;
import com.cht.demo.bean.Summary;
import com.cht.demo.util.GeoUtils;
import com.cht.demo.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class DataTaipeiApiTest extends GeoCooker {

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

	/**
	 * 配電站
	 * 
	 * @throws Exception
	 */
	@Test
	void toSubstationGeoJson() throws Exception {

		try {
			var file = "data/配電站.xml";
			String name = file.substring(0, file.lastIndexOf("."));

			File xmlFile = new File(file);

			// 將XML轉換為JsonNode
			JsonNode jsonNode = xmlMapper.readTree(xmlFile);

			// 提取featureMember的數據
			JsonNode features = jsonNode.get("featureMember");
			var head = "UTL_場站";

			var fc = GeoUtils.newFeatureCollection();
			// 遍歷featureMember
			for (JsonNode feature : features) {
				// 提取座標數據
				String coordinates = feature.get(head).get("geometry").get("Point").get("coordinates").asText();
				String[] coordinateArray = coordinates.split(" ");
				double longitude = Double.parseDouble(coordinateArray[0]);
				double latitude = Double.parseDouble(coordinateArray[1]);
				var result = t.transform(new ProjCoordinate(longitude, latitude), new ProjCoordinate());

				var lon = result.x;
				var lan = result.y;

				// log.info("{}, {}", result.x, result.y); // 250000.0 2544283.12479424

				Map<String, Object> properties = new HashMap<String, Object>();
				Iterator<String> fieldNames = feature.get(head).fieldNames();
				while (fieldNames.hasNext()) {
					String fieldName = fieldNames.next();
					if (!fieldName.equals("geometry")) {
						JsonNode fieldValue = feature.get(head).get(fieldName);
						properties.put(fieldName, fieldValue.asText());
					}
				}

				findByLatLon(lan, lon).ifPresent(me -> {

					var district = me.getAddress().getSuburb();

					// 將設置日期加入屬性
					properties.put("district", district);
					log.info("lon: {}, lat: {}, district:{}", me.getLon(), me.getLat(), district);

				});

				// 提取設置日期
				String setDate = "";
				JsonNode timeInstant = feature.get(head).get("設置日期").get("TimeInstant");
				if (timeInstant != null) {
					setDate = timeInstant.get("timePosition").asText();
				}
				// 將設置日期加入屬性
				properties.put("設置日期", setDate);

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

	/**
	 * 變電箱
	 * 
	 * @throws Exception
	 */
	@Test
	void toTransformerBoxGeoJson() throws Exception {

		try {
			var file = "data/變電箱.xml";
			String name = file.substring(0, file.lastIndexOf("."));

			File xmlFile = new File(file);

			// 將XML轉換為JsonNode
			JsonNode jsonNode = xmlMapper.readTree(xmlFile);

			// 提取featureMember的數據
			JsonNode features = jsonNode.get("featureMember");
			var head = "UTL_其他設施";

			var fc = GeoUtils.newFeatureCollection();
			// 遍歷featureMember
			for (JsonNode feature : features) {
				// 提取設施名稱
				String facilityName = feature.get(head).get("設施名稱").asText();
				// 只保留設施名稱為"變電箱"的資料
				if (facilityName.contains("變電箱")) {

					// 提取座標數據
					String coordinates = feature.get(head).get("geometry").get("Point").get("coordinates").asText();
					String[] coordinateArray = coordinates.split(" ");
					double longitude = Double.parseDouble(coordinateArray[0]);
					double latitude = Double.parseDouble(coordinateArray[1]);
					var result = t.transform(new ProjCoordinate(longitude, latitude), new ProjCoordinate());

					var lon = result.x;
					var lan = result.y;

					// log.info("{}, {}", result.x, result.y); // 250000.0 2544283.12479424

					Map<String, Object> properties = new HashMap<String, Object>();
					Iterator<String> fieldNames = feature.get(head).fieldNames();
					while (fieldNames.hasNext()) {
						String fieldName = fieldNames.next();
						if (!fieldName.equals("geometry")) {
							JsonNode fieldValue = feature.get(head).get(fieldName);
							properties.put(fieldName, fieldValue.asText());
						}

					}

					// 提取設置日期
					String setDate = "";
					JsonNode timeInstant = feature.get(head).get("設置日期").get("TimeInstant");
					if (timeInstant != null) {
						setDate = timeInstant.get("timePosition").asText();
					}
					// 將設置日期加入屬性
					properties.put("設置日期", setDate);

					var pe = new PointEntity(properties, lon, lan);
					GeoUtils.addPoint(fc, pe);
				}
			}

			// 將GeoJSON寫入文件
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			objectMapper.writeValue(new File(String.format("%s.geojson", name)), fc);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 變電箱統計
	 * 
	 * @throws Exception
	 */
	@Test
	void toTransformerBoxSummaryGeoJson() throws Exception {

		try {
			var file = "data/變電箱.xml";
			String name = file.substring(0, file.lastIndexOf("."));

			File xmlFile = new File(file);

			// 將XML轉換為JsonNode
			JsonNode jsonNode = xmlMapper.readTree(xmlFile);

			// 提取featureMember的數據
			JsonNode features = jsonNode.get("featureMember");
			var head = "UTL_其他設施";

			var fc = GeoUtils.newFeatureCollection();
			// 遍歷featureMember
			for (JsonNode feature : features) {
				// 提取設施名稱
				String facilityName = feature.get(head).get("設施名稱").asText();
				// 只保留設施名稱為"變電箱"的資料
				if (facilityName.equals("變電箱")) {

					// 提取座標數據
					String coordinates = feature.get(head).get("geometry").get("Point").get("coordinates").asText();
					String[] coordinateArray = coordinates.split(" ");
					double longitude = Double.parseDouble(coordinateArray[0]);
					double latitude = Double.parseDouble(coordinateArray[1]);
					var result = t.transform(new ProjCoordinate(longitude, latitude), new ProjCoordinate());

					var lon = result.x;
					var lan = result.y;

					// log.info("{}, {}", result.x, result.y); // 250000.0 2544283.12479424

					Map<String, Object> properties = new HashMap<String, Object>();
					Iterator<String> fieldNames = feature.get(head).fieldNames();
					while (fieldNames.hasNext()) {
						String fieldName = fieldNames.next();
						if (!fieldName.equals("geometry")) {
							JsonNode fieldValue = feature.get(head).get(fieldName);
							properties.put(fieldName, fieldValue.asText());
						}

					}

					// 提取設置日期
					String setDate = "";
					JsonNode timeInstant = feature.get(head).get("設置日期").get("TimeInstant");
					if (timeInstant != null) {
						setDate = timeInstant.get("timePosition").asText();
					}
					// 將設置日期加入屬性
					properties.put("設置日期", setDate);

					var pe = new PointEntity(properties, lon, lan);
					GeoUtils.addPoint(fc, pe);
				}
			}

			// 將GeoJSON寫入文件
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			objectMapper.writeValue(new File(String.format("%s.geojson", name)), fc);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 變壓器
	 * 
	 * @throws Exception
	 */
	@Test
	void toTransformerGeoJson() throws Exception {

		try {
			var file = "data/變壓器.xml";
			String name = file.substring(0, file.lastIndexOf("."));

			File xmlFile = new File(file);

			// 將XML轉換為JsonNode
			JsonNode jsonNode = xmlMapper.readTree(xmlFile);

			// 提取featureMember的數據
			JsonNode features = jsonNode.get("featureMember");
			var head = "UTL_其他設施";

			var fc = GeoUtils.newFeatureCollection();
			// 遍歷featureMember
			for (JsonNode feature : features) {
				// 提取設施名稱
				String facilityName = feature.get(head).get("設施名稱").asText();
				// 只保留設施名稱為"變電箱"的資料
				if (facilityName.contains("變壓器")) {

					// 提取座標數據
					String coordinates = feature.get(head).get("geometry").get("Point").get("coordinates").asText();
					String[] coordinateArray = coordinates.split(" ");
					double longitude = Double.parseDouble(coordinateArray[0]);
					double latitude = Double.parseDouble(coordinateArray[1]);
					var result = t.transform(new ProjCoordinate(longitude, latitude), new ProjCoordinate());

					var lon = result.x;
					var lan = result.y;

					// log.info("{}, {}", result.x, result.y); // 250000.0 2544283.12479424

					Map<String, Object> properties = new HashMap<String, Object>();
					Iterator<String> fieldNames = feature.get(head).fieldNames();
					while (fieldNames.hasNext()) {
						String fieldName = fieldNames.next();
						if (!fieldName.equals("geometry")) {
							JsonNode fieldValue = feature.get(head).get(fieldName);
							properties.put(fieldName, fieldValue.asText());
						}

					}

					findByLatLon(lan, lon).ifPresent(me -> {

						var district = me.getAddress().getSuburb();

						// 將設置日期加入屬性
						properties.put("district", district);
						log.info("lon: {}, lat: {}, district:{}", me.getLon(), me.getLat(), district);

					});

					// 提取設置日期
					String setDate = "";
					JsonNode timeInstant = feature.get(head).get("設置日期").get("TimeInstant");
					if (timeInstant != null) {
						setDate = timeInstant.get("timePosition").asText();
					}
					// 將設置日期加入屬性
					properties.put("設置日期", setDate);

					var pe = new PointEntity(properties, lon, lan);
					GeoUtils.addPoint(fc, pe);
				}
			}

			// 將GeoJSON寫入文件
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			objectMapper.writeValue(new File(String.format("%s.geojson", name)), fc);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 變電箱統計
	 * 
	 * @throws Exception
	 */
	@Test
	void toTransformerSummaryGeoJson() throws Exception {

		try {
			var file = "data/變壓器.xml";
			String name = file.substring(0, file.lastIndexOf("."));

			File xmlFile = new File(file);

			// 將XML轉換為JsonNode
			JsonNode jsonNode = xmlMapper.readTree(xmlFile);

			// 提取featureMember的數據
			JsonNode features = jsonNode.get("featureMember");
			var head = "UTL_其他設施";

			var districts = Summary.districts();

			// 遍歷featureMember
			for (JsonNode feature : features) {
				// 提取設施名稱
				String facilityName = feature.get(head).get("設施名稱").asText();
				// 只保留設施名稱為"變電箱"的資料
				if (facilityName.equals("變壓器")) {

					// 提取座標數據
					String coordinates = feature.get(head).get("geometry").get("Point").get("coordinates").asText();
					String[] coordinateArray = coordinates.split(" ");
					double longitude = Double.parseDouble(coordinateArray[0]);
					double latitude = Double.parseDouble(coordinateArray[1]);
					var result = t.transform(new ProjCoordinate(longitude, latitude), new ProjCoordinate());

					var lon = result.x;
					var lan = result.y;
					findByLatLon(lan, lon).ifPresent(me -> {

						var district = me.getAddress().getSuburb();

						log.info("lon: {}, lat: {}, district:{}", me.getLon(), me.getLat(), district);
						districts.increase(district, 1);
					});

				}
			}
			log.info("lon: {}", JsonUtils.toPrettyPrintJson(districts));
			// 將GeoJSON寫入文件
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			objectMapper.writeValue(new File(String.format("%s行政區統計.geojson", name)), districts);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 變電站統計(from geojson)
	 * 
	 * @throws Exception
	 */
	@Test
	void toTransformerStationSummaryGeoJson() throws Exception {

		try {
			var file = "data/變壓器.geojson";
			String name = file.substring(0, file.lastIndexOf("."));
			// 讀取JSON檔案
			JSONObject json = readJSONFile(file);
			// 提取features陣列
			JSONArray features = json.getJSONArray("features");
			// 建立儲存district的List
			List<String> districts = new ArrayList<>();
			// 遍歷features陣列
			for (int i = 0; i < features.length(); i++) {
				JSONObject feature = features.getJSONObject(i);
				JSONObject properties = feature.getJSONObject("properties");
				String district = properties.getString("district");
				districts.add(district);
			}

			var districtss = Summary.districts();

			// 遍歷featureMember
			for (String district : districts) {

				districtss.increase(district, 1);

			}
			log.info("lon: {}", JsonUtils.toPrettyPrintJson(districtss));
			// 將GeoJSON寫入文件
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			objectMapper.writeValue(new File(String.format("%s行政區統計.geojson", name)), districtss);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 變電箱統計(第三組建)
	 * 
	 * @throws Exception
	 */
	@Test
	void merge() throws Exception {
		try {
			// 讀取配電站.geojson檔案
//			JSONObject geojson1 = readJSONFile("data/配電站.geojson");
			// 讀取配電箱.geojson檔案
			JSONObject geojson2 = readJSONFile("data/變壓器行政區統計.geojson");
			// 提取配電站和配電箱的數據
//			JSONArray data1 = geojson1.getJSONArray("data").getJSONObject(0).getJSONArray("data");
			JSONArray data2 = geojson2.getJSONArray("data").getJSONObject(0).getJSONArray("data");
			// 依照固定行政區順序組合數據
			String[] districts = { "北投區", "士林區", "內湖區", "南港區", "松山區", "信義區", "中山區", "大同區", "中正區", "萬華區", "大安區", "文山區" };
			JSONArray combinedData = new JSONArray();
//			JSONArray data1Combined = new JSONArray();
//			for (String district : districts) {
//				int index = getIndex(data1, district);
//				if (index != -1) {
//					data1Combined.put(data1.getJSONObject(index).getInt("y"));
//				}
//			}
//			JSONObject data1Object = new JSONObject();
//			data1Object.put("name", "配電站");
//			data1Object.put("data", data1Combined);
//			combinedData.put(data1Object);
			JSONArray data2Combined = new JSONArray();
			for (String district : districts) {
				int index = getIndex(data2, district);
				if (index != -1) {
					data2Combined.put(data2.getJSONObject(index).getInt("y"));
				}
			}
			JSONObject data2Object = new JSONObject();
			data2Object.put("name", "變壓器");
			data2Object.put("data", data2Combined);
			combinedData.put(data2Object);
			JSONObject combinedJSON = new JSONObject();
			combinedJSON.put("data", combinedData);
			// 將結果儲存為新的JSON檔案
			writeJSONFile(combinedJSON, "data/變壓器組件行政區統計.json");
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	private static JSONObject readJSONFile(String filename) throws IOException, JSONException {
		FileReader fileReader = new FileReader(filename);
		StringBuilder stringBuilder = new StringBuilder();
		int character;
		while ((character = fileReader.read()) != -1) {
			stringBuilder.append((char) character);
		}
		fileReader.close();
		return new JSONObject(stringBuilder.toString());
	}

	private static void writeJSONFile(JSONObject jsonObject, String filename) throws IOException {
		FileWriter fileWriter = new FileWriter(filename);
		fileWriter.write(jsonObject.toString());
		fileWriter.close();
	}

	private static int getIndex(JSONArray jsonArray, String district) throws JSONException {
		for (int i = 0; i < jsonArray.length(); i++) {
			String x = jsonArray.getJSONObject(i).getString("x");
			if (district.equals(x)) {
				return i;
			}
		}
		return -1;
	}

}
