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
package screept.launcher.utils;

/**
 * Ready-made JVM combinations for the launcher. A profile sets the initial heap, the thread stack and the garbage collector;
 * the maximum heap (-Xmx) is always the one chosen by the user, a profile never touches it.
 */
public final class JvmProfiles {

	/** Collector flags: "" means no flag, so the JVM chooses its own */
	public static final String GC_JVM_DEFAULT = "";
	public static final String GC_SERIAL = "-XX:+UseSerialGC";
	public static final String GC_PARALLEL = "-XX:+UseParallelGC";
	public static final String GC_G1 = "-XX:+UseG1GC";

	/** One combination: nameId is the i18n id of its name */
	public static final class Profile {
		public final String nameId;
		public final long initialMb;
		public final int stackMb;
		public final String gc;

		private Profile(String nameId, long initialMb, int stackMb, String gc) {
			this.nameId = nameId;
			this.initialMb = initialMb;
			this.stackMb = stackMb;
			this.gc = gc;
		}

		public boolean matches(long initialMbValue, int stackMbValue, String gcValue) {
			return initialMb == initialMbValue && stackMb == stackMbValue && gc.equals(gcValue);
		}
	}

	/** Less RAM: small heap at start, one collector thread (measured: about 1 GB less commit than Parallel at idle) */
	public static final Profile LIGHT = new Profile("launcher.profile_light", 128, 6, GC_SERIAL);
	/** Balanced: the default for new installs (G1: about half the memory of Parallel at idle, short pauses) */
	public static final Profile MEDIUM = new Profile("launcher.profile_medium", 128, 6, GC_G1);
	/** More RAM at start for fewer heap resizes */
	public static final Profile FAST = new Profile("launcher.profile_fast", 512, 6, GC_PARALLEL);

	public static final Profile[] ALL = { LIGHT, MEDIUM, FAST };

	private JvmProfiles() {
	}

	/** The profile whose values are exactly the given ones, or null when the combination is the user's own (custom) */
	public static Profile match(long initialMb, int stackMb, String gc) {
		for (Profile profile : ALL) {
			if (profile.matches(initialMb, stackMb, gc == null ? GC_JVM_DEFAULT : gc.trim())) {
				return profile;
			}
		}
		return null;
	}
}
