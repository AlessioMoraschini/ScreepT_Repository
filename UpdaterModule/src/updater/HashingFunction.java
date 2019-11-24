package updater;

public enum HashingFunction {MD5("MD5"),
							 SHA1("SHA-1"),
							 SHA256("SHA-256"),
							 SHA384("SHA-384"),
							 SHA512("SHA-512");

	String algorithmCode;
	
	HashingFunction(String algorithm) {
		this.algorithmCode = algorithm;
	}
}
