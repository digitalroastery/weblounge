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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.impl.security.AbstractSecurityContext;
import ch.o2it.weblounge.common.impl.security.AuthorityImpl;
import ch.o2it.weblounge.common.impl.security.PermissionImpl;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.impl.util.WebloungeDateFormat;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Utility class used to parse generic content including language sensitive
 * information and a security context.
 */
public abstract class WebloungeContentReader extends DefaultHandler {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(WebloungeContentReader.class);

  /** The associated site */
  protected Site site = null;

  /** The attribute and element clipboard */
  protected Map<String, Object> clipboard = null;

  /** The date format used to parse dates */
  protected final static WebloungeDateFormat dateFormat = new WebloungeDateFormat();

  /** The characters */
  protected StringBuffer characters = null;

  /** Parser context */
  protected enum Context {
    Unknown, Security, Creation, Modification, Publish, Content
  };

  /** The initial parser context */
  protected Context contentReaderContext = Context.Unknown;

  /**
   * Creates a new content SAX event reader that will parse the SAX data.
   * 
   * @param site
   *          the associated site
   */
  public WebloungeContentReader(Site site) {
    this.site = site;
    clipboard = new HashMap<String, Object>();
    characters = new StringBuffer();
  }

  /**
   * Resets the content reader by setting everything back to the original state
   * where the object has been created. Only exception: The site that was passed
   * remains.
   */
  public void reset() {
    clipboard.clear();
    characters = new StringBuffer();
    contentReaderContext = Context.Unknown;
  }

  /**
   * This method is called if the creation date was found and the user. This
   * date can later be obtained using <code>{@link #getCreationDate()}</code>.
   * 
   * @param user
   *          the creator
   * @param date
   *          the creation date
   */
  protected abstract void setCreated(User user, Date date);

  /**
   * This method is called if the user was found that changed this element. This
   * user can later be obtained using <code>{@link #getModifiedBy()}</code>.
   * 
   * @param modifier
   *          the user
   * @param date
   *          the modification date
   */
  protected abstract void setModified(User modifier, Date date);

  /**
   * This method is called if a new permission was found.
   * 
   * @param permission
   *          the permission to grant
   * @param authority
   *          the authority that this permission is granted to
   */
  protected abstract void allow(Permission permission, Authority authority);

  /**
   * This method is called when the date of publication is read in.
   * 
   * @param publisher
   *          the user who published the resource
   * @param startDate
   *          the start date of publication
   * @param endDate
   *          the end date of publication
   */
  protected abstract void setPublished(User publisher, Date startDate,
      Date endDate);

  /**
   * The parser found the start of an element. Information about this element as
   * well as the attached attributes are passed to this method.
   * <p>
   * <b>Note:</b> This implementation is not suitable for mixed content, since
   * the characters buffer is cleared at the beginning of each tag.
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
      Attributes attrs) throws SAXException {
    characters = new StringBuffer();

    // security context
    if ("security".equals(raw)) {
      contentReaderContext = Context.Security;
      return;
    }

    // permission
    else if (contentReaderContext == Context.Security && "permission".equals(raw)) {
      String id = attrs.getValue("id");
      String type = attrs.getValue("type");
      // clipboard.put("id", id);
      // clipboard.put("type", type);
    }

    // creation context
    else if ("created".equals(raw)) {
      contentReaderContext = Context.Creation;
      return;
    }

    // modification context
    else if ("modified".equals(raw)) {
      contentReaderContext = Context.Modification;
      return;
    }

    // publishing context
    else if ("published".equals(raw)) {
      contentReaderContext = Context.Publish;
      return;
    }

    // user
    else if ("user".equals(raw)) {
      clipboard.put("user", attrs.getValue("id"));
      clipboard.put("realm", attrs.getValue("realm"));
    }

  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // owner, creator, modifier
    if ("user".equals(raw)) {
      String login = (String) clipboard.get("user");
      String realm = (String) clipboard.get("realm");
      String name = getCharacters();
      User user = new UserImpl(login, realm, name);
      clipboard.put("user", user);
    }

    // date
    else if ("date".equals(raw)) {
      try {
        Date d = dateFormat.parse(getCharacters());
        clipboard.put("date", d);
      } catch (Exception e) {
        throw new IllegalStateException("Reading in date " + characters + " failed: '" + getCharacters() + "'");
      }
    }

    // publishing start date
    else if (contentReaderContext == Context.Publish && "from".equals(raw)) {
      try {
        Date d = dateFormat.parse(getCharacters());
        clipboard.put("publish.start", d);
      } catch (Exception e) {
        throw new IllegalStateException("Reading publishing start date failed: '" + getCharacters() + "'");
      }
    }

    // publishing end date
    else if (contentReaderContext == Context.Publish && "to".equals(raw)) {
      try {
        Date d = dateFormat.parse(getCharacters());
        clipboard.put("publish.end", d);
      } catch (Exception e) {
        throw new IllegalStateException("Reading publishing end date failed: '" + getCharacters() + "'");
      }
    }

    // created
    else if (contentReaderContext == Context.Creation && "created".equals(raw)) {
      User owner = (User) clipboard.get("user");
      if (owner == null)
        throw new IllegalStateException("Creator not found");
      Date date = (Date) clipboard.get("date");
      if (date == null)
        throw new IllegalStateException("Creation date not found");
      setCreated(owner, date);
      contentReaderContext = Context.Unknown;
    }

    // modified
    else if (contentReaderContext == Context.Modification && "modified".equals(raw)) {
      User modifier = (User) clipboard.get("user");
      if (modifier == null)
        throw new IllegalStateException("Modifier not found");
      Date date = (Date) clipboard.get("date");
      if (date == null)
        throw new IllegalStateException("Modification date not found");
      setModified(modifier, date);
      contentReaderContext = Context.Unknown;
    }

    // published
    else if (contentReaderContext == Context.Publish && "published".equals(raw)) {
      User publisher = (User) clipboard.get("user");
      if (publisher == null)
        throw new IllegalStateException("Publisher not found");
      Date startDate = (Date) clipboard.get("publish.start");
      if (startDate == null)
        throw new IllegalStateException("Publication start date not found");
      Date endDate = (Date) clipboard.get("publish.start");
      if (endDate == null)
        throw new IllegalStateException("Publication end date not found");
      setPublished(publisher, startDate, endDate);
      contentReaderContext = Context.Unknown;
    }

    /** security permission */
    else if (contentReaderContext == Context.Security && "permission".equals(raw)) {
      String id = (String) clipboard.get("id");
      Permission permission = new PermissionImpl(id);
      String type = (String) clipboard.get("type");
      if (type != null) {
        type = AbstractSecurityContext.resolveAuthorityTypeShortcut(type);
        StringTokenizer tok = new StringTokenizer(getCharacters(), " ,;");
        while (tok.hasMoreTokens()) {
          String authorityId = tok.nextToken();
          Authority authority = new AuthorityImpl(type, authorityId);
          allow(permission, authority);
        }
      }
    }

    /** Security */
    else if (contentReaderContext == Context.Security && "security".equals(raw)) {
      contentReaderContext = Context.Unknown;
    }

  }

  /**
   * Returns the trimmed contents of the characters buffer.
   */
  protected String getCharacters() {
    return characters.toString().trim();
  }

  /**
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char[] chars, int start, int end) throws SAXException {
    characters.append(chars, start, end);
  }

  /**
   * The parser encountered problems while parsing. The warning is printed out
   * but the parsing process continues.
   * 
   * @param e
   *          information about the warning
   */
  public void warning(SAXParseException e) throws SAXException {
    log_.warn("Warning while decoding " + this + ": " + e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The error is printed out and
   * the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void error(SAXParseException e) throws SAXException {
    log_.warn("Error while decoding " + this + ": " + e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The fatal error is printed
   * out and the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void fatalError(SAXParseException e) throws SAXException {
    log_.warn("Fatal error while decoding " + this + ": " + e.getMessage());
  }

}