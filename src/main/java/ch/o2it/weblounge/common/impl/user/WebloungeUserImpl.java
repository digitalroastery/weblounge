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

import ch.o2it.weblounge.common.impl.security.GroupImpl;
import ch.o2it.weblounge.common.impl.security.RoleImpl;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.DigestType;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.WebloungeUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Default implementation of a weblounge user.
 */
public class WebloungeUserImpl extends AuthenticatedUserImpl implements WebloungeUser {

  /** Logging facility */
  private static final Logger log_ = LoggerFactory.getLogger(WebloungeUserImpl.class);

  /** Enabled flag */
  protected boolean enabled = true;

  /** First name of the person */
  protected String firstName = null;

  /** Family name of the person */
  protected String lastName = null;

  /** E-mail address */
  protected String email = null;

  /** Preferred language */
  protected Language language = null;

  /** Cached initials */
  private String initials = null;

  /** Password challenge */
  protected String challenge = null;

  /** Password challenge response */
  protected byte[] response = null;

  /** Password hash type, either plain or md5 */
  protected DigestType responseDigestType = DigestType.plain;

  /** Additional properties of this user */
  protected Map<String, Object> properties = new HashMap<String, Object>();

  /** Date of the last login */
  protected Date lastLogin = null;

  /** Source of the last login */
  protected String lastLoginSource = null;

  /** Cached version of automatically generated initials */
  private String cachedInitials = null;

  /** Cached version of automatically generated name */
  private String cachedName = null;

  /**
   * Creates a user with the given login and initializes it from the weblounge
   * database.
   * 
   * @param login
   *          the username
   * @param realm
   *          the login domain
   */
  public WebloungeUserImpl(String login, String realm) {
    super(login, realm);
  }

  /**
   * Creates a user with login <code>login</code> and the default realm
   * {@link #DefaultRealm}.
   * 
   * @param login
   *          the username
   */
  public WebloungeUserImpl(String login) {
    this(login, DefaultRealm);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.user.WebloungeUser#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.user.WebloungeUser#canLogin()
   */
  public boolean canLogin() {
    return isEnabled() && password != null;
  }

  /**
   * Sets the enabled flag. Set it to <code>true</code> to enable the login.
   * 
   * @param enabled
   *          <code>true</code> to enable this login
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Sets this person's first name.
   * 
   * @param firstname
   *          the first name
   */
  public void setFirstName(String firstname) {
    this.firstName = firstname;
    cachedInitials = null;
    cachedName = null;
  }

  /**
   * Returns the first name of this person.
   * 
   * @return the person's first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets this person's last name.
   * 
   * @param lastname
   *          the last name
   */
  public void setLastName(String lastname) {
    this.lastName = lastname;
    cachedInitials = null;
    cachedName = null;
  }

  /**
   * Returns the last name of this person.
   * 
   * @return the person's last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Returns the name of this user. If possible, the value returned consists of
   * type <first name><last name>.
   * 
   * @returns the full user name
   */
  public String getName() {
    if (name != null)
      return name;
    if (cachedName != null)
      return cachedName;

    String first = getFirstName();
    String last = getLastName();
    if ((first == null || first.equals("")) && (last == null || last.equals("")))
      return null;

    // Create the name
    StringBuffer name = new StringBuffer();
    if (first != null && !first.equals("")) {
      name.append(first);
    }
    if (last != null && !last.trim().equals("")) {
      if (name.length() > 0)
        name.append(" ");
      name.append(last);
    }

    // Cache for further reference
    cachedName = name.toString();
    return cachedName;
  }

  /**
   * Sets the person's email.
   * 
   * @param email
   *          the email address
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the email address of this person.
   * 
   * @return the person's email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the person's preferred language.
   * 
   * @param language
   *          the preferred language
   */
  public void setLanguage(Language language) {
    this.language = language;
  }

  /**
   * Returns the preferred language of this person.
   * 
   * @return the person's preferred language
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * Returns the short version of the persons name, which are constructed from
   * the first and the last name of the user.
   * 
   * @return the persons initials
   */
  public String getInitials() {
    if (initials != null)
      return initials;
    if (cachedInitials != null)
      return cachedInitials;
    StringBuffer initials = new StringBuffer();
    String first = getFirstName();
    String last = getLastName();
    if (first != null && !first.equals("") && last != null && !last.equals("")) {
      initials.append(first.substring(0, 1));
      initials.append(last.subSequence(0, 1));
      cachedInitials = initials.toString().toLowerCase();
      return cachedInitials;
    }
    return null;
  }

  /**
   * Sets the person's initials.
   * 
   * @param initials
   *          the person's initials
   */
  public void setInitials(String initials) {
    this.initials = initials;
  }

  /**
   * Sets the last login date.
   * 
   * @param date
   *          the login date
   * @param src
   *          the login source
   */
  public void setLastLogin(Date date, String src) {
    lastLogin = date;
    lastLoginSource = src;
  }

  /**
   * Returns the data where the user logged in for the last time.
   * 
   * @return the last login
   */
  public Date getLastLogin() {
    return lastLogin;
  }

  /**
   * Returns the last login source. The source can be either an ip address or a
   * host name.
   * 
   * @return the source of the last login
   */
  public String getLastLoginFrom() {
    return lastLoginSource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.user.WebloungeUser#getProperty(java.lang.String)
   */
  public Object getProperty(String name) {
    return properties.get(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.user.WebloungeUser#removeProperty(java.lang.String)
   */
  public Object removeProperty(String name) {
    return properties.remove(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.user.WebloungeUser#setProperty(java.lang.String,
   *      java.lang.Object)
   */
  public void setProperty(String name, Object value) {
    properties.put(name, value);
  }

  /**
   * Initializes this user object by reading all information from the
   * <code>XML</code> configuration node.
   * 
   * @param userNode
   *          the <code>XML</code> node containing the user configuration
   * @param site
   *          the associated site
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  public static WebloungeUserImpl fromXml(Node userNode, Site site)
      throws IllegalStateException {
    XPath xpath_ = XPathFactory.newInstance().newXPath();
    return fromXml(userNode, site, xpath_);
  }

  /**
   * Initializes this user object by reading all information from the
   * <code>XML</code> configuration node.
   * 
   * @param userNode
   *          the <code>XML</code> node containing the user configuration
   * @param site
   *          the associated site
   * @param xpath
   *          the {@link XPath} processor
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  public static WebloungeUserImpl fromXml(Node userNode, Site site, XPath xpath)
      throws IllegalStateException {

    Node rootNode = XPathHelper.select(userNode, "//user", xpath);
    if (rootNode == null)
      return null;

    String login = XPathHelper.valueOf(rootNode, "@id", xpath);
    WebloungeUserImpl user = new WebloungeUserImpl(login);

    String realm = XPathHelper.valueOf(rootNode, "@realm", xpath);
    if (realm != null)
      user.realm = realm;
    user.enabled = ConfigurationUtils.isTrue(XPathHelper.valueOf(rootNode, "@enabled", xpath));
    user.firstName = XPathHelper.valueOf(rootNode, "//profile/firstname", xpath);
    user.lastName = XPathHelper.valueOf(rootNode, "//profile/lastname", xpath);
    user.initials = XPathHelper.valueOf(rootNode, "//profile/initials", xpath);
    user.email = XPathHelper.valueOf(rootNode, "//profile/email", xpath);
    String language = XPathHelper.valueOf(rootNode, "//profile/language", xpath);
    if (language != null) {
      Language l = site.getLanguage(language);
      user.language = (l != null) ? l : site.getDefaultLanguage();
    }

    // Password
    String password = XPathHelper.valueOf(rootNode, "//password", xpath);
    if (password != null) {
      String digestType = null;
      try {
        digestType = XPathHelper.valueOf(rootNode, "//password/@type", xpath);
        user.passwordDigestType = DigestType.valueOf(digestType);
        user.password = password.getBytes();
      } catch (Exception e) {
        throw new IllegalStateException("Unknown password digest found: " + digestType);
      }
    }

    // Challenge / Response
    user.challenge = XPathHelper.valueOf(rootNode, "//security/challenge", xpath);
    String response = XPathHelper.valueOf(rootNode, "//security/response", xpath);
    if (response != null) {
      String digestType = null;
      try {
        digestType = XPathHelper.valueOf(rootNode, "//security/response/@type", xpath);
        user.responseDigestType = DigestType.valueOf(digestType);
        user.response = response.getBytes();
      } catch (Exception e) {
        throw new IllegalStateException("Unknown response digest found: " + digestType);
      }
    }

    // Last login
    String lastLogin = XPathHelper.valueOf(rootNode, "//security/lastlogin/date", xpath);
    try {
      user.lastLogin = WebloungeDateFormat.parseStatic(lastLogin);
      user.lastLoginSource = XPathHelper.valueOf(rootNode, "//security/lastlogin/ip", xpath);
    } catch (ParseException e) {
      // It's not important. Let's log and then forget about it
      log_.error("Unable to parse last login date: " + lastLogin, e);
    }

    // Roles
    NodeList roles = XPathHelper.selectList(rootNode, "//security/roles/role", xpath);
    if (roles != null) {
      for (int i = 0; i < roles.getLength(); i++) {
        Node roleNode = roles.item(i);
        String roleContext = XPathHelper.valueOf(roleNode, "@context", xpath);
        String roleId = XPathHelper.valueOf(roleNode, "text()", xpath);
        Role r = site.getRole(roleId, roleContext);
        if (r != null)
          user.assignRole(r);
        else
          user.assignRole(new RoleImpl(roleId, roleContext));
      }
    }

    // Groups
    NodeList groups = XPathHelper.selectList(rootNode, "//security/groups/group", xpath);
    if (groups != null) {
      for (int i = 0; i < groups.getLength(); i++) {
        Node groupNode = groups.item(i);
        String groupContext = XPathHelper.valueOf(groupNode, "@context", xpath);
        String groupId = XPathHelper.valueOf(groupNode, "text()", xpath);
        Group g = site.getGroup(groupId, groupContext);
        if (g != null)
          user.addMembership(g);
        else
          user.addMembership(new GroupImpl(groupId, groupContext));
      }
    }

    // Properties
    NodeList properties = XPathHelper.selectList(rootNode, "//property", xpath);
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
    b.append("<user id=\"" + login + "\"");
    if (realm != null) {
      b.append(" realm=\"");
      b.append(realm);
      b.append("\"");
    }
    b.append(" enabled=\"" + enabled + "\"");
    b.append(">");

    //
    // Profile
    //
    
    b.append("<profile>");
    
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

    // Initials
    if (getInitials() != null) {
      b.append("<initials>");
      b.append(getInitials());
      b.append("</initials>");
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

    b.append("</profile>");

    //
    // Security
    //
    
    b.append("<security>");
    
    // Password
    b.append("<password type=\"");
    b.append(passwordDigestType.toString());
    b.append("\">");
    b.append(new String(password));
    b.append("</password>");

    // challenge - response
    if (challenge != null && response != null) {
      b.append("<challenge>");
      b.append(challenge);
      b.append("</challenge>");
      b.append("<response type=\"");
      b.append(responseDigestType.toString());
      b.append("\">");
      b.append(new String(response));
      b.append("</response>");
    }

    // Last login
    if (lastLogin != null && lastLoginSource != null) {
      b.append("<lastlogin>");
      b.append("<date>");
      b.append(WebloungeDateFormat.formatStatic(lastLogin));
      b.append("</date>");
      b.append("<ip>");
      b.append(lastLoginSource);
      b.append("</ip>");
      b.append("</lastlogin>");
    }

    // Groups
    if (groups != null && groups.size() > 0) {
      b.append("<groups>");
      for (Iterator<Group> gi = groups.iterator(); gi.hasNext();) {
        Group g = gi.next();
        b.append("<group context=\"");
        b.append(g.getContext());
        b.append("\">");
        b.append(g.getIdentifier());
        b.append("</group>");
      }
      b.append("</groups>");
    }

    // Roles
    if (roles != null && roles.size() > 0) {
      b.append("<roles>");
      for (Iterator<Role> ri = roles.iterator(); ri.hasNext();) {
        Role r = ri.next();
        if (!r.equals(SystemRole.GUEST)) {
          b.append("<role context=\"");
          b.append(r.getContext());
          b.append("\">");
          b.append(r.getIdentifier());
          b.append("</role>");
        }
      }
      b.append("</roles>");
    }

    b.append("</security>");

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

    b.append("</user>");
    return b.toString();
  }

}