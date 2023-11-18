package com.cht.demo.util;

import java.util.ArrayList;
import java.util.Map;

import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.jackson.CrsType;

import com.cht.demo.bean.PointEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeoUtils {

	public static FeatureCollection newFeatureCollection() {
		var fc = new FeatureCollection();
		
		var crs = new Crs();
		crs.setType(CrsType.name);
		crs.setProperties(Map.of("name", "urn:ogc:def:crs:OGC:1.3:CRS84"));				
		fc.setCrs(crs);
		
		fc.setFeatures(new ArrayList<Feature>());
		
		return fc;
	}
	
	public static FeatureCollection addPoint(FeatureCollection fc, PointEntity pe) {
		var f = new Feature();
		f.setProperties(pe.getProperties());
		
		var p = new Point();
		p.setCoordinates(new LngLatAlt(pe.getLon(), pe.getLat()));					
		
		f.setGeometry(p);
		
		fc.getFeatures().add(f);
		
		return fc;
	}
	
	
	public static double degreesToRadians(double degrees) {
		return degrees * (Math.PI / 180);
	}
	
	public static double distance(double lat1, double lon1, double lat2, double lon2) {
		lat1 = degreesToRadians(lat1);
		lon1 = degreesToRadians(lon1);
		lat2 = degreesToRadians(lat2);
		lon2 = degreesToRadians(lon2);
		
		double x = (lon2 - lon1) * Math.cos((lat1 + lat2) / 2);
		double y = (lat2 - lat1);
		
		return Math.sqrt((x * x) + (y * y)) * 6378137.0;
	}
	
	/**
	 * Calculate the distance between x to path.
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * 
	 * @param range		the minimum distance between (lat1,lon1) to (lat2,lon2)
	 * 
	 * @param latx
	 * @param lonx
	 * 
	 * @return
	 */
	public static double distanceOfPath(
			double lat1, double lon1,
			double lat2, double lon2,
			double range,
			double latx, double lonx) {
		
		double a = GeoUtils.distance(latx, lonx, lat1, lon1);
		double b = GeoUtils.distance(latx, lonx, lat2, lon2);
		double c = GeoUtils.distance(lat1, lon1, lat2, lon2);
		
		if (c < range) { // circle is too small
			return Math.min(a, b); // EARLY RETURN
		}
		
		// center of the circle (from lat1,lon1 to lat2,lon2)
		double latp = (lat1 + lat2) / 2;
		double lonp = (lon1 + lon2) / 2;		
		
		double r = c / 2;
		
		double d = distance(latx, lonx, latp, lonp);
		
		if (d > r) { // out of circle
			return Math.min(a, b);
			
		} else { // use triangle area to get the distance between x and line
			double s = (a + b + c) / 2;			
			double area = Math.sqrt(s * (s - a) * (s - b) * (s - c));			
			// good
			return area * 2 / c; // vertical height from x to line 
		}
	}
	
}
