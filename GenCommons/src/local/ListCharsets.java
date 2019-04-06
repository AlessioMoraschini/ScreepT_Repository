package local;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.Vector;

import javax.swing.JComboBox;

/** this class is used to get charset names defined in current system
 * 
 * @author Alessio Moraschini
 *
 */
public class ListCharsets {
	
	private static SortedMap charsets = Charset.availableCharsets();
    private static Set names = charsets.keySet();
    private static Vector<String> nameList= new Vector<>();
    
    public static Vector<String>  getCharsets() {
    	for (Iterator e = names.iterator(); e.hasNext();) {
	      String name = (String) e.next();
	      nameList.add(name);
//	      Charset charset = (Charset) charsets.get(name);
//	      System.out.println(charset);
//	      Set aliases = charset.aliases();
//	      for (Iterator ee = aliases.iterator(); ee.hasNext();) {
//	        System.out.println("    " + ee.next());
//	      }
    	}
    	return nameList;
    	//JComboBox<String> combo = new JComboBox<String>(nameList);
    }
}
