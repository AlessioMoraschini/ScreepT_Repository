package frameutils.frame.panels.arch;

import java.io.IOException;

import org.apache.log4j.Logger;

public interface IParentPanel{

	public void sync();
	public void reload(String target) throws Exception;
	public void doAction(String target) throws IOException;
}
