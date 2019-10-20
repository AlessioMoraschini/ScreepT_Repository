package netscanner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class provaTimeformat {

	public static void main(String[] args) {

		String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH_mm_ss");
        String time=sdf.format(cal.getTime());
        System.out.println("Time: "+time+"\n"+"Date: "+date);
        int i=0;
        Vector<String> prova=new Vector<String>();
        while (i<100){
        	prova.add(sdf.format(cal.getTimeInMillis()));
        	System.out.println("tempo "+prova.get(i)+" ------ "+i);
        	i++;
        }
	}

}
