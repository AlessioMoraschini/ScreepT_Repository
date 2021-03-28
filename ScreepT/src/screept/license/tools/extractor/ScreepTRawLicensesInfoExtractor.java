package screept.license.tools.extractor;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import licenses.info.extractor.LicensesInfoExtractor;
import various.common.light.files.CustomFileFilters;
import various.common.light.files.FileWorker;

public class ScreepTRawLicensesInfoExtractor {

	private static String screeptLicenseFileName = "license_list_screept.xml";

	private static String completeXml = "./UTILS_BUILD/complete_license_list.xml";
	private static String licensesBuildsSrc = "./licenses_builds/";
	private static String rawXmlFolderSrc = licensesBuildsSrc + "raw_licenses_list/";
	private static String rawXmlFolderOut = rawXmlFolderSrc + "generated/";

	public static void initPaths(File root) {
		completeXml = root.getAbsolutePath() + "UTILS_BUILD/complete_license_list.xml";
		licensesBuildsSrc = root.getAbsolutePath() + "licenses_builds/";
		rawXmlFolderSrc = licensesBuildsSrc + "raw_licenses_list/";
		rawXmlFolderOut = rawXmlFolderSrc + "generated/";

		System.out.println("Paths generated from root [" + root.getAbsolutePath() + "]:");
		System.out.println("completeXml: " + completeXml);
		System.out.println("licensesBuildsSrc: " + licensesBuildsSrc);
		System.out.println("rawXmlFolderSrc: " + rawXmlFolderSrc);
		System.out.println("rawXmlFolderOut: " + rawXmlFolderOut);
	}

	public static void main(String[] args) throws Exception {

		System.out.println("ScreepTRawLicensesInfoExtractor.main() -> ARGS: " + Arrays.toString(args));

		File rootPrj = args != null && args.length > 1 ? new File(args[1]) : null;
		if(rootPrj != null && rootPrj.exists() && rootPrj.isDirectory()) {
			initPaths(rootPrj);
		}

		File outDir = new File(rawXmlFolderOut);
		outDir.mkdirs();
		FileWorker.deleteDirContentRecursive(outDir);

		File rawXmlFolder = new File(rawXmlFolderSrc);
		File[] definitions = rawXmlFolder.listFiles(CustomFileFilters.customTypeFilter(CustomFileFilters.FILE_ONLY));
		for(File definition : definitions) {
			String outputFile = rawXmlFolderOut + definition.getName();
			extractFromCompleteList(definition.getAbsolutePath(), outputFile, completeXml);
		}

		// Now copy also to licenses build directory if specified through params
		if(Boolean.valueOf(args[0])) {
			for (File generated : new File(rawXmlFolderOut).listFiles()) {
				if(!screeptLicenseFileName.equals(generated.getName())) {
					FileUtils.copyFileToDirectory(generated, new File(licensesBuildsSrc + "plugins"));
					System.out.println(generated + " -> copied to -> " + licensesBuildsSrc + "plugins");
				} else {
					FileUtils.copyFileToDirectory(generated, new File(licensesBuildsSrc));
					System.out.println(generated + " -> copied to -> " + licensesBuildsSrc);
				}
			}
		}
	}

	private static void extractFromCompleteList(String sourceFile, String outFile, String completeListFile) throws Exception {
		System.out.print(
				LicensesInfoExtractor.syncFromCompleteList(
						new File(sourceFile), new File(outFile), new File(completeListFile)));

	}
}
