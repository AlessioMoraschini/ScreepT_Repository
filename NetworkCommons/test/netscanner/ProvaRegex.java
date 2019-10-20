package netscanner;

import java.util.regex.Pattern;

public class ProvaRegex {

	public static void main(String[] args) {

		String Fcontrol=".*mare.*";
		String payload=new String("il mare � blu 12 volte ciao user pass PASS91");
		if (Pattern.matches(Fcontrol, payload)) 
		{
			System.out.println("Corrispondenza trovata!");
			
		}	
		else {
			System.out.println("niente da fare, non c'�!");
		}
		
	}

}
