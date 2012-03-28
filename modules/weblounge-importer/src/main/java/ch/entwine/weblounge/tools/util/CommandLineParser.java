/**
 * CommandLineParser.java
 *
 * Copyright 2005 by Entwine
 * Zurich,  Switzerland (CH)
 * All rights reserved.
 * 
 * This software is confidential and proprietary information ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into.
 */

package ch.entwine.weblounge.tools.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Parses the given commandline and returns its option values and
 * parsedCommands.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */

public class CommandLineParser {

	/** The defined command line options */
	@SuppressWarnings("rawtypes")
  private Map definedOptions = new HashMap();

	/** The required command line options */
	@SuppressWarnings("rawtypes")
  private Map requiredOptions = new HashMap();

	/** The parsed command line options */
	@SuppressWarnings("rawtypes")
  private Map parsedOptions = new HashMap();

	/** The defined command line actions */
	@SuppressWarnings("rawtypes")
  private Map definedCommands = new HashMap();

	/** The parsed command line actions */
	@SuppressWarnings("rawtypes")
  private Set parsedCommands = new HashSet();
	
	/**
	 * Creates a new commandline parser for the given arguments.
	 */
	public CommandLineParser() { }

	/**
	 * Resets the parser to its initial state.
	 */
	public void reset() {
		parsedOptions.clear();
		parsedCommands.clear();
	}

	/**
	 * Defines the option for the given commandline.
	 * 
	 * @param option the option
	 */
	public void defineOption(String option) {
		if (option != null) {
			definedOptions.put(option, null);
		}
	}

	/**
	 * Defines the option for the given commandline. The string array
	 * contains the various typings of the option name.
	 * 
	 * @param option the option names
	 */
	public void defineOption(String[] option) {
		if (option != null) {
			for (int i=0; i < option.length; i++) {
				definedOptions.put(option[i], option);
			}
		}
	}

	/**
	 * Defines the option for the given commandline and marks it as
	 * beeing required.
	 * 
	 * @param option the required option
	 */
	public void defineRequiredOption(String option) {
		if (option != null) {
			definedOptions.put(option, null);
			requiredOptions.put(option, null);
		}
	}

	/**
	 * Defines the option for the given commandline and marks it as beeing
	 * required. The string array contains the various typings of the option name.
	 * 
	 * @param option the required option names
	 */
	public void defineRequiredOption(String[] option) {
		if (option != null) {
			for (int i=0; i < option.length; i++) {
				definedOptions.put(option[i], option);
				requiredOptions.put(option[i], option);
			}
		}
	}
	
	/**
	 * Returns all parsedOptions that have been passed on the commandline.
	 * 
	 * @return the parsedOptions
	 */
	public Set getOptions() {
		return parsedOptions.keySet();
	}

	/**
	 * Returns <code>true</code> if the option has been specified.
	 * 
	 * @param option the option name
	 * @return <code>true</code> if the option has been specified
	 */
	public boolean providesOption(String option) {
		return getOption(option) != null;
	}

	/**
	 * Returns <code>true</code> if the option has been specified. The
	 * string array must specifiy the various typings of the option name.
	 * 
	 * @param option the option names
	 * @return <code>true</code> if the option has been specified
	 */
	public boolean providesOption(String[] option) {
		return getOption(option) != null;
	}

	/**
	 * Returns the option value for <code>option</code> or <code>null</code>
	 * if no such option has been passed.
	 * 
	 * @param option the option name
	 * @return the option value
	 */
	public String getOption(String option) {
		String v = (String)parsedOptions.get(option);
		if (v != null) {
			return v;
		}
		String[] options = (String[])definedOptions.get(option);
		if (options != null) {
			for (int i=0; i < options.length; i++) {
				v = (String)parsedOptions.get(options[i]);
				if (v != null) {
					return v;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the option value for <code>option</code> or <code>defaultValue</code>
	 * if no such option has been passed.
	 * 
	 * @param option the option name
	 * @return the option value
	 */
	public String getOption(String option, String defaultValue) {
		String v = getOption(option);
		return (v != null) ? v : defaultValue;
	}

	/**
	 * Returns the option value for <code>option</code> or <code>null</code>
	 * if no such option has been passed. The string array must specifiy the various 
	 * typings of the option name.
	 * 
	 * @param option the option names
	 * @return the option value
	 */
	public String getOption(String[] option) {
		if (option != null) {
			for (int i=0; i < option.length; i++) {
				String v = (String)parsedOptions.get(option[i]);
				if (v != null) {
					return v;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the option value for <code>option</code> or <code>defaultValue</code>
	 * if no such option has been passed.
	 * 
	 * @param option the option names
	 * @return the option value
	 */
	public String getOption(String[] option, String defaultValue) {
		String v = getOption(option);
		return (v != null) ? v : defaultValue;
	}
	
	/**
	 * Defines the command for the given commandline.
	 * 
	 * @param command the command
	 */
	public void defineCommand(String command) {
		if (command != null) {
			definedCommands.put(command, null);
		}
	}

	/**
	 * Defines the command for the given commandline. The string array
	 * contains the various typings of the command name.
	 * 
	 * @param command the command names
	 */
	public void defineCommand(String[] command) {
		if (command != null) {
			for (int i=0; i < command.length; i++) {
				definedCommands.put(command[i], command);
			}
		}
	}

	/**
	 * Returns all parsedCommands that have been passed on the commandline.
	 * 
	 * @return the parsedCommands
	 */
	public Set getCommands() {
		return parsedCommands;
	}

	/**
	 * Returns <code>true</code> if the command has been specified.
	 * 
	 * @param command the command name
	 * @return <code>true</code> if the command has been specified
	 */
	public boolean providesCommand(String command) {
		if (parsedCommands.contains(command)) {
			return true;
		}
		String[] commands = (String[])definedCommands.get(command);
		return providesCommand(commands);
	}

	/**
	 * Returns <code>true</code> if the command has been specified. The
	 * string array must specifiy the various typings of the command.
	 * 
	 * @param command the command names
	 * @return <code>true</code> if the command has been specified
	 */
	public boolean providesCommand(String[] command) {
		if (command != null) {
			for (int i=0; i < command.length; i++) {
				if (parsedCommands.contains(command[i])) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Parses the commandline and puts the values into the <code>parsedOptions</code>
	 * and <code>parsedCommands</code> collections.
	 * 
	 * @param args the commandline arguments
	 * @throws IllegalStateException if an unknown option or command has been detected
	 * 		or if the commandline is malformed
	 */
	public void parse(String[] args) throws IllegalStateException {
		Stack stack = createStack(args);
		List required = new ArrayList();
		required.addAll(requiredOptions.keySet());
		while (!stack.empty()) {
			String arg = (String)stack.pop();
			if (arg.startsWith("--") && arg.trim().length() > 2) {
				int equalsSign = arg.indexOf("=");
				if (equalsSign > 2) {
					String option = arg.substring(2, equalsSign);
					if (definedOptions.containsKey(option)) {
						String value = (equalsSign < arg.length() - 1) ? arg.substring(equalsSign + 1) : null;
						putOption(option, value);
					} else {
						throw new IllegalStateException("Argument '" + arg + "' is unknown!");
					}
				} else if (equalsSign < 0) {
					String option = arg.substring(2);
					if (definedOptions.containsKey(option)) {
						putOption(option, null);
					} else if (definedCommands.containsKey(option)) {
						parsedCommands.add(option);
					} else {
						throw new IllegalStateException("Argument '" + arg + "' is unknown!");
					}
				} else {
					throw new IllegalStateException("Commandline is malformed. Found '--' without command name!");
				}
			} else if (arg.startsWith("-") && arg.trim().length() > 1) {
				if (arg.length() > 2) {
					for (int i=1; i < arg.length(); i++) {
						String command = arg.substring(i, i + 1);
						if (definedCommands.containsKey(command)) {
							parsedCommands.add(command);
						} else {
							throw new IllegalStateException("Argument '" + arg + "' is unknown!");
						}
					}
				} else {
					String option = arg.substring(1, 2);
					if (!stack.empty() && definedOptions.containsKey(option)) {
						String value = (String)stack.pop();
						putOption(option, value);
					} else if (definedCommands.containsKey(option)) {
						parsedCommands.add(option);
					} else {
						throw new IllegalStateException("Commandline is malformed. Found option '" + option + "' without option value!");
					}
				}
			} else if (!arg.startsWith("-")) {
				if (definedCommands.containsKey(arg)) {
					parsedCommands.add(arg);
				} else {
					for (int i=0; i < arg.length(); i++) {
						String command = arg.substring(i, i + 1);
						if (definedCommands.containsKey(command)) {
							parsedCommands.add(command);
						} else {
							throw new IllegalStateException("Command '" + arg + "' is unknown!");
						}
					}
				}
			} else {
				throw new IllegalStateException("Commandline is malformed. Found '-' without option name or command!");
			}
		}
		checkRequired();
	}
	
	/**
	 * Checks if all required options have been provided. It this is not the case,
	 * an <code>IllegalArgumentException</code> is thrown.
	 */
	private void checkRequired() {
		Map options = new HashMap();
		options.putAll(requiredOptions);
		Iterator oi = parsedOptions.keySet().iterator();
		while (oi.hasNext()) {
			String option = (String)oi.next();
			String[] names = (String[])options.remove(option);
			if (names != null) {
				for (int i=0; i < names.length; i ++) {
					options.remove(names[i]);
				}
			}
		}
		if (options.size() > 0) {
			throw new IllegalStateException("Required options are missing!");
		}
	}
	
	/**
	 * Registers the option value while checking for duplicate definition.
	 * 
	 * @param option the option
	 * @param value the option value
	 */
	private void putOption(String option, String value) {
		if (providesOption(option)) {
			throw new IllegalStateException("Option '" + option + "' has duplicate value!");
		}
		parsedOptions.put(option, value);
	}

	/**
	 * Creates a stack from the given string array.
	 * 
	 * @param args the string array
	 * @return the created stack
	 */
	private Stack createStack(String[] args) {
		Stack stack = new Stack();
		if (args != null) {
			for (int i = args.length - 1; i >= 0; stack.push(args[i--]));
		}
		return stack;
	}

}