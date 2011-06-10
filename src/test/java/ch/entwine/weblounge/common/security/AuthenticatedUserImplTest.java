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
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl;
import ch.entwine.weblounge.common.impl.security.PasswordImpl;
import ch.entwine.weblounge.common.impl.security.RoleImpl;
import ch.entwine.weblounge.common.impl.security.SystemRole;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

/**
 * Test case for the implementation at {@link AuthenticatedUserImpl}.
 */
public class AuthenticatedUserImplTest {
  
  /** Login */
  protected String login = "john";
  
  /** User realm */
  protected String realm = "testland";
  
  /** The password */
  protected String password = "pass";

  /** The private key */
  protected String privateKey = "secret";

  /** The public key */
  protected String publicKey = "not-so-secret";

  /** The user under test */
  protected AuthenticatedUserImpl user = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    user = new AuthenticatedUserImpl(login, realm);
    user.addPrivateCredentials(new PasswordImpl(password, DigestType.plain));
    user.addPublicCredentials(SystemRole.EDITOR.getClosure());
    user.addPublicCredentials(publicKey);
    user.addPublicCredentials(Long.valueOf(1));
    user.addPrivateCredentials(privateKey);
    user.addPrivateCredentials(Long.valueOf(1));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getLogin()}.
   */
  @Test
  public void testGetLogin() {
    assertEquals(login, user.getLogin());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#setLoginContext(javax.security.auth.login.LoginContext)}.
   */
  @Test @Ignore
  public void testSetLoginContext() {
    System.err.println("Don't know how to test this");
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getLoginContext()}.
   */
  @Test @Ignore
  public void testGetLoginContext() {
    System.err.println("Don't know how to test this");
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getPassword()}.
   */
  @Test
  public void testGetPassword() throws Exception {
    byte[] pwmd5 = DigestUtils.md5(password.getBytes("utf-8"));
    user.addPrivateCredentials(new PasswordImpl(new String(pwmd5), DigestType.md5));
    assertEquals(4, user.getPrivateCredentials().size());
    for (Object o : user.getPrivateCredentials(Password.class)) {
      Password pw = (Password)o;
      if (DigestType.plain.equals(pw.getDigestType())) {
        assertEquals(this.password, pw.getPassword());
      } else {
        assertEquals(new String(pwmd5), pw.getPassword());
      }
    }
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getPublicCredentials()}.
   */
  @Test
  public void testGetPublicCredentials() {
    assertEquals(5, user.getPublicCredentials().size());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getPublicCredentials(java.lang.Class)}.
   */
  @Test
  public void testGetPublicCredentialsClassOfQ() {
    assertEquals(1, user.getPublicCredentials(String.class).size());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getPrivateCredentials()}.
   */
  @Test
  public void testGetPrivateCredentials() {
    assertEquals(3, user.getPrivateCredentials().size());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getPrivateCredentials(java.lang.Class)}.
   */
  @Test
  public void testGetPrivateCredentialsClassOfQ() {
    assertEquals(1, user.getPrivateCredentials(String.class).size());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#setLogin(java.lang.String)}.
   */
  @Test
  public void testSetLogin() {
    user.setLogin("test");
    assertEquals("test", user.getLogin());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#assignRole(ch.entwine.weblounge.common.security.Role)}.
   */
  @Test
  public void testAssignRole() {
    Role r = new RoleImpl("addicts", "whisky");
    user.addPublicCredentials(r);
    assertEquals(3, user.getPrivateCredentials().size());
    assertTrue(user.getPublicCredentials().contains(r));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getRoles()}.
   */
  @Test
  public void testGetRoles() {
    Set<Object> roles = user.getPublicCredentials(Role.class);
    assertEquals(3, roles.size());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#hasRole(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testHasRoleStringString() {
    Role editor = SystemRole.EDITOR;
    assertTrue(user.getPublicCredentials().contains(new RoleImpl(editor.getContext(), editor.getIdentifier())));
    assertFalse(user.getPublicCredentials().contains(new RoleImpl("test", editor.getIdentifier())));
    assertFalse(user.getPublicCredentials().contains(new RoleImpl(editor.getContext(), "test")));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#hasRole(ch.entwine.weblounge.common.security.Role)}.
   */
  @Test
  public void testHasRoleRole() {
    Role editor = SystemRole.EDITOR;
    assertTrue(user.getPublicCredentials().contains(editor));
    assertTrue(user.getPublicCredentials().contains(new RoleImpl(editor.getContext(), editor.getIdentifier())));
    assertTrue(user.getPublicCredentials().contains(SystemRole.TRANSLATOR));
    assertFalse(user.getPublicCredentials().contains(SystemRole.PUBLISHER));
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getAuthorityType()}.
   */
  @Test
  public void testGetAuthorityType() {
    assertEquals(User.class.getName(), user.getAuthorityType());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#getAuthorityId()}.
   */
  @Test
  public void testGetAuthorityId() {
    assertEquals("testland:john", user.getAuthorityId());
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#isAuthorizedBy(ch.entwine.weblounge.common.security.Authority)}.
   */
  @Test @Ignore
  public void testIsAuthorizedBy() {
    fail("Not yet implemented");

//    User u = new UserImpl("john", "testland", "John Doe");
//
//    AuthenticatedUser translator = new AuthenticatedUserImpl("translator");
//    translator.assignRole(SystemRole.TRANSLATOR);
//    assertFalse(user.isAuthorizedBy(translator));
//
//    AuthenticatedUser editor = new AuthenticatedUserImpl("editor");
//    editor.assignRole(SystemRole.EDITOR);
//    assertFalse(user.isAuthorizedBy(editor));
//
//    AuthenticatedUser publisher = new AuthenticatedUserImpl("publisher");
//    editor.assignRole(SystemRole.PUBLISHER);
//    assertFalse(user.isAuthorizedBy(publisher));
  }

}
