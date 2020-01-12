package gui;

import java.io.File;

public class LicenseAgreementTest {

	public static void main(String args[]) {
		
		LightLicenseViewer.mandatoryChoice = false;
		LightLicenseViewer.getInstance(new File(""), "Here is the EULA (End User License Agreement)", "ScreepT - License (EULA)");

	}
}
