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

import java.text.Normalizer;
import java.util.Locale;

/** Text helpers of the assistant: lower case, no accents, only letters and digits separated by single spaces */
final class NpcText {

	private NpcText() {
	}

	/** " words of the text " (with a space at both ends, so whole words can be searched with contains) */
	static String normalize(String text) {
		if (text == null) {
			return " ";
		}
		String plain = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		String clean = plain.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
		return clean.isEmpty() ? " " : " " + clean + " ";
	}

	/** Whole word (or phrase) of the text; a keyword ending with * matches any word that starts with it */
	static boolean hasKeyword(String normalized, String keyword) {
		if (keyword.endsWith("*")) {
			return normalized.contains(" " + keyword.substring(0, keyword.length() - 1));
		}
		return normalized.contains(" " + keyword + " ");
	}

	static boolean isEmpty(String normalized) {
		return normalized.trim().isEmpty();
	}

	static String cut(String text, int max) {
		return text.length() <= max ? text : text.substring(0, max - 3).trim() + "...";
	}
}
