package com.cht.demo;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.geojson.FeatureCollection;
import org.junit.jupiter.api.Test;

import com.cht.demo.bean.Summary;
import com.cht.demo.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 積水相關計算
 */

@Slf4j
public class PondingCooker extends GeoCooker {
	
	// ====== 安裝彙整
	
	FeatureCollection loadFeatureCollection(String filename) throws IOException {
		try (var fis = new FileInputStream(filename)) {
			var reader = new InputStreamReader(fis, "UTF-8");
			
			return JsonUtils.fromJson(reader, FeatureCollection.class);			
		}
	}
	
	/**
	 * 積水 GeoJson
	 * 
	 * @throws Exception
	 */
	
	@Test
	void toPondingAveragePerDistrict() throws Exception {
		var format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		
		var fc = loadFeatureCollection("data/積水.geojson");

		var summarys = new HashMap<String, Summary>();
		
		for (var f : fc.getFeatures()) {
			try {			
				String district = f.getProperty("TOWN_NAME");			
				
				String d = f.getProperty("FD_DEPTH");
				var i = d.indexOf("~");
				if (i > 0) {
					d = d.substring(0, i);
				}
				
				int depth = Integer.parseInt(d);
				
				var date = LocalDate.parse((String) f.getProperty("FDATE"), format);
				
				var summary = summarys.computeIfAbsent(district, x -> {
					var s = new Summary();
					
					var data = new Summary.Data();
					data.setType("line");
					data.setName(String.format("%s積水均值", district));
					data.setData(new ArrayList<>());
					
					s.getData().add(data);
					
					return s;
				});
				
				var datas = summary.getData().get(0).getData();
				
				var datetime = String.format("%sT00:00:00+08:00", date);
				
				var data = new Summary.Data(datetime, depth);
				datas.add(data);
				
			} catch (Exception e) {
				log.error("Error", e);
			}
		}
		
		summarys.forEach((district, summary) -> {
			var newdatas = new ArrayList<Summary.Data>();
			
			var datas = summary.getData().get(0).getData();
			datas.stream()
				.collect(Collectors.groupingBy(Summary.Data::getX))
				.forEach((x, ds) -> {
					if (ds.isEmpty()) {
						return;						
					}
					
					int total = 0;
					for (var d : ds) {
						total += d.getY();
					}
					
					var newdata = new Summary.Data(x, total / ds.size());
					newdatas.add(newdata);
				});
			
			Collections.sort(newdatas, (a, z) -> {
				return a.getX().compareTo(z.getX());
			});
			
			summary.getData().get(0).setData(newdatas);
			
			log.info("{}", JsonUtils.toPrettyPrintJson(summary.getData().get(0)));
		});

		var districts = Arrays.asList(
				"北投區",
				"士林區",
				"內湖區",
				"南港區",
				"松山區",
				"信義區",
				"中山區",
				"大同區",
				"中正區",
				"萬華區",
				"大安區",
				"文山區"
				);
		
		for (var district : districts) {		
			var summary = summarys.get(district);
			
			try (var fw = new FileWriter(String.format("data/%s積水均值.json", district))) {
				JsonUtils.toPrettyPrintJson(fw, summary.getData().get(0));
			}
		}
	}
}
