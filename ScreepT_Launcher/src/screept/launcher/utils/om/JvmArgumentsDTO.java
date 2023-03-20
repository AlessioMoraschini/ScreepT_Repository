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
package screept.launcher.utils.om;

public class JvmArgumentsDTO {

	public String JVM_MEMORY_OPTIONS = ""; 
	public String JVM_STACK_SIZE = ""; 
	public String MAIN_JAR_FILE = "";
	public String GUI_SCALE_ARG_VALUE = "";
	public String JVM_ARGS = "";
	public String[] ARGS = new String[0];
	
	public JvmArgumentsDTO() {};
	
	public JvmArgumentsDTO(String JVM_MEMORY_OPTIONS,
						String JVM_STACK_SIZE,
						String MAIN_JAR_FILE,
						String GUI_SCALE_ARG_VALUE,
						String JVM_ARGS,
						String[] ARGS) {
		
		this.JVM_MEMORY_OPTIONS = JVM_MEMORY_OPTIONS;
		this.JVM_STACK_SIZE = JVM_STACK_SIZE;
		this.MAIN_JAR_FILE = MAIN_JAR_FILE;
		this.GUI_SCALE_ARG_VALUE = GUI_SCALE_ARG_VALUE;
		this.JVM_ARGS = JVM_ARGS;
		this.ARGS = ARGS;
	}
}
