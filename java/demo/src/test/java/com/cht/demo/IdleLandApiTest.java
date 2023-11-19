package com.cht.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.geojson.FeatureCollection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;

import com.cht.demo.bean.PointEntity;
import com.cht.demo.bean.Summary;
import com.cht.demo.util.GeoUtils;
import com.cht.demo.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.opencsv.CSVReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdleLandApiTest extends GeoCooker {

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

	/**
	 * 閒置土地
	 * 
	 * @throws Exception
	 */
	@Test
	void toSubstationGeoJson() throws Exception {

		try {
			var file = "data/land.csv";
			String fileName = file.substring(0, file.lastIndexOf("."));
			var fc = GeoUtils.newFeatureCollection();
			try (CSVReader reader = new CSVReader(new FileReader(file, StandardCharsets.UTF_8))) {
				String[] nextLine;
				List<String> buildingAddresses = new ArrayList<>();
				boolean firstLine = true;
				while ((nextLine = reader.readNext()) != null) {
					if (firstLine) {
						firstLine = false;
						continue;
					}
					String buildingAddress = nextLine[1];
					String name = nextLine[0];
					var district = StringUtils.trim(buildingAddress.substring(0, 3));
					var address = StringUtils.trim(buildingAddress.substring(3));
					address = GeoUtils.toGeoAddress(address);
					address = String.format("%s, %s, 臺北市", address, district);
					log.info("name: {}, district: {}, address: {}", name, district, address);

					findByAddress(address).ifPresent(me -> {
						log.info("lon: {}, lat: {}", me.getLon(), me.getLat());

						var pe = new PointEntity(Map.of("name", name, "district", district), me.getLon(), me.getLat());

						GeoUtils.addPoint(fc, pe);
					});
				}

				// 將GeoJSON寫入文件
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				objectMapper.writeValue(new File(String.format("%s.geojson", fileName)), fc);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 閒置土地統計(from geojson)
	 * 
	 * @throws Exception
	 */
	@Test
	void toTransformerStationSummaryGeoJson() throws Exception {

		try {
			var file = "data/land.geojson";
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
			objectMapper.writeValue(new File(String.format("%s行政區統計.json", name)), districtss);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 積水 GeoJson 各行政區歷史積水次數加總
	 * 
	 * @throws Exception
	 */
	@Test
	void toPondingSummary() throws Exception {
		
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        
		var fc = loadFeatureCollection("data/積水.geojson");

		var districts = Summary.districts();
		 Map<String, Integer> fdateCount = new HashMap<>();
		for (var f : fc.getFeatures()) {
			 String fdate = (String) f.getProperties().get("FDATE");
	            if (fdateCount.containsKey(fdate)) {
	                fdateCount.put(fdate, fdateCount.get(fdate) + 1);
	            } else {
	                fdateCount.put(fdate, 1);
	            }
		}
		log.error(JsonUtils.toPrettyPrintJson(fdateCount));
		
		// 將計算結果轉換成指定格式的JSON
		ObjectMapper objectMapper = new ObjectMapper();
		ArrayNode dataArray = objectMapper.createArrayNode();
		ObjectNode dataObject = objectMapper.createObjectNode();
		ArrayNode dataPoints = objectMapper.createArrayNode();
		// 對 fdateCount 的鍵值進行排序
		List<String> sortedDates = new ArrayList<>(fdateCount.keySet());
		Collections.sort(sortedDates);  // 將日期排序
		for (String fdate : sortedDates) {
		    int count = fdateCount.get(fdate);
		    Date date = inputFormat.parse(fdate);
		    String formattedDate = outputFormat.format(date);
		    ObjectNode dataPoint = objectMapper.createObjectNode();
		    dataPoint.put("x", formattedDate);
		    dataPoint.put("y", count);
		    dataPoints.add(dataPoint);
		}
		dataObject.put("name", "台北歷史積水次數加總");
		dataObject.set("data", dataPoints);
		dataArray.add(dataObject);
		ObjectNode outputObject = objectMapper.createObjectNode();
		outputObject.set("data", dataArray);
		try (var fw = new FileWriter("data/積水次數加總.json")) {
		    JsonUtils.toPrettyPrintJson(fw, outputObject);
		}
	}
	
	/**
	 * 積水 GeoJson 各行政區最大最小值
	 * 
	 * @throws Exception
	 */
	@Test
	void toPondingSummaryByDistricts() throws Exception {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        
		  String geojsonFilePath = "data/積水.geojson";
		  try {
	            // 讀取GeoJSON檔案
	            String geojsonContent = new String(Files.readAllBytes(Paths.get(geojsonFilePath)));
	            // 解析GeoJSON
	            JSONObject geojson = new JSONObject(geojsonContent);
	            JSONArray features = geojson.getJSONArray("features");
	            // 建立結果Map
	            Map<String, Map<String, Integer[]>> result = new HashMap<>();
	            // 遍歷features
	            for (int i = 0; i < features.length(); i++) {
	                JSONObject feature = features.getJSONObject(i);
	                JSONObject properties = feature.getJSONObject("properties");
	                String townName = properties.getString("TOWN_NAME");
	                String fdate = properties.getString("FDATE");
	                String fdDepthString = properties.getString("FD_DEPTH");
	                // 處理FD_DEPTH格式為xx~xx的情況
	                int fdDepth;
	                if (fdDepthString.contains("~")) {
	                    String[] depths = fdDepthString.split("~");
	                    int depth1 = Integer.parseInt(depths[0]);
	                    int depth2 = Integer.parseInt(depths[1]);
	                    fdDepth = Math.max(depth1, depth2);
	                } else {
	                    fdDepth = Integer.parseInt(fdDepthString);
	                }
	                // 更新結果Map
	                if (!result.containsKey(townName)) {
	                    result.put(townName, new HashMap<>());
	                }
	                Map<String, Integer[]> townData = result.get(townName);
	                if (!townData.containsKey(fdate)) {
	                    townData.put(fdate, new Integer[]{fdDepth, fdDepth});
	                } else {
	                    Integer[] depths = townData.get(fdate);
	                    depths[0] = Math.min(depths[0], fdDepth);
	                    depths[1] = Math.max(depths[1], fdDepth);
	                }
	            }
	         // 輸出結果為GeoJSON格式
	            for (String townName : result.keySet()) {
	                JSONObject outputGeojson = new JSONObject();
	                outputGeojson.put("type", "rangeArea");
	                outputGeojson.put("name", townName + "積水最高值與最低值");
	                JSONArray data = new JSONArray();
	                Map<String, Integer[]> townData = result.get(townName);
	                List<String> sortedDates = new ArrayList<>(townData.keySet());
	                Collections.sort(sortedDates);  // 將日期排序
	                for (String fdate : sortedDates) {
	                    Integer[] depths = townData.get(fdate);
	                    JSONObject item = new JSONObject();
	                    Date date = inputFormat.parse(fdate);
	                    String formattedDate = outputFormat.format(date);
	                    item.put("x", formattedDate);
	                    item.put("y", List.of(depths[0], depths[1]));
	                    data.put(item);
	                }
	                outputGeojson.put("data", data);
	                // 寫入輸出檔案
	                String outputFilePath = townName + "積水最高值與最低值.json";
	                FileWriter writer = new FileWriter(outputFilePath);
	                writer.write(outputGeojson.toString());
	                writer.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
	
	/**
	 * 閒置土地
	 * 
	 * @throws Exception
	 */
	@Test
	void toTempleGeoJson() throws Exception {

		try {
			var file = "data/temple.csv";
			String fileName = file.substring(0, file.lastIndexOf("."));
			var fc = GeoUtils.newFeatureCollection();
			try (CSVReader reader = new CSVReader(new FileReader(file, Charset.forName("Big5")))) {
				String[] nextLine;
				List<String> buildingAddresses = new ArrayList<>();
				boolean firstLine = true;
				while ((nextLine = reader.readNext()) != null) {
					if (firstLine) {
						firstLine = false;
						continue;
					}
//					String buildingAddress = nextLine[3];
					String name = nextLine[1];
					var district = nextLine[2];
					var address = nextLine[3];
					address = GeoUtils.toGeoAddress(address);
					address = String.format("%s, %s, 臺北市", address, district);
					log.info("name: {}, district: {}, address: {}", name, district, address);

					findByAddress(address).ifPresent(me -> {
						log.info("lon: {}, lat: {}", me.getLon(), me.getLat());

						var pe = new PointEntity(Map.of("name", name, "district", district), me.getLon(), me.getLat());

						GeoUtils.addPoint(fc, pe);
					});
				}

				// 將GeoJSON寫入文件
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				objectMapper.writeValue(new File(String.format("%s.geojson", fileName)), fc);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	FeatureCollection loadFeatureCollection(String filename) throws IOException {
		try (var fis = new FileInputStream(filename)) {
			var reader = new InputStreamReader(fis, "UTF-8");
			
			return JsonUtils.fromJson(reader, FeatureCollection.class);			
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
