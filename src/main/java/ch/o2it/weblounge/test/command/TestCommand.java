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

package ch.o2it.weblounge.test.command;

import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.test.harness.ContentRepositoryEndpointTest;
import ch.o2it.weblounge.test.harness.HTMLActionTest;
import ch.o2it.weblounge.test.harness.I18nTest;
import ch.o2it.weblounge.test.harness.IntegrationTest;
import ch.o2it.weblounge.test.harness.JSONActionTest;
import ch.o2it.weblounge.test.harness.JavaServerPagesTest;
import ch.o2it.weblounge.test.harness.ProtectedStaticResourcesTest;
import ch.o2it.weblounge.test.harness.SearchEndpointTest;
import ch.o2it.weblounge.test.harness.StaticResourcesTest;
import ch.o2it.weblounge.test.harness.XMLActionTest;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
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

  /**
   * Creates a new test command.
   */
  public TestCommand() {
    tests.add(new HTMLActionTest());
    tests.add(new XMLActionTest());
    tests.add(new JSONActionTest());
    tests.add(new StaticResourcesTest());
    tests.add(new ProtectedStaticResourcesTest());
    tests.add(new JavaServerPagesTest());
    tests.add(new I18nTest());
    tests.add(new ContentRepositoryEndpointTest());
    tests.add(new SearchEndpointTest());
  }
  
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
    commands.put("osgi.command.function", new String[] { "test", "tests" });
    bundleContext.registerService(getClass().getName(), this, commands);
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
      if ("list".equals(args[0]))
        list();
    } else if (args.length == 2) {
      if ("run".equals(args[0])) {
        if ("all".equals(args[1])) {
          executeAll(tests);
        } else {
          String id = args[1];

          // Look up the test
          IntegrationTest test = null;
          try {
            test = tests.get(Integer.parseInt(id) - 1);
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

    // Setup the number formatter
    int digits = 1 + (int) (Math.log(tests.size() + 1) / Math.log(10));
    StringBuffer format = new StringBuffer();
    for (int i = 0; i < digits; i++)
      format.append("#");
    DecimalFormat formatter = new DecimalFormat(format.toString());

    // Print the header
    StringBuffer header = new StringBuffer();
    for (int i = 0; i < digits; i++)
      header.append(" ");
    header.append("Id    ");
    header.append(" Name");
    System.out.println(header.toString());

    // Display the test list
    for (int i = 0; i < tests.size(); i++) {
      IntegrationTest test = tests.get(i);
      StringBuffer buf = new StringBuffer();
      buf.append("[ ").append(formatter.format(i + 1)).append(" ] ");
      while (buf.length() < 8)
        buf.append(" ");
      buf.append(test.getName());
      System.out.println(buf.toString());
    }
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

    // Print the summary
    logger.info(" ");
    logger.info(" ");
    logger.info("------------------------------------------------------------------------");
    logger.info("Test Summary:");
    logger.info("------------------------------------------------------------------------");
    for (IntegrationTest test : tests) {
      StringBuffer buf = new StringBuffer(test.getName());
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

}
