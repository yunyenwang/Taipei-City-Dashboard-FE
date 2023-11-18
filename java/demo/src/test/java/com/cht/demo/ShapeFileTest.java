package com.cht.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.filter.Filter;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShapeFileTest {

	@Test
	void test() throws Exception {
		var crsFactory = new CRSFactory();
		
		var WGS84 = crsFactory.createFromName("EPSG:4326");	// lat, lng
		var TWD97 = crsFactory.createFromName("EPSG:3826");
		
		var ctFactory = new CoordinateTransformFactory();
		var transformer = ctFactory.createTransform(TWD97, WGS84);
		
		File shp = null;
		
		var zis = new ZipInputStream(ShapeFileTest.class.getResourceAsStream("/身障設施_202309.zip"), Charset.forName("BIG5"));
		for (var ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()) {
			var name = ze.getName();			
			
			var file = new File("/tmp", name);
			try (var fos = new FileOutputStream(file)) {
				IOUtils.copy(zis, fos);
			}
			
			if (name.endsWith(".shp")) {
				shp = file;
			}
		}
		
		var map = new HashMap<String, Object>();
        map.put("url", shp.toURI().toURL());

        var dataStore = DataStoreFinder.getDataStore(map);
        var typeName = dataStore.getTypeNames()[0];

        var source = dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

        var collection = source.getFeatures(filter);
        try (var features = collection.features()) {
            while (features.hasNext()) {
                var feature = features.next();
                
                var id = feature.getID();	// 身障設施_202309.1
                
                var point = (Point) feature.getDefaultGeometryProperty().getValue();
                
                var c = point.getCoordinate();
                var x = c.getX();
                var y = c.getY();
                
                var result = transformer.transform(new ProjCoordinate(x, y), new ProjCoordinate());
                
                log.info("{} = {},{}", id, result.x, result.y);
            }
        }
	}
	
	@Test
	void district() throws Exception {
		var crsFactory = new CRSFactory();
		
		var WGS84 = crsFactory.createFromName("EPSG:4326");	// lat, lng
		var TWD97 = crsFactory.createFromName("EPSG:3826");
		
		var ctFactory = new CoordinateTransformFactory();
		var transformer = ctFactory.createTransform(TWD97, WGS84);
		
		File shp = null;
		
		try (var fis = new FileInputStream("data/臺北市里界圖_20220915.zip")) {		
			var zis = new ZipInputStream(fis, Charset.forName("BIG5"));
			for (var ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()) {
				var name = ze.getName();			
				
				var file = new File("/tmp", name);
				try (var fos = new FileOutputStream(file)) {
					IOUtils.copy(zis, fos);
				}
				
				if (name.endsWith(".shp")) {
					shp = file;
				}
			}
			
			var map = new HashMap<String, Object>();
	        map.put("url", shp.toURI().toURL());
	
	        var dataStore = DataStoreFinder.getDataStore(map);
	        var typeName = dataStore.getTypeNames()[0];
	
	        var source = dataStore.getFeatureSource(typeName);
	        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")
	
	        var collection = source.getFeatures(filter);
	        try (var features = collection.features()) {
	            while (features.hasNext()) {
	                var feature = features.next();
	                
	                var id = feature.getID();
	                
	                var point = (Point) feature.getDefaultGeometryProperty().getValue();
	                
	                var c = point.getCoordinate();
	                var x = c.getX();
	                var y = c.getY();
	                
	                var result = transformer.transform(new ProjCoordinate(x, y), new ProjCoordinate());
	                
	                log.info("{} = {},{}", id, result.x, result.y);
	            }
	        }
		}
	}
}
