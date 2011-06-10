/**
 * CommandLineParserTest.java
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

import ch.entwine.weblounge.tools.util.CommandLineParser;

import junit.framework.TestCase;

/**
 * Test class for the command line parser.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */

public class CommandLineParserTest extends TestCase {

	/** The command line parser */
	private CommandLineParser parser;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(CommandLineParserTest.class);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		parser = new CommandLineParser();
		parser.defineCommand("unmerge");
		parser.defineCommand(new String[] {"u", "update"});
		parser.defineCommand(new String[] {"v", "verbose"});
		parser.defineCommand(new String[] {"p", "pretend"});
		parser.defineOption("m");
		parser.defineOption(new String[] {"n", "number"});
		parser.defineRequiredOption(new String[] {"s", "scope"});
		parser.defineOption(new String[] {"t", "type"});
	}

	public final void testRequiredOption() {
		try {
			parser.parse(new String[] {"--type=5"});
			fail("Missing required option 's' not detected");
		} catch (Exception e) { }
		parser.reset();
	}
	
	/*
	 * Class under test for boolean hasOption(String)
	 */
	public final void testProvidesOptionString() {
		parser.parse(new String[] {"--scope=5"});
		if (!parser.providesOption("s") || !parser.providesOption("scope")) {
			fail();
		}
		parser.reset();
	}

	/*
	 * Class under test for boolean hasOption(String[])
	 */
	public final void testProvidesOptionStringArray() {
		parser.parse(new String[] {"--scope=5"});
		if (!parser.providesOption(new String[] {"s", "scope"})) {
			fail();
		}
		parser.reset();
	}

	/*
	 * Class under test for String getOption(String)
	 */
	public final void testGetOptionString() {
		parser.parse(new String[] {"--scope=5"});
		if (!"5".equals(parser.getOption("s")) || !"5".equals(parser.getOption("scope"))) {
			fail();
		}
		parser.reset();
	}

	/*
	 * Class under test for String getOption(String, String)
	 */
	public final void testGetOptionStringString() {
		parser.parse(new String[] {"--scope=5"});
		if (!"5".equals(parser.getOption("s", "6")) || !"6".equals(parser.getOption("t", "6"))) {
			fail();
		}
		parser.reset();
	}

	/*
	 * Class under test for String getOption(String[])
	 */
	public final void testGetOptionStringArray() {
		parser.parse(new String[] {"--scope=5"});
		if (!"5".equals(parser.getOption(new String[] {"s", "scope"}))) {
			fail();
		}
		parser.reset();
	}

	/*
	 * Class under test for String getOption(String[], String)
	 */
	public final void testGetOptionStringArrayString() {
		parser.parse(new String[] {"--scope=5"});
		if (!"5".equals(parser.getOption(new String[] {"s", "scope"})) || !"6".equals(parser.getOption(new String[] {"t", "type"}, "6"))) {
			fail();
		}
		parser.reset();
	}

	/*
	 * Class under test for boolean hasCommand(String)
	 */
	public final void testProvidesCommandString() {
		parser.parse(new String[] {"-u", "--scope=5"});
		if (!parser.providesCommand("u") || !parser.providesCommand("update")) {
			fail();
		}
		parser.reset();
	}

	/*
	 * Class under test for boolean hasCommand(String[])
	 */
	public final void testProvidesCommandStringArray() {
		parser.parse(new String[] {"-u", "--scope=5"});
		if (!parser.providesCommand(new String[] {"u", "update"})) {
			fail();
		}
		parser.reset();
	}

	public final void testParse() {
		try {
			parser.parse(new String[] {"unmerge", "-s", "5"}); parser.reset();
			parser.parse(new String[] {"unmerge", "-s", "5", "-u"}); parser.reset();
			parser.parse(new String[] {"unmerge", "-s", "5", "u"}); parser.reset();
			parser.parse(new String[] {"unmerge", "-s", "5", "-uvp"}); parser.reset();
			parser.parse(new String[] {"unmerge", "-s", "5", "uvp"}); parser.reset();
			parser.parse(new String[] {"unmerge", "--scope"}); parser.reset();
			parser.parse(new String[] {"unmerge", "--scope=5"}); parser.reset();
		} catch (IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

}