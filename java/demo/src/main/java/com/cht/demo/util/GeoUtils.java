package com.cht.demo.util;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeoUtils {

	public static double degreesToRadians(double degrees) {
		return degrees * (Math.PI / 180);
	}
	
	public static double distance(double lat1, double lng1, double lat2, double lng2) {
		lat1 = degreesToRadians(lat1);
		lng1 = degreesToRadians(lng1);
		lat2 = degreesToRadians(lat2);
		lng2 = degreesToRadians(lng2);
		
		double x = (lng2 - lng1) * Math.cos((lat1 + lat2) / 2);
		double y = (lat2 - lat1);
		
		return Math.sqrt((x * x) + (y * y)) * 6378137.0;
	}
	
	/**
	 * Calculate the distance between x to path.
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * 
	 * @param range		the minimum distance between (lat1,lng1) to (lat2,lng2)
	 * 
	 * @param latx
	 * @param lngx
	 * 
	 * @return
	 */
	public static double distanceOfPath(
			double lat1, double lng1,
			double lat2, double lng2,
			double range,
			double latx, double lngx) {
		
		double a = GeoUtils.distance(latx, lngx, lat1, lng1);
		double b = GeoUtils.distance(latx, lngx, lat2, lng2);
		double c = GeoUtils.distance(lat1, lng1, lat2, lng2);
		
		if (c < range) { // circle is too small
			return Math.min(a, b); // EARLY RETURN
		}
		
		// center of the circle (from lat1,lng1 to lat2,lng2)
		double latp = (lat1 + lat2) / 2;
		double lngp = (lng1 + lng2) / 2;		
		
		double r = c / 2;
		
		double d = distance(latx, lngx, latp, lngp);
		
		if (d > r) { // out of circle
			return Math.min(a, b);
			
		} else { // use triangle area to get the distance between x and line
			double s = (a + b + c) / 2;			
			double area = Math.sqrt(s * (s - a) * (s - b) * (s - c));			
			// good
			return area * 2 / c; // vertical height from x to line 
		}
	}
	
//	/**
//	 * Read the address from Google.
//	 * 
//	 * @param lat
//	 * @param lng
//	 * @param lang
//	 * @return
//	 */
//	public static String geocode(double lat, double lng, String lang) {
//		try {
//			URL url = new URL(String.format("http://10.12.2.90/cache/point?lat=%.9f&lng=%.9f&lang=%s", lat, lng, lang));
//			String res = IOUtils.toString(url.openStream(), "UTF-8");
//			int i = res.indexOf(',');
//			if (i > 0) {
//				String sc = res.substring(0, i);
//				if ("200".equals(sc)) {
//					i = res.indexOf(',', i + 1);
//					if (i > 0) {
//						return res.substring(i + 1).replace("\"", "");
//					}					
//				}
//				
//				return String.format("No Address(%s)", sc);				
//			}
//			
//		} catch (Exception e) {
//			LOG.error("Failed to query the GEO address", e);
//		}		
//		
//		return "No Address (Server Error)";
//	}
	
	/**
	 * Read the address from Beleb.
	 * 
	 * @param deviceId - serial number of the GPS tracker
	 * @param lat
	 * @param lng
	 * @param lang - 'th' or 'en'
	 * @return
	 */
//	public static String geocode(String deviceId, double lat, double lng, String lang) {
//		try {
//			URL url = new URL(String.format("http://10.12.2.90/cache2/point?device_id=%s&lat=%.9f&lng=%.9f&lang=%s", deviceId, lat, lng, lang));
//			String res = IOUtils.toString(url.openStream(), "UTF-8");
//			int i = res.indexOf(',');
//			if (i > 0) {
//				String sc = res.substring(0, i);
//				if ("200".equals(sc)) {
//					i = res.indexOf(',', i + 1);
//					if (i > 0) {
//						return res.substring(i + 1).replace("\"", "");
//					}					
//				}
//				
//				return String.format("No Address(%s)", sc);				
//			}
//			
//		} catch (Exception e) {
//			LOG.error("Failed to query the GEO address", e);
//		}		
//		
//		return "No Address (Server Error)";
//	}
}
