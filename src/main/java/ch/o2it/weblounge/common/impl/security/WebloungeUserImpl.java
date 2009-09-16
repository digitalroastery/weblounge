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

package ch.o2it.weblounge.common.impl.security;

import ch.o2it.weblounge.common.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.MD5;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.LoginContext;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.WebloungeUser;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Default implementation of a weblounge user.
 * 
 * @author Tobias Wunden
 * @version 1.0 Thu Jun 27 2002
 * @since WebLounge 1.0
 */

public class WebloungeUserImpl extends AuthenticatedUserImpl implements WebloungeUser {

  /** the channel subscriptions */
  // protected List<Subscription> subscriptions = null;

  /** the enabled flag */
  protected boolean enabled = true;

  /** date of the last login */
  protected Date lastLogin = null;

  /** source of the last login */
  protected String lastLoginSource = null;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String loggerClass = WebloungeUserImpl.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(loggerClass);

  /**
   * Creates a user with the given login and initializes it from the weblunge
   * database.
   * 
   * @param xml
   *          the xml resource containing the user's data
   * @param login
   *          the username
   * @param context
   *          the login context
   * @param site
   *          the site where the user logged in
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws XMLDBException
   *           if loading the user data from the database fails
   */
  WebloungeUserImpl(InputStream xml, String login, LoginContext context,
      Site site) throws IOException, SAXException, ParserConfigurationException {
    super(login, context, site);
    init(xml);
  }

  /**
   * Reads the user <code>login</code> from the weblounge database.
   * 
   * @param xml
   *          the xml resource containing the user's data
   * @param login
   *          the username
   * @param site
   *          the site where the user logged in
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws XMLDBException
   *           if loading the user data from the database fails
   */
  public WebloungeUserImpl(InputStream xml, String login, Site site)
      throws IOException, SAXException, ParserConfigurationException {
    this(xml, login, null, site);
  }

  /**
   * Initializes this user object by reading all needed information from the xml
   * configuration node.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  protected void init(InputStream resource) throws IOException, SAXException,
      ParserConfigurationException {
    if (resource != null) {
      WebloungeUserReader reader = new WebloungeUserReader(this, site);
      reader.read(resource);
    }
  }

  /**
   * Returns <code>true</code> if the user is enabled, <code>false</code>
   * otherwise.
   * 
   * @return <code>true</code> for enabled users
   */
  public boolean isEnabled() {
    return enabled;
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
  public String getLastLoginSource() {
    return lastLoginSource;
  }

  /**
   * Subscribes this user to the given channel.
   * 
   * @param channel
   * @param mode
   */
  // public void subscribe(Channel channel, int mode) {
  // if (subscriptions == null) {
  // subscriptions = new ArrayList<Subscription>();
  // }
  // subscriptions.add(new SubscriptionImpl(channel, mode));
  // }

  /**
   * This method is called to enable subclasses of this class to include their
   * special user data into the database.
   * 
   * @return the custom user properties as xml nodes
   */
  protected String getXmlExtension() {
    return null;
  }

  /**
   * Returns an xml representation of this user.
   * 
   * @return the user as an xml document fragment
   */
  public Node toXml() {
    StringBuffer b = new StringBuffer();

    // Add root node
    b.append("<user id=\"" + login + "\" enabled=\"" + enabled + "\">");

    // Password
    b.append("<password type=\"md5\">");
    if (passwordType == PASSWORD_TYPE_PLAIN) {
      password = MD5.md(password);
      passwordType = PASSWORD_TYPE_MD5;
    }
    b.append(password);
    b.append("</password>");

    // Firstname
    b.append("<firstname>");
    b.append(firstName);
    b.append("</firstname>");

    // Lastname
    b.append("<lastname>");
    b.append(lastName);
    b.append("</lastname>");

    // Initials
    b.append("<initials>");
    b.append(getInitials());
    b.append("</initials>");

    // Email
    b.append("<email>");
    b.append(email);
    b.append("</email>");

    // Groups
    if (groups != null && groups.size() > 0) {
      b.append("<groups>");
      for (Iterator gi = groups.iterator(); gi.hasNext();) {
        Group g = (Group) gi.next();
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
      for (Iterator ri = roles.iterator(); ri.hasNext();) {
        Role r = (Role) ri.next();
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

    // Subscriptions
    // if (subscriptions != null && subscriptions.size() > 0) {
    // b.append("<subscriptions>");
    // for (Iterator si = subscriptions.iterator(); si.hasNext();) {
    // Subscription s = (Subscription)si.next();
    // b.append("<channel id=\"");
    // b.append(s.getChannel());
    // b.append("\" context=\"");
    // b.append(s.getContext());
    // b.append("\" mode=\"");
    // b.append(s.getMode() == Subscription.MODE_SINGLE ? "single" : "digest");
    // b.append("\"/>");
    // }
    // b.append("</subscriptions>");
    // }

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

    // Custom
    String custom = getXmlExtension();
    if (custom != null) {
      b.append(custom);
    }

    b.append("</user>");

    try {
      InputSource is = new InputSource(new StringReader(b.toString()));
      DocumentBuilder docBuilder = XMLUtilities.getDocumentBuilder();
      Document doc = docBuilder.parse(is);
      return doc.getFirstChild();
    } catch (SAXException e) {
      log_.error("Error building dom tree for pagelet", e);
    } catch (IOException e) {
      log_.error("Error reading pagelet xml", e);
    } catch (ParserConfigurationException e) {
      log_.error("Error parsing pagelet xml", e);
    }
    return null;
  }

}