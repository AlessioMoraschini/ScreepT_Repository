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
package network.scanner.csd;

import java.util.StringTokenizer;

public class charAnimationPrinter {

    public charAnimationPrinter() {
    	
    }
    
    public void Print(String msg,int interval) {
    if(interval==0) {
    	System.out.println(msg);
    }else {
	StringTokenizer out_msg_tokenizer=new StringTokenizer(msg, "\n");
	try 
	{
		for(int q=0;q<msg.length();q++){
			String token=out_msg_tokenizer.nextToken();
			for(int r=0;r<token.length();r++) {
				String character=token.substring(r, r+1); 
				Thread.sleep(interval/4);  //aspetta 0,1 secondi
				System.out.printf(character);
				if(!character.equals(" ")) {
					Thread.sleep(interval); // aspetta 0,1 second1i
				}				    
			}
			System.out.printf("\n");
		}
    }catch (Exception e) {}
    }
    }
	
}
