package frameutils.html;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
  
/** 
 * SwingFXWebView 
 */  
public class JavaFxPanelHtml extends JPanel {  
     
	private int W = 1200;
	private int H = 700;
	
    private Stage stage;  
    private WebView browser;  
    private JFXPanel jfxPanel;  
    private WebEngine webEngine;  
  
    public JavaFxPanelHtml(){  
    	jfxPanel = new JFXPanel();  
    	setBorder(BorderFactory.createEmptyBorder());
        setLayout(new MigLayout("", "[1000px,grow]", "[1000px,grow]"));
        add(jfxPanel, "cell 0 0,grow");
        
    }
    
    public void resizeComp(int width, int height) {
    	W = width;
    	H = height;
    	
    	try {
			browser.setPrefWidth(W);
			browser.setPrefHeight(H);
			stage.setMaxWidth(W);
			stage.setHeight(H);
		} catch (NullPointerException e) {
			
		}
    }
     
    public void loadUrl(String url) {
    	PlatformImpl.startup(new Runnable() {  
            @Override
            public void run() {  
                
                stage = new Stage();  
                 
                stage.setTitle("Hello Java FX");  
                stage.setResizable(true);  
   
                Group root = new Group();  
                Scene scene = new Scene(root,1400,1400);  
                stage.setScene(scene);  
                 
                // Set up the embedded browser:
                browser = new WebView();
                webEngine = browser.getEngine();
                webEngine.load(url);

                resizeComp(W, H);
                
                ObservableList<Node> children = root.getChildren();
                children.add(browser);
                jfxPanel.setScene(scene);  
            }  
        });  
    }
    
    public void reload() {
    	Platform.runLater(new Runnable() {

            @Override
            public void run() {
                webEngine.reload();
            }
        });
    }
}