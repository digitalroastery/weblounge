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

package ch.o2it.weblounge.test.site;

import ch.o2it.weblounge.common.impl.site.SiteImpl;
import ch.o2it.weblounge.common.impl.testing.IntegrationTestBase;
import ch.o2it.weblounge.testing.IntegrationTest;

import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Implementation of the <code>Site</code> API that hosts the weblounge
 * integration test suite.
 */
public class TestSite extends SiteImpl {

  /** Serial version UID */
  private static final long serialVersionUID = -241760236588222514L;

  /** Logger */
  private static final Logger logger = LoggerFactory.getLogger(TestSite.class);

  /** Name of the package containing the tests */
  private final static String TEST_PKG = "ch/o2it/weblounge/test/harness";

  /**
   * Creates a new test site implementation.
   */
  public TestSite() {
    setAutoStart(true);
  }

  /**
   * Callback from the OSGi environment on component activation.
   * 
   * @param ctx
   *          the component context
   */
  @Override
  protected void activate(ComponentContext ctx) throws Exception {
    super.activate(ctx);
    
    Bundle bundle = ctx.getBundleContext().getBundle();
    List<IntegrationTest> tests = loadIntegrationTests(TEST_PKG, bundle);
    for (IntegrationTest test : tests) {
      logger.debug("Registering integration test " + test.getClass());
      ctx.getBundleContext().registerService(IntegrationTest.class.getName(), test, null);
    }
  }

  /**
   * Loads the integration test classes from the class path and publishes them
   * to the OSGi registry.
   * 
   * @param path
   *          the bundle path to load classes from
   * @param bundle
   *          the bundle
   */
  private List<IntegrationTest> loadIntegrationTests(String path, Bundle bundle) {
    List<IntegrationTest> tests = new ArrayList<IntegrationTest>();

    // Load the classes in question
    ClassLoader loader = this.getClass().getClassLoader();
    Enumeration<?> entries = bundle.findEntries(path, "*.class", true);
    if (entries == null) {
      logger.warn("No integration tests found in package " + TEST_PKG);
      return tests;
    }

    // Look at the classes and instantiate those that implement the integration
    // test interface.
    while (entries.hasMoreElements()) {
      URL url = (URL) entries.nextElement();
      Class<?> c = null;
      try {
        String pathToClass = url.getPath();
        pathToClass = pathToClass.substring(1, pathToClass.indexOf(".class"));
        pathToClass = pathToClass.replace('/', '.');
        c = loader.loadClass(pathToClass);
        boolean implementsInterface = Arrays.asList(c.getInterfaces()).contains(IntegrationTest.class);
        boolean extendsBaseClass = IntegrationTestBase.class.getName().equals(c.getSuperclass().getName());
        if (!implementsInterface && !extendsBaseClass)
          continue;
        IntegrationTest test = (IntegrationTest) c.newInstance();
        tests.add(test);
      } catch (InstantiationException e) {
        logger.error("Error creating instance of integration test " + c);
      } catch (IllegalAccessException e) {
        logger.error("Access error creating integration test instance of " + c);
      } catch (ClassNotFoundException e1) {
        logger.error("Url " + url + " is not a class");
      }
    }
    return tests;
  }

}
