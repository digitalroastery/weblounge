/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.user;

import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.DigestType;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.LoginContext;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.WebloungeUser;

import org.apache.commons.codec.digest.DigestUtils;
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

  /**
   * Creates a user with the given login and initializes it from the weblounge
   * database.
   * 
   * @param login
   *          the username
   * @param context
   *          the login context
   * @param site
   *          the site where the user logged in
   */
  WebloungeUserImpl(String login, LoginContext context, Site site) {
    super(login, context, site);
  }

  /**
   * Reads the user <code>login</code> from the weblounge database.
   * 
   * @param login
   *          the username
   * @param site
   *          the site where the user logged in
   */
  public WebloungeUserImpl(String login, Site site) {
    this(login, null, site);
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
    String name = "";
    if (getFirstName() != null && !getFirstName().trim().equals("")) {
      if (getLastName() != null && !getLastName().trim().equals("")) {
        name = getFirstName() + " " + getLastName();
      } else {
        name = getFirstName();
      }
    } else if (getLastName() != null && !getLastName().trim().equals("")) {
      name = getLastName();
    } else {
      name = getLogin();
    }
    return name;
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
    if (initials != null) {
      return initials;
    }
    String firstName = getFirstName();
    String lastName = getLastName();
    if (firstName != null && lastName != null) {
      initials = firstName.substring(0, 1) + lastName.substring(0, 1);
    }
    return initials;
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
   */
  public void setLastLogin(Date date) {
    lastLogin = date;
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
   * Sets the last login source.
   * 
   * @param src
   *          the login source
   */
  public void setLastLoginSource(String src) {
    lastLoginSource = src;
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
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  public static WebloungeUserImpl fromXml(Node userNode, XPath xpath,
      LoginContext context, Site site) throws IllegalStateException {
    Node rootNode = XPathHelper.select(userNode, "//user", xpath);
    if (rootNode == null)
      return null;

    String login = XPathHelper.valueOf(rootNode, "@id", xpath);
    WebloungeUserImpl user = new WebloungeUserImpl(login, context, site);

    user.enabled = ConfigurationUtils.isTrue(XPathHelper.valueOf(rootNode, "@enabled", xpath));
    user.firstName = XPathHelper.valueOf(rootNode, "/firstname", xpath);
    user.lastName = XPathHelper.valueOf(rootNode, "/lastname", xpath);
    user.initials = XPathHelper.valueOf(rootNode, "/initials", xpath);
    user.email = XPathHelper.valueOf(rootNode, "/email", xpath);
    String language = XPathHelper.valueOf(rootNode, "/language", xpath);
    if (language != null) {
      Language l = site.getLanguage(language);
      user.language = (l != null) ? l : site.getDefaultLanguage();
    }

    String password = XPathHelper.valueOf(rootNode, "/password", xpath);
    if (password != null) {
      String digestType = null;
      try {
        digestType = XPathHelper.valueOf(rootNode, "/password/@type", xpath);
        user.passwordDigestType = DigestType.valueOf(digestType);
        user.password = password.getBytes();
      } catch (Exception e) {
        throw new IllegalStateException("Unknown password digest found: " + digestType);
      }
    }

    user.challenge = XPathHelper.valueOf(rootNode, "/challenge", xpath);
    String response = XPathHelper.valueOf(rootNode, "/response", xpath);
    if (response != null) {
      String digestType = null;
      try {
        digestType = XPathHelper.valueOf(rootNode, "/response/@type", xpath);
        user.responseDigestType = DigestType.valueOf(digestType);
        user.response = response.getBytes();
      } catch (Exception e) {
        throw new IllegalStateException("Unknown response digest found: " + digestType);
      }
    }

    // Roles
    NodeList roles = XPathHelper.selectList(rootNode, "/role", xpath);
    if (roles != null) {
      for (int i=0; i < roles.getLength(); i++) {
        String roleContext = XPathHelper.valueOf(roles.item(i), "@context", xpath);
        String roleId = XPathHelper.valueOf(roles.item(i), "text()", xpath);
        Role r = site.getRole(roleId, roleContext);
        if (r != null)
          user.roles.add(r);
      }
    }
    
    // Groups
    NodeList groups = XPathHelper.selectList(rootNode, "/group", xpath);
    if (roles != null) {
      for (int i=0; i < groups.getLength(); i++) {
        String groupContext = XPathHelper.valueOf(roles.item(i), "@context", xpath);
        String groupId = XPathHelper.valueOf(roles.item(i), "text()", xpath);
        Group g = site.getGroup(groupId, groupContext);
        if (g != null) {
          user.groups.add(g);
          g.addMember(user);
        }
      }
    }
    
    String lastLogin = XPathHelper.valueOf(rootNode, "/lastlog/date", xpath);
    try {
      user.lastLogin = WebloungeDateFormat.parseStatic(lastLogin);
      user.lastLoginSource = XPathHelper.valueOf(rootNode, "/lastlog/ip", xpath);
    } catch (ParseException e) {
      // It's not important. Let's log and then forget about it
      log_.error("Unable to parse last login date: " + lastLogin, e);
    }

    NodeList properties = XPathHelper.selectList(rootNode, "/properties/property", xpath);
    if (properties != null) {
      for (int i=0; i < properties.getLength(); i++) {
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
   * Returns an xml representation of this user.
   * 
   * @return the user as an xml document fragment
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();

    // Add root node
    b.append("<user id=\"" + login + "\" enabled=\"" + enabled + "\">");

    // Password
    b.append("<password type=\"");
    b.append(passwordDigestType.toString());
    b.append("\">");
    switch (passwordDigestType) {
      case plain:
        b.append(password);
        break;
      case md5:
        b.append(DigestUtils.md5(password));
        break;
    }
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

    // Groups
    if (groups != null && groups.size() > 0) {
      b.append("<groups>");
      for (Iterator<Group> gi = groups.iterator(); gi.hasNext();) {
        Group g = gi.next();
        b.append("<group id=\"");
        b.append(g.getIdentifier());
        b.append("\" context=\"");
        b.append(g.getContext());
        b.append("\"/>");
      }
      b.append("</groups>");
    }

    // Roles
    if (roles != null && roles.size() > 0) {
      b.append("<roles>");
      for (Iterator<Role> ri = roles.iterator(); ri.hasNext();) {
        Role r = ri.next();
        if (!r.equals(SystemRole.GUEST)) {
          b.append("<role id=\"");
          b.append(r.getIdentifier());
          b.append("\" context=\"");
          b.append(r.getContext());
          b.append("\"/>");
        }
      }
      b.append("</roles>");
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

    // challenge - response
    if (challenge != null && response != null) {
      b.append("<challenge>");
      b.append(challenge);
      b.append("</challenge>");
      b.append("<response type=\"");
      b.append(responseDigestType.toString());
      b.append("\">");
      switch (responseDigestType) {
        case plain:
          b.append(response);
          break;
        case md5:
          b.append(DigestUtils.md5(response));
          break;
      }
      b.append("</response>");
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

    b.append("</user>");
    return b.toString();
  }

}