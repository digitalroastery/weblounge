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

import ch.entwine.weblounge.testing.IntegrationTest;

/**
 * Convenience implementation for integration tests.
 */
public abstract class IntegrationTestBase implements IntegrationTest {

  /** The test name */
  protected String name = null;

  /** Name of the test group */
  protected String groupName = null;

  /** Execution order */
  protected int executionOrder = 0;

  /**
   * Creates a new test base for an integration test with the given name.
   * 
   * @param name
   *          the name
   */
  protected IntegrationTestBase(String name) {
    this(name, null, 0);
  }

  /**
   * Creates a new test base for an integration test with the given name. The
   * test will be a member of the group with name <code>groupName</code>.
   * <p>
   * Pass <code>null</code> for <code>groupName</code> to indicate that the test
   * should not belong to any group.
   * 
   * @param name
   *          the name
   * @param groupName
   *          name of the test group
   */
  protected IntegrationTestBase(String name, String groupName) {
    this(name, groupName, 0);
  }

  /**
   * Creates a new test base for an integration test with the given name. The
   * test will be a member of the group with name <code>groupName</code> and be
   * executed within that group according to <code>executionOrder</code>.
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
  protected IntegrationTestBase(String name, String groupName,
      int executionOrder) {
    if (name == null)
      throw new IllegalArgumentException("Name cannot be null");
    this.name = name;
    this.groupName = groupName;
    this.executionOrder = executionOrder;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.kernel.IntegrationTest#execute(java.lang.String)
   */
  public abstract void execute(String serverUrl) throws Exception;

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.kernel.IntegrationTest#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the test's group name.
   * 
   * @param group
   *          the group name
   */
  public void setGroup(String group) {
    this.groupName = group;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.IntegrationTest#getGroup()
   */
  public String getGroup() {
    return groupName;
  }

  /**
   * Sets the execution order within the test's group. Lower numbers are ranked
   * higher.
   * 
   * @param order
   *          the execution order
   */
  public void setExecutionOrder(int order) {
    this.executionOrder = order;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.testing.IntegrationTest#getExecutionOrder()
   */
  public int getExecutionOrder() {
    return executionOrder;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName().toLowerCase();
  }

}
