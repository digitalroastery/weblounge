/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.common.impl.testing;


import java.util.ArrayList;
import java.util.List;

/**
 * This integration test is automatically created from a test definition using
 * the {@link IntegrationTestParser}.
 */
public class IntegrationTestGroup extends IntegrationTestBase {

  /** The list of test cases */
  protected List<IntegrationTestCase> testCases = new ArrayList<IntegrationTestCase>();

  /**
   * Creates an integration test with the given name.
   * 
   * @param name
   *          the test name
   */
  public IntegrationTestGroup(String name) {
    this(name, null, 0);
  }

  /**
   * Creates an integration test with the given name. The test will be a member
   * of the group with name <code>groupName</code>.
   * <p>
   * Pass <code>null</code> for <code>groupName</code> to indicate that the test
   * should not belong to any group.
   * 
   * @param name
   *          the test name
   * @param groupName
   *          the group name
   */
  public IntegrationTestGroup(String name, String groupName) {
    this(name, groupName, 0);
  }

  /**
   * Creates an integration test with the given name. The test will be a member
   * of the group with name <code>groupName</code> and be executed within that
   * group according to <code>executionOrder</code>.
   * <p>
   * Pass <code>null</code> for <code>groupName</code> to indicate that the test
   * should not belong to any group. Also note that smaller execution orders are
   * ranked higher, i. e. tests are executed earlier.
   * 
   * @param name
   *          the name
   * @param groupName
   *          name of the test group
   * @param executionOrder
   *          the execution order
   */
  public IntegrationTestGroup(String name, String groupName, int executionOrder) {
    super(name, groupName, executionOrder);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.testing.IntegrationTestBase#execute(java.lang.String)
   */
  @Override
  public void execute(String serverUrl) throws Exception {
    for (IntegrationTestCase testCase : testCases) {
      testCase.execute(serverUrl);
    }
  }

  /**
   * Adds the integration test case to the list of test cases.
   * 
   * @param testCase
   *          the test case
   * @throws IllegalArgumentException
   *           if the test case is <code>null</code>
   */
  public void addTestCase(IntegrationTestCase testCase)
      throws IllegalArgumentException {
    if (testCase == null)
      throw new IllegalArgumentException("Test case must not be null");
    testCases.add(testCase);
  }

  /**
   * Returns the list of test cases.
   * 
   * @return the test cases
   */
  public List<IntegrationTestCase> getTestCases() {
    return testCases;
  }

}
