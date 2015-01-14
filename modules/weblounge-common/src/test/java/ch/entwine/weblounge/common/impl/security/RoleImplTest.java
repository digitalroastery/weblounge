/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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

package ch.entwine.weblounge.common.impl.security;

import org.junit.Test;

/**
 * Test cases for class {@link RoleImpl}
 */
public class RoleImplTest {
  
  /**
   * Test case for constructor {@link RoleImpl(String, String)}
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWithNullContext() {
    new RoleImpl(null, "identifier");
  }
  
  /**
   * Test case for constructor {@link RoleImpl(String, String)}
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWithNullIdentifier() {
    new RoleImpl("context", null);
  }

  /**
   * Test case for constructor {@link RoleImpl(String)}
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWithNullRole() {
    new RoleImpl(null);
  }
  

}
