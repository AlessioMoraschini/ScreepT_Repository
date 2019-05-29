package gUI.utils.tree.texteditor;

import java.awt.Color;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import amfileutils.FileStuff;
import frameutils.GuiUtils;
import gUI.panels.CrypterPanel;
import gUI.panels.KeyGenPanel;
import gUI.panels.TextEditor;
import gUI.utils.progressDialog.TestUpdateProgress;
import iNIT.INItializer;
import maingUI.mainGUI;
import media.ImageGeneralWorker;
import menusettings.MenuSetter;
import resources.IconsPathConfigurator;
import splitpanel.FileNamed;
import variousutils.HandleString;

public class TreeFileMouseListenerTxtEditor {
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(TreeFileMouseListenerTxtEditor.class); 
	
	// gui elements
	private JTree tree;
	private FileNamed selectedNode;
	private TextEditor textEditor;
	
	// constructor
	public TreeFileMouseListenerTxtEditor(JTree tree, TextEditor textEdit){
		this.tree = tree;
		this.tree.setComponentPopupMenu(getPopUpMenu());
		this.tree.addMouseListener(getMouseListener());
		textEditor = textEdit;
	}
	
	public MouseListener getMouseListener() {
	    return new MouseAdapter() {

	        @Override
	        public void mousePressed(MouseEvent arg0) {
	            if(arg0.getButton() == MouseEvent.BUTTON3){
	                TreePath pathForLocation = tree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
	                if(pathForLocation != null){
	                    selectedNode = (FileNamed) pathForLocation.getLastPathComponent();
	                } else{
	                    selectedNode = null;
	                }

	            }
	            super.mousePressed(arg0);
	        }
	    };
	}

	private JPopupMenu getPopUpMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem itemLoadFile = new JMenuItem("Open File");
	    itemLoadFile.addActionListener(getLoadActionListener());
	    menu.add(itemLoadFile);
	    
	    JMenuItem sysExplorerItem = new JMenuItem("View in system explorer");
	    sysExplorerItem.addActionListener(openInSystemExplorer());
	    menu.add(sysExplorerItem);
	       
	    // set menu icons
	    MenuSetter.setIconTextGap(menu, 12);
	    
	    sysExplorerItem.setIcon(new ImageIcon(IconsPathConfigurator.iconFolderPath));
	    itemLoadFile.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD));

	    return menu;
	}

	private ActionListener getLoadActionListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread starter = new Thread(new Runnable() {
					public void run() {
						textEditor.loadFile(selectedNode.mFile);
					}
				});
				starter.start();
				
			}
		};
	}
	
	private ActionListener openInSystemExplorer() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GuiUtils.openInFileSystem(selectedNode.mFile);
			}
		};
	}
	
}
