package gui.texteditor.commons.om;

import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RUndoManager;

public class CustomUndoManager extends RUndoManager {
	private static final long serialVersionUID = -7562942804894142566L;

	private Runnable beforeRedo;
	private Runnable beforeUndo;
	private Runnable afterRedo;
	private Runnable afterUndo;

	public CustomUndoManager(RTextArea textArea, Runnable afterRedo, Runnable afterUndo) {
		super(textArea);
		setAfterRedo(afterRedo);
		setAfterUndo(afterUndo);
		setBeforeRedo(null);
		setBeforeUndo(null);
	}

	public Runnable getAfterRedo() {
		return afterRedo;
	}

	public void setAfterRedo(Runnable afterRedo) {
		this.afterRedo = afterRedo != null ? afterRedo : ()->{};
	}

	public Runnable getAfterUndo() {
		return afterUndo;
	}

	public void setAfterUndo(Runnable afterUndo) {
		this.afterUndo = afterUndo != null ? afterUndo : ()->{};
	}

	public Runnable getBeforeRedo() {
		return beforeRedo;
	}

	public void setBeforeRedo(Runnable beforeRedo) {
		this.beforeRedo = beforeRedo != null ? beforeRedo : ()->{};
	}

	public Runnable getBeforeUndo() {
		return beforeUndo;
	}

	public void setBeforeUndo(Runnable beforeUndo) {
		this.beforeUndo = beforeUndo != null ? beforeUndo : ()->{};
	}

	public void setCommonAfterAction(Runnable commonAfterAction) {
		setAfterRedo(commonAfterAction);
		setAfterUndo(commonAfterAction);
	}

	public void setCommonBeforeAction(Runnable commonAfterAction) {
		setBeforeRedo(commonAfterAction);
		setBeforeUndo(commonAfterAction);
	}

	@Override
	public void redo() {
		beforeRedo.run();
		super.redo();
		afterRedo.run();
	}

	@Override
	public void undo() {
		beforeUndo.run();
		super.undo();
		afterUndo.run();
	}


}
