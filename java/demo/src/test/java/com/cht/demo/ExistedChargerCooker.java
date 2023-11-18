package com.cht.demo;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import com.cht.demo.util.GeoUtils;
import com.cht.demo.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExistedChargerCooker extends GeoCooker {

	String toGeoAddress(String address) {
		var chs = address.toCharArray();
		
		for (int i = chs.length - 1;i >= 0;i--) {
			var ch = chs[i];
			if ('段' == ch || '巷' == ch || '路' == ch || '街' == ch) {
				var address1 = StringUtils.trim(address.substring(i + 1));
				var x = address1.indexOf('號');
				if (x > 0) {
					address1 = address1.substring(0, x + 1);
				}				
				
				var address2 = StringUtils.trim(address.substring(0, i + 1));
				
				address = String.format("%s, %s", address1, address2);
				
				break;
			}			
		}
		
		return address;		
	}
	
	@Test
	void groupByDistrict() throws Exception {
		try (var fis = new FileInputStream("data/電動汽車充電格.csv")) {
			var reader = new InputStreamReader(fis, "UTF-8");			
			var parser = new CSVParser(reader, CSVFormat.EXCEL.builder().setHeader().build());
			
			for (var r : parser) {
				var ssss = r.get(0);
				var district = StringUtils.trim(ssss.substring(0, 3));
				var name =  StringUtils.trim(ssss.substring(3));				
				var address = r.get(1);
				address = toGeoAddress(address);
				
				log.info("name: {}, district: {}, address: {}", name, district, address);
				
				var ome = findByAddress(address);
				ome.ifPresent(me -> {
					log.info("lon: {}, lat: {}", me.getLon(), me.getLat());
					
					var fc = GeoUtils.newFeatureCollection();
					
					var pe = new PointEntity(Map.of(
							"name", name,
							"district", district),
							me.getLon(), me.getLat()
							);
					
					GeoUtils.addPoint(fc, pe);
					
					log.info("{}", JsonUtils.toPrettyPrintJson(fc));
					
				});
				
				Thread.sleep(1_000);
				
			}
		}
	}
}
