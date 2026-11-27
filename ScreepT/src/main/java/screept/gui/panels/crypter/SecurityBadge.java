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
package screept.gui.panels.crypter;

import javax.swing.JLabel;

import screept.crypter.engine.CryptoProfile;

/**
 * One line under the crypter controls that tells, in plain words, how the current choice protects the data and how
 * good the password is. It only looks at the length and character classes of the password (never stores it as text).
 */
public final class SecurityBadge extends JLabel {

	private static final long serialVersionUID = 1L;
	private static final String GREEN = "#6bd96b";
	private static final String AMBER = "#ffb347";
	private static final String RED = "#ff7a6b";
	private static final String GREY = "#a0a8b0";

	private String last = "";

	public SecurityBadge() {
		setFont(getFont().deriveFont(13f));
	}

	/**
	 * @param profile  null for the legacy modes
	 * @param keyImageMissing true if the selected protection needs a key image that has not been chosen yet
	 */
	public void refresh(CryptoProfile profile, char[] password, boolean keyImageMissing) {
		String text;
		if (profile == null) {
			text = line(AMBER, "Legacy mode", "no tampering detection and the password counts only up to 16 characters. Prefer a Secure mode.");
		} else {
			text = line(GREEN, "Protected", profile.describe() + ": wrong passwords and any change to the file are detected.");
		}
		text += "  " + passwordPart(password) + (keyImageMissing ? "  " + line(AMBER, "Key image", "choose it to continue.") : "");
		if (!text.equals(last)) {
			last = text;
			setText("<html>" + text + "</html>");
			setToolTipText("<html><body style='width:380px'>" + text + "</body></html>");
		}
	}

	private static String passwordPart(char[] password) {
		if (password == null || password.length == 0) {
			return line(GREY, "Password", "not entered yet.");
		}
		int bits = estimateBits(password);
		if (bits < 40) {
			return line(RED, "Password weak", "use a longer one, with different kinds of characters.");
		} else if (bits < 60) {
			return line(AMBER, "Password fair", "a few more characters would help.");
		} else if (bits < 80) {
			return line(GREEN, "Password good", "");
		}
		return line(GREEN, "Password strong", "");
	}

	private static String line(String color, String head, String tail) {
		return "<b style='color:" + color + "'>&#9679; " + head + "</b>" + (tail.isEmpty() ? "" : " <span style='color:" + GREY + "'>" + tail + "</span>");
	}

	/** Rough estimate: length times the size of the alphabet used, reduced for repeated characters. */
	static int estimateBits(char[] pw) {
		boolean lower = false;
		boolean upper = false;
		boolean digit = false;
		boolean other = false;
		java.util.Set<Character> distinct = new java.util.HashSet<Character>();
		for (char c : pw) {
			distinct.add(c);
			if (Character.isLowerCase(c)) {
				lower = true;
			} else if (Character.isUpperCase(c)) {
				upper = true;
			} else if (Character.isDigit(c)) {
				digit = true;
			} else {
				other = true;
			}
		}
		int pool = (lower ? 26 : 0) + (upper ? 26 : 0) + (digit ? 10 : 0) + (other ? 33 : 0);
		double perChar = Math.log(Math.max(pool, 2)) / Math.log(2);
		double effectiveLength = Math.min(pw.length, distinct.size() * 1.5);
		return (int) (effectiveLength * perChar * 0.8);
	}
}
