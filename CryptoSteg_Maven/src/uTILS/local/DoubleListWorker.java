package uTILS.local;

import java.util.List;

public class DoubleListWorker {

	public static Double getAvgValue(List<Double> valueList) {
		double sum = valueList.stream().mapToDouble(Double::doubleValue).sum();
		int size = valueList.size();
		return (sum/size);
		
	}
}
