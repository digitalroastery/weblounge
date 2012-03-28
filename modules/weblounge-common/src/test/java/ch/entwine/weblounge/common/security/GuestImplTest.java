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

package ch.entwine.weblounge.common.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.security.Guest;
import ch.entwine.weblounge.common.impl.security.SystemRole;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

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
    guest = new Guest(Security.SYSTEM_CONTEXT);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getRoleClosure()}
   * .
   */
  @Test
  public void testGetRoleClosure() {
    Set<Object> roles = guest.getPublicCredentials(Role.class);
    assertEquals(1, roles.size());
    assertTrue(SystemRole.GUEST.equals(roles.iterator().next()));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#hasRole(ch.entwine.weblounge.common.security.Role)}
   * .
   */
  @Test
  public void testHasRoleRole() {
    assertTrue(guest.getPublicCredentials().contains(SystemRole.GUEST));
  }

}
