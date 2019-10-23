package tabbededitor;

import java.io.File;
import java.util.Collection;

public interface IChangeMonManager {

	public void checkChanges();
	public void updateCurrMonitor(File file);
	public void checkAndAdviceIfCurrentChanged(boolean forceCheck, boolean ask) throws Exception;
	public boolean isAlreadyChecking();
	public boolean setAlreadyChecking(boolean alreadyChecking);
	public Runnable getRunnableChanges();
	public void cleanFromHistory(File target);
	public void removePendingFiredChanges(File target);
}
