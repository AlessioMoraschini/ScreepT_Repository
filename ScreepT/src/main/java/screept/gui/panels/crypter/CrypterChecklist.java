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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import screept.crypter.engine.CryptoProfile;
import screept.crypter.engine.FileCrypter;
import screept.crypter.engine.StegoPicture;
import screept.crypter.impl.manager.CryptoMode;

/**
 * The two "what is needed" lines of the Crypter: what Crypt and Decrypt need right now, with the selected mode and
 * files. For decryption the headers of the selected files are read (no password needed), so the user is told exactly
 * when a key image or the private key is needed, and by which file.
 */
public final class CrypterChecklist {

	static final String OK = "#6bd96b", MISSING = "#ffb347", OFF = "#8a96a3";
	private static final int MAX_INSPECTED = 50;

	/** Everything the lines depend on. */
	public static final class State {
		public CryptoMode mode;
		/** Profile of the mode (Custom resolved), null for Legacy. */
		public CryptoProfile profile;
		public File[] sources = new File[0];
		public boolean passwordEntered;
		public File keyImage;
		public boolean hideInPicture;
		public File picture;
		/** Capacity of the picture in bytes, -1 if unknown. */
		public long pictureCapacity = -1;
		public boolean haveKeys;
		public int people;
	}

	/** Header inspections, by path + size + date (the lines are refreshed several times per second). */
	private final Map<String, CryptoProfile> cache = new LinkedHashMap<String, CryptoProfile>(16, 0.75f, true) {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, CryptoProfile> e) {
			return size() > 200;
		}
	};
	private static final CryptoProfile NOT_ENGINE = CryptoProfile.standard();

	public String encryptLine(State s) {
		List<String> parts = new ArrayList<String>();
		if (s.mode != null && s.profile == null) {
			parts.add(item(MISSING, "Legacy modes only decrypt old files: choose a Secure mode to encrypt"));
			return row("CRYPT", parts);
		}
		parts.add(files(s));
		boolean publicKeys = s.profile != null && !s.profile.kdf.passwordBased();
		if (publicKeys) {
			parts.add(s.haveKeys ? item(OK, "for you + " + s.people + (s.people == 1 ? " person" : " people") + " (Keys...)")
					: item(MISSING, "create your keys first (Keys...)"));
			parts.add(item(OFF, "password not needed"));
		} else {
			parts.add(s.passwordEntered ? item(OK, "password") : item(MISSING, "write a password"));
		}
		if (s.profile != null && s.profile.needsKeyImage()) {
			parts.add(s.keyImage != null ? item(OK, "key image " + s.keyImage.getName()) : item(MISSING, "choose the key image"));
		} else {
			parts.add(item(OFF, "key image not needed"));
		}
		if (s.hideInPicture) {
			if (publicKeys) {
				parts.add(item(MISSING, "public key files cannot be hidden in a picture"));
			} else if (s.picture == null) {
				parts.add(item(MISSING, "choose the picture (Picture...)"));
			} else {
				parts.add(item(OK, "hidden in " + s.picture.getName() + (s.pictureCapacity >= 0 ? " (holds " + kb(s.pictureCapacity) + ")" : "")));
			}
		}
		return row("CRYPT", parts);
	}

	public String decryptLine(State s) {
		List<String> parts = new ArrayList<String>();
		parts.add(files(s));
		boolean needPassword = false, needPrivateKey = false, legacy = false, picture = false;
		String keyImageFor = null;
		int n = 0;
		for (File f : s.sources) {
			if (f == null || !f.isFile() || n++ >= MAX_INSPECTED) {
				needPassword |= f != null && f.isDirectory(); // folders: read file by file at decryption time
				continue;
			}
			CryptoProfile p = inspect(f);
			if (p != null) {
				if (p.kdf.passwordBased()) {
					needPassword = true;
				} else {
					needPrivateKey = true;
				}
				if (p.needsKeyImage() && keyImageFor == null) {
					keyImageFor = f.getName();
				}
			} else if (FileCrypter.isPicture(f) && s.profile != null) {
				picture = true;
				needPassword = true;
			} else {
				legacy = true;
				needPassword |= s.mode == null || s.mode.isAES();
				if (s.mode != null && s.mode.isSTEG() && keyImageFor == null) {
					keyImageFor = f.getName();
				}
			}
		}
		if (needPrivateKey) {
			parts.add(!s.haveKeys ? item(MISSING, "your private key is needed: create or restore it (Keys...)")
					: s.passwordEntered ? item(OK, "password of your private key") : item(MISSING, "write the password of your private key"));
		}
		if (needPassword || s.sources.length == 0) {
			parts.add(s.passwordEntered ? item(OK, "password") : item(MISSING, "write the password"));
		}
		if (keyImageFor != null) {
			parts.add(s.keyImage != null ? item(OK, "key image " + s.keyImage.getName()) : item(MISSING, "key image needed by " + keyImageFor));
		} else if (s.sources.length > 0) {
			parts.add(item(OFF, "key image not needed"));
		}
		if (picture) {
			parts.add(item(OK, "pictures: hidden data looked for with the password"));
		}
		if (legacy && s.profile != null) {
			parts.add(item(MISSING, "some files are not Secure files: if they are old ones, select their Legacy mode"));
		}
		return row("DECRYPT", parts);
	}

	private static String files(State s) {
		int n = s.sources.length;
		return n == 0 ? item(MISSING, "add files or folders") : item(OK, n + (n == 1 ? " item" : " items"));
	}

	private CryptoProfile inspect(File f) {
		String key = f.getAbsolutePath() + '|' + f.length() + '|' + f.lastModified();
		CryptoProfile p = cache.get(key);
		if (p == null) {
			p = FileCrypter.inspect(f);
			cache.put(key, p == null ? NOT_ENGINE : p);
			return p;
		}
		return p == NOT_ENGINE ? null : p;
	}

	public static long capacity(int width, int height) {
		return StegoPicture.capacity(width, height);
	}

	private static String kb(long bytes) {
		return bytes >= 1 << 20 ? String.format("%.1f MB", bytes / 1048576.0) : (bytes / 1024) + " KB";
	}

	private static String item(String color, String text) {
		String mark = OK.equals(color) ? "✓ " : MISSING.equals(color) ? "✗ " : "";
		return "<font color='" + color + "'>" + mark + text.replace("&", "&amp;").replace("<", "&lt;") + "</font>";
	}

	private static String row(String title, List<String> parts) {
		StringBuilder sb = new StringBuilder("<b>").append(title).append("</b>&nbsp;&nbsp;");
		for (int i = 0; i < parts.size(); i++) {
			sb.append(i == 0 ? "" : "&nbsp;&nbsp;&middot;&nbsp;&nbsp;").append(parts.get(i));
		}
		return sb.toString();
	}
}
