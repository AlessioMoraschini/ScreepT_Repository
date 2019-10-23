package om;

import java.awt.Dimension;
import java.awt.Point;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import frameutils.utils.GuiUtils;

public class ColumnTextArea extends RSyntaxTextArea{
	private static final long serialVersionUID = -2828990655552935752L;

	public AtomicBoolean isRectangularModeOn = new AtomicBoolean(true);
	RSyntaxTextArea currentRef;
	
	public ColumnTextArea(boolean rectangularSelectionActive) {
		super();
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
        
        this.getCaret().setBlinkRate(400);
	}
	
	public ColumnTextArea(int rows, int columns){
        super(rows, columns);
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
    }
	
	public void setRectangularSelectionMode(boolean rectangularSelection) {
		if (rectangularSelection) {
			this.setCaret(new ColumnTextArea.ColumnSelectionCaret());
		}else {
			this.setCaret(new DefaultCaret());
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

    class ColumnSelectionCaret extends javax.swing.text.DefaultCaret {
        private static final long serialVersionUID = 1L;
        java.awt.Point lastPoint = new java.awt.Point(0,0);
        boolean altDown = false;
        boolean ctrlDown = false;
        boolean moving = false;

        @Override
        public void mouseMoved(java.awt.event.MouseEvent e) {
            super.mouseMoved(e);
        }

        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            super.mouseClicked(e);
            if(e.getClickCount()==1){
            	this.customRemoveHighlights();
            }
            
        }

        @Override
        public void mousePressed(java.awt.event.MouseEvent e){
            super.mousePressed(e);
            this.lastPoint = new java.awt.Point(e.getX(), e.getY());
            this.ctrlDown = e.isControlDown();
            this.altDown = e.isAltDown() && !e.isControlDown() && !e.isShiftDown();
            
            currentRef = ((RSyntaxTextArea)this.getComponent());
            Point pt = new Point(e.getX(), e.getY());
            int pos = currentRef.viewToModel(pt);
            currentRef.setCaretPosition(pos-1);
            currentRef.setCaretPosition(pos);
            currentRef.revalidate();
            currentRef.repaint();
        }

        @Override
        protected void moveCaret(java.awt.event.MouseEvent e) {
        	if(!this.moving){
        		this.customRemoveHighlights();
        		this.moving = true;
        	}
            if(isRectangularModeOn.get()) {
                java.awt.Point pt = new java.awt.Point(e.getX(), e.getY());
                javax.swing.text.Position.Bias[] biasRet = new javax.swing.text.Position.Bias[1];
                int pos = getComponent().getUI().viewToModel(getComponent(), pt, biasRet);
                if(biasRet[0] == null)
                    biasRet[0] = javax.swing.text.Position.Bias.Forward;
                if(this.altDown){
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
        
        @Override
        public void mouseReleased(java.awt.event.MouseEvent e){
        	super.mouseReleased(e);
        	if(isRectangularModeOn.get()) {
        		this.moving = false;
        	}
        }

        protected void rectangularHighlight(java.awt.Point start, java.awt.Point end) {
            
        	if (isRectangularModeOn.get()) {
        		javax.swing.text.JTextComponent comp = this.getComponent();
	        	this.customRemoveHighlights();
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