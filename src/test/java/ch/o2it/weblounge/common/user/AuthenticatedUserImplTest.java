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

package ch.o2it.weblounge.common.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.security.GroupImpl;
import ch.o2it.weblounge.common.impl.security.RoleImpl;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.security.DigestType;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.Role;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for the implementation at {@link AuthenticatedUserImpl}.
 */
public class AuthenticatedUserImplTest {
  
  /** Login */
  protected String login = "john";
  
  /** User realm */
  protected String realm = "testland";
  
  /** The password */
  protected byte[] password = "pass".getBytes();

  /** Whisky drinker group */
  protected Group addictsGroup = new GroupImpl("alcoholics", "addicts");

  /** Whisky drinker group */
  protected Group whiskyDrinkerGroup = new GroupImpl("whisky", "addicts");

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
    user.setPassword(password, DigestType.plain);
    user.addMembership(whiskyDrinkerGroup);
    user.assignRole(SystemRole.EDITOR);
    addictsGroup.addMember(whiskyDrinkerGroup);
    user.addPublicCredentials(publicKey);
    user.addPublicCredentials(Long.valueOf(1));
    user.addPrivateCredentials(privateKey);
    user.addPrivateCredentials(Long.valueOf(1));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getLogin()}.
   */
  @Test
  public void testGetLogin() {
    assertEquals(login, user.getLogin());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#setLoginContext(javax.security.auth.login.LoginContext)}.
   */
  @Test
  public void testSetLoginContext() {
    System.err.println("Don't know how to test this");
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getLoginContext()}.
   */
  @Test
  public void testGetLoginContext() {
    System.err.println("Don't know how to test this");
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#setPassword(byte[], ch.o2it.weblounge.common.security.DigestType)}.
   */
  @Test
  public void testSetPasswordByteArrayDigestType() {
    String pass = "pass";
    user.setPassword(pass.getBytes(), DigestType.plain);
    assertEquals(DigestType.plain, user.getPasswordDigestType());
    assertEquals(new String(pass.getBytes()), new String(user.getPassword()));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#setPassword(byte[])}.
   */
  @Test
  public void testSetPasswordByteArray() {
    user.setPassword("pass".getBytes());
    assertEquals(DigestType.md5, user.getPasswordDigestType());
    assertEquals(new String(DigestUtils.md5("pass".getBytes())), new String(user.getPassword()));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getPassword()}.
   */
  @Test
  public void testGetPassword() {
    assertEquals(DigestType.plain, user.getPasswordDigestType());
    assertEquals(new String(password), new String(user.getPassword()));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#checkPassword(java.lang.String)}.
   */
  @Test
  public void testCheckPassword() {
    assertTrue(user.checkPassword(new String(password)));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getPasswordDigestType()}.
   */
  @Test
  public void testGetPasswordDigestType() {
    assertEquals(DigestType.plain, user.getPasswordDigestType());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getPublicCredentials()}.
   */
  @Test
  public void testGetPublicCredentials() {
    assertEquals(2, user.getPublicCredentials().size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getPublicCredentials(java.lang.Class)}.
   */
  @Test
  public void testGetPublicCredentialsClassOfQ() {
    assertEquals(1, user.getPublicCredentials(String.class).size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getPrivateCredentials()}.
   */
  @Test
  public void testGetPrivateCredentials() {
    assertEquals(2, user.getPrivateCredentials().size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getPrivateCredentials(java.lang.Class)}.
   */
  @Test
  public void testGetPrivateCredentialsClassOfQ() {
    assertEquals(1, user.getPrivateCredentials(String.class).size());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#setLogin(java.lang.String)}.
   */
  @Test
  public void testSetLogin() {
    user.setLogin("test");
    assertEquals("test", user.getLogin());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#addMembership(ch.o2it.weblounge.common.security.Group)}.
   */
  @Test
  public void testAddMembership() {
    Group g = new GroupImpl("martini", "drinkers");
    user.addMembership(g);
    assertTrue(user.isMemberOf(g));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#removeMembership(ch.o2it.weblounge.common.security.Group)}.
   */
  @Test
  public void testRemoveMembership() {
    user.removeMembership(whiskyDrinkerGroup);
    assertFalse(user.isMemberOf(whiskyDrinkerGroup));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getGroupClosure()}.
   */
  @Test
  public void testGetGroupClosure() {
    assertEquals(2, user.getGroupClosure().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getGroups()}.
   */
  @Test
  public void testGetGroups() {
    assertEquals(1, user.getGroups().length);
    assertEquals(whiskyDrinkerGroup, user.getGroups()[0]);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#isMemberOf(ch.o2it.weblounge.common.security.Group)}.
   */
  @Test
  public void testIsMemberOf() {
    assertTrue(user.isMemberOf(whiskyDrinkerGroup));
    assertFalse(user.isMemberOf(addictsGroup));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#assignRole(ch.o2it.weblounge.common.security.Role)}.
   */
  @Test
  public void testAssignRole() {
    Role r = new RoleImpl("addicts", "whisky");
    user.assignRole(r);
    assertEquals(2, user.getRoles().length);
    assertTrue(user.hasRole(r));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#unassignRole(ch.o2it.weblounge.common.security.Role)}.
   */
  @Test
  public void testUnassignRole() {
    user.unassignRole(SystemRole.EDITOR);
    assertEquals(0, user.getRoles().length);
    assertFalse(user.hasRole(SystemRole.EDITOR));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getRoleClosure()}.
   */
  @Test
  public void testGetRoleClosure() {
    assertEquals(3, user.getRoleClosure().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getRoles()}.
   */
  @Test
  public void testGetRoles() {
    assertEquals(1, user.getRoles().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#hasRole(java.lang.String, java.lang.String)}.
   */
  @Test
  public void testHasRoleStringString() {
    Role editor = SystemRole.EDITOR;
    assertTrue(user.hasRole(editor.getContext(), editor.getIdentifier()));
    assertFalse(user.hasRole("test", editor.getIdentifier()));
    assertFalse(user.hasRole(editor.getContext(), "test"));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#hasRole(ch.o2it.weblounge.common.security.Role)}.
   */
  @Test
  public void testHasRoleRole() {
    Role editor = SystemRole.EDITOR;
    assertTrue(user.hasRole(editor));
    assertTrue(user.hasRole(new RoleImpl(editor.getContext(), editor.getIdentifier())));
    assertTrue(user.hasRole(SystemRole.TRANSLATOR));
    assertFalse(user.hasRole(SystemRole.PUBLISHER));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getAuthorityType()}.
   */
  @Test
  public void testGetAuthorityType() {
    assertEquals(User.class.getName(), user.getAuthorityType());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#getAuthorityId()}.
   */
  @Test
  public void testGetAuthorityId() {
    assertEquals("testland:john", user.getAuthorityId());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.user.AuthenticatedUserImpl#isAuthorizedBy(ch.o2it.weblounge.common.security.Authority)}.
   */
  @Test @Ignore
  public void testIsAuthorizedBy() {
    fail("Not yet implemented");

    User u = new UserImpl("john", "testland", "John Doe");

    AuthenticatedUser translator = new AuthenticatedUserImpl("translator");
    translator.assignRole(SystemRole.TRANSLATOR);
    assertFalse(user.isAuthorizedBy(translator));

    AuthenticatedUser editor = new AuthenticatedUserImpl("editor");
    editor.assignRole(SystemRole.EDITOR);
    assertFalse(user.isAuthorizedBy(editor));

    AuthenticatedUser publisher = new AuthenticatedUserImpl("publisher");
    editor.assignRole(SystemRole.PUBLISHER);
    assertFalse(user.isAuthorizedBy(publisher));
  }

}
