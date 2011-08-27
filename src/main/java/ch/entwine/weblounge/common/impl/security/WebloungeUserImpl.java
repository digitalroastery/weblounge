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
import ch.entwine.weblounge.common.impl.util.WebloungeDateFormat;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.DigestType;
import ch.entwine.weblounge.common.security.Password;
import ch.entwine.weblounge.common.security.WebloungeUser;
import ch.entwine.weblounge.common.site.Site;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Default implementation of a weblounge user.
 */
public class WebloungeUserImpl extends AuthenticatedUserImpl implements WebloungeUser {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeUserImpl.class);

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
   * @see ch.entwine.weblounge.common.security.WebloungeUser#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.WebloungeUser#canLogin()
   */
  public boolean canLogin() {
    Set<Object> passwords = getPrivateCredentials(Password.class);
    return isEnabled() && passwords.size() > 0;
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
    if (StringUtils.trimToNull(first) == null && StringUtils.trimToNull(last) == null)
      return null;

    // Create the name
    StringBuffer name = new StringBuffer();
    if (StringUtils.trimToNull(first) != null) {
      name.append(first);
    }
    if (StringUtils.trimToNull(last) != null) {
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
    if (!StringUtils.isBlank(first) && !StringUtils.isBlank(last)) {
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
   * @see ch.entwine.weblounge.common.security.WebloungeUser#getProperty(java.lang.String)
   */
  public Object getProperty(String name) {
    return properties.get(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.WebloungeUser#removeProperty(java.lang.String)
   */
  public Object removeProperty(String name) {
    return properties.remove(name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.WebloungeUser#setProperty(java.lang.String,
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
   */
  public static WebloungeUserImpl fromXml(Node userNode, Site site)
      throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(userNode, site, xpath);
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
   */
  public static WebloungeUserImpl fromXml(Node userNode, Site site, XPath xpath)
      throws IllegalStateException {

    if (userNode == null)
      return null;

    String login = XPathHelper.valueOf(userNode, "@id", xpath);
    WebloungeUserImpl user = new WebloungeUserImpl(login);

    String realm = XPathHelper.valueOf(userNode, "@realm", xpath);
    if (realm != null)
      user.realm = realm;
    user.enabled = ConfigurationUtils.isTrue(XPathHelper.valueOf(userNode, "@enabled", xpath));
    String name = XPathHelper.valueOf(userNode, "profile/name", xpath);
    if (name != null) {
      user.name = XPathHelper.valueOf(userNode, "profile/name", xpath);
    } else {
      user.firstName = XPathHelper.valueOf(userNode, "profile/firstname", xpath);
      user.lastName = XPathHelper.valueOf(userNode, "profile/lastname", xpath);
    }
    user.initials = XPathHelper.valueOf(userNode, "profile/initials", xpath);
    user.email = XPathHelper.valueOf(userNode, "profile/email", xpath);
    String language = XPathHelper.valueOf(userNode, "profile/language", xpath);
    if (language != null) {
      Language l = LanguageUtils.getLanguage(language);
      user.language = (l != null) ? l : site.getDefaultLanguage();
    }

    // Password
    String password = XPathHelper.valueOf(userNode, "security/password", xpath);
    if (password != null) {
      String digestType = null;
      try {
        digestType = XPathHelper.valueOf(userNode, "security/password/@type", xpath);
        Password pw = new PasswordImpl(password, DigestType.valueOf(digestType));
        user.addPrivateCredentials(pw);
      } catch (Throwable t) {
        throw new IllegalStateException("Unknown password digest found: " + digestType);
      }
    }

    // Challenge / Response
    user.challenge = XPathHelper.valueOf(userNode, "security/challenge", xpath);
    String response = XPathHelper.valueOf(userNode, "security/response", xpath);
    if (response != null) {
      String digestType = null;
      try {
        digestType = XPathHelper.valueOf(userNode, "security/response/@type", xpath);
        user.responseDigestType = DigestType.valueOf(digestType);
        user.response = response.getBytes();
      } catch (Throwable t) {
        throw new IllegalStateException("Unknown response digest found: " + digestType);
      }
    }

    // Last login
    String lastLogin = XPathHelper.valueOf(userNode, "security/lastlogin/date", xpath);
    try {
      if (lastLogin != null)
        user.lastLogin = WebloungeDateFormat.parseStatic(lastLogin);
      user.lastLoginSource = XPathHelper.valueOf(userNode, "security/lastlogin/ip", xpath);
    } catch (ParseException e) {
      // It's not important. Let's log and then forget about it
      logger.error("Unable to parse last login date '{}'", lastLogin, e);
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

    // Initials
    if (getInitials() != null) {
      b.append("<initials><![CDATA[");
      b.append(getInitials());
      b.append("]]></initials>");
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
    Set<Object> passwords = getPrivateCredentials(Password.class);
    for (Object o : passwords) {
      Password password = (Password)o;
      b.append("<password type=\"");
      b.append(password.getDigestType().toString());
      b.append("\"><![CDATA[");
      b.append(password.getPassword());
      b.append("]]></password>");
    }

    // challenge - response
    if (challenge != null && response != null) {
      b.append("<challenge><![CDATA[");
      b.append(challenge);
      b.append("]]></challenge>");
      b.append("<response type=\"");
      b.append(responseDigestType.toString());
      b.append("\"><![CDATA[");
      b.append(new String(response));
      b.append("]]></response>");
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

    b.append("</security>");

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

    b.append("</user>");
    return b.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    // Overwritten to document that we are using the super impl
    return super.equals(obj);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.security.AuthenticatedUserImpl#hashCode()
   */
  @Override
  public int hashCode() {
    // Overwritten to document that we are using the super impl
    return super.hashCode();
  }

}