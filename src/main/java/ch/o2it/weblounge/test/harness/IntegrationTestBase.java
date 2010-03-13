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

package ch.o2it.weblounge.test.harness;

/**
 * Convenience implementation for integration tests which deals with names and
 * other secondary stuff.
 */
public abstract class IntegrationTestBase implements IntegrationTest {

  /** The test name */
  protected String name = null;

  /**
   * Creates a new test base for an integration test with the given name.
   * 
   * @param name
   *          the name
   */
  protected IntegrationTestBase(String name) {
    if (name == null)
      throw new IllegalArgumentException("Name cannot be null");
    this.name = name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.test.harness.IntegrationTest#execute(java.lang.String)
   */
  public abstract void execute(String serverUrl) throws Exception;

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.test.harness.IntegrationTest#getName()
   */
  public String getName() {
    return name;
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
