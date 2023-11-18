package com.cht.demo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.jackson.CrsType;
import org.junit.jupiter.api.Test;

import com.cht.demo.bean.PointEntity;
import com.cht.demo.bean.Summary;
import com.cht.demo.util.GeoUtils;
import com.cht.demo.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExistedChargerCooker extends GeoCooker {
		
	/**
	 * 電動汽車充電格
	 * 
	 * @throws Exception
	 */
	@Test
	void toExistedChargerGeoJson() throws Exception {
		try (var fis = new FileInputStream("data/電動汽車充電格.csv")) {
			var reader = new InputStreamReader(fis, "UTF-8");			
			var parser = new CSVParser(reader, CSVFormat.EXCEL.builder().setHeader().build());
			
			var fc = GeoUtils.newFeatureCollection();
			
			for (var r : parser) {
				var ssss = r.get(0);
				
				var district = StringUtils.trim(ssss.substring(0, 3));
				var name =  StringUtils.trim(ssss.substring(3));				
				var address = r.get(1);
				address = GeoUtils.toGeoAddress(address);				
				address = String.format("%s, %s, 臺北市", address, district);
				
				log.info("name: {}, district: {}, address: {}", name, district, address);
				
				findByAddress(address).ifPresent(me -> {
					log.info("lon: {}, lat: {}", me.getLon(), me.getLat());
					
					var pe = new PointEntity(Map.of(
							"name", name,
							"district", district),
							me.getLon(), me.getLat()
							);
					
					GeoUtils.addPoint(fc, pe);
				});
				
				Thread.sleep(1_000);				
			}
			
			System.out.println(JsonUtils.toPrettyPrintJson(fc));
		}
	}
	
	/**
	 * 電動汽車充電格 統計
	 * 
	 * @throws Exception
	 */	
	@Test
	void toExistedChargerSummary() throws Exception {
		try (var fis = new FileInputStream("data/電動汽車充電格.csv")) {
			var reader = new InputStreamReader(fis, "UTF-8");			
			var parser = new CSVParser(reader, CSVFormat.EXCEL.builder().setHeader().build());
			
			var districts = Summary.districts();
			
			for (var r : parser) {
				var ssss = r.get(0);
				
				var district = StringUtils.trim(ssss.substring(0, 3));
				var name =  StringUtils.trim(ssss.substring(3));				
				var address = r.get(1);
				address = GeoUtils.toGeoAddress(address);				
				address = String.format("%s, %s, 臺北市", address, district);
				
				districts.increase(district, 1);
			}
			
			System.out.println(JsonUtils.toPrettyPrintJson(districts));
		}
	}
	
	/**
	 * 所有停車場 GeoJson
	 * 
	 * @throws Exception
	 */
	
	@Test
	void toParkingLotGeoJson() throws Exception {
		try (var fis = new FileInputStream("data/停車場.json")) {
			var reader = new InputStreamReader(fis, "UTF-8");			
			
			var fc = GeoUtils.newFeatureCollection();
			
			var pl = JsonUtils.fromJson(reader, ParkingLot.class);
			for (var p : pl.data.park) {
				var c = fromTwd97ToWgs84(p.x, p.y);				
				p.x = c.x;
				p.y = c.y;
				
				var pe = new PointEntity(Map.of(
						"name", p.getName(),
						"district", p.getDistrict()),
						p.getX(), p.getY()
						);
				
				GeoUtils.addPoint(fc, pe);
			}
			
			try (var fw = new FileWriter("data/停車場.geojson")) {
				JsonUtils.toPrettyPrintJson(fw, fc);
			}			
		}
	}
	
	@Test
	void toParkingLotSummary() throws Exception {
		try (var fis = new FileInputStream("data/停車場.json")) {
			var reader = new InputStreamReader(fis, "UTF-8");			
			
			var districts = Summary.districts();
			
			var pl = JsonUtils.fromJson(reader, ParkingLot.class);
			for (var p : pl.data.park) {
				
				districts.increase(p.getDistrict(), 1);
			}
			
			try (var fw = new FileWriter("data/停車場彙整.json")) {
				JsonUtils.toPrettyPrintJson(fw, districts);
			}			
		}
	}
	
	@Data
	public static class ParkingLot {
		
		Meta data = new Meta();
		
		@Data
		public static class Meta {
			List<Park> park;
		}
		
		@Data
		public static class Park {
			
			@JsonProperty("area")
			String district;
			
			String name;
			
			@JsonProperty("tw97x")
			Double x;
			
			@JsonProperty("tw97y")
			Double y;
		}
	}
	
	/**
	 * 公園 GeoJson
	 * 
	 * @throws Exception
	 */
	
	@Test
	void toParkGeoJson() throws Exception {
		var l2d = getLieToDistrict();
		
		try (var fis = new FileInputStream("data/公園.json")) {
			var reader = new InputStreamReader(fis, "UTF-8");			
			
			var fc = GeoUtils.newFeatureCollection();
			
			var ms = JsonUtils.fromJson(reader, Map[].class);
			for (var m : ms) {
				var name = (String) m.get("pm_name");
				var lon = Double.parseDouble((String) m.get("pm_Longitude"));
				var lat = Double.parseDouble((String) m.get("pm_Latitude"));
				var lie = (String) m.get("pm_libie");
				
				var district = l2d.get(lie);
				if (StringUtils.isBlank(district)) {
					continue;
				}
				
				var pe = new PointEntity(Map.of(
						"name", name,
						"district", district),
						lon, lat
						);
				
				GeoUtils.addPoint(fc, pe);
			}
			
			try (var fw = new FileWriter("data/公園.geojson")) {
				JsonUtils.toPrettyPrintJson(fw, fc);
			}			
		}
	}
	
	@Test
	void toParkSummary() throws Exception {
		var l2d = getLieToDistrict();
		
		try (var fis = new FileInputStream("data/公園.json")) {
			var reader = new InputStreamReader(fis, "UTF-8");			
			
			var districts = Summary.districts();
			
			var ms = JsonUtils.fromJson(reader, Map[].class);
			for (var m : ms) {
				var lie = (String) m.get("pm_libie");
				
				var district = l2d.get(lie);
				if (StringUtils.isBlank(district)) {
					continue;
				}
				
				districts.increase(district, 1);				
			}
			
			try (var fw = new FileWriter("data/公園彙整.json")) {
				JsonUtils.toPrettyPrintJson(fw, districts);
			}			
		}
	}
		
	
	/**
	 * 超級市場 GeoJson
	 * 
	 * @throws Exception
	 */
	
	@Test
	void toMarketGeoJson() throws Exception {
		try (var fis = new FileInputStream("data/超級市場.csv")) {
			var reader = new InputStreamReader(fis, "BIG5");
			var parser = new CSVParser(reader, CSVFormat.EXCEL.builder().setHeader().setSkipHeaderRecord(true).build());
			
			var fc = GeoUtils.newFeatureCollection();
			
			for (var p : parser) {
				var name = p.get(1);
				var address = p.get(4);
				var lon = Double.parseDouble(p.get(5));
				var lat = Double.parseDouble(p.get(6));
				var district = address.substring(3, 6);
				
				var pe = new PointEntity(Map.of(
						"name", name,
						"district", district),
						lon, lat
						);
				
				GeoUtils.addPoint(fc, pe);
			}
			
			try (var fw = new FileWriter("data/超級市場.geojson")) {
				JsonUtils.toPrettyPrintJson(fw, fc);
			}
		}			
	}
	
	@Test
	void toMarketSummary() throws Exception {
		try (var fis = new FileInputStream("data/超級市場.csv")) {
			var reader = new InputStreamReader(fis, "BIG5");
			var parser = new CSVParser(reader, CSVFormat.EXCEL.builder().setHeader().setSkipHeaderRecord(true).build());
			
			var districts = Summary.districts();
			
			for (var p : parser) {
				var name = p.get(1);
				var address = p.get(4);
				var lon = Double.parseDouble(p.get(5));
				var lat = Double.parseDouble(p.get(6));
				var district = address.substring(3, 6);
				
				districts.increase(district, 1);
			}
			
			try (var fw = new FileWriter("data/超級市場彙整.json")) {
				JsonUtils.toPrettyPrintJson(fw, districts);
			}
		}			
	}
	
	// ====== 安裝彙整
	
	FeatureCollection loadFeatureCollection(String filename) throws IOException {
		try (var fis = new FileInputStream(filename)) {
			var reader = new InputStreamReader(fis, "UTF-8");
			
			return JsonUtils.fromJson(reader, FeatureCollection.class);			
		}
	}
	
	boolean isNearBy(FeatureCollection charger, LngLatAlt p1) {
		for (var c : charger.getFeatures()) {				
			var p2 = ((Point) c.getGeometry()).getCoordinates();
			
			var distance = GeoUtils.distance(						
					p1.getLatitude(), p1.getLongitude(),
					p2.getLatitude(), p2.getLongitude());
			
			if (distance < 500) {
				return true;
			}
		}
		
		return false;
	}
	
	int countNearBy(FeatureCollection charger, FeatureCollection target) {
		var count = new AtomicInteger();
		for (var t : target.getFeatures()) {
			var p1 = ((Point) t.getGeometry()).getCoordinates();
			
			if (isNearBy(charger, p1)) {
				count.incrementAndGet();
				break;
			}
			
//			for (var c : charger.getFeatures()) {				
//				var p2 = ((Point) c.getGeometry()).getCoordinates();
//				
//				var distance = GeoUtils.distance(						
//						p1.getLatitude(), p1.getLongitude(),
//						p2.getLatitude(), p2.getLongitude());
//				
//				if (distance < 500) {
//					count.incrementAndGet();
//					break;
//				}
//			}
		}		
		
		return count.intValue();
	}
	
	@Test
	void nearBy() throws Exception {
		var charger = loadFeatureCollection("data/電動汽車充電格.geojson");
		
		var park = loadFeatureCollection("data/公園.geojson");
		var lot = loadFeatureCollection("data/停車場.geojson");
		var market = loadFeatureCollection("data/超級市場.geojson");
		
		int parkInstalled = countNearBy(charger, park);		
		int lotInstalled = countNearBy(charger, lot);		
		int marketInstalled = countNearBy(charger, market);
		
		var s = new Stack();
		
		var installed = new Stack.StackData();
		var notyet = new Stack.StackData();
		
		s.getData().add(installed);
		s.getData().add(notyet);
		
		installed.getData().add(parkInstalled);
		installed.getData().add(lotInstalled);
		installed.getData().add(marketInstalled);
		
		notyet.getData().add(park.getFeatures().size());
		notyet.getData().add(lot.getFeatures().size());
		notyet.getData().add(market.getFeatures().size());
		
		try (var fw = new FileWriter("data/公園停車場賣場安裝彙整.json")) {
			JsonUtils.toPrettyPrintJson(fw, s);
		}		
	}
	
	@Data
	public static class Stack {
		
		List<StackData> data = new ArrayList<>();
		
		@Data
		public static class StackData {
			String name;		
			List<Integer> data = new ArrayList<>();			
		}		
	}
	
	@Test
	void nearByPoints() throws Exception {
		var charger = loadFeatureCollection("data/電動汽車充電格.geojson");
		
		var park = loadFeatureCollection("data/公園.geojson");
		var lot = loadFeatureCollection("data/停車場.geojson");
		var market = loadFeatureCollection("data/超級市場.geojson");
		
		var fc = GeoUtils.newFeatureCollection();
		
		for (var f : park.getFeatures()) {
			String name = f.getProperty("name");
			String district = f.getProperty("district");
			String type = "公園";
			var p1 = ((Point) f.getGeometry()).getCoordinates();
			
			var installed = isNearBy(charger, p1);		
			
			var properties = new HashMap<String, Object>();
			properties.put("name", name);
			properties.put("district", district);
			properties.put("type", type);
			properties.put("installed", installed);
			
			var pe = new PointEntity(properties, p1.getLongitude(), p1.getLatitude());
			
			GeoUtils.addPoint(fc, pe);
		}
		
		for (var f : lot.getFeatures()) {
			String name = f.getProperty("name");
			String district = f.getProperty("district");
			String type = "停車場";
			var p1 = ((Point) f.getGeometry()).getCoordinates();
			
			var installed = isNearBy(charger, p1);		
			
			var properties = new HashMap<String, Object>();
			properties.put("name", name);
			properties.put("district", district);
			properties.put("type", type);
			properties.put("installed", installed);
			
			var pe = new PointEntity(properties, p1.getLongitude(), p1.getLatitude());
			
			GeoUtils.addPoint(fc, pe);
		}
		
		for (var f : market.getFeatures()) {
			String name = f.getProperty("name");
			String district = f.getProperty("district");
			String type = "賣場";
			var p1 = ((Point) f.getGeometry()).getCoordinates();
			
			var installed = isNearBy(charger, p1);		
			
			var properties = new HashMap<String, Object>();
			properties.put("name", name);
			properties.put("district", district);
			properties.put("type", type);
			properties.put("installed", installed);
			
			var pe = new PointEntity(properties, p1.getLongitude(), p1.getLatitude());
			
			GeoUtils.addPoint(fc, pe);
		}
		
		
		try (var fw = new FileWriter("data/公園停車場賣場安裝.geojson")) {
			JsonUtils.toPrettyPrintJson(fw, fc);
		}
	}
	
	
	/**
	 * 積水 GeoJson
	 * 
	 * @throws Exception
	 */
	
	@Test
	void toPondingSummary() throws Exception {
		var fc = loadFeatureCollection("data/積水.geojson");

		var districts = Summary.districts();
		
		for (var f : fc.getFeatures()) {
			var district = (String) f.getProperty("TOWN_NAME");
			
			districts.increase(district, 1);
		}
		
		try (var fw = new FileWriter("data/積水彙整.json")) {
			JsonUtils.toPrettyPrintJson(fw, districts);
		}
	}
	
	/**
	 * 地震
	 * 
	 * @throws Exception
	 */
	
	@Test
	void toEarthQuack() throws Exception {
		try (var fis = new FileInputStream("data/地震.csv")) {
			var reader = new InputStreamReader(fis, "UTF-8");			
			var parser = new CSVParser(reader, CSVFormat.EXCEL.builder().setHeader().build());
			
			var fc = GeoUtils.newFeatureCollection();
			
			for (var r : parser) {
				var ssss = r.get(0);
				
				var district = String.format("%s區", StringUtils.trim(ssss.substring(0, 2)));
				var name =  StringUtils.trim(ssss.substring(3));
				var address = GeoUtils.toGeoAddress(name);
				address = String.format("%s, %s, 臺北市", address, district);
				
				log.info("name: {}, district: {}, address: {}", name, district, address);
				
				findByAddress(address).ifPresent(me -> {
					log.info("lon: {}, lat: {}", me.getLon(), me.getLat());
					
					var pe = new PointEntity(Map.of(
							"name", name,
							"district", district),
							me.getLon(), me.getLat()
							);
					
					GeoUtils.addPoint(fc, pe);
				});
				
				Thread.sleep(1_000);				
			}
			
			try (var fw = new FileWriter("data/地震.geojson")) {
				JsonUtils.toPrettyPrintJson(fw, fc);
			}
		}
	}

	/**
	 * 地震彙整
	 * 
	 * @throws Exception
	 */
	
	@Test
	void toEarthQuakeSummary() throws Exception {
		var fc = loadFeatureCollection("data/地震.geojson");

		var districts = Summary.districts();
		
		for (var f : fc.getFeatures()) {
			var district = (String) f.getProperty("district");
			
			districts.increase(district, 1);
		}
		
		try (var fw = new FileWriter("data/地震彙整.json")) {
			JsonUtils.toPrettyPrintJson(fw, districts);
		}
	}

	
//	/**
//	 * 電動汽車充電格 統計
//	 * 
//	 * @throws Exception
//	 */	
//	@Test
//	void toExistedChargerSummary() throws Exception {
//		try (var fis = new FileInputStream("data/電動汽車充電格.csv")) {
//			var reader = new InputStreamReader(fis, "UTF-8");			
//			var parser = new CSVParser(reader, CSVFormat.EXCEL.builder().setHeader().build());
//			
//			var districts = Summary.districts();
//			
//			for (var r : parser) {
//				var ssss = r.get(0);
//				
//				var district = StringUtils.trim(ssss.substring(0, 3));
//				var name =  StringUtils.trim(ssss.substring(3));				
//				var address = r.get(1);
//				address = GeoUtils.toGeoAddress(address);				
//				address = String.format("%s, %s, 臺北市", address, district);
//				
//				districts.increase(district, 1);
//			}
//			
//			System.out.println(JsonUtils.toPrettyPrintJson(districts));
//		}
//	}
}
