package reference_or_value;

import classes.Prova_val_ref;

public class Val_Ref_Test_Main {

	public static void main(String[] args) {

		Prova_val_ref uno = new Prova_val_ref(1,1);
		Prova_val_ref due = new Prova_val_ref(2,2);
		
		System.out.println(uno.toString()+due.toString());
		uno=due;
		System.out.println("now assing uno = due ->\n" + "Uno_" + uno.toString()+ "Due_" + due.toString());
		uno.increment(2);
		System.out.println("now uno increment by two (gives uno=[4,4])->\n" + "Uno_" + uno.toString()+ "Due_" +due.toString());
		uno=new Prova_val_ref(due);
		System.out.println("now use copy contructor uno=new prova_val_ref(due) : gives (4,4) each ->\n" + "Uno_" + uno.toString()+ "Due_" +due.toString());
		uno.increment(1);
		System.out.println("\"now uno increment by two (gives uno=[5,5], due=[4,4] this time!)->\n" + "Uno_" + uno.toString()+ "Due_" +due.toString());
		uno=new Prova_val_ref(due);
		System.out.println("now use copy Object function: uno=2.Clone() : gives (4,4) each) ->\n" + "Uno_" + uno.toString()+ "Due_" +due.toString());
		uno.increment(1);
		System.out.println("\"now uno increment by two (gives uno=[5,5], due=[4,4] again!)->\n" + "Uno_" + uno.toString()+ "Due_" +due.toString());

	}

}
