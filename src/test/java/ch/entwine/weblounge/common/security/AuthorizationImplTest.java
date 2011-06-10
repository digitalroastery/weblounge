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

import ch.entwine.weblounge.common.impl.security.AuthorityImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the implementation at {@link AuthorityImpl}.
 */
public class AuthorizationImplTest {
  
  /** Authorization type */
  protected String authorityType = Role.class.getName();

  /** Authority id */
  protected String authorityId = SystemRole.EDITOR.toString();
  
  /** The authorization instance under test */
  protected AuthorityImpl authority = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    authority = new AuthorityImpl(authorityType, authorityId);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthorityImpl#getAuthorityType()}.
   */
  @Test
  public void testGetAuthorityType() {
    assertEquals(authorityType, authority.getAuthorityType());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthorityImpl#getAuthorityId()}.
   */
  @Test
  public void testGetAuthorityId() {
    assertEquals(authorityId, authority.getAuthorityId());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthorityImpl#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    Authority a = new AuthorityImpl(authorityType, authorityId);
    assertTrue(authority.equals(a));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthorityImpl#isAuthorizedBy(ch.entwine.weblounge.common.security.Authority)}.
   */
  @Test
  public void testIsAuthorizedBy() {
    assertTrue(authority.isAuthorizedBy(authority));
  }

}
