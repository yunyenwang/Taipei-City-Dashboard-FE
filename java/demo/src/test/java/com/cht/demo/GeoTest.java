package com.cht.demo;

import java.io.InputStreamReader;

import org.geojson.FeatureCollection;
import org.junit.jupiter.api.Test;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import com.cht.demo.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeoTest {

	@Test
	void test() {
		var crsFactory = new CRSFactory();
		
		var WGS84 = crsFactory.createFromName("EPSG:4326");	// lat, lng
		var TWD97 = crsFactory.createFromName("EPSG:3826");
		
		var TWD67 = crsFactory.createFromName("EPSG:3828");
//		var WMS = crsFactory.createFromName("epsg:3857"); //  
		
		var lng = 121;	// x
		var lat = 23;	// y
		
//		var lng = 121.49890900000;	// x
//		var lat = 24.99508858000;	// y
		
		var ctFactory = new CoordinateTransformFactory();
		var t = ctFactory.createTransform(WGS84, TWD97);

		var result = t.transform(new ProjCoordinate(lng, lat), new ProjCoordinate());
		
		
		log.info("{}, {}", result.x, result.y); // 250000.0 2544283.12479424
	}
	
	@Test
	void geojson() throws Exception {
//		var is = GeoTest.class.getResourceAsStream("/dis_origin_center.geojson");
		var is = GeoTest.class.getResourceAsStream("/dis_origin_radius.geojson");
		var isr = new InputStreamReader(is);
		
		var fc = JsonUtils.fromJson(isr, FeatureCollection.class);
		
		fc.getFeatures().forEach(f -> {
			var gm = f.getGeometry();
			log.info("{}", gm);
		});
		
		
	}
}
