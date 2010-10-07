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

package ch.o2it.weblounge.common.impl.user;

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.DigestType;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * This class implements the system administrator for weblounge.
 */
public final class WebloungeAdminImpl extends WebloungeUserImpl {

  /**
   * Creates a new weblounge administrator.
   * <p>
   * Use {@link #setLogin(String)} and {@link #setPassword(String)} to set them
   * according to your needs.
   */
  public WebloungeAdminImpl(String login) {
    super(login, User.SystemRealm);
    assignRole(SystemRole.SYSTEMADMIN);
    setName("Weblounge Administrator");
  }

  /**
   * Returns <code>true</code> if <code>authority</code> represents the same
   * user.
   * 
   * @see ch.o2it.weblounge.common.security.Authority#isAuthorizedBy(ch.o2it.weblounge.common.security.Authority)
   */
  public boolean isAuthorizedBy(Authority authority) {
    return authority instanceof WebloungeAdminImpl;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.user.UserImpl#setRealm(java.lang.String)
   */
  @Override
  public void setRealm(String realm) {
    throw new UnsupportedOperationException("The admin user realm cannot be changed");
  }

  /**
   * Initializes this admin object by reading all information from the
   * <code>XML</code> configuration node.
   * 
   * @param userNode
   *          the <code>XML</code> node containing the admin configuration
   * @param site
   *          the associated site
   */
  public static WebloungeAdminImpl fromXml(Node userNode, Site site)
      throws IllegalStateException {
    XPath xpath_ = XPathFactory.newInstance().newXPath();
    return fromXml(userNode, site, xpath_);
  }

  /**
   * Initializes this admin object by reading all information from the
   * <code>XML</code> configuration node.
   * 
   * @param userNode
   *          the <code>XML</code> node containing the admin configuration
   * @param site
   *          the associated site
   * @param xpath
   *          the {@link XPath} processor
   */
  public static WebloungeAdminImpl fromXml(Node userNode, Site site, XPath xpath)
      throws IllegalStateException {

    if (userNode == null)
      return null;

    String login = XPathHelper.valueOf(userNode, "login", xpath);      
    WebloungeAdminImpl user = new WebloungeAdminImpl(login);

    Node enabledAttribute = userNode.getAttributes().getNamedItem("enabled");
    user.enabled = enabledAttribute == null || ConfigurationUtils.isTrue(XPathHelper.valueOf(userNode, "@enabled", xpath));

    String name = XPathHelper.valueOf(userNode, "name", xpath);
    if (name != null) {
      user.name = name;
    } else {
      user.firstName = XPathHelper.valueOf(userNode, "firstname", xpath);
      user.lastName = XPathHelper.valueOf(userNode, "lastname", xpath);
    }
    user.email = XPathHelper.valueOf(userNode, "email", xpath);
    String language = XPathHelper.valueOf(userNode, "language", xpath);
    if (language != null) {
      Language l = LanguageSupport.getLanguage(language);
      user.language = (l != null) ? l : site.getDefaultLanguage();
    }

    // Password
    String password = XPathHelper.valueOf(userNode, "password", xpath);
    if (password != null) {
      String digestType = null;
      try {
        digestType = XPathHelper.valueOf(userNode, "password/@type", xpath);
        user.passwordDigestType = DigestType.valueOf(digestType);
        user.password = password.getBytes();
      } catch (Exception e) {
        throw new IllegalStateException("Unknown password digest found: " + digestType);
      }
    }

    // Properties
    NodeList properties = XPathHelper.selectList(userNode, "properties/property", xpath);
    if (properties != null) {
      for (int i = 0; i < properties.getLength(); i++) {
        String key = XPathHelper.valueOf(properties.item(i), "name", xpath);
        String value = XPathHelper.valueOf(properties.item(i), "value", xpath);
        // TODO: Check for serialized objects or xml nodes
        if (key != null && value != null)
          user.properties.put(key, value);
      }
    }

    return user;
  }

  /**
   * Returns an <code>XML</code> representation of this user.
   * 
   * @return the user as an <code>XML</code> document fragment
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();

    // Add root node
    b.append("<administrator");
    if (!enabled) {
      b.append(" enabled=\"" + enabled + "\"");
    }
    b.append(">");
    
    // Login
    b.append("<login>").append(login).append("</login>");

    // Password
    b.append("<password type=\"");
    b.append(passwordDigestType.toString());
    b.append("\">");
    b.append(new String(password));
    b.append("</password>");

    // First name
    if (firstName != null) {
      b.append("<firstname>");
      b.append(firstName);
      b.append("</firstname>");
    }

    // Last name
    if (lastName != null) {
      b.append("<lastname>");
      b.append(lastName);
      b.append("</lastname>");
    }

    // Name, if first name and last name were not given
    if (name != null && firstName == null && lastName == null) {
      b.append("<name>");
      b.append(name);
      b.append("</name>");
    }

    // Email
    if (email != null) {
      b.append("<email>");
      b.append(email);
      b.append("</email>");
    }

    // Language
    if (language != null) {
      b.append("<language>");
      b.append(language.getIdentifier());
      b.append("</language>");
    }

    // properties
    if (properties != null && properties.size() > 0) {
      b.append("<properties>");
      for (Map.Entry<String, Object> entry : properties.entrySet()) {
        b.append("<property>");
        b.append("<name>");
        b.append(entry.getKey());
        b.append("</name>");
        b.append("<value>");
        // TODO: Examine object. If XML node or serializable, serialize with
        // care
        b.append(entry.getValue().toString());
        b.append("</value>");
        b.append("</property>");
      }
      b.append("</properties>");
    }

    b.append("</administrator>");
    return b.toString();
  }

}