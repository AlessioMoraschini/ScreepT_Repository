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
package screept.gui.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import various.common.light.om.KeyGenImageType;

/**
 * The key image generator engine (no window): size and type of the result, randomness, progress, cancellation and the
 * limits of the size.
 */
public class ImageWorkerScreepTKeyImageTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void everyTypeGivesTheRequestedSizeAndType() {
		for (KeyGenImageType t : KeyGenImageType.values()) {
			BufferedImage img = ImageWorkerScreepT.generateKeyImage(64, 48, t.getType(), null, null);
			assertNotNull(t.toString(), img);
			assertEquals(64, img.getWidth());
			assertEquals(48, img.getHeight());
			assertEquals(t.getType(), img.getType());
		}
	}

	@Test
	public void sizeIsLimitedToTheGeneratorMaximum() {
		BufferedImage img = ImageWorkerScreepT.generateKeyImage(ImageWorkerScreepT.MAX_WIDTH_GEN + 500, 1, KeyGenImageType.MID_SECURITY.getType(), null, null);
		assertEquals(ImageWorkerScreepT.MAX_WIDTH_GEN, img.getWidth());
		assertEquals(1, img.getHeight());
	}

	@Test
	public void generatedPixelsAreRandomAndDifferentEveryTime() {
		BufferedImage a = ImageWorkerScreepT.generateKeyImage(100, 100, KeyGenImageType.MID_SECURITY.getType(), null, null);
		BufferedImage b = ImageWorkerScreepT.generateKeyImage(100, 100, KeyGenImageType.MID_SECURITY.getType(), null, null);
		Set<Integer> distinct = new HashSet<Integer>();
		int equalPixels = 0;
		for (int y = 0; y < 100; y++) {
			for (int x = 0; x < 100; x++) {
				distinct.add(a.getRGB(x, y));
				if (a.getRGB(x, y) == b.getRGB(x, y)) {
					equalPixels++;
				}
			}
		}
		assertTrue("almost every pixel is a different colour: " + distinct.size(), distinct.size() > 9500);
		assertTrue("the second key differs from the first: " + equalPixels, equalPixels < 20);
	}

	@Test
	public void progressGoesUpAndStaysInRange() {
		final List<Integer> seen = new ArrayList<Integer>();
		ImageWorkerScreepT.generateKeyImage(100, 400, KeyGenImageType.LOW_SECURITY.getType(), new IntConsumer() {
			@Override
			public void accept(int percent) {
				seen.add(percent);
			}
		}, null);
		assertEquals("one report per row", 400, seen.size());
		int previous = -1;
		for (int p : seen) {
			assertTrue("in range: " + p, p >= 0 && p <= 100);
			assertTrue("never goes back", p >= previous);
			previous = p;
		}
		assertEquals(0, (int) seen.get(0));
	}

	@Test
	public void stoppedGenerationGivesNoImage() {
		final AtomicBoolean cancel = new AtomicBoolean(true);
		assertNull("cancelled before the start", ImageWorkerScreepT.generateKeyImage(100, 100, KeyGenImageType.MID_SECURITY.getType(), null, cancel));
		final AtomicBoolean later = new AtomicBoolean(false);
		BufferedImage img = ImageWorkerScreepT.generateKeyImage(100, 400, KeyGenImageType.MID_SECURITY.getType(), new IntConsumer() {
			@Override
			public void accept(int percent) {
				if (percent >= 50) {
					later.set(true);
				}
			}
		}, later);
		assertNull("cancelled half way: nothing half made is returned", img);
	}

	@Test
	public void generatedKeyIsLosslessAsPngForTheTypesWithoutAlpha() throws Exception {
		for (KeyGenImageType t : new KeyGenImageType[] { KeyGenImageType.MID_SECURITY, KeyGenImageType.LOW_SECURITY }) {
			BufferedImage img = ImageWorkerScreepT.generateKeyImage(50, 40, t.getType(), null, null);
			File f = tmp.newFile(t + ".png");
			assertTrue(ImageIO.write(img, "png", f));
			BufferedImage back = ImageIO.read(f);
			assertEquals(50, back.getWidth());
			assertEquals(40, back.getHeight());
			for (int y = 0; y < 40; y++) {
				for (int x = 0; x < 50; x++) {
					assertEquals(t + " pixel " + x + "," + y, img.getRGB(x, y), back.getRGB(x, y));
				}
			}
		}
	}
}
