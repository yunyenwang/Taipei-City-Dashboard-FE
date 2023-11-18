package com.cht.demo.bean;

import java.util.ArrayList;
import java.util.List;

@lombok.Data
public class Summary {
	
	List<Data> data = new ArrayList<Data>();	
	
	@lombok.Data	
	public static class Data {
		public String type;
		public String name;		
		public String x;
		public Integer y;
		
		List<Data> data;

		public Data() {			
		}
		
		public Data(String x, Integer y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public void increase(String x, Integer count) {
		data.forEach(d -> {
			d.data.stream()
				.filter(dd -> dd.x.equals(x))
				.forEach(dd -> {
					dd.y += count;
				});
		});
	}
	
	public static Summary districts() {
		var s = new Summary();
		
		var data = new Summary.Data();
		data.setName("");
		data.data = new ArrayList<Data>();		
		data.data.add(new Data("北投區", 0));		
		data.data.add(new Data("士林區", 0));
		data.data.add(new Data("內湖區", 0));
		data.data.add(new Data("南港區", 0));
		data.data.add(new Data("松山區", 0));
		data.data.add(new Data("信義區", 0));
		data.data.add(new Data("中山區", 0));
		data.data.add(new Data("大同區", 0));
		data.data.add(new Data("中正區", 0));
		data.data.add(new Data("萬華區", 0));
		data.data.add(new Data("大安區", 0));
		data.data.add(new Data("文山區", 0));
		
		s.data.add(data);
		
		return s;
	}
}
