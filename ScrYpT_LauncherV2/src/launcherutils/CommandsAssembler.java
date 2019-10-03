package launcherutils;

import java.io.File;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import launcherutils.om.JvmArgumentsDTO;
import om.OSinfo;
import resources.GeneralConfig;
import utils.SysUtils;

public class CommandsAssembler {

	// CommandsAssembler logger
	static Logger logger = Logger.getLogger(CommandsAssembler.class);
		
	// OBJECT FIELDS
	private LinkedList<String> execCommand;
	public JvmHelper jvmHelper;
	
	public CommandsAssembler(JvmHelper jvmHelper) {
		this.execCommand = new LinkedList<String>();	
		this.jvmHelper = jvmHelper;
	}
	
	// SOME USEFUL LAUNCH-RELATED METHOD
	
	/**
	 * Add command to read path (according to currentOS) to the command list
	 * @param pathEntry
	 */
	public void addPathCommand(String pathEntry) {

		OSinfo currentOS = SysUtils.getOsInfoLazy();
		
		if (!jvmHelper.javaPath.equals(JvmHelper.JVM_OK)) {
			if (currentOS.isWindows()) {
				execCommand.add("set JDK_PATH=" + pathEntry + ";");
				execCommand.add("set PATH=%JDK_PATH%%PATH%");
				execCommand.add("echo %PATH%");

			} else if (currentOS.isLinux()) {

				execCommand.add("PATH=" + pathEntry + ":$PATH");

			} else if (currentOS.isMac()) {

				execCommand.add("PATH=" + pathEntry + ":$PATH");

			} 
		}		
	}
	
	/**
	 * add command to start main application jar with given JVM args
	 */
	public void addLaunchCommand(JvmArgumentsDTO dto, File[] argumentsFiles) {
		
		// Make objects null-safe
		dto = (dto != null)? dto : new JvmArgumentsDTO();
		argumentsFiles = (argumentsFiles != null)? argumentsFiles : new File[0];
		
		String complete_ARGS = dto.JVM_MEMORY_OPTIONS 
							 + dto.JVM_STACK_SIZE 
							 + " -XshowSettings:vm -Dawt.useSystemAAFontSettings=on -Dsun.java2d.ddscale=true -Dsun.java2d.uiScale="
							 + dto.GUI_SCALE_ARG_VALUE 
							 + dto.JVM_ARGS;
		
		if (GeneralConfig.FLAG_LAUNCH_JAR_COMMAND) {
			// THIS USES JAR WITH NO CLASSPATH - USEFUL FOR CENTRALIZED BUILD
			execCommand.add(SysUtils.startJarCommand(complete_ARGS, dto.MAIN_JAR_FILE, argumentsFiles));
		
		}else {
			// THIS USES MAIN CLASS WITH CLASSPATH - USEFUL FOR FRAGMENTED BUILD JARS OR CLASSES
			execCommand.add(SysUtils.startClassCommand(complete_ARGS, 
					GeneralConfig.MAIN_GENERAL_CLASSPATH, 
					GeneralConfig.MAIN_GENERAL_STARTER_PACKAGE, 
					argumentsFiles));
		}
	}
	
	/**
	 * extract a String containing all added commands separed by 2 newlines (to write after on a bash/batch file).
	 * @return
	 */
	public String getCombinedCommandList() {
		StringBuilder builder = new StringBuilder();
		builder.ensureCapacity(execCommand.size());
		
		for(String currentCommand : execCommand) {
			builder.append(currentCommand);
			builder.append(" \n\n");
		}
		// add exit command to end of batch/bash
		builder.append("exit 0");
		
		return builder.toString();
	}
	
	/**
	 * this will empty the current list of eventually loaded commands, refreshing to new LinkedList()
	 */
	public void emptyCommandsList() {
		this.execCommand = new LinkedList<String>();	
	}
}
