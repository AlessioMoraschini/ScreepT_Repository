package amfileutils.om;

import java.util.ArrayList;

public class DeleteFilesByExtensionDTO {
	private boolean esit;
	private boolean nothingFound;
	private ArrayList<String> notFoundExtensionResult;
	
	public DeleteFilesByExtensionDTO() {
		esit = false;
		notFoundExtensionResult = new ArrayList<String>();
		nothingFound = true;
	}

	
	public boolean isNothingFound() {
		return nothingFound;
	}

	public void setNothingFound(boolean nothingFound) {
		this.nothingFound = nothingFound;
	}

	public boolean isEsit() {
		return esit;
	}

	public void setEsit(boolean esit) {
		this.esit = esit;
	}

	public ArrayList<String> getNotFoundExtensionResult() {
		return notFoundExtensionResult;
	}

	public void setNotFoundExtensionResult(ArrayList<String> notFoundExtensionResult) {
		this.notFoundExtensionResult = notFoundExtensionResult;
	}
}
