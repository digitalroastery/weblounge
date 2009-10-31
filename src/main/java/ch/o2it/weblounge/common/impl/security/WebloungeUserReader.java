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

import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.AuthenticatedUser;
import ch.o2it.weblounge.common.security.Group;
import ch.o2it.weblounge.common.security.GroupRegistry;
import ch.o2it.weblounge.common.security.Role;
import ch.o2it.weblounge.common.security.RoleRegistry;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility class used to parse user data.
 */
public class WebloungeUserReader extends DefaultHandler {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(WebloungeUserReader.class);

  /** The user object */
  protected WebloungeUserImpl user = null;

  /** The groups defined by the associated site */
  protected GroupRegistry groups = null;

  /** The roles defined by the associated site */
  protected RoleRegistry roles = null;

  /** The site */
  protected Site site_;

  /** The node content */
  protected StringBuffer nodeContent = null;

  /**
   * Creates a new user data reader that will parse the sax data and store it in
   * the user object.
   * 
   * @param user
   *          the user object
   * @param site
   *          the associated site
   */
  WebloungeUserReader(WebloungeUserImpl user, Site site) {
    this.user = user;
    this.groups = site.getGroups();
    this.roles = site.getRoles();
    this.site_ = site;
  }

  /**
   * Starts the sax parser and reads in the data.
   * 
   * @param data
   *          the xml data resource
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   */
  void read(InputStream data) throws SAXException, IOException,
      ParserConfigurationException {
    XMLUtilities.parse(data, this);
  }

  /**
   * The parser found the start of an element. Information about this element as
   * well as the attached attributes are passed to this method.
   * 
   * @param uri
   *          information about the namespace
   * @param local
   *          the local name of the element
   * @param raw
   *          the raw name of the element
   * @param attrs
   *          the element's attributes
   */
  public void startElement(String uri, String local, String raw,
      Attributes attrs) {
    log_.debug("User reader found element '" + local + "'");
    nodeContent = new StringBuffer();

    // read the user url
    if (local.equals("user")) {
      user.login = attrs.getValue("id");
      user.enabled = "true".equalsIgnoreCase(attrs.getValue("enabled"));
      log_.debug("Reading user '" + user.login + "'");
    }

    // groups
    else if (local.equals("group")) {
      String context = attrs.getValue("context");
      String id = attrs.getValue("id");
      Group group = groups.getGroup(context, id);
      if (group == null) {
        group = new GroupImpl(context, id);
        groups.addGroup(group);
        log_.warn(user + " is member of unknown group '" + group + "'");
      }
      group.addMember(user);
    }

    // roles
    else if (local.equals("role")) {
      String context = attrs.getValue("context");
      String id = attrs.getValue("id");
      Role role = roles.getRole(context, id);
      if (role == null) {
        role = new RoleImpl(context, id, site_);
        roles.addRole(role);
        log_.debug(user + " is assigned unknown role '" + role + "'");
      }
      user.assignRole(role);
    }

    // subscriptions
    // TODO: Implement subscriptions
    // else if (local.equals("channel")) {
    // String context = attrs.getValue("context");
    // String id = attrs.getValue("id");
    // String mode = attrs.getValue("mode");
    // Channel channel = new ChannelImpl(context, id);
    // int m = ("single".equals(mode)) ? Subscription.MODE_SINGLE :
    // Subscription.MODE_DIGEST;
    // user.subscribe(channel, m);
    // }

    // password
    else if (local.equals("password")) {
      String type = attrs.getValue("type");
      if (type != null && type.toLowerCase().equals("md5"))
        user.passwordType = AuthenticatedUser.PASSWORD_TYPE_MD5;
      else
        user.passwordType = AuthenticatedUser.PASSWORD_TYPE_PLAIN;
    } else
      log_.debug("SAX parser found unhandled element '" + local + "'");
  }

  /**
   * The parser found the end of an element.
   * 
   * @param uri
   *          information about the namespace
   * @param local
   *          the local name of the element
   * @param raw
   *          the raw name of the element
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {
    super.endElement(uri, local, raw);

    // first name
    if (local.equals("firstname")) {
      user.setFirstName(nodeContent.toString());
    }

    // last name
    else if (local.equals("lastname")) {
      user.setLastName(nodeContent.toString());
    }

    // initials
    else if (local.equals("initials")) {
      user.setInitials(nodeContent.toString());
    }

    // email
    else if (local.equals("email")) {
      user.setEmail(nodeContent.toString());
    }

    // language
    else if (local.equals("language")) {
      Language l = site_.getLanguage(nodeContent.toString());
      user.setLanguage((l != null) ? l : site_.getDefaultLanguage());
    }

    // password
    else if (local.equals("password")) {
      user.setPassword(nodeContent.toString().getBytes(), user.passwordType);
    }

    // last login date
    else if (local.equals("date")) {
      try {
        user.setLastLogin(WebloungeDateFormat.parseStatic(nodeContent.toString()));
      } catch (ParseException e) {
      }
    }

    // last login source
    else if (local.equals("ip")) {
      user.setLastLoginSource(nodeContent.toString());
    }

  }

  /**
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char[] chars, int offset, int count) {
    String text = new String(chars, offset, count);
    nodeContent.append(text);
  }

  /**
   * The parser encountered problems while parsing. The warning is printed out
   * but the parsing process continues.
   * 
   * @param e
   *          information about the warning
   */
  public void warning(SAXParseException e) {
    log_.warn("Warning while decoding " + user + ": " + e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The error is printed out and
   * the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void error(SAXParseException e) {
    log_.warn("Error while decoding " + user + ": " + e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The fatal error is printed
   * out and the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void fatalError(SAXParseException e) {
    log_.warn("Fatal error while decoding " + user + ": " + e.getMessage());
  }

}