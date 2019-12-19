package junit.netscanner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import network.scanner.dmad.CapMerger;

public class Prova_copia_cap {

	public static void main(String[] args) {

		String in1="Files_FTP/Local_NOfilter-192.168.1.9-2018_03_10.02_14.cap";
		String in2="Files_FTP/NOfilter--2018_03_10_00_25_30.cap";
		String out="Export/Merge.cap";
		CapMerger.MergeToOUT(in1, out,false);
		CapMerger.MergeToOUT(in2, out,true);
		File IN1=new File(in1);
		File IN2=new File(in2);
		File OUT=new File(out);		
		String date = new SimpleDateFormat("yyyy_MM_dd.HH.mm.ss").format(new Date());
		
		System.out.println(date+"Lenght1: "+IN1.length()+" - Lenght2: "+IN2.length()+" - Lenght output: "+OUT.length());
		
		
		
		
	}

}
