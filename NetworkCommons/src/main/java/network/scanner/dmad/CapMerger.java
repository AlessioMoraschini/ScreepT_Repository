/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package network.scanner.dmad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class CapMerger {
public static final long globheader=24; //numero di byte del globalheader del .cap inputhPath1


	public CapMerger() {
		
	}
	
	//funziona per globalheader .cap di 24 byte
	//ad ogni nuovo file aggiunto vengono rimossi questi byte per non corrompere il file .cap
	// consultare "https://wiki.wireshark.org/Development/LibpcapFileFormat" per informazioni
	
	//se append � settato a false creail file o lo sovrascrive,
	//altrimenti appende il cap contenuto in inputpath uno al cap presente in outputhpath
	public static void MergeToOUT(String inputPath1, String OutputPath, boolean append) {
		
		File in1=new File(inputPath1);
		File out=new File(OutputPath);
		FileOutputStream fOut;
		FileInputStream fin1;
		
		try {
			fin1 = new FileInputStream(in1);
			if (append) {
				fin1.getChannel().position(globheader);//scarta i primi 24 byte(header)
			}
			
	    	fOut = new FileOutputStream(out,append);
	    	FileChannel sourceChannel1 = fin1.getChannel();
	    	FileChannel destChannel = fOut.getChannel();
	        destChannel.transferFrom(sourceChannel1, destChannel.size(), sourceChannel1.size());
	        sourceChannel1.close();
	        destChannel.close();
	        fin1.close();
	        fOut.close();
		}catch(Exception e) {
			
		}
	}
	
}
