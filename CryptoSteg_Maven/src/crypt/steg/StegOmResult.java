package crypt.steg;

import java.awt.image.BufferedImage;

/**
 * this object represent the result after STEG encription, using both result image and number of encoded bytes contained
 * The bytes are stored sequentially row after row
 * 
 * @author Alessio Moraschini
 *
 */
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
