package gUI.utils.tree.crypter.dragndrop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

import gUI.panels.KeyDictionaryPanel;
import gUI.utils.tree.crypter.om.CrypterDragFileModel;

public class VirtualFileDropPane extends JPanel {

	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(KeyDictionaryPanel.class); 
    
	public static final String DEFAULT_ROOT = "Source Files and Folders";
	
	private DropTarget dropTarget;
    private DropTargetHandler dropTargetHandler;
    private Point dragPoint;

    private boolean dragOver = false;

    // public fields
    public JTree filesTree;
    public JLabel message;
    private Component targetDropComponent;

    // constructor
    public VirtualFileDropPane(JTree targetTree, Component targetDrop) {
    	
    	targetDropComponent = (targetDrop == null)? this : targetDrop;
    	
    	filesTree = targetTree;
    	File sourceRootFile = new File(DEFAULT_ROOT);
        CrypterDragFileModel fileModelRoot = new CrypterDragFileModel(sourceRootFile);
        TreeNode root = new DefaultMutableTreeNode(fileModelRoot);
        if (filesTree != null) {
        	// if jtree initialized setModel with standard root
			filesTree.setModel(new DefaultTreeModel(root));
		}else {
			// if not initialized yet create new
			filesTree = new JTree(root);
		}
        filesTree.setCellRenderer(new DragNDropVirtualCellRenderer());
		new TreeDragNDropListenerCrypter(filesTree); // pop-up menu listener
        
        setLayout(new GridBagLayout());
        message = new JLabel("Drag here your files to code/decode them");
        message.setFont(message.getFont().deriveFont(java.awt.Font.BOLD, 18));
        add(message);

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 400);
    }

    protected DropTarget getMyDropTarget() {
        if (dropTarget == null) {
            dropTarget = new DropTarget(targetDropComponent, DnDConstants.ACTION_COPY_OR_MOVE, null);
        }
        return dropTarget;
    }

    protected DropTargetHandler getDropTargetHandler() {
        if (dropTargetHandler == null) {
            dropTargetHandler = new DropTargetHandler();
        }
        return dropTargetHandler;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        try {
            getMyDropTarget().addDropTargetListener(getDropTargetHandler());
        } catch (TooManyListenersException ex) {
            logger.error(ex);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        getMyDropTarget().removeDropTargetListener(getDropTargetHandler());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dragOver) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(0, 255, 0, 64));
            g2d.fill(new Rectangle(getWidth(), getHeight()));
            g2d.dispose();
        }
    }

    // here there is action to update given JTree
    protected void importFiles(final List<File> files) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
            	// Insert new nodes as children of root node
            	DefaultMutableTreeNode root = (DefaultMutableTreeNode)filesTree.getModel().getRoot();
            	for(File iFile : files) {
            		CrypterDragFileModel currentNodeModel = new CrypterDragFileModel(iFile);
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(currentNodeModel);
	            	root.add(newNode);
	            	filesTree.updateUI();
            	}
            	filesTree.setModel(new DefaultTreeModel(root));
            	((DefaultTreeModel)filesTree.getModel()).reload();
                message.setText("You dropped " + files.size() + " files");
            }
        };
        SwingUtilities.invokeLater(run);
    }
    
    public void addFileToTree(File fileToAdd) {
    	DefaultMutableTreeNode root = (DefaultMutableTreeNode)filesTree.getModel().getRoot();
		CrypterDragFileModel currentNodeModel = new CrypterDragFileModel(fileToAdd);
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(currentNodeModel);
    	root.add(newNode);
    	filesTree.updateUI();
    	filesTree.setModel(new DefaultTreeModel(root));
    	((DefaultTreeModel)filesTree.getModel()).reload();
        message.setText("You dropped a new file!");
    }
    
    protected class DropTargetHandler implements DropTargetListener {

        protected void processDrag(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            processDrag(dtde);
            SwingUtilities.invokeLater(new DragUpdate(true, dtde.getLocation()));
            repaint();
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            processDrag(dtde);
            SwingUtilities.invokeLater(new DragUpdate(true, dtde.getLocation()));
            repaint();
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            SwingUtilities.invokeLater(new DragUpdate(false, null));
            repaint();
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {

            SwingUtilities.invokeLater(new DragUpdate(false, null));

            Transferable transferable = dtde.getTransferable();
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(dtde.getDropAction());
                try {

                    List transferData = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if (transferData != null && transferData.size() > 0) {
                        importFiles(transferData);
                        dtde.dropComplete(true);
                    }

                } catch (Exception ex) {
                    logger.error(ex);
                }
            } else {
                dtde.rejectDrop();
            }
        }
    }

    public class DragUpdate implements Runnable {

        private boolean dragOver;
        private Point dragPoint;

        public DragUpdate(boolean dragOver, Point dragPoint) {
            this.dragOver = dragOver;
            this.dragPoint = dragPoint;
        }

        @Override
        public void run() {
            VirtualFileDropPane.this.dragOver = dragOver;
            VirtualFileDropPane.this.dragPoint = dragPoint;
            VirtualFileDropPane.this.repaint();
        }
    }
    
/////////////// USEFUL PUBLIC METHODS ////////////////
    
    public File[] getTreeNodes() {
		TreeNode root = (TreeNode)filesTree.getModel().getRoot();
		int Nfiles = filesTree.getModel().getChildCount(root);
		File[] nodeArray= new File[Nfiles];
				
		for(int i=0; i<Nfiles; i++) {
			DefaultMutableTreeNode currentModelNode = (DefaultMutableTreeNode)filesTree.getModel().getChild(root, i);
			nodeArray[i] = ((CrypterDragFileModel)currentModelNode.getUserObject()).realFile;
		}
		return nodeArray;
	}

    public int getNumFirstLevelNodes() {
    	TreeNode root = (TreeNode)filesTree.getModel().getRoot();
		return filesTree.getModel().getChildCount(root);
    }
}
