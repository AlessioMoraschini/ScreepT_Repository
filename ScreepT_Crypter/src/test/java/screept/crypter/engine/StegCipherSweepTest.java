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
package screept.crypter.engine;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Test;

import screept.crypter.impl.steg.StegCipher;
import screept.crypter.impl.steg.StegOmResult;
import screept.crypter.util.ImageEncodingUtils;

/**
 * The legacy key image encoders at every ratio between the data and the pixels of the key image: from one byte per
 * pixel up to four, with the odd sizes whose last pixel carries fewer bytes. Whatever is accepted must come back intact.
 */
public class StegCipherSweepTest {

	private static final int TYPE = BufferedImage.TYPE_3BYTE_BGR;
	private static final int[] KEY_SIDES = { 6, 9, 14, 25 };
	private static final int[] SIZES = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 17, 31, 64, 99, 100, 101, 102, 103, 150, 199, 250, 301, 400 };

	private static byte[] data(int n, long seed) {
		byte[] b = new byte[n];
		new Random(seed).nextBytes(b);
		return b;
	}

	private static BufferedImage viaPng(BufferedImage img) throws Exception {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		assertTrue(ImageIO.write(img, "png", o));
		return ImageIO.read(new ByteArrayInputStream(o.toByteArray()));
	}

	private interface Variant {
		byte[] roundTrip(BufferedImage key, byte[] plain) throws Exception;
	}

	private static void sweep(String name, Variant v) throws Exception {
		int worked = 0;
		for (int side : KEY_SIDES) {
			BufferedImage key = ImageEncodingUtils.generaKeyImage(side, side, TYPE);
			for (int size : SIZES) {
				byte[] plain = data(size, side * 1000L + size);
				byte[] back;
				try {
					back = v.roundTrip(key, plain);
				} catch (Exception refused) {
					continue; // some encoders need a bigger key image than the smallest ones
				}
				if (back.length < size) {
					// more data than the pixels can carry: what was coded is the beginning of the data
					assertTrue(name + " lost bytes with room for them: " + size + " bytes, key " + side, size > side * side);
					plain = java.util.Arrays.copyOf(plain, back.length);
				}
				assertArrayEquals(name + " " + size + " bytes, key " + side + "x" + side, plain, back);
				worked++;
			}
		}
		assertTrue(name + " round trips: " + worked, worked >= 20);
	}

	@Test
	public void plainEncoderAtEveryRatio() throws Exception {
		sweep("plain", new Variant() {
			@Override
			public byte[] roundTrip(BufferedImage key, byte[] plain) throws Exception {
				StegOmResult r = new StegCipher(key, TYPE).stegaCrypt(plain);
				return new StegCipher(key, TYPE).stegaDeCryptToArray(viaPng(r.getImgRes()), Integer.valueOf(r.getnBytesCoded()));
			}
		});
	}

	@Test
	public void multiByteEncoderAtEveryRatio() throws Exception {
		sweep("multibyte", new Variant() {
			@Override
			public byte[] roundTrip(BufferedImage key, byte[] plain) throws Exception {
				StegOmResult r = new StegCipher(key, TYPE).stegaCryptMultiByte(plain);
				return new StegCipher(key, TYPE).stegaDeCryptMultiByte(viaPng(r.getImgRes()), Integer.valueOf(r.getnBytesCoded()));
			}
		});
	}

	@Test
	public void fourByteXorEncoderAtEveryRatioWithAndWithoutShuffle() throws Exception {
		for (final boolean shuffle : new boolean[] { false, true }) {
			sweep("4bytexor shuffle=" + shuffle, new Variant() {
				@Override
				public byte[] roundTrip(BufferedImage key, byte[] plain) throws Exception {
					StegOmResult r = new StegCipher(key, TYPE).stegaCrypt4ByteXor(plain, shuffle);
					return new StegCipher(key, TYPE).stegaDeCrypt4ByteXor(viaPng(r.getImgRes()), Integer.valueOf(r.getnBytesCoded()), shuffle);
				}
			});
		}
	}
}
