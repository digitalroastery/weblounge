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


import static ch.entwine.weblounge.common.impl.security.SystemRole.EDITOR;
import static ch.entwine.weblounge.common.impl.security.SystemRole.GUEST;
import static ch.entwine.weblounge.common.impl.security.SystemRole.PUBLISHER;
import static ch.entwine.weblounge.common.security.SystemAction.PUBLISH;
import static ch.entwine.weblounge.common.security.SystemAction.WRITE;

import ch.entwine.weblounge.common.impl.util.xml.XMLUtils;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Role;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

/**
 * Test case for {@link SecurityUtils}.
 */

public class SecurityUtilsTest extends TestCase {

  /** The xml context definition */
  private Node allowConfig;

  /** The xml context definition */
  private Node denyConfig;

  /** the XPath object used to parse the configuration */
  private XPath path;

  public static void main(String[] args) {
    junit.textui.TestRunner.run(SecurityUtilsTest.class);
  }

  /**
   * {@inheritDoc}
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Before
  protected void setUp() throws Exception {
    super.setUp();
    allowConfig = createAllowSecurityContext();
    denyConfig = createDenySecurityContext();
    path = XMLUtils.getXPath();
  }

  /**
   * Test for void allow(Action, Authority)
   */
  @Test
  public final void testPermitPermissionAuthority() {
    SecurityContextImpl context = SecurityContextImpl.fromXml(allowConfig, path);

    // Deny all
    context.allow(PUBLISH, EDITOR);

    // Test (publish, translator) - expected: success
    if (!SecurityUtils.checkAuthorization(context, PUBLISH, EDITOR)) {
      fail("Check for Action " + PUBLISH + " and role " + EDITOR + " failed while it shouldn't");
    }
  }

  /**
   * Test for void allow(Action, Authority[])
   */
  @Test
  public final void testPermitPermissionAuthorityArray() {
    SecurityContextImpl context = SecurityContextImpl.fromXml(allowConfig, path);

    // Deny all
    context.allow(PUBLISH, EDITOR);

    // Test (publish, editor) - expected: success
    if (!SecurityUtils.checkAuthorization(context, PUBLISH, EDITOR)) {
      fail("Check for Action " + PUBLISH + " and role " + EDITOR + " failed while it shouldn't");
    }
  }

  /**
   * Test for void deny(Action, Authority)
   */
  @Test
  public final void testDenyPermissionAuthority() {
    SecurityContextImpl context = SecurityContextImpl.fromXml(denyConfig, path);

    // Deny all
    context.deny(WRITE, EDITOR);

    // Test (write, editor) - expected: failure
    if (SecurityUtils.checkAuthorization(context, WRITE, EDITOR)) {
      fail("Check for Action " + WRITE + " and role " + EDITOR + " passed while it shouldn't");
    }
  }

  /**
   * Test for void deny(Action, Authority[])
   */
  @Test
  public final void testDenyPermissionAuthorityArray() {
    SecurityContextImpl context = SecurityContextImpl.fromXml(denyConfig, path);

    // Deny all
    context.deny(WRITE, EDITOR);

    // Test (write, editor) - expected: failure
    if (SecurityUtils.checkAuthorization(context, WRITE, EDITOR)) {
      fail("Check for Action " + WRITE + " and role " + EDITOR + " passed while it shouldn't");
    }
  }

  /**
   * Test for denyAll()
   */
  @Test
  public final void testDenyAll() {
    SecurityContextImpl context = SecurityContextImpl.fromXml(denyConfig, path);

    // Deny all
    context.denyAll();

    // Test (write, editor) - expected: failure
    if (SecurityUtils.checkAuthorization(context, WRITE, EDITOR)) {
      fail("Check for Action " + WRITE + " and role " + EDITOR + " passed while it shouldn't");
    }

    // Test (publish, publisher) - expected: success
    if (SecurityUtils.checkAuthorization(context, PUBLISH, PUBLISHER)) {
      fail("Check for Action " + WRITE + " and role " + EDITOR + " passed while it shouldn't");
    }
  }

  /**
   * Test for denyAll(Action)
   */
  @Test
  public final void testDenyAllPermission() {
    SecurityContextImpl context = SecurityContextImpl.fromXml(denyConfig, path);

    // Deny all
    context.denyAll(WRITE);

    // Test (write, editor) - expected: failure
    if (SecurityUtils.checkAuthorization(context, WRITE, EDITOR)) {
      fail("Check for Action " + WRITE + " and role " + EDITOR + " passed while it shouldn't");
    }

    // Test (publish, publisher) - expected: success
    if (!SecurityUtils.checkAuthorization(context, PUBLISH, PUBLISHER)) {
      fail("Check for Action " + WRITE + " and role " + PUBLISHER + " failed while it shouldn't");
    }
  }

  /**
   * Test for boolean check(Action, Authority)
   */
  @Test
  public final void testCheckPermissionAuthority() {
    SecurityContextImpl context = SecurityContextImpl.fromXml(allowConfig, path);

    // Test (write, editor) - expected: success
    if (!SecurityUtils.checkAuthorization(context, WRITE, EDITOR)) {
      fail("Check for Action " + WRITE + " and role " + EDITOR + " failed while it shouldn't");
    }

    // Test (publish, editor) - expected: failure
    if (SecurityUtils.checkAuthorization(context, PUBLISH, EDITOR)) {
      fail("Check for Action " + PUBLISH + " and role " + EDITOR + " passed while it shouldn't");
    }

  }

  /**
   * Test checkOne(Action, Authority[])
   */
  @Test
  public final void testCheckOneOf() {
    Set<Action> actions = new HashSet<Action>();
    actions.add(WRITE);
    actions.add(PUBLISH);

    // Create the security context
    SecurityContextImpl context = SecurityContextImpl.fromXml(allowConfig, path);

    // Test one of (editor, publisher) - expected: success
    if (!SecurityUtils.checkAuthorizationForSome(context, actions, EDITOR)) {
      fail(EDITOR + " was expected to pass but failed");
    }

    // Test one of (translator, editor) - expected: failure
    if (SecurityUtils.checkAuthorizationForSome(context, actions, GUEST)) {
      fail(GUEST + " was not expected to pass");
    }
  }

  /**
   * Test checkAll(Action, Authority[])
   */
  @Test
  public final void testCheckAllOf() {
    Set<Action> actions = new HashSet<Action>();
    actions.add(WRITE);
    actions.add(PUBLISH);

    // Create the security context
    SecurityContextImpl context = SecurityContextImpl.fromXml(allowConfig, path);

    // Test one of (editor, publisher) - expected: success
    if (SecurityUtils.checkAuthorizationForAll(context, actions, PUBLISHER)) {
      fail("Role " + PUBLISHER + " was expected to have access but didn't");
    }

    // Test one of (editor, publisher) - expected: failure
    if (SecurityUtils.checkAuthorizationForAll(context, actions, EDITOR)) {
      fail("Role " + EDITOR + " was expected to fail but didn't");
    }
  }

  /**
   * Creates a simple security context definition.
   * 
   * @return the security context definition
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  private Node createAllowSecurityContext() throws ParserConfigurationException,
      SAXException, IOException {
    Node root;
    StringBuffer xml = new StringBuffer();
    xml.append("<security>");
    xml.append("<owner>tobias.wunden</owner>");
    xml.append("<acl order=\"allow,deny\">");
    xml.append("<allow id=\"weblounge:publish\" type=\"role\">weblounge:publisher</allow>");
    xml.append("<allow id=\"weblounge:write\" type=\"" + Role.class.getName() + "\">weblounge:editor</allow>");
    xml.append("<allow id=\"weblounge:write\" type=\"role\">weblounge:editor,weblounge:translator</allow>");
    xml.append("<allow id=\"weblounge:write\" type=\"user\">tobias.wunden</allow>");
    xml.append("<deny id=\"weblounge:write\" type=\"any\">*</deny>");
    xml.append("<deny id=\"weblounge:publish\" type=\"any\">*</deny>");
    xml.append("</acl>");
    xml.append("</security>");

    // Create xml builder
    DocumentBuilder builder = XMLUtils.getDocumentBuilder();

    // Read document and create xml node
    root = builder.parse(new ByteArrayInputStream(xml.toString().getBytes()));
    return root;
  }

  /**
   * Creates a simple security context definition.
   * 
   * @return the security context definition
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  private Node createDenySecurityContext() throws ParserConfigurationException,
      SAXException, IOException {
    Node root;
    StringBuffer xml = new StringBuffer();
    xml.append("<security>");
    xml.append("<owner>tobias.wunden</owner>");
    xml.append("<acl order=\"deny,allow\">");
    xml.append("<allow id=\"weblounge:write\" type=\"any\">*</allow>");
    xml.append("<allow id=\"weblounge:publish\" type=\"any\">*</allow>");
    xml.append("<deny id=\"weblounge:write\" type=\"role\">weblounge:editor,weblounge:translator</deny>");
    xml.append("<deny id=\"weblounge:write\" type=\"user\">tobias.wunden</deny>");
    xml.append("</acl>");
    xml.append("</security>");

    // Create xml builder
    DocumentBuilder builder = XMLUtils.getDocumentBuilder();

    // Read document and create xml node
    root = builder.parse(new ByteArrayInputStream(xml.toString().getBytes()));
    return root;
  }

}