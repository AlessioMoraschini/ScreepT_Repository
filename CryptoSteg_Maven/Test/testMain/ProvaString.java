package testMain;

import uTILS.local.HandleString;

public class ProvaString {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		String input = "<          ><come se Og.gi non     po.>tessi ma<i> andare AUhUD. casa e �  no non sono io ";
//		String output = HandleString.upperAfterDots(input, '.');
//		Boolean.getBoolean("false");
//		System.out.println(Boolean.parseBoolean("True"));
//		System.out.println(output);
//		System.out.println("Directory IMG passed without slash, exists? -> "+new File("IMG").getAbsolutePath());
		
		String input= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><books><book> <title>Basic XML</title><price>100</price><qty>5</qty></book><book><title>Basic Java</title><price>200</price><qty>15</qty></book></books>\r\n";
		String formattedOut = HandleString.xmlPrettyFormat(input, 3);
		System.out.println(formattedOut);
		System.out.println(HandleString.injectStringInSelection(input, "SONO ALESSIO", 1, 5));
		
	}

}
