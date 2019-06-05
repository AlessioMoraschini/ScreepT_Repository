package om;

import enums.ScalingAlgorithm;

public class ImgConverterOption {

	// DEFAULT VALUES (usfeul to keep valid object during iniLoad, in case of null read or exception)
	public static final boolean DEFAULT_SAME_SIZE = true;
	public static final boolean DEFAULT_SAME_EXT = true;
	public static final int DEFAULT_QUALITY = 100;
	public static final ScalingAlgorithm DEFAULT_ALGORITHM = ScalingAlgorithm.REPLICATE;
	
	// FIELDS	
	public boolean sameSize;
	public boolean sameExtension;
	public int quality;
	public ScalingAlgorithm algorithm;
	
	// CONSTRUCTOR
	public ImgConverterOption() {
	}

	// GETTERS AND SETTERS

	public boolean isSameSize() {
		return sameSize;
	}
	
	public void setSameSize(boolean sameSize) {
		this.sameSize = sameSize;
	}
	
	public boolean isSameExtension() {
		return sameExtension;
	}
	
	public void setSameExtension(boolean sameExtension) {
		this.sameExtension = sameExtension;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public void setQuality(int quality) {
		this.quality = quality;
	}
	
	public ScalingAlgorithm getAlgorithm() {
		return algorithm;
	}
	
	public void setAlgorithm(ScalingAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
}
