/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.security;

import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.security.Guest;
import ch.o2it.weblounge.common.impl.security.SystemRole;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for the implementation of {@link Guest}.
 */
public class GuestImplTest {

  /** The guest instance under test */
  protected Guest guest = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    guest = new Guest();
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.security.AuthenticatedUserImpl#getRoleClosure()}
   * .
   */
  @Test
  @Ignore
  public void testGetRoleClosure() {
    // assertEquals(1, guest.getRoleClosure().length);
    // assertTrue(guest.getRoleClosure()[0].equals(SystemRole.GUEST));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.security.AuthenticatedUserImpl#hasRole(ch.o2it.weblounge.common.security.Role)}
   * .
   */
  @Test
  public void testHasRoleRole() {
    assertTrue(guest.getPublicCredentials().contains(SystemRole.GUEST));
  }

}
