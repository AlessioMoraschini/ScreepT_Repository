package guiTEST;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.junit.Test;

import gUI.utils.progressDialog.ProgressCancelableDialog;

public class ProgressBarTest {
	
	@Test
	public void testProgressBar(){
		ActionListener closer = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
				
			}
		};
		ProgressCancelableDialog bar = new ProgressCancelableDialog("Prova", null, true);
		bar.setCancelOperation(closer);
		for (int i = 0; i <= 100; i++) {
			bar.update(String.valueOf(bar.getProgress()), i);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				System.out.println("INTERRUPTED!");
			}
		}
	}	
}
