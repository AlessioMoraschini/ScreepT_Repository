package utils.panels;

import org.apache.log4j.Logger;

public interface IParentPanel{

	// ParentPanel logger creation
	static Logger logger = Logger.getLogger(IParentPanel.class);
	
	public void sync();
}
