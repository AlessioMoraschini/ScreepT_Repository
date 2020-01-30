package speech;

import classes.Text2Speech_check;

public class TextToSpeechTest {

	public static void main(String[] args) {

		// way 1
	    Text2Speech_check obj=new Text2Speech_check(); obj.dospeak("Hello i am kevin ","kevin16");    

	    // way 2
//	    Text2Speech_v2 su = new Text2Speech_v2();  
//	    try {
//			su.init("kevin16");
//			su.doSpeak("Ciao sono alessio!");  
//		    su.terminate();
//		} catch (EngineException | AudioException | EngineStateError | PropertyVetoException | IllegalArgumentException | InterruptedException e) {
//			e.printStackTrace();
//		}  
	     
	}

}
