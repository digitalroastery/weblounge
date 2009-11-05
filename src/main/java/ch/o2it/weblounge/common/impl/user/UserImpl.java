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

import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.user.User;

import org.w3c.dom.Node;

import javax.xml.xpath.XPath;

/**
 * Default implementation for a weblounge user that is not logged in. This class
 * is primarily used for managing user references when it comes to serializing
 * and deserializing user data.
 */
public class UserImpl implements User {

  /** The identifier for this user in the given domain */
  protected String login = null;

  /** The user domain */
  protected String realm = null;

  /** The user's name */
  protected String name = null;

  /**
   * Creates a new user with the given login.
   * 
   * @param login
   *          the login
   */
  public UserImpl(String login) {
    this(login, null, null);
  }

  /**
   * Creates a new user with the given login and realm.
   * 
   * @param login
   *          the login
   * @param realm
   *          the user realm
   */
  public UserImpl(String login, String realm) {
    this(login, realm, null);
  }

  /**
   * Creates a new user with the given login, realm and name.
   * 
   * @param login
   *          the login
   * @param realm
   *          the user realm
   * @param name
   *          the name
   */
  public UserImpl(String login, String realm, String name) {
    if (login == null)
      throw new IllegalStateException("Cannot create user without id");
    this.login = login;
    this.realm = realm;
    this.name = name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.user.User#getLogin()
   */
  public String getLogin() {
    return login;
  }

  /**
   * Sets the display name for the user.
   * 
   * @param name
   *          the display name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.user.User#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the domain where the user is registered. Using the realm, a user can
   * be defined in an external system that is connected using a user service
   * instead of defining it in the system itself.
   * 
   * @param realm
   *          the user realm
   */
  public void setRealm(String realm) {
    // TODO: Ensure that the realm is suitable as an xml attribute
    this.realm = realm;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.user.User#getRealm()
   */
  public String getRealm() {
    return realm;
  }

  /**
   * This method will create a user from the given <code>XML</code> node or
   * <code>null</code> if no user information is contained in the node.
   * 
   * <p>
   * The input is expected as:
   * 
   * <pre>
   * &lt;user realm=&quot;realm&quot; id=&quot;login&quot; &gt;Jon Doe&lt;/user&gt;
   * </pre>
   * 
   * @param xml
   *          the xml node
   * @param xpath
   *          the xpath object
   * @return the user or <code>null</code> if
   */
  public static UserImpl fromXml(Node xml, XPath xpath) {
    Node userNode = XPathHelper.select(xml, "/user", xpath);
    if (userNode == null)
      return null;

    String login = XPathHelper.valueOf(userNode, "@id", xpath);
    if (login == null)
      throw new IllegalStateException("Found user node without id");
    String realm = XPathHelper.valueOf(userNode, "@realm", xpath);
    String name = XPathHelper.valueOf(userNode, "./text()", xpath);
    UserImpl user = new UserImpl(login, realm, name);
    return user;
  }
  
  /**
   * {@inheritDoc}
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return login.hashCode();
  }
  
  /**
   * {@inheritDoc}
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof User) {
      User u = (User)obj;
      if (realm != null && !realm.equals(u.getRealm()))
        return false;
      if (u.getRealm() != null && realm == null)
        return false;
      return login.equals(u.getLogin());
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.user.User#toXml()
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<user");
    if (realm != null) {
      buf.append(" realm=\"");
      buf.append(realm);
      buf.append("\"");
    }
    buf.append(" id=\"");
    buf.append(login);
    buf.append("\"");
    if (name != null) {
      buf.append("><![CDATA[");
      buf.append(name);
      buf.append("]]>");
    }
    buf.append("</user>");
    return buf.toString();
  }

}
