/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.crypter.engine;

import static org.junit.Assert.assertEquals;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * A key image must give the same key on every supported JDK (8..21), or files protected on one machine would not open
 * on another. Expected digests were computed on JDK 8; the test is run on 8 and 17 as well.
 */
public class KeyImageDigestStabilityTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private static final Map<String, String> EXPECTED = new LinkedHashMap<String, String>();
	static {
		EXPECTED.put("rgb.png", "a2561d48ca83186e19587730349d694e74e454c1f3babed8f095aab68c289b8f");
		EXPECTED.put("argb.png", "5890c5d5c369b7f73c2c8fc54b35507cb803e4ba842488a03b4d87811c9247ed");
		EXPECTED.put("gray8.png", "8f24d37eec791e4d46675dbcdbe6e634050cde942ba45738eef6166cc4ba0cfe");
		EXPECTED.put("gray16.png", "8f24d37eec791e4d46675dbcdbe6e634050cde942ba45738eef6166cc4ba0cfe");
		EXPECTED.put("indexed.png", "dc2b0a6b9b39aaec38f6618579b2a9fb71cfab6ea8c5eb3823a7493162729423");
		EXPECTED.put("linear-rgb.png", "a2561d48ca83186e19587730349d694e74e454c1f3babed8f095aab68c289b8f");
		EXPECTED.put("rgb.bmp", "a2561d48ca83186e19587730349d694e74e454c1f3babed8f095aab68c289b8f");
		EXPECTED.put("rgb.jpg", "07769a33de8b84a98e816076b11df7fbd1fb7309f21ba73412c26ec664b84d4f");
	}

	@Test(timeout = 60000)
	public void keyImageDigestIsTheSameOnEveryJdk() throws Exception {
		StringBuilder actual = new StringBuilder();
		boolean ok = true;
		for (String name : EXPECTED.keySet()) {
			File f = new File(tmp.getRoot(), name);
			ImageIO.write(image(name), name.substring(name.lastIndexOf('.') + 1), f);
			CryptoKey k = CryptoKey.of("x".toCharArray(), f);
			String hex = PrimitiveVectorsTest.hex(k.keyImageDigest());
			actual.append(String.format("EXPECTED.put(\"%s\", \"%s\");%n", name, hex));
			ok &= hex.equals(EXPECTED.get(name));
		}
		System.out.println("java " + System.getProperty("java.version") + "\n" + actual);
		assertEquals("digests on java " + System.getProperty("java.version"), true, ok);
	}

	private static BufferedImage image(String name) {
		int w = 37, h = 23;
		BufferedImage img;
		if (name.startsWith("gray16")) {
			img = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
		} else if (name.startsWith("gray8")) {
			img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		} else if (name.startsWith("argb")) {
			img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		} else if (name.startsWith("indexed")) {
			img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED);
		} else if (name.startsWith("linear")) {
			ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), false, false,
					java.awt.Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
			WritableRaster r = cm.createCompatibleWritableRaster(w, h);
			img = new BufferedImage(cm, r, false, null);
		} else {
			img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		}
		WritableRaster r = img.getRaster();
		int bands = r.getNumBands();
		int max = (1 << img.getColorModel().getComponentSize(0)) - 1;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				for (int b = 0; b < bands; b++) {
					int v = (x * 31 + y * 17 + b * 101 + x * y) % 256;
					r.setSample(x, y, b, name.startsWith("indexed") ? v : v * max / 255);
				}
			}
		}
		return img;
	}
}
