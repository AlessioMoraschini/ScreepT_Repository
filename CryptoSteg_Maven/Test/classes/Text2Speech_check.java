package classes;
import java.util.Locale;

import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;

import com.sun.speech.freetts.*;
//import javax.speech.*;
//import javax.speech.synthesis.Synthesizer;
//import javax.speech.synthesis.SynthesizerModeDesc;
//import javax.speech.synthesis.Voice; 

public class Text2Speech_check {
	String speaktext; 
	public void dospeak(String speak,String  voicename)    
	{    
	    speaktext=speak;    
	    String voiceName =voicename;    
	    try    
	    {    
	    	System.setProperty("freetts.voices",  
	    	        "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");  
//	        SynthesizerModeDesc desc = new SynthesizerModeDesc(null,"general",  Locale.US,null,null);    
//	        Synthesizer synthesizer =  Central.createSynthesizer(desc);    
	        //synthesizer.allocate();    
	        //synthesizer.resume();     
	        //desc = (SynthesizerModeDesc)  synthesizer.getEngineModeDesc();  
	        VoiceManager managerVoice = VoiceManager.getInstance();
	        Voice[] voices = managerVoice.getVoices();      
	        Voice voice = null;
	        for (int i = 0; i < voices.length; i++)    
	        {    
	        	System.out.println(voices[i].getDescription());
	            if (voices[i].getName().equals(voiceName))    
	            {    
	                voice = voices[i];  
	                voice.setDurationStretch(3);
	                break;     
	            }     
	        }
	        // default voice
	        if(voice == null) {
	        	voice = managerVoice.getVoice("Kevin");
	        }
//	        synthesizer.getSynthesizerProperties().setVoice(voice);    
//	        System.out.print("Speaking : "+speaktext);    
//	        synthesizer.speakPlainText(speaktext, null);    
//	        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);    
//	        synthesizer.deallocate();    
	        voice.allocate();
	        voice.speak(speaktext);
	        voice.deallocate();
	    }    
	    catch (Exception e)   
	    {    
	        String message = " missing speech.properties in " + System.getProperty("user.home") + "\n";    
	        System.out.println(""+e);    
	        System.out.println(message);    
	    } 
    } 
}
