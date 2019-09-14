package guiTEST.dragNdrop;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import gUI.utils.tree.crypter.dragndrop.DragNDropVirtualCellRenderer;
import gUI.utils.tree.crypter.dragndrop.VirtualFileDropPane;
import gUI.utils.tree.crypter.om.CrypterDragFileModel;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

public class TestDragNDropFiles {
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		new TestDragNDropFiles();
    }
	
	public TestDragNDropFiles() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                JFrame frame = new JFrame("Testing Drag N' Drop files");
                frame.getContentPane().setBackground(Color.LIGHT_GRAY);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+GeneralConfig.DEBUG_GRAPHICS+"", "[600px,grow]", "[200px,grow][][200px,grow][150px,grow]"));
                
                JTree tree = new JTree();
                VirtualFileDropPane dropPane = new VirtualFileDropPane(tree, frame); // link drop pane to update tree

                frame.getContentPane().add(dropPane, "cell 0 2,growx,aligny top");
                frame.getContentPane().add(tree, "cell 0 0,grow");
                
                JButton testContent = new JButton("Extract content files");
                testContent.setFont(new Font("SansSerif", Font.PLAIN, 15));
                testContent.setBackground(Color.LIGHT_GRAY);
                frame.getContentPane().add(testContent, "cell 0 1,growx");
               
                JTextArea textArea = new JTextArea();
                textArea.setBorder(new BevelBorder(BevelBorder.LOWERED, new Color(0, 0, 51), null, null, null));
                textArea.setBackground(new Color(255, 255, 204));
                frame.getContentPane().add(textArea, "cell 0 3,grow");
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                
                testContent.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						textArea.setText("");
						for(File nodeFile : dropPane.getTreeNodes()) {
							textArea.append("\n"+nodeFile.getAbsolutePath());
						}
					}
				});
            }
        });
    }
}
