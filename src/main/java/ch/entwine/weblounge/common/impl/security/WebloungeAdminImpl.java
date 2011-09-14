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

import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.Password;
import ch.entwine.weblounge.common.security.Security;
import ch.entwine.weblounge.common.site.Site;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Map;
import java.util.Set;

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
    super(login, Security.SYSTEM_CONTEXT);
    addPublicCredentials(SystemRole.SYSTEMADMIN);
    setName("Weblounge Administrator");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.security.UserImpl#setRealm(java.lang.String)
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
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(userNode, site, xpath);
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
      Language l = LanguageUtils.getLanguage(language);
      user.language = (l != null) ? l : site.getDefaultLanguage();
    }

    // Password
    String password = XPathHelper.valueOf(userNode, "ns:password", xpath);
    if (password != null) {
      String digestType = null;
      try {
        digestType = XPathHelper.valueOf(userNode, "ns:password/@type", xpath);
        Password pw = new PasswordImpl(password, DigestType.valueOf(digestType));
        user.addPrivateCredentials(pw);
      } catch (Throwable t) {
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
    Set<Object> passwords = getPrivateCredentials(Password.class);
    for (Object o : passwords) {
      Password password = (Password)o;
      b.append("<password type=\"");
      b.append(password.getDigestType().toString());
      b.append("\"><![CDATA[");
      b.append(password.getPassword());
      b.append("]]></password>");
    }

    // First name
    if (firstName != null) {
      b.append("<firstname><![CDATA[");
      b.append(firstName);
      b.append("]]></firstname>");
    }

    // Last name
    if (lastName != null) {
      b.append("<lastname><![CDATA[");
      b.append(lastName);
      b.append("]]></lastname>");
    }

    // Name, if first name and last name were not given
    if (name != null && firstName == null && lastName == null) {
      b.append("<name><![CDATA[");
      b.append(name);
      b.append("]]></name>");
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
        b.append("<value><![CDATA[");
        // TODO: Examine object. If XML node or serializable, serialize with
        // care
        b.append(entry.getValue().toString());
        b.append("]]></value>");
        b.append("</property>");
      }
      b.append("</properties>");
    }

    b.append("</administrator>");
    return b.toString();
  }

}