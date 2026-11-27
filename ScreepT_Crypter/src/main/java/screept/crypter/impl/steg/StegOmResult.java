/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.crypter.impl.steg;

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
