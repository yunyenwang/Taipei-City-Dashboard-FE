package com.cht.demo.util;

import java.util.List;

public class MathUtils {

	/**
	 * 平均值
	 * 
	 * @param values
	 * @return
	 */
	public static double average(List<Double> values) {
		if (values.isEmpty()) {
			return 0;
		}
		
	    double sum = 0.0;
	    for (double v : values) {
	        sum += v;
	    }

	    return sum / values.size();
	}
	
	/**
	 * 標準差
	 * 
	 * (採用 Population standard deviation)
	 * 
	 * TODO - 還有另外一種 Sample standard deviation 的計算，就是分母變成 n - 1
	 * 
	 * https://en.wikipedia.org/wiki/Standard_deviation
	 * 
	 * @param values
	 * @return
	 */
	public static double calculateStandardDeviation(List<Double> values) {
		if (values.isEmpty()) {
			return 0;
		}
		
		double mean = average(values);

	    double standardDeviation = 0.0;
	    for (double num : values) {
	        standardDeviation += Math.pow(num - mean, 2);
	    }

	    return Math.sqrt(standardDeviation / values.size());
	}
}
