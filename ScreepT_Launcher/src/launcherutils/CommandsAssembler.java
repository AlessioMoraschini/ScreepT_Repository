/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package launcherutils;

import java.io.File;

import org.apache.log4j.Logger;

import launcherutils.om.JvmArgumentsDTO;
import om.LimitedConcurrentList;
import om.OSinfo;
import resources.GeneralConfig;
import utils.SysUtils;

public class CommandsAssembler {

	// CommandsAssembler logger
	static Logger logger = Logger.getLogger(CommandsAssembler.class);
		
	// OBJECT FIELDS
	private LimitedConcurrentList<String> execCommand;
	public JvmHelper jvmHelper;
	
	public CommandsAssembler(JvmHelper jvmHelper) {
		this.execCommand = new LimitedConcurrentList<String>(Integer.MAX_VALUE);	
		this.jvmHelper = jvmHelper;
	}
	
	// SOME USEFUL LAUNCH-RELATED METHOD
	
	/**
	 * Add command to read path (according to currentOS) to the command list
	 * @param pathEntry
	 */
	public void addPathCommand(String pathEntry) {

		OSinfo currentOS = SysUtils.getOsInfoLazy();
		
		
		if (!pathEntry.equals(JvmHelper.JVM_OK) && !pathEntry.equals(JvmHelper.JVM_IGNORE_WARN)) {
			if (currentOS.isWindows()) {
				// bugfix - avoid ; char when path is null, for example when JVM is not found
				pathEntry = (pathEntry != null && !pathEntry.equals(""))? pathEntry + ";": "";
				for(String currentAddedCommand : GeneralConfig.BATCH_LAUNCHER_ADDED_INSTRUCTIONS) {
					execCommand.add(currentAddedCommand);
				}
				execCommand.add("set JDK_PATH=" + pathEntry);
				execCommand.add("set PATH=%JDK_PATH%%PATH%");
				execCommand.add("echo %PATH%");

			} else if (currentOS.isLinux()) {
				pathEntry = (pathEntry != null && !pathEntry.equals(""))? pathEntry + ":" : "";
				execCommand.add("PATH=" + pathEntry + "$PATH");
			
			} else if (currentOS.isMac()) {
				pathEntry = (pathEntry != null && !pathEntry.equals(""))? pathEntry + ":" : "";
				execCommand.add("PATH=" + pathEntry + "$PATH");
			} 
		}		
	}
	
	/**
	 * add command to start main application jar with given JVM args
	 */
	public void addLaunchCommand(JvmArgumentsDTO dto, File[] argumentsFiles, boolean viewShell) {
		
		// Make objects null-safe
		dto = (dto != null)? dto : new JvmArgumentsDTO();
		argumentsFiles = (argumentsFiles != null)? argumentsFiles : new File[0];
		
		StringBuilder builder = new StringBuilder();
		builder.append(dto.JVM_MEMORY_OPTIONS)
			   .append(dto.JVM_STACK_SIZE)
			   .append(" ").append(GeneralConfig.JVM_CUSTOM_ADDED_SETTINGS)
			   .append(" -Dsun.java2d.uiScale=")
			   .append(dto.GUI_SCALE_ARG_VALUE)
			   .append(dto.JVM_ARGS);
		
		String complete_ARGS = builder.toString();

		if (Boolean.valueOf(GeneralConfig.linkedMappings.getProperty("FLAG_LAUNCH_JAR_COMMAND"))) {
			// THIS USES JAR WITH NO CLASSPATH - USEFUL FOR CENTRALIZED BUILD
			execCommand.add(SysUtils.startJarCommand(complete_ARGS, dto.MAIN_JAR_FILE, argumentsFiles, viewShell));
		}else {
			
			String classPath = SysUtils.getOsInfoLazy().isWindows() ? 
					GeneralConfig.MAIN_GENERAL_CLASSPATH_WIN : SysUtils.getClassPathFromAllJarsFoundInRoot(GeneralConfig.CURRENT_EXECUTION_DIR, true);
			// THIS USES MAIN CLASS WITH CLASSPATH - USEFUL FOR FRAGMENTED BUILD JARS OR CLASSES
			execCommand.add(SysUtils.startClassCommand(complete_ARGS, 
					classPath, 
					GeneralConfig.MAIN_GENERAL_STARTER_PACKAGE, 
					argumentsFiles, viewShell));
		}
	}
	
	/**
	 * extract a String containing all added commands separed by 2 newlines (to write after on a bash/batch file).
	 * @return
	 */
	public String getCombinedCommandList() {
		StringBuilder builder = new StringBuilder();
		builder.ensureCapacity(execCommand.getList().size()+1);
		
		// Add shebang
		if(!SysUtils.getOsInfoLazy().isWindows()) {
			builder.append("#!/bin/bash").append("\n\n");
		}
		
		for(String currentCommand : execCommand.getList()) {
			builder.append(currentCommand);
			builder.append(" \n\n");
		}
		// add exit command to end of batch/bash
		builder.append("exit 0");
		
		return builder.toString();
	}
	
	/**
	 * this will empty the current list of eventually loaded commands, refreshing to new LimitedList()
	 */
	public void emptyCommandsList() {
		this.execCommand = new LimitedConcurrentList<String>(Integer.MAX_VALUE);	
	}
}
