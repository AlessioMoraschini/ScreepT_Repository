package gui.filesearch.resultview;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import frameutils.frame.arch.ParentFrame;
import gui.filesearch.FileSearchActions;
import various.common.light.files.om.FileContentSearchDTO;
import various.common.light.files.om.SearchResult;
import various.common.light.utility.string.StringWorker;

import java.awt.GridBagLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;

public class FileSearchResultViewer extends ParentFrame {
	private static final long serialVersionUID = 6124630872670342725L;
	
	public static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd - hh:mm:ss");

	public FileSearchActions searchActions;
	private JScrollPane scrollPane;
	public JTree resultsTree;
	
	public FileContentSearchDTO searchDTO;
	public int sourceFilesTotal;
	
	private String date;
	
	public void setTitle(String preamble, boolean refreshDate, boolean shortTitle) {
		if(date == null || refreshDate)
			date = DATE_FORMATTER.format(new Date());
		
		setTitle(StringWorker.trimToEmpty(preamble) + (shortTitle ? "" : "File Search Results Viewer - ") + date);
	}
	
	public FileSearchResultViewer(FileSearchActions searchActions, JFrame parentFrame, FileContentSearchDTO searchDTO) {
		super();
		
		this.sourceFilesTotal = 0;
		this.searchDTO = searchDTO;
		setTitle("", true, false);
		setTitle("File Search Results Viewer - " + DATE_FORMATTER.format(new Date()));
		
		this.searchActions = searchActions != null ? searchActions : new FileSearchActions() {};
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		scrollPane = new JScrollPane();
		scrollPane.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		initTree();
		scrollPane.setViewportView(resultsTree);
		
		addHandlers();
		
		resizeToDefault(true, true, false);
		setLocationRelativeTo(parentFrame);
		setVisible(true);
		setAlwaysOnTop(true);
	}
	
	public void addHandlers() {
		
		// DEFAULT CLOSE ACTIONS 
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
		    }
		});
	}
	
	public void addResult(SearchResult resultToAdd) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)resultsTree.getModel().getRoot();
		ResultViewDTO fileRoot = new ResultViewDTO(0, resultToAdd, true);
		fileRoot.sourceFilesTotal = sourceFilesTotal;
		DefaultMutableTreeNode newFileNode = new DefaultMutableTreeNode(fileRoot);
		
		root.add(newFileNode);
		
		Iterator<Integer> lineIterator = resultToAdd.resultLines.keySet().iterator();
		while (lineIterator.hasNext()) {
			Integer line = lineIterator.next();
			ResultViewDTO fileLineResult = new ResultViewDTO(line, resultToAdd);
			DefaultMutableTreeNode lineResultNode = new DefaultMutableTreeNode(fileLineResult);
			newFileNode.add(lineResultNode);
		}
		
		resultsTree.updateUI();
	}
	
	private void initTree() {
		resultsTree = new JTree();
		ResultViewDTO rootDto = new ResultViewDTO(true);
		rootDto.result.searchString = searchDTO != null ? searchDTO.searchString : "";
		rootDto.searchDTO = searchDTO;
		TreeNode rootNode = new DefaultMutableTreeNode(rootDto);
		resultsTree.setModel(new DefaultTreeModel(rootNode));
		resultsTree.setCellRenderer(new FileSearchResultsTreeCellRenderer(resultsTree));
		new FileSearchTreeMouseListener(resultsTree, searchActions);
		resultsTree.setBackground(Color.DARK_GRAY);
		resultsTree.updateUI();
		ToolTipManager.sharedInstance().registerComponent(resultsTree);
	}
	
	public ResultViewDTO getRootObj() {
		DefaultMutableTreeNode mutableTreeRoot = (DefaultMutableTreeNode) resultsTree.getModel().getRoot();
		return (ResultViewDTO) mutableTreeRoot.getUserObject();
	}
	
	public void removeResults() {
		initTree();
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(900, 550);
	}


	/**
	 * Object model used for single tree element
	 */
	public class ResultViewDTO {
		public SearchResult result;
		public int line;
		public boolean root;
		public boolean fileHead;
		public int occurrences = 0;
		public int sourceFilesTotal;
		public FileContentSearchDTO searchDTO;
		
		public ResultViewDTO(int line, SearchResult result) {
			this.result = result;
			this.line = line;
			this.root = false;
			this.fileHead = false;
			this.sourceFilesTotal = 0;
		}

		public ResultViewDTO(boolean root) {
			this(0, new SearchResult(null));
			this.root = root;
			this.sourceFilesTotal = 0;
		}

		public ResultViewDTO(int line, SearchResult result, boolean fileHead) {
			this(line, result);
			this.fileHead = fileHead;
			this.sourceFilesTotal = 0;
		}
		
		public String getSearchDTOString() {
			if(searchDTO != null) {
				return searchDTO.toStringLight();
			}
			
			return "";
		}

		@Override
		public String toString() {
			occurrences = result.getOccurrencesFound();
			String viewedString = root
					? "Search Results: " + getSearchDTOString()
					: fileHead 
						? result.ownerFile.getAbsolutePath() + " - Occurrences: " + occurrences + getCheckedOutRatioString()
						: "(Line: " + line + ") - \"" + result.getLineMatchText(line) + "\"";
			return viewedString;
		}
		
		public String getCheckedOutRatioString() {
			String files = result.fileScannedTilNow > 1 ? " files " : " file ";
			return "  -  (Checked " + result.fileScannedTilNow + files + "out of " + sourceFilesTotal + ")";
		}
		
	}
}
