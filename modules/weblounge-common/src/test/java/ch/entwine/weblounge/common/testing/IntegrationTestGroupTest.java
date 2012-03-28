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

package ch.entwine.weblounge.common.testing;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.impl.testing.IntegrationTestCase;
import ch.entwine.weblounge.common.impl.testing.IntegrationTestGroup;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test case for {@link IntegrationTestGroup}.
 */
public class IntegrationTestGroupTest {

  /** The group to test */
  protected IntegrationTestGroup group = null;

  /** Name of the test group */
  protected String name = "test-group";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    group = new IntegrationTestGroup(name);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.testing.IntegrationTestGroup#execute(java.lang.String)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public void testExecute() throws Exception {
    final List<Object> calls = new ArrayList<Object>();
    for (int i = 0; i < 3; i++) {
      IntegrationTestCase testCase = new IntegrationTestCase("test", "/test", null) {
        /**
         * {@inheritDoc}
         * 
         * @see ch.entwine.weblounge.common.impl.testing.IntegrationTestCase#execute(java.lang.String)
         */
        @Override
        public void execute(String serverUrl) throws Exception {
          calls.add(new Object());
        }
      };
      group.addTestCase(testCase);
    }
    group.execute("localhost");
    assertEquals(3, calls.size());
  }

}
