package variousutils;

public enum EscapeType {HTML_3,
						HTML_4,
						ECMASCRIPT,
						JSON,
						JAVA,
						XML_10,
						XML_11;

	private EscapeType() {
	}
	
	public static String[] getTypesString() {
		String[] stringArray = new String[EscapeType.values().length];
		int i = 0;
		for(EscapeType  currType : EscapeType.values()) {
			stringArray[i] = currType.name();
			i++;
		}
		
		return stringArray;
	}
}
