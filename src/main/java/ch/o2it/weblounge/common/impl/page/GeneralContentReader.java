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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.WebloungeDateFormat;
import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.impl.security.AbstractSecurityContext;
import ch.o2it.weblounge.common.impl.security.AuthorityImpl;
import ch.o2it.weblounge.common.impl.security.PermissionImpl;
import ch.o2it.weblounge.common.impl.security.PermissionSecurityContext;
import ch.o2it.weblounge.common.impl.security.SystemRole;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Utility class used to parse generic content including language sensitive
 * information and a security context.
 */
public class GeneralContentReader extends DefaultHandler {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(GeneralContentReader.class);

  /** The associated site */
  protected Site site = null;

  /** The security context */
  protected PermissionSecurityContext securityCtx = null;

  /** The publishing context */
  protected PublishingContext publishingCtx = null;

  /** The content type */
  protected String type = null;

  /** The attribute and element clipboard */
  protected Map<String, Object> clipboard = null;

  /** The date format used to parse dates */
  protected final static WebloungeDateFormat dateFormat = new WebloungeDateFormat();

  /** The original content language */
  protected Language originalLanguage = null;

  /** The characters */
  protected StringBuffer characters = null;

  /** The creation date */
  protected Date creationDate = null;

  /** The modification date */
  protected Date modificationDate = null;

  /** The editor */
  protected User editor = null;

  /** The owner */
  protected User owner = null;

  /** The keywords */
  protected List<String> keywords = null;

  /** The parser contexts */
  protected static final int CTXT_UNKNOWN = -1;
  protected static final int CTXT_SECURITY = 1;
  protected static final int CTXT_CREATION = 2;
  protected static final int CTXT_MODIFICATION = 3;
  protected static final int CTXT_PUBLISH = 4;
  protected static final int CTXT_KEYWORDS = 5;
  protected static final int CTXT_CONTENT = 6;

  /** The initial parser context */
  protected int contentReaderContext = CTXT_UNKNOWN;

  /**
   * Creates a new content SAX event reader that will parse the SAX data.
   * 
   * @param site
   *          the associated site
   */
  public GeneralContentReader(Site site) {
    this.site = site;
    clipboard = new HashMap<String, Object>();
    securityCtx = getDefaultSecurityContext();
    publishingCtx = getDefaultPublishingContext();
  }

  /**
   * Returns the default security context, which is applied if no specific
   * security context information could be read.
   * <p>
   * Subclasses should overwrite this method to apply their specialized default
   * security constraints.
   * 
   * @return the default security context
   */
  protected PermissionSecurityContext getDefaultSecurityContext() {
    PermissionSecurityContext ctxt = new PermissionSecurityContext();
    ctxt.setOwner(site.getAdministrator());
    ctxt.allow(SystemPermission.READ, SystemRole.GUEST);
    ctxt.allow(SystemPermission.TRANSLATE, SystemRole.TRANSLATOR);
    ctxt.allow(SystemPermission.WRITE, SystemRole.EDITOR);
    ctxt.allow(SystemPermission.MANAGE, SystemRole.EDITOR);
    ctxt.allow(SystemPermission.PUBLISH, SystemRole.PUBLISHER);
    return ctxt;
  }

  /**
   * Returns the security context. If no context has been read in so far, the
   * default security context is being returned. Note that the context is being
   * cleared (set to <code>null</code>) after every call to this method.
   * 
   * @return the security context
   */
  public PermissionSecurityContext getSecurityContext() {
    if (securityCtx == null) {
      securityCtx = getDefaultSecurityContext();
    }
    PermissionSecurityContext ctxt = securityCtx;
    securityCtx = null;
    return ctxt;
  }

  /**
   * Returns the default publishing context, which is applied if no specific
   * publishing context information could be read.
   * <p>
   * Subclasses should overwrite this method to apply their specialized default
   * publishing constraints.
   * 
   * @return the default publishing context
   */
  protected PublishingContext getDefaultPublishingContext() {
    PublishingContext ctxt = new PublishingContextImpl();
    return ctxt;
  }

  /**
   * Returns the publishing context. If no context has been read in so far, the
   * default publishing context is being returned. Note that the context is
   * being cleared (set to <code>null</code>) after every call to this method.
   * 
   * @return the publishing context
   */
  public PublishingContext getPublishingContext() {
    if (publishingCtx == null) {
      publishingCtx = getDefaultPublishingContext();
    }
    PublishingContext ctxt = publishingCtx;
    publishingCtx = null;
    return ctxt;
  }

  /**
   * This method is called if the reader found the original content language.
   * This language can later be obtained using
   * <code>{@link #getOriginalLanguage()}</code>.
   * 
   * @param language
   *          the original content
   */
  protected void setOriginalLanguage(Language language) {
    originalLanguage = language;
  }

  /**
   * Returns the content's original language or <code>null</code> if no original
   * language was specified.
   * 
   * @return the original language
   */
  public Language getOriginalLanguage() {
    return originalLanguage;
  }

  /**
   * This method is called if the user was found that changed this element. This
   * user can later be obtained using <code>{@link #getModifiedBy()}</code>.
   * 
   * @param user
   *          the user that modified the resource
   */
  protected void setModifiedBy(User user) {
    editor = user;
  }

  /**
   * Returns the last user that modified this content.
   * 
   * @return the user that modified this content
   */
  public User getModifiedBy() {
    return editor;
  }

  /**
   * This method is called if the user was found that owns the current element.
   * It is important to know that the owner will be set after the security
   * context was closed.
   * 
   * @param user
   *          the owner
   */
  protected void setOwner(User user) {
    owner = user;
  }

  /**
   * This method is called if the creation date was found. This date can later
   * be obtained using <code>{@link #getCreationDate()}</code>.
   * 
   * @param date
   *          the creation date
   */
  protected void setCreationDate(Date date) {
    creationDate = date;
  }

  /**
   * This method is called if the user was found that changed this element. This
   * user can later be obtained using <code>{@link #getModifiedBy()}</code>.
   * 
   * @param date
   *          the modification date
   */
  protected void setModificationDate(Date date) {
    modificationDate = date;
  }

  /**
   * Returns the modifcation date.
   * 
   * @return the modification date
   */
  public Date getModificationDate() {
    return modificationDate;
  }

  /**
   * Returns the creation date.
   * 
   * @return the modification date
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * Adds a keyword to the list of keywords.
   * 
   * @param keyword
   *          the new keyword
   */
  protected void addKeyword(String keyword) {
    if (keywords == null) {
      keywords = new ArrayList<String>();
    }
    keywords.add(keyword);
  }

  /**
   * Returns the keywords that have been registered for this item.
   * 
   * @return the keywords
   */
  public String[] getKeywords() {
    if (keywords != null) {
      return keywords.toArray(new String[keywords.size()]);
    }
    return new String[] {};
  }

  /**
   * Sets the content type.
   * 
   * @param type
   *          the content type
   */
  protected void setType(String type) {
    this.type = type;
  }

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
    if ("security".equals(local)) {
      securityCtx = null;
      contentReaderContext = CTXT_SECURITY;
      return;
    }

    // permission
    else if (contentReaderContext == CTXT_SECURITY && "permission".equals(local)) {
      String id = attrs.getValue("id");
      String type = attrs.getValue("type");
      clipboard.put("id", id);
      clipboard.put("type", type);
    }

    // creation context
    else if ("created".equals(local) || "creationdate".equals(local)) {
      contentReaderContext = CTXT_CREATION;
      return;
    }

    // modification context
    else if ("modified".equals(local)) {
      contentReaderContext = CTXT_MODIFICATION;
      return;
    }

    // publishing context
    else if ("publish".equals(local)) {
      publishingCtx = null;
      contentReaderContext = CTXT_PUBLISH;
      return;
    }

    // content
    else if ("content".equals(local)) {
      contentReaderContext = CTXT_CONTENT;
      String language = attrs.getValue("language");
      Language l = site.getLanguage(language);
      clipboard.put("language", l);
      if (attrs.getValue("original") != null && attrs.getValue("original").equals("true")) {
        setOriginalLanguage(l);
      }
    }

    // keywords
    else if ("keywords".equals(local)) {
      contentReaderContext = CTXT_KEYWORDS;
    }

  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    /** security owner */
    if (contentReaderContext == CTXT_SECURITY && "owner".equals(local)) {
      String login = characters.toString();
      owner = site.getUsers().getUser(login);
    }

    /** security permission */
    else if (contentReaderContext == CTXT_SECURITY && "permission".equals(local)) {
      String id = (String) clipboard.get("id");
      Permission permission = new PermissionImpl(id);
      String type = (String) clipboard.get("type");
      if (type != null) {
        if (securityCtx == null) {
          securityCtx = new PermissionSecurityContext();
          securityCtx.setOwner(site.getAdministrator());
        }
        type = AbstractSecurityContext.resolveAuthorityTypeShortcut(type);
        StringTokenizer tok = new StringTokenizer(characters.toString(), " ,;");
        while (tok.hasMoreTokens()) {
          String authorityId = tok.nextToken();
          Authority authority = new AuthorityImpl(type, authorityId);
          securityCtx.allow(permission, authority);
        }
      }
    }

    /** content type */
    else if (contentReaderContext == CTXT_UNKNOWN && "type".equals(local)) {
      setType(characters.toString());
    }

    /** created */
    else if (contentReaderContext == CTXT_CREATION && ("created".equals(local) || "creationdate".equals(local))) {
      if (characters.length() > 0) {
        try {
          creationDate = dateFormat.parse(characters.toString());
        } catch (Exception e) {
          log_.warn("The creation date on " + this + " is malformed: '" + characters.toString() + "'");
        }
      } else {
        creationDate = new Date();
      }
    }

    /** publishing from */
    else if (contentReaderContext == CTXT_PUBLISH && "from".equals(local)) {
      if (publishingCtx == null) {
        publishingCtx = new PublishingContextImpl();
      }
      if (characters.length() > 0) {
        try {
          Date d = dateFormat.parse(characters.toString());
          publishingCtx.setPublishFrom(d);
        } catch (Exception e) {
          publishingCtx.setPublishFrom(new Date());
          log_.warn("The publishing 'from' date on " + this + " is malformed: '" + characters.toString() + "'");
        }
      } else {
        publishingCtx.setPublishFrom(new Date());
      }
    }

    /** publishing to */
    else if (contentReaderContext == CTXT_PUBLISH && "to".equals(local)) {
      if (publishingCtx == null) {
        publishingCtx = new PublishingContextImpl();
      }
      if (characters.length() > 0) {
        try {
          Date d = dateFormat.parse(characters.toString());
          publishingCtx.setPublishTo(d);
        } catch (Exception e) {
          publishingCtx.setPublishTo(new Date(Long.MAX_VALUE));
          log_.warn("The publishing 'to' date on " + this + " is malformed: '" + characters.toString() + "'");
        }
      } else {
        publishingCtx.setPublishTo(new Date(Long.MAX_VALUE));
      }
    }

    /** keywords */
    else if (contentReaderContext == CTXT_KEYWORDS && "keyword".equals(local)) {
      String keywords = characters.toString();
      StringTokenizer tok = new StringTokenizer(keywords, ",");
      while (tok.hasMoreTokens()) {
        addKeyword(tok.nextToken().trim());
      }
    }

    /** modifying user */
    else if (contentReaderContext == CTXT_MODIFICATION && "user".equals(local)) {
      String login = characters.toString();
      User user = site.getUsers().getUser(login);
      setModifiedBy(user);
    }

    /** modification date */
    else if (contentReaderContext == CTXT_MODIFICATION && "date".equals(local)) {
      if (characters.length() > 0) {
        try {
          Date d = dateFormat.parse(characters.toString());
          setModificationDate(d);
        } catch (Exception e) {
          log_.warn("The modification date on " + this + " is malformed: '" + characters.toString() + "'");
        }
      }
    }

    /** Security */
    else if (contentReaderContext == CTXT_SECURITY && "security".equals(local)) {
      contentReaderContext = CTXT_UNKNOWN;
    }

    /** Publishing */
    else if (contentReaderContext == CTXT_PUBLISH && "publish".equals(local)) {
      setOwner(owner);
      contentReaderContext = CTXT_UNKNOWN;
    }

    /** Modification */
    // TDOO: Check tag
    else if (contentReaderContext == CTXT_MODIFICATION && "modified".equals(local)) {
      contentReaderContext = CTXT_UNKNOWN;
    }

    /** Content */
    // TDOO: Check tag
    else if (contentReaderContext == CTXT_CONTENT && "content".equals(local)) {
      contentReaderContext = CTXT_UNKNOWN;
    }

    /** Keywords */
    // TDOO: Check tag
    else if (contentReaderContext == CTXT_KEYWORDS && "keywords".equals(local)) {
      contentReaderContext = CTXT_UNKNOWN;
    }

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