/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
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
package screept.gui.frames.about;

import java.util.concurrent.ThreadLocalRandom;

/** How close a sentence is to waking up an easter egg, said like the old game: acqua, fuochino, fuoco */
enum NpcHeat {
	/** Nothing to do with eggs */
	COLD("Acqua! Ice cold.", "Acqua, acqua... not a single egg smells that.", "Acqua. You are in the middle of the ocean."),
	/** The sentence talks about secrets, games, surprises */
	WARM("Fuochino... getting warm.", "Fuochino! Something is cooking.", "Fuochino, fuochino... keep going."),
	/** The sentence is about the thing an egg is about, without naming it */
	HOT("Fuoco! You are burning!", "Fuoco, fuoco! One word away.", "Fuoco! I can feel the pixels sweating."),
	/** The sentence names an egg */
	MATCH("Fuoco! Bullseye.");

	private final String[] lines;

	NpcHeat(String... lines) {
		this.lines = lines;
	}

	String line() {
		return lines[ThreadLocalRandom.current().nextInt(lines.length)];
	}
}
