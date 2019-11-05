package om;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import frameutils.utils.GuiUtils;
import resources.GeneralConfig;

public class ColumnTextArea extends RSyntaxTextArea{
	private static final long serialVersionUID = -2828990655552935752L;
	
	// ColumnTextArea logger creation
	static Logger logger = Logger.getLogger(ColumnTextArea.class);

	public final static int DEF_CARET_BLINK_RATE = GeneralConfig.DEF_CARET_BLINK_RATE;
	
	public AtomicBoolean isRectangularModeOn = new AtomicBoolean(true);
	RSyntaxTextArea currentRef;
	
	public int lastStartPosition = 0;
	
	public ColumnTextArea(boolean rectangularSelectionActive) {
		super();
		currentRef = this;
		setRectangularSelectionMode(rectangularSelectionActive);
		
        //Disable ALT key presses when the textarea is focused
        this.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ALT, 0, true), "ALTKEY");
        this.getActionMap().put("ALTKEY", new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
//                System.out.println("Performed");
            }
        });
        
        this.getCaret().setBlinkRate(DEF_CARET_BLINK_RATE);
	}
	
	public ColumnTextArea(int rows, int columns){
        super(rows, columns);
        currentRef = this;
        this.setCaret(new ColumnTextArea.ColumnSelectionCaret());

        //Disable ALT key presses when the textarea is focused
        this.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ALT, 0, true), "ALTKEY");
        this.getActionMap().put("ALTKEY", new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
//                System.out.println("Performed");
            }
        });
        
        this.getCaret().setBlinkRate(DEF_CARET_BLINK_RATE);
    }
	
	public void setRectangularSelectionMode(boolean rectangularSelection) {
		if (rectangularSelection) {
			this.setCaret(new ColumnTextArea.ColumnSelectionCaret());
		}else {
			this.setCaret(new RSyntaxTextArea().getCaret());
		}

		isRectangularModeOn.set(rectangularSelection);
	}

    @Override
    public java.lang.String getSelectedText(){
        if (isRectangularModeOn.get()) {
			javax.swing.text.Highlighter.Highlight[] selections = this.getHighlighter().getHighlights();
			String text = "";
			int cnt = selections.length;
			if (cnt == 0) {
				text = super.getSelectedText();
			} else {
				for (int i = 0; i < cnt; i++) {
					int start = selections[i].getStartOffset();
					int end = selections[i].getEndOffset();
					java.lang.String selectedText = "";
					try {
						selectedText = this.getDocument().getText(start, end - start);
					} catch (javax.swing.text.BadLocationException e) {
						e.printStackTrace();
					}
					if (i == 0) {
						text = selectedText;
					} else {
						text += '\n' + selectedText;
					}
				}
			}
			return (text);
		}else {
			return super.getSelectedText();
		}
    }


    @Override
    public void copy(){
        if (isRectangularModeOn.get()) {
			java.awt.datatransfer.StringSelection ss = new java.awt.datatransfer.StringSelection(this.getSelectedText());
			java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
		}else {
			super.copy();
		}
    }
    
    @Override
    public void copyAsStyledText() {
    	int selStart = getSelectionStart();
    	int selEnd = getSelectionEnd();
    	if(selEnd == selStart) {
    		setSelectionStart(0);
        	setSelectionEnd(getText().length()-1);
    	}
    	
    	super.copyAsStyledText();
    	
    	requestFocus();
    	setSelectionStart(selStart);
    	setSelectionEnd(selEnd);
    }

    class ColumnSelectionCaret extends javax.swing.text.DefaultCaret {
        private static final long serialVersionUID = 1L;
        java.awt.Point lastPoint = new java.awt.Point(0,0);
        boolean altDown = false;
        boolean ctrlDown = false;
        boolean shiftDown = false;
        boolean moving = false;

        @Override
        public void mouseMoved(java.awt.event.MouseEvent e) {
            super.mouseMoved(e);
        }

        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            super.mouseClicked(e);
            boolean rightClick = SwingUtilities.isRightMouseButton(e);
            if(!rightClick && isRectangularModeOn.get() && e.getClickCount()==1 && !shiftDown){
                this.getComponent().getHighlighter().removeAllHighlights();
            }
            
        }
        
        @Override
        public void mouseReleased(java.awt.event.MouseEvent e){
        	if(isRectangularModeOn.get()) {
        		this.moving = false;
        	}
        	
        	this.altDown = e.isAltDown();
            this.ctrlDown = e.isControlDown();
            this.shiftDown = e.isShiftDown();
        	
            super.mouseReleased(e);
            
            SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateSelectionIfShiftPressed();
				}
			});
            
        }

        @Override
        public void mousePressed(java.awt.event.MouseEvent e){
            super.mousePressed(e);
            this.altDown = e.isAltDown();
            this.ctrlDown = e.isControlDown();
            this.shiftDown = e.isShiftDown();
            
            if (isRectangularModeOn.get()) {
				this.lastPoint = new java.awt.Point(e.getX(), e.getY());
			}
            
            if (e.getClickCount()==1) {
				try {
	                if(currentRef != null){
	                    	
                    	boolean rightClick = SwingUtilities.isRightMouseButton(e);
                    	if (!shiftDown && !rightClick) {
							currentRef.removeAllLineHighlights();
							currentRef.getHighlighter().removeAllHighlights();
						}
			
						int start = currentRef.getSelectionStart();
						int end = currentRef.getSelectionEnd();
						boolean turningBack = end < start;
						
						// Update last start position, used on key released if shift is pressed
						if (shiftDown) {
							lastStartPosition = currentRef.getSelectedText() != null ? start : currentRef.getCaretPosition();
						}
						
						updateSelectionIfShiftPressed();
					}
				
				} catch (Exception e1) {
					logger.error("Error tryin' to refresh highlight on mouse pressed", e1);
				}
			}
        }
        
        private void updateSelectionIfShiftPressed() {
        	// SELECT with shift (only if position is different) -> select from currentCaret to viewToModel
            if(shiftDown) {
            	try {
					currentRef.requestFocus();
					currentRef.setSelectionStart(lastStartPosition);
					currentRef.setSelectionEnd(currentRef.getCaretPosition());
				} catch (Exception e) {
				}
            }
        }

        @Override
        protected void moveCaret(java.awt.event.MouseEvent e) {
            if(isRectangularModeOn.get()) {
            	
	        	if(!this.moving){
		                this.getComponent().getHighlighter().removeAllHighlights();
	        		this.moving = true;
	        	}
	            if(this.altDown){
                java.awt.Point pt = new java.awt.Point(e.getX(), e.getY());
                javax.swing.text.Position.Bias[] biasRet = new javax.swing.text.Position.Bias[1];
                int pos = getComponent().getUI().viewToModel(getComponent(), pt, biasRet);
                if(biasRet[0] == null)
                    biasRet[0] = javax.swing.text.Position.Bias.Forward;
	                if (pos >= 0) {
	                    this.setDot(pos);
	                    java.awt.Point start = new java.awt.Point(Math.min(this.lastPoint.x,pt.x), Math.min(this.lastPoint.y,pt.y));
	                    java.awt.Point end = new java.awt.Point(Math.max(this.lastPoint.x,pt.x), Math.max(this.lastPoint.y,pt.y));
	                    this.rectangularHighlight(start,end);
	                }
                }
        	}
            super.moveCaret(e);
    	}
        
        private void customRemoveHighlights() {
//        	if(!this.ctrlDown){
        		this.getComponent().getHighlighter().removeAllHighlights();
//        	}
        }
        
        protected void rectangularHighlight(java.awt.Point start, java.awt.Point end) {
            
        	if (isRectangularModeOn.get()) {
        		javax.swing.text.JTextComponent comp = this.getComponent();
				comp.getHighlighter().removeAllHighlights();
				int y = start.y;
				int firstX = start.x;
				int lastX = end.x;
				int pos1 = comp.getUI().viewToModel(comp, new java.awt.Point(firstX, y));
				int pos2 = comp.getUI().viewToModel(comp, new java.awt.Point(lastX, y));
				try {
					comp.getHighlighter().addHighlight(pos1, pos2, this.getSelectionPainter());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				y++;
				while (y < end.y) {
					int pos1new = comp.getUI().viewToModel(comp, new java.awt.Point(firstX, y));
					int pos2new = comp.getUI().viewToModel(comp, new java.awt.Point(lastX, y));
					if (pos1 != pos1new) {
						pos1 = pos1new;
						pos2 = pos2new;
						try {
							comp.getHighlighter().addHighlight(pos1, pos2, this.getSelectionPainter());
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					y++;
				}
        	}
        }

    	protected void defaultHighlight(java.awt.Point start, java.awt.Point end) {
    		Dimension screenSize = GuiUtils.getScreenSize();
    		
    		if (isRectangularModeOn.get()) {
    			javax.swing.text.JTextComponent comp = this.getComponent();
    			this.customRemoveHighlights();
				int y = start.y;
    			int firstX = start.x;
    			int lastX = end.x;
    			// skip last when only one line
    			boolean lastMissing = false;
    			
    			// FIRST LINE from start x to max X, for first Y only
    			int pos1 = comp.getUI().viewToModel(comp, new java.awt.Point(firstX, y));
    			int pos2 = comp.getUI().viewToModel(comp, new java.awt.Point((int)screenSize.getWidth(), y));
    			try {
    				comp.getHighlighter().addHighlight(pos1, pos2, this.getSelectionPainter());
    			} catch (Exception ex) {
    				ex.printStackTrace();
    			}
    			y++;
    			
    			// START MIDDLE LINES UNTIL LAST EXCLUDED
    			while (y < end.y-1) {
    				lastMissing = true;
    				int pos1new = comp.getUI().viewToModel(comp, new java.awt.Point(0, y));
    				int pos2new = comp.getUI().viewToModel(comp, new java.awt.Point((int)screenSize.getWidth(), y));
    				if (pos1 != pos1new) {
    					pos1 = pos1new;
    					pos2 = pos2new;
    					try {
							comp.getHighlighter().addHighlight(pos1, pos2, this.getSelectionPainter());
    					} catch (Exception ex) {
    						ex.printStackTrace();
    					}
    				}
    				y++;
    			} 
    			
    			// LAST LINE
    			if (lastMissing) {
					pos1 = comp.getUI().viewToModel(comp, new java.awt.Point(0, y));
					pos2 = comp.getUI().viewToModel(comp, new java.awt.Point(lastX, y));
					try {
						comp.getHighlighter().addHighlight(pos1, pos2, this.getSelectionPainter());
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
    		}
    	}
    }
}