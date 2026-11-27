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
package screept.crypter.om;

public class CrypterResultDTO {
	
	public boolean resultOK;
	public boolean interrupted;
	
	public CrypterResultDTO() {
		resultOK = false;    
		interrupted = false; 
	}

	public CrypterResultDTO(boolean resultOK, boolean interrupted) {
		this.resultOK = resultOK;
		this.interrupted = interrupted;
	}

	
	public boolean isResultOK() {
		return resultOK;
	}

	public void setResultOK(boolean resultOK) {
		this.resultOK = resultOK;
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}


	@Override
	public String toString() {
		return "CrypterResultDTO [resultOK=" + resultOK + ", interrupted=" + interrupted + "]";
	}
}
