/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
 *  http://weblounge.o2it.ch
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.kernel.command;

import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.testing.IntegrationTest;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * OSGi shell command implementation for the test harness.
 */
public final class TestCommand {

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(TestCommand.class);

  /** The tests */
  private List<IntegrationTest> tests = new ArrayList<IntegrationTest>();

  /** Comparator used to get the test suite in order */
  private static final IntegrationTestComparator testComparator = new IntegrationTestComparator();

  /**
   * Callback for OSGi's declarative services component activation.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if component activation fails
   */
  void activate(ComponentContext context) {
    BundleContext bundleContext = context.getBundleContext();
    logger.debug("Registering test commands");
    Dictionary<String, Object> commands = new Hashtable<String, Object>();
    commands.put("osgi.command.scope", "weblounge");
    commands.put("osgi.command.function", new String[] { "lt", "test", "tests" });
    bundleContext.registerService(getClass().getName(), this, commands);
  }

  /**
   * Adds <code>test</code> to the list of integration tests. This method will
   * usually be called as a result of on {@link IntegrationTest} being published
   * in the OSGi service registry.
   * 
   * @param test
   *          the test implementation
   */
  void addIntegrationTest(IntegrationTest test) {
    if (!tests.contains(test)) {
      tests.add(test);
      Collections.sort(tests, testComparator);
      logger.debug("Installing {}", test.getName());
    }
  }

  /**
   * Removes <code>test</code> from the list of integration tests. This method
   * will usually be called as a result of on {@link IntegrationTest}
   * disappearing from the OSGi service registry.
   * 
   * @param test
   *          the test to remove
   */
  void removeIntegrationTest(IntegrationTest test) {
    tests.remove(test);
    logger.debug("Removed {}", test.getName());
  }

  /**
   * Command signature that allows to do
   * <ul>
   * <li><code>test list</code></li>
   * <li><code>test all</code></li>
   * <li><code>test <id></li>
   * </ul>
   * 
   * @param args
   *          the list of arguments to this command
   */
  public void test(String[] args) {
    if (args.length == 0) {
      list();
      return;
    } else if (args.length == 1) {
      if ("list".equals(args[0])) {
        list();
      } else if ("all".equals(args[0])) {
        executeAll(tests);
      } else {
        String id = args[0];

        // Look up the test
        IntegrationTest test = null;
        try {
          int testIndex = Integer.parseInt(id);
          test = tests.get(testIndex - 1);
          List<IntegrationTest> tests = new ArrayList<IntegrationTest>();
          tests.add(test);
          executeAll(tests);
        } catch (NumberFormatException e) {
          System.out.println("Unknown test: " + id);
          return;
        } catch (IndexOutOfBoundsException e) {
          System.out.println("Unknown test: " + id + " Please choose between [1.." + tests.size() + "]");
          return;
        }
      }
    } else {
      printUsage();
    }
  }

  /**
   * Prints a list of currently registered tests.
   */
  private void list() {
    // Are there any tests?
    if (tests.size() == 0) {
      System.out.println("No tests found");
      return;
    }

    // Memorize the group
    String currentGroup = "";

    // Display the test list
    for (int i = 0; i < tests.size(); i++) {
      IntegrationTest test = tests.get(i);
      String group = StringUtils.trimToEmpty(test.getGroup());
      if ("".equals(group))
        group = "DEFAULT";
      if (!group.equals(currentGroup)) {
        System.out.println("");
        System.out.println(group);
        System.out.println("   ID|Name");
        currentGroup = group;
      }
      StringBuffer buf = new StringBuffer();
      System.out.format("%5s", Integer.toString(i + 1));
      buf.append("|");
      buf.append(test.getName());
      System.out.println(buf.toString());
    }
    System.out.println("");
  }

  /**
   * Prints the command usage to the commandline.
   */
  private void printUsage() {
    System.out.println("  Usage:");
    System.out.println("    test list");
    System.out.println("    test all");
    System.out.println("    test <id>");
  }

  /**
   * Executes all registered tests.
   */
  private void executeAll(List<IntegrationTest> tests) {
    List<IntegrationTest> succeeded = new ArrayList<IntegrationTest>();
    List<IntegrationTest> failed = new ArrayList<IntegrationTest>();

    // Print the test header
    Date startDate = new Date();
    logger.info("------------------------------------------------------------------------");
    logger.info("Running Integration Tests");
    logger.info("------------------------------------------------------------------------");
    logger.info("Tests: " + tests.size());
    logger.info("Started at: " + WebloungeDateFormat.formatStatic(startDate));

    // Execute the tests
    for (IntegrationTest test : tests) {
      if (execute(test))
        succeeded.add(test);
      else
        failed.add(test);
    }

    Date endDate = new Date();
    long time = endDate.getTime() - startDate.getTime();
    String testcount = Integer.toString(tests.size());

    // Print the summary
    logger.info(" ");
    logger.info(" ");
    logger.info("------------------------------------------------------------------------");
    logger.info("Test Summary:");
    logger.info("------------------------------------------------------------------------");
    for (IntegrationTest test : tests) {
      StringBuffer buf = new StringBuffer();
      
      // Test number
      int pos = 0;
      for (int i = 0; i < this.tests.size(); i++) {
        if (this.tests.get(i) == test) {
          pos = i + 1;
          break;
        }
      }
      
      // Test number
      String num = Integer.toString(pos);
      for (int i = num.length(); i < testcount.length(); i++)
        buf.append(" ");
      buf.append(num);
      buf.append(" ");

      // Test name
      buf.append(test.getName());
      buf.append(" ");

      int testNameLenght = buf.length();
      if (succeeded.contains(test)) {
        for (int i = 0; i < 64 - testNameLenght; i++)
          buf.append(".");
        buf.append(" SUCCESS");
      } else {
        for (int i = 0; i < 64 - testNameLenght; i++)
          buf.append(".");
        buf.append(" FAILURE");
      }
      logger.info(buf.toString());
      pos++;
    }
    logger.info("------------------------------------------------------------------------");
    logger.info("------------------------------------------------------------------------");
    if (failed.size() == 0)
      logger.info("SUCCESS: " + succeeded.size() + " TEST" + (succeeded.size() > 1 ? "S" : "") + " PASSED");
    else
      logger.info("FAILURE: " + failed.size() + " TEST" + (succeeded.size() > 1 ? "S" : "") + " FAILED");
    logger.info("------------------------------------------------------------------------");
    logger.info("Total time: " + ConfigurationUtils.toHumanReadableDuration(time));
    logger.info("Finished at: " + WebloungeDateFormat.formatStatic(endDate));
    logger.info("------------------------------------------------------------------------");
  }

  /**
   * Executes a single integration test and returns <code>true</code> if the
   * test passed, <code>false</code> otherwise.
   * 
   * @param test
   *          the test to run
   * @return <code>true</code> if the test passed
   */
  private boolean execute(IntegrationTest test) {
    logger.info("");
    logger.info("------------------------------------------------------------------------");
    logger.info("Running test '" + test + "'");
    logger.info("------------------------------------------------------------------------");
    try {
      test.execute("http://127.0.0.1:8080");
      logger.info("Test '" + test + "' succeeded");
      return true;
    } catch (Throwable t) {
      logger.warn("Test '" + test + "' failed: {}", t.getMessage(), t);
      return false;
    }
  }

  /**
   * Helper class used to sort the integration tests.
   */
  private static final class IntegrationTestComparator implements Comparator<IntegrationTest> {

    /**
     * Creates a new comparator.
     */
    IntegrationTestComparator() {
      // Nothing to do here.
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(IntegrationTest test, IntegrationTest otherTest) {
      // Sort by test group
      String testGroup = StringUtils.trimToEmpty(test.getGroup());
      String otherTestGroup = StringUtils.trimToEmpty(otherTest.getGroup());
      int groupComparison = testGroup.compareTo(otherTestGroup);
      if (groupComparison != 0) {
        if (IntegrationTest.WEBLOUNGE_CONTENT_TEST_GROUP.equals(testGroup))
          return -1;
        else if (IntegrationTest.WEBLOUNGE_ENDPOINT_TEST_GROUP.equals(otherTestGroup))
          return -1;
        else if (IntegrationTest.WEBLOUNGE_CONTENT_TEST_GROUP.equals(otherTestGroup))
          return 1;
        else if (IntegrationTest.WEBLOUNGE_ENDPOINT_TEST_GROUP.equals(otherTestGroup))
          return 1;
        return groupComparison;
      }
      
      // Sort by order
      Integer testOrder = Integer.valueOf(test.getExecutionOrder());
      Integer otherTestOrder = Integer.valueOf(otherTest.getExecutionOrder());
      int orderComparison = testOrder.compareTo(otherTestOrder);
      if (orderComparison != 0)
        return orderComparison;
      return test.getName().compareTo(otherTest.getName());
    }

  }

}
