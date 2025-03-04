package screept.license.tools.extractor;

import licenses.info.extractor.AbstractLicenseExtractor;
import licenses.info.extractor.ScreepTLicensesInfoExtractor;
import licenses.info.extractor.ScreepTRawLicensesInfoExtractor;

public class LicenseExtractor {

	public static void main(String[] args) throws Exception {
		AbstractLicenseExtractor.logger.setLoggerAvailable(false);

		if(args[0].equals("-raw")) {
			ScreepTRawLicensesInfoExtractor.main(getSubArgs(args));

		} else if(args[0].equals("-generateHtml")){
			ScreepTLicensesInfoExtractor.main(getSubArgs(args));
		}
	}

	public static String[] getSubArgs(String[] args) {
		String[] newArgs = new String[args.length - 1];

		for(int i = 1; i < args.length; i++) {
			newArgs[i-1] = args[i];
		}

		return newArgs;
	}
}
