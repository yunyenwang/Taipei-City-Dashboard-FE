package com.cht.demo.bean;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PointEntity {

	Map<String, Object> properties;
	
	Float lon;
	Float lat;	
}
