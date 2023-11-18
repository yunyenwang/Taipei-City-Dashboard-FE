package com.cht.demo.bean;

import lombok.Data;

@Data
public class MapEntity {

	Float lat;
	Float lon;	
}

/*

[
   {
      "place_id":232947163,
      "licence":"Data © OpenStreetMap contributors, ODbL 1.0. http://osm.org/copyright",
      "osm_type":"node",
      "osm_id":10910780386,
      "lat":"25.0857838",
      "lon":"121.5236343",
      "class":"place",
      "type":"house",
      "place_rank":30,
      "importance":9.99999999995449e-06,
      "addresstype":"place",
      "name":"",
      "display_name":"175號, 承德路四段, 義信里, 士林區, 社子, 臺北市, 111, 臺灣",
      "address":{
         "house_number":"175號",
         "road":"承德路四段",
         "neighbourhood":"義信里",
         "suburb":"士林區",
         "village":"社子",
         "city":"臺北市",
         "ISO3166-2-lvl4":"TW-TPE",
         "postcode":"111",
         "country":"臺灣",
         "country_code":"tw"
      },
      "boundingbox":[
         "25.0857338",
         "25.0858338",
         "121.5235843",
         "121.5236843"
      ]
   }
]


 */
