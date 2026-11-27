/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.launcher.utils.om;

public class JvmArgumentsDTO {

	public String JVM_MEMORY_OPTIONS = ""; 
	public String JVM_STACK_SIZE = ""; 
	public String JVM_GC_OPTIONS = ""; // one GC flag, or empty for the JVM default
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
