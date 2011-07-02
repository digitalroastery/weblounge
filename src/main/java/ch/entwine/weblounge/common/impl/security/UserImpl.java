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

import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.security.User;

import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

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

  /** the public credentials set */
  protected Set<Object> publicCredentials = null;

  /** the private credentials set */
  protected Set<Object> privateCredentials = null;

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
   * @see ch.entwine.weblounge.common.security.User#getLogin()
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
   * @see ch.entwine.weblounge.common.security.User#getName()
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
   * @see ch.entwine.weblounge.common.security.User#getRealm()
   */
  public String getRealm() {
    return realm;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.User#addPrivateCredentials(java.lang.Object[])
   */
  public void addPrivateCredentials(Object... credentials) {
    if (privateCredentials == null) {
      privateCredentials = new HashSet<Object>();
    }
    for (Object o : credentials) {
      privateCredentials.add(o);
    }
  }

  /**
   * Removes the private credentials from this user.
   * 
   * @param credentials
   *          the credentials
   * @return <code>true</code> if the credentials was removed
   */
  public boolean removePrivateCredentials(Object credentials) {
    if (privateCredentials == null)
      return false;
    return privateCredentials.remove(credentials);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.User#getPrivateCredentials()
   */
  public Set<Object> getPrivateCredentials() {
    if (privateCredentials != null) {
      return privateCredentials;
    } else {
      return new HashSet<Object>();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.User#getPrivateCredentials(java.lang.Class)
   */
  public Set<Object> getPrivateCredentials(Class<?> type) {
    Set<Object> set = new HashSet<Object>();
    if (privateCredentials != null) {
      for (Object c : privateCredentials) {
        if (type.isAssignableFrom(c.getClass()))
          set.add(c);
      }
    }
    return set;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.User#addPublicCredentials(java.lang.Object[])
   */
  public void addPublicCredentials(Object... credentials) {
    if (publicCredentials == null) {
      publicCredentials = new HashSet<Object>();
    }
    for (Object o : credentials) {
      publicCredentials.add(o);
    }
  }

  /**
   * Removes the public credentials from this user.
   * 
   * @param credentials
   *          the credentials
   * @return <code>true</code> if the credentials was removed
   */
  public boolean removePublicCredentials(Object credentials) {
    if (publicCredentials == null)
      return false;
    return publicCredentials.remove(credentials);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.User#getPublicCredentials()
   */
  public Set<Object> getPublicCredentials() {
    if (publicCredentials != null) {
      return publicCredentials;
    } else {
      return new HashSet<Object>();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.User#getPublicCredentials(java.lang.Class)
   */
  public Set<Object> getPublicCredentials(Class<?> type) {
    Set<Object> set = new HashSet<Object>();
    if (publicCredentials != null) {
      for (Object c : publicCredentials) {
        if (type.isAssignableFrom(c.getClass()))
          set.add(c);
      }
    }
    return set;
  }

  /**
   * This method will create a user from the given <code>XML</code> node or
   * <code>null</code> if no user information is contained in the node.
   * 
   * <p>
   * The input is expected as:
   * 
   * <pre>
   * &lt;user id=&quot;login&quot; realm=&quot;realm&quot;&gt;Jon Doe&lt;/user&gt;
   * </pre>
   * 
   * @param xml
   *          the XML node
   * @return the user or <code>null</code> if
   */
  public static UserImpl fromXml(Node xml) {
    XPath xpathProcessor = XPathFactory.newInstance().newXPath();
    return fromXml(xml, xpathProcessor);
  }

  /**
   * This method will create a user from the given <code>XML</code> node or
   * <code>null</code> if no user information is contained in the node.
   * 
   * <p>
   * The input is expected as:
   * 
   * <pre>
   * &lt;user id=&quot;login&quot; realm=&quot;realm&quot;&gt;Jon Doe&lt;/user&gt;
   * </pre>
   * 
   * @param xml
   *          the XML node
   * @param xpath
   *          the XPATH object
   * @return the user or <code>null</code> if
   */
  public static UserImpl fromXml(Node xml, XPath xpath) {
    String login = XPathHelper.valueOf(xml, "@id", xpath);
    if (login == null)
      throw new IllegalStateException("Found user node without id");
    String realm = XPathHelper.valueOf(xml, "@realm", xpath);
    String name = XPathHelper.valueOf(xml, "text()", xpath);
    UserImpl user = new UserImpl(login, realm, name);
    return user;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    UserImpl user = (UserImpl) super.clone();
    user.login = login;
    user.realm = realm;
    user.name = name;
    return user;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return login.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof User) {
      User u = (User) obj;
      if (realm != null && !realm.equals(u.getRealm()))
        return false;
      if (u.getRealm() != null && realm == null)
        return false;
      return login.equals(u.getLogin());
    }
    return super.equals(obj);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (name != null) ? name : login;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.User#toXml()
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<user");

    // id
    buf.append(" id=\"");
    buf.append(login);
    buf.append("\"");

    // realm
    if (realm != null) {
      buf.append(" realm=\"");
      buf.append(realm);
      buf.append("\"");
    }

    buf.append(">");

    // name
    if (name != null) {
      buf.append("<![CDATA[").append(name).append("]]>");
    }

    buf.append("</user>");
    return buf.toString();
  }

}
