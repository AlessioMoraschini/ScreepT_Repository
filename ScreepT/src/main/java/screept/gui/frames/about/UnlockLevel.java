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

/** How much of the vault of easter eggs a sentence opens */
public enum UnlockLevel {
	/** Nothing: just chatting */
	NONE,
	/** The sentence names some eggs: those can be launched ("mini powers"), the others stay locked */
	PARTIAL,
	/** The master key ("i love screept"): every egg ("super powers") */
	FULL
}
