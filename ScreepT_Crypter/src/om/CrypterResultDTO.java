package om;

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
