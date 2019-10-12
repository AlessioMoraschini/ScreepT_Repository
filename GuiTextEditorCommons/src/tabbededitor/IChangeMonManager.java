package tabbededitor;

import java.io.File;

public interface IChangeMonManager {

	public void checkChanges();
	public void updateCurrMonitor(File file);
	public void checkAndAdviceIfCurrentChanged(boolean forceCheck, boolean ask) throws Exception;
	public boolean isAlreadyChecking();
	public boolean setAlreadyChecking(boolean alreadyChecking);
	public Runnable getRunnableChanges();
}
