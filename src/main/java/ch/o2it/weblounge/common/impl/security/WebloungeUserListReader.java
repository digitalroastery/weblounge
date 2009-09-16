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

import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.security.WebloungeUser;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility class used to parse user data.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class WebloungeUserListReader extends DefaultHandler {

  /** The user object */
  protected WebloungeUserProxy user;

  /** The resulting users */
  protected List<WebloungeUser> users = new ArrayList<WebloungeUser>();

  /** The site */
  protected Site site_;

  /** The node content */
  protected StringBuffer nodeContent = null;

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = WebloungeUserListReader.class.getName();

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Creates a new user data reader that will parse the sax data and store it in
   * user objects.
   * 
   * @param site
   *          the associated site
   */
  WebloungeUserListReader(Site site) {
    this.site_ = site;
  }

  /**
   * Starts the sax parser and reads in the data.
   * 
   * @param data
   *          the xml data resource
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  WebloungeUser[] read(InputStream data) throws IOException, SAXException,
      ParserConfigurationException {
    XMLUtilities.parse(data, this);
    WebloungeUser[] u = new WebloungeUser[users.size()];
    return users.toArray(u);
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
      user = new WebloungeUserProxy(attrs.getValue("id"), site_);
      user.enabled = "true".equalsIgnoreCase(attrs.getValue("enable"));
      users.add(user);
      log_.debug("Reading user '" + user.login + "'");
    }

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
      user.firstname = nodeContent.toString();
    }

    // last name
    else if (local.equals("lastname")) {
      user.lastname = nodeContent.toString();
    }

    // email
    else if (local.equals("email")) {
      user.email = nodeContent.toString();
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