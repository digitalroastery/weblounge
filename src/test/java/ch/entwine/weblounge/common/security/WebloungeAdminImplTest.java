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

import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.security.WebloungeAdminImpl;
import ch.entwine.weblounge.common.site.Site;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link WebloungeAdminImpl}.
 */
public class WebloungeAdminImplTest extends WebloungeUserImplTest {
  
  /** Name of the site administrator */
  protected String adminName = null;
  
  /** Test user */
  protected UserImpl user = null;

  /** The site object */
  protected Site mockSite = null;

  /** Login. Static because of the weblounge administrator tests */
  protected static String login = "john";

  /** Realm */
  protected String realm = "testland";

  /** Password */
  protected String password = "pass";

  /** The digest */
  protected DigestType passwordDigestType = DigestType.md5;

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
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.UserImpl#getName()}.
   */
  @Test
  public void testGetName() {
    assertEquals(adminName, user.getName());
  }

}
