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

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.PageManager;
import ch.o2it.weblounge.common.page.PageletLocation;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Utility class used to parse page data.
 */
public final class PageReader extends GeneralContentReader {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(PageReader.class);

  /** The page object */
  private PageImpl page_ = null;

  /** The pagelet data */
  private PageletImpl pagelet_ = null;

  /** The composer name */
  private String composer_ = null;

  /** The pagelet position within the composer */
  private int position_ = 0;

  /** True if reading pagelets in the headline section */
  private boolean isHeadline_ = false;

  /** The parser states */
  private static final int STATE_UNKNOWN = -1;
  private static final int STATE_RENDERER = 1;
  private static final int STATE_LAYOUT = 2;
  private static final int STATE_CONTENT = 9;
  private static final int STATE_TEXT = 10;
  private static final int STATE_PROPERTY = 11;
  private static final int STATE_TITLE = 12;
  private static final int STATE_PAGELOCK = 15;

  /** Parser context */
  private static final int CTXT_PAGE = 0;
  private static final int CTXT_COMPOSER = 1;
  private static final int CTXT_PAGELET = 2;

  /** The initial parser state */
  private int state_ = STATE_UNKNOWN;

  /** The parser context */
  private int context_ = CTXT_PAGE;

  /**
   * Creates a new page data reader that will parse the SAX data and store it in
   * the page object.
   * 
   * @param site
   *          the associated site
   */
  public PageReader(Site site) {
    super(site);
  }

  /**
   * This method is called, when a <code>Page</code> object is instantiated by
   * the {@link PageManager}.
   * 
   * @param data
   *          the document data
   * @param site
   *          the associated site
   * @param version
   *          the page version
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws IOException
   *           if reading the input stream fails
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public PageImpl read(InputStream is, PageURIImpl uri) throws SAXException,
      IOException, ParserConfigurationException {
    page_ = new PageImpl(uri);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.newSAXParser().parse(is, this);
    return page_;
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
      Attributes attrs) throws SAXException {
    super.startElement(uri, local, raw, attrs);

    // read the page url
    if ("page".equals(local)) {
      context_ = CTXT_PAGE;
      state_ = STATE_UNKNOWN;
      page_.uri.id = attrs.getValue("id");
    }

    // pagelet or headline
    else if ("pagelet".equals(local)) {
      context_ = CTXT_PAGELET;
      state_ = STATE_UNKNOWN;
      String module = attrs.getValue("module");
      String id = attrs.getValue("id");
      if (isHeadline_) {
        pagelet_ = new PageletImpl(site, module, id);
      } else {
        PageletLocation location = new PageletLocationImpl(page_.getURI(), composer_, position_);
        pagelet_ = new PageletImpl(location, module, id);
        position_++;
      }
    }

    // headline
    else if ("headline".equals(local)) {
      isHeadline_ = true;
    }

    // composer
    else if ("composer".equals(local)) {
      context_ = CTXT_COMPOSER;
      composer_ = attrs.getValue("id");
      position_ = 0;
    }

    // content
    else if ("content".equals(local)) {
      state_ = STATE_CONTENT;
      String language = attrs.getValue("language");
      Language l = site.getLanguage(language);
      if (l != null) {
        clipboard.put("language", l);
        if (attrs.getValue("original") != null && attrs.getValue("original").equals("true")) {
          pagelet_.setOriginalLanguage(l);
        }
      } else {
        clipboard.remove("language");
      }
    }

    // element
    else if ("text".equals(local)) {
      state_ = STATE_TEXT;
      clipboard.put("text.id", attrs.getValue("id"));
    }

    // property
    else if ("property".equals(local)) {
      state_ = STATE_PROPERTY;
      clipboard.put("property.id", attrs.getValue("id"));
    }

    // title
    else if ("title".equals(local)) {
      state_ = STATE_TITLE;
      String language = attrs.getValue("language");
      Language l = site.getLanguage(language);
      if (l != null) {
        clipboard.put("language", l);
        if (attrs.getValue("original") != null && attrs.getValue("original").equals("true")) {
          log_.info("Found original title language");
          page_.setOriginalLanguage(l);
        }
      } else {
        clipboard.remove("language");
      }
    }

    // page renderer
    else if ("renderer".equals(local)) {
      state_ = STATE_RENDERER;
      return;
    }

    // page layout
    else if ("layout".equals(local)) {
      state_ = STATE_LAYOUT;
      return;
    }

    // pagelock
    else if ("pagelock".equals(local)) {
      state_ = STATE_PAGELOCK;
      return;
    }

  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // Pagelet
    if ("pagelet".equals(local)) {
      if (isHeadline_)
        page_.headlines.add(pagelet_);
      else
        page_.appendPagelet(pagelet_, composer_);
      pagelet_ = null;
      context_ = isHeadline_ ? CTXT_PAGE : CTXT_COMPOSER;
    }

    // Headline
    if ("headline".equals(local)) {
      context_ = CTXT_PAGE;
      isHeadline_ = false;
    }

    // Security
    else if (contentReaderContext == CTXT_SECURITY && "security".equals(local)) {
      if (pagelet_ != null) {
        pagelet_.securityCtx = getSecurityContext();
      } else {
        page_.securityCtx = getSecurityContext();
      }
    }

    // Publishing
    else if (contentReaderContext == CTXT_PUBLISH && "publish".equals(local)) {
      if (pagelet_ != null) {
        pagelet_.publishingCtx = getPublishingContext();
      } else {
        page_.publishingCtx = getPublishingContext();
      }
    }

    // Renderer
    else if (state_ == STATE_RENDERER && "renderer".equals(local)) {
      page_.renderer = characters.toString();
    }

    // Layout
    else if (state_ == STATE_LAYOUT && "layout".equals(local)) {
      page_.layout = characters.toString();
    }

    // Title
    else if (state_ == STATE_TITLE && "title".equals(local)) {
      Language l = (Language) clipboard.get("language");
      page_.setTitle(characters.toString(), l);
    }

    // Pagelock
    else if (state_ == STATE_PAGELOCK && "pagelock".equals(local)) {
      String login = characters.toString();
      User editor = site.getUser(login);
      if (page_ != null)
        page_.editor = editor;
    }

    // Text
    else if (state_ == STATE_TEXT && "text".equals(local)) {
      String id = (String) clipboard.get("text.id");
      Language l = (Language) clipboard.get("language");
      pagelet_.setContent(id, characters.toString(), l);
    }

    // Property
    else if (state_ == STATE_PROPERTY && "property".equals(local)) {
      String id = (String) clipboard.get("property.id");
      pagelet_.setProperty(id, characters.toString());
    }

    super.endElement(uri, local, raw);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * ch.o2it.weblounge.core.content.GeneralContentReader#addKeyword(java.lang
   * .String)
   */
  protected void addKeyword(String keyword) {
    super.addKeyword(keyword);
    page_.addKeyword(keyword);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * ch.o2it.weblounge.core.content.GeneralContentReader#setModificationDate
   * (java.util.Date)
   */
  protected void setModificationDate(Date date) {
    Language l = (Language) clipboard.get("language");
    switch (context_) {
    case CTXT_PAGE:
      page_.setModifiedSince(date);
      break;
    case CTXT_PAGELET:
      pagelet_.setModifiedSince(date, l);
      break;
    }
    super.setModificationDate(date);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * ch.o2it.weblounge.core.content.GeneralContentReader#setModifiedBy(ch.o2it
   * .weblounge.api.staff.User)
   */
  protected void setModifiedBy(User user) {
    Language l = (Language) clipboard.get("language");
    switch (context_) {
    case CTXT_PAGE:
      page_.setModifiedBy(user);
      break;
    case CTXT_PAGELET:
      pagelet_.setModifiedBy(user, l);
      break;
    }
    super.setModifiedBy(user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * ch.o2it.weblounge.core.content.GeneralContentReader#setOwner(ch.o2it.weblounge
   * .api.staff.User)
   */
  protected void setOwner(User user) {
    switch (context_) {
    case CTXT_PAGE:
      page_.securityCtx.setOwner(user);
      break;
    case CTXT_PAGELET:
      pagelet_.securityCtx.setOwner(user);
      break;
    }
    super.setModifiedBy(user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * ch.o2it.weblounge.core.content.GeneralContentReader#setType(java.lang.String
   * )
   */
  protected void setType(String type) {
    page_.type = characters.toString();
    super.setType(type);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * ch.o2it.weblounge.core.content.GeneralContentReader#setOriginalLanguage
   * (ch.o2it.weblounge.api.language.Language)
   */
  protected void setOriginalLanguage(Language language) {
    pagelet_.setOriginalLanguage(language);
    super.setOriginalLanguage(language);
  }

  /**
   * The parser encountered problems while parsing. The warning is printed out
   * but the parsing process continues.
   * 
   * @param e
   *          information about the warning
   */
  public void warning(SAXParseException e) {
    log_.warn("Warning while reading " + page_ + ": " + e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The error is printed out and
   * the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void error(SAXParseException e) {
    log_.warn("Error while reading " + page_ + ": " + e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The fatal error is printed
   * out and the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void fatalError(SAXParseException e) {
    log_.warn("Fatal error while reading " + page_ + ": " + e.getMessage());
  }

}