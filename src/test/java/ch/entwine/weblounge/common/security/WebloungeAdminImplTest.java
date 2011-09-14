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

import ch.entwine.weblounge.common.impl.security.WebloungeAdminImpl;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link WebloungeAdminImpl}.
 */
public class WebloungeAdminImplTest extends WebloungeUserImplTest {
  
  /** Name of the site administrator */
  protected String adminName = null;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setUpPrerequisites();
    user = new WebloungeAdminImpl(login);
    realm = Security.SYSTEM_CONTEXT;
    adminName = "Weblounge Administrator";
    setUpUser();
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

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.UserImpl#getRealm()}.
   */
  @Test
  public void testGetRealm() {
    assertEquals(Security.SYSTEM_CONTEXT, user.getRealm());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getName()}.
   */
  @Test
  public void testGetName() {
    assertEquals(adminName, user.getName());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getName()}.
   */
  @Test
  public void testGetNameFirstnameOnly() {
    user.setLastName(null);
    assertEquals(adminName, user.getName());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getName()}.
   */
  @Test
  public void testGetNameLastnameOnly() {
    user.setFirstName(null);
    assertEquals(adminName, user.getName());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getName()}.
   */
  @Test
  public void testGetNameNoFirstNoLastname() {
    user.setFirstName(null);
    user.setLastName(null);
    assertEquals(adminName, user.getName());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#setFirstName(java.lang.String)}.
   */
  @Test
  public void testSetFirstName() {
    user.setFirstName("James");
    assertEquals("James", user.getFirstName());
    assertEquals(adminName, user.getName());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#setLastName(java.lang.String)}.
   */
  @Test
  public void testSetLastName() {
    user.setLastName("Joyce");
    assertEquals("Joyce", user.getLastName());
    assertEquals(adminName, user.getName());
  }

}
