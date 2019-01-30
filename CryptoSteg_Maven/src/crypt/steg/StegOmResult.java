package crypt.steg;

import java.awt.image.BufferedImage;

public class StegOmResult {
	
	private BufferedImage imgRes;
	private String nBytesCoded;
	
	public StegOmResult(BufferedImage imRes, String strBytes) {
		imgRes = imRes;
		nBytesCoded = strBytes;
	}

	public BufferedImage getImgRes() {
		return imgRes;
	}

	public void setImgRes(BufferedImage imgRes) {
		this.imgRes = imgRes;
	}

	public String getnBytesCoded() {
		return nBytesCoded;
	}

	public void setnBytesCoded(String nBytesCoded) {
		this.nBytesCoded = nBytesCoded;
	}
	
	
}
