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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.entwine.weblounge.common.impl.language.English;
import ch.entwine.weblounge.common.impl.security.PasswordImpl;
import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.security.WebloungeUserImpl;
import ch.entwine.weblounge.common.impl.util.WebloungeDateFormat;
import ch.entwine.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Set;

/**
 * Tests the {@link WebloungeUserImpl} implementation.
 */
public class WebloungeUserImplTest {

  /** Test user */
  protected WebloungeUserImpl user = null;

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

  /** First name */
  protected String firstname = "John";

  /** Last name */
  protected String lastname = "Doe";

  /** Initials */
  protected static final String initials = "jd";

  /** E-Mail */
  protected String email = "john.doe@testland.org";

  /** The last login date */
  protected Date lastLoginDate = null;

  /** Where the last login came from */
  protected String lastLoginSource = "192.168.0.1";

  /** Test property */
  protected static final String propertyName = "ch.entwine.weblounge.lastpage";

  /** Test property value */
  protected String propertyValue = "/test";
  
  /** Application role */
  protected Role apprenticeRole = new RoleImpl("myapp", "apprentice");

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setUpPrerequisites();
    user = new WebloungeUserImpl(login, realm);
    setUpUser();
  }

  /**
   * Initializes a mock site, the last login date etc.
   * 
   * <p>
   * This is not done in the constructor so that other test classes can easily
   * overwrite this one and do not need to do all of the setup themselves.
   * 
   * @throws Exception
   *           if setup fails
   */
  protected void setUpPrerequisites() throws Exception {
    mockSite = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(mockSite.getIdentifier()).andReturn("test");
    EasyMock.expect(mockSite.getRole("apprentice", "myapp")).andReturn(apprenticeRole);
    EasyMock.replay(mockSite);
    lastLoginDate = WebloungeDateFormat.parseStatic("2009-03-17T03:22:05Z");
  }

  /**
   * Initializes the user. This is not done in the constructor so that other
   * test classes can easily overwrite this one and do not need to do all of the
   * setup themselves.
   */
  protected void setUpUser() throws Exception {
    user.addPrivateCredentials(new PasswordImpl(password, passwordDigestType));
    user.setFirstName(firstname);
    user.setLastName(lastname);
    user.setLanguage(English.getInstance());
    user.setEmail(email);
    user.addPublicCredentials(apprenticeRole);
    lastLoginDate = WebloungeDateFormat.parseStatic("2009-03-17T03:22:05Z");
    user.setLastLogin(lastLoginDate, lastLoginSource);
    user.setProperty(propertyName, propertyValue);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.UserImpl#getLogin()}.
   */
  @Test
  public void testGetLogin() {
    assertEquals(login, user.getLogin());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.UserImpl#setRealm(java.lang.String)}
   * .
   */
  @Test
  public void testSetRealm() {
    String realm = "wonderland";
    user.setRealm(realm);
    assertEquals(realm, user.getRealm());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.UserImpl#getRealm()}.
   */
  @Test
  public void testGetRealm() {
    assertEquals(realm, user.getRealm());
    User u = new WebloungeUserImpl(login);
    assertEquals(User.DefaultRealm, u.getRealm());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.UserImpl#getRealm()}.
   */
  @Test
  public void testDefaultRealm() {
    User u = new WebloungeUserImpl(login);
    assertEquals(User.DefaultRealm, u.getRealm());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.UserImpl#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObject() {
    assertTrue(user.equals(new UserImpl(login, realm)));
    assertTrue(user.equals(new UserImpl(login, realm)));
    assertFalse(user.equals(new UserImpl(login, "wonderland")));
    assertFalse(user.equals(new UserImpl("james", realm)));
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.UserImpl#setName(java.lang.String)}
   * .
   */
  @Test
  public void testSetName() {
    String name = "James Joyce";
    user.setName(name);
    assertEquals(name, user.getName());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getName()}.
   */
  @Test
  public void testGetName() {
    String name = firstname + " " + lastname;
    assertEquals(name, user.getName());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getName()}.
   */
  @Test
  public void testGetNameFirstnameOnly() {
    user.setLastName(null);
    assertEquals(firstname, user.getName());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getName()}.
   */
  @Test
  public void testGetNameLastnameOnly() {
    user.setFirstName(null);
    assertEquals(lastname, user.getName());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getName()}.
   */
  @Test
  public void testGetNameNoFirstNoLastname() {
    user.setFirstName(null);
    user.setLastName(null);
    assertTrue(user.getName() == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#isEnabled()}.
   */
  @Test
  public void testIsEnabled() {
    assertTrue(user.isEnabled());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#canLogin()}.
   */
  @Test
  public void testCanLogin() {
    assertTrue(user.canLogin());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#canLogin()}.
   */
  @Test
  public void testCanLoginWithoutPassword() {
    Set<Object> pwList = user.getPrivateCredentials(Password.class);
    for (Object pw : pwList)
      user.removePrivateCredentials(pw);
    assertFalse(user.canLogin());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#canLogin()}.
   */
  @Test
  public void testCanLoginWithoutEnabled() {
    user.setEnabled(false);
    assertFalse(user.canLogin());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#setEnabled(boolean)}
   * .
   */
  @Test
  public void testSetEnabled() {
    user.setEnabled(false);
    assertFalse(user.isEnabled());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#setFirstName(java.lang.String)}
   * .
   */
  @Test
  public void testSetFirstName() {
    user.setFirstName("James");
    assertEquals("James", user.getFirstName());
    assertEquals("James Doe", user.getName());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getFirstName()}
   * .
   */
  @Test
  public void testGetFirstName() {
    assertEquals(firstname, user.getFirstName());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#setLastName(java.lang.String)}
   * .
   */
  @Test
  public void testSetLastName() {
    user.setLastName("Joyce");
    assertEquals("Joyce", user.getLastName());
    assertEquals("John Joyce", user.getName());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getLastName()}.
   */
  @Test
  public void testGetLastName() {
    assertEquals(lastname, user.getLastName());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getEmail()}.
   */
  @Test
  public void testGetEmail() {
    assertEquals(email, user.getEmail());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getLanguage()}.
   */
  @Test
  public void testGetLanguage() {
    assertEquals(English.getInstance(), user.getLanguage());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getInitials()}.
   */
  @Test
  public void testGetInitials() {
    assertEquals(initials, user.getInitials());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#setInitials(java.lang.String)}
   * .
   */
  @Test
  public void testSetInitials() {
    user.setInitials("test");
    assertEquals("test", user.getInitials());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getLastLogin()}
   * .
   */
  @Test
  public void testGetLastLogin() {
    assertEquals(lastLoginDate, user.getLastLogin());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getLastLoginFrom()}
   * .
   */
  @Test
  public void testGetLastLoginFrom() {
    assertEquals(lastLoginSource, user.getLastLoginFrom());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#getProperty(java.lang.String)}
   * .
   */
  @Test
  public void testGetProperty() {
    assertEquals(propertyValue, user.getProperty(propertyName));
    assertTrue(user.getProperty("test") == null);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.security.WebloungeUserImpl#removeProperty(java.lang.String)}
   * .
   */
  @Test
  public void testRemoveProperty() {
    assertTrue(user.removeProperty("test") == null);
    assertEquals(propertyValue, user.removeProperty(propertyName));
    assertTrue(user.removeProperty(propertyName) == null);
  }

  /**
   * Test method for role assignments.
   */
  @Test
  public void testRole() {
    assertTrue(user.getPublicCredentials().contains(apprenticeRole));
  }

}
