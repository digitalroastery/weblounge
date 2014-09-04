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

package ch.entwine.weblounge.common.impl.security;

import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.security.Security;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link WebloungeAdminImpl}.
 */
public class WebloungeAdminImplTest extends UserImplTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    this.login = Security.ADMIN_USER;
    this.realm = Security.SYSTEM_CONTEXT;
    this.name = Security.ADMIN_NAME;
    user = new WebloungeAdminImpl(login);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.UserImpl#setRealm(java.lang.String)}.
   */
  @Test
  public void testSetRealm() {
    try {
      user.setRealm("test");
    } catch (UnsupportedOperationException e) {
      // This is expected
    }
  }

  @Test
  public void testHasAdminRole() {
    assertTrue(SecurityUtils.userHasRole(user, SystemRole.SYSTEMADMIN));
    assertTrue(SecurityUtils.userHasRole(user, SystemRole.SITEADMIN));
    assertTrue(SecurityUtils.userHasRole(user, SystemRole.EDITOR));
    // FIXME Re-enable this assertion once the GUEST role is no longer included in the EDITOR role
    //assertFalse(SecurityUtils.userHasRole(user, SystemRole.GUEST));
  }

  /**
   * Test method for
   * {@link WebloungeAdminImpl#implies(ch.entwine.weblounge.common.security.Authority)}
   */
  @Test
  public void testImpliesSystemAdministratorRole() {
    assertTrue(user.implies(SystemRole.SYSTEMADMIN));
    assertTrue(user.implies(SystemRole.GUEST));
    assertTrue(user.implies(new RoleImpl("foo", "bar")));
  }

}
