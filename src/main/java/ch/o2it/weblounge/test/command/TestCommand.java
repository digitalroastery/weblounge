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
import ch.o2it.weblounge.test.harness.HTMLActionTest;
import ch.o2it.weblounge.test.harness.IntegrationTest;
import ch.o2it.weblounge.test.harness.JSONActionTest;
import ch.o2it.weblounge.test.harness.JavaServerPagesTest;
import ch.o2it.weblounge.test.harness.ProtectedStaticResourcesTest;
import ch.o2it.weblounge.test.harness.StaticResourcesTest;
import ch.o2it.weblounge.test.harness.XMLActionTest;

import org.osgi.framework.BundleContext;
import org.osgi.service.command.CommandProcessor;
import org.osgi.service.command.CommandSession;
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
  private static final Logger log_ = LoggerFactory.getLogger(TestCommand.class);

  /** The tests */
  private List<IntegrationTest> tests = new ArrayList<IntegrationTest>();

  /**
   * Command signature that allows to do
   * <ul>
   * <li><code>test list</code></li>
   * <li><code>test all</code></li>
   * <li><code>test <id></li>
   * </ul>
   * 
   * @param session
   *          the command session
   * @param args
   *          the list of arguments to this command
   */
  public void test(CommandSession session, String[] args) {
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
    log_.info("------------------------------------------------------------------------");
    log_.info("Running Integration Tests");
    log_.info("------------------------------------------------------------------------");
    log_.info("Tests: " + tests.size());
    log_.info("Started at: " + WebloungeDateFormat.formatStatic(startDate));
    
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
    log_.info(" ");
    log_.info(" ");
    log_.info("------------------------------------------------------------------------");
    log_.info("Test Summary:");
    log_.info("------------------------------------------------------------------------");
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
      log_.info(buf.toString());
    }
    log_.info("------------------------------------------------------------------------");
    log_.info("------------------------------------------------------------------------");
    if (failed.size() == 0)
      log_.info("SUCCESS: " + succeeded.size() + " TEST" + (succeeded.size() > 1 ? "S" : "") + " PASSED");
    else
      log_.info("FAILURE: " + failed.size() + " TEST"  + (succeeded.size() > 1 ? "S" : "") + " FAILED");
    log_.info("------------------------------------------------------------------------");
    log_.info("Total time: " + ConfigurationUtils.toHumanReadableDuration(time));
    log_.info("Finished at: " + WebloungeDateFormat.formatStatic(endDate));
    log_.info("------------------------------------------------------------------------");
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
    log_.info("------------------------------------------------------------------------");
    log_.info("Running test '" + test + "'");
    log_.info("------------------------------------------------------------------------");
    try {
      test.execute("http://127.0.0.1:8080");
      log_.info("Test '" + test + "' succeeded");
      return true;
    } catch (Throwable t) {
      log_.warn("Test '" + test + "' failed: {}", t.getMessage(), t);
      return false;
    }
  }

  /**
   * Callback from the OSGi environment to activate the commands.
   * 
   * @param context
   *          the component context
   */
  public void activate(ComponentContext context) {
    BundleContext bundleContext = context.getBundleContext();
    log_.debug("Registering test commands");
    Dictionary<String, Object> commands = new Hashtable<String, Object>();
    commands.put(CommandProcessor.COMMAND_SCOPE, "weblounge");
    commands.put(CommandProcessor.COMMAND_FUNCTION, new String[] {
        "test",
        "tests" });
    bundleContext.registerService(getClass().getName(), this, commands);

    // Load the tests
    tests.add(new HTMLActionTest());
    tests.add(new XMLActionTest());
    tests.add(new JSONActionTest());
    tests.add(new StaticResourcesTest());
    tests.add(new ProtectedStaticResourcesTest());
    tests.add(new JavaServerPagesTest());
  }

}
