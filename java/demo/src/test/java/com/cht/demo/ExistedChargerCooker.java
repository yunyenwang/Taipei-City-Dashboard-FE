package com.cht.demo;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
			
//			var districts = new HashMap<String, Integer>();
			
			for (var r : parser) {
				var ssss = r.get(0);
				
				var district = StringUtils.trim(ssss.substring(0, 3));
				var name =  StringUtils.trim(ssss.substring(3));				
				var address = r.get(1);
				address = GeoUtils.toGeoAddress(address);				
				address = String.format("%s, %s, 臺北市", address, district);
				
				districts.increase(district, 1);
				
//				var count = districts.computeIfAbsent(district, d -> 0);
//				districts.put(district, count + 1);
			}
			
			System.out.println(JsonUtils.toPrettyPrintJson(districts));
		}
	}
	
	@Test
	void toParkingLot() throws Exception {
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
	
}
