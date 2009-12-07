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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.impl.content.WebloungeContentReader;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.PageManager;
import ch.o2it.weblounge.common.page.PageletLocation;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
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
public final class PageReader extends WebloungeContentReader {

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
  private static final int STATE_CONTENT = 9;
  private static final int STATE_TEXT = 10;
  private static final int STATE_PROPERTY = 11;
  private static final int STATE_TITLE = 12;

  private enum ParserContext {
    Lost, Page, Head, Body, Composer, Pagelet
  };

  /** The initial parser state */
  private int state_ = STATE_UNKNOWN;

  /** The parser context */
  private ParserContext context_ = ParserContext.Lost;

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
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#allow(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  @Override
  protected void allow(Permission permission, Authority authority) {
    page_.securityCtx.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setCreated(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  @Override
  protected void setCreated(User user, Date date) {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  @Override
  protected void setModified(User modifier, Date date) {
    // TODO:
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setPublish(java.util.Date,
   *      java.util.Date)
   */
  @Override
  protected void setPublish(Date startDate, Date endDate) {
    page_.publishingCtx.setPublishFrom(startDate);
    page_.publishingCtx.setPublishTo(endDate);
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
    if ("page".equals(raw)) {
      context_ = ParserContext.Page;
      state_ = STATE_UNKNOWN;
      page_.uri.id = attrs.getValue("id");
    }

    // in the header
    else if ("head".equals(raw)) {
      context_ = ParserContext.Head;
      state_ = STATE_UNKNOWN;
    }

    // in the body
    else if ("body".equals(raw)) {
      context_ = ParserContext.Body;
      state_ = STATE_UNKNOWN;
    }

    // pagelet or headline
    else if ("pagelet".equals(raw)) {
      context_ = ParserContext.Pagelet;
      state_ = STATE_UNKNOWN;
      String module = attrs.getValue("module");
      String id = attrs.getValue("id");
      if (isHeadline_) {
        pagelet_ = new PageletImpl(module, id);
      } else {
        PageletLocation location = new PageletLocationImpl(page_.getURI(), composer_, position_);
        pagelet_ = new PageletImpl(location, module, id);
        position_++;
      }
    }

    // headline
    else if ("headline".equals(raw)) {
      isHeadline_ = true;
    }

    // composer
    else if ("composer".equals(raw)) {
      context_ = ParserContext.Composer;
      composer_ = attrs.getValue("id");
      position_ = 0;
    }

    // content
    else if ("content".equals(raw)) {
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
    else if ("text".equals(raw)) {
      state_ = STATE_TEXT;
      clipboard.put("text.id", attrs.getValue("id"));
    }

    // property
    else if ("property".equals(raw)) {
      state_ = STATE_PROPERTY;
      clipboard.put("property.id", attrs.getValue("id"));
    }

    // title
    else if ("title".equals(raw)) {
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

  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // Pagelet
    if ("pagelet".equals(raw)) {
      if (isHeadline_)
        page_.headlines.add(pagelet_);
      else
        page_.appendPagelet(pagelet_, composer_);
      pagelet_ = null;
      context_ = isHeadline_ ? ParserContext.Page : ParserContext.Composer;
    }

    // Headline
    if ("headline".equals(raw)) {
      context_ = ParserContext.Page;
      isHeadline_ = false;
    }

    // Template
    else if (context_.equals(ParserContext.Head) && "template".equals(raw)) {
      page_.template = characters.toString();
    }

    // Layout
    else if (context_.equals(ParserContext.Head) && "layout".equals(raw)) {
      page_.layout = characters.toString();
    }

    // Type
    else if (context_.equals(ParserContext.Head) && "type".equals(raw)) {
      page_.type = characters.toString();
    }

    // Title
    else if (state_ == STATE_TITLE && "title".equals(raw)) {
      Language l = (Language) clipboard.get("language");
      page_.setTitle(characters.toString(), l);
    }

    // Pagelock
    else if (context_.equals(ParserContext.Head) && "pagelock".equals(raw)) {
      String login = characters.toString();
      User editor = site.getUser(login);
      if (page_ != null)
        page_.editor = editor;
    }

    // Text
    else if (state_ == STATE_TEXT && "text".equals(raw)) {
      String id = (String) clipboard.get("text.id");
      Language l = (Language) clipboard.get("language");
      pagelet_.setContent(id, characters.toString(), l);
    }

    // Property
    else if (state_ == STATE_PROPERTY && "property".equals(raw)) {
      String id = (String) clipboard.get("property.id");
      pagelet_.setProperty(id, characters.toString());
    }

    else if (context_.equals(ParserContext.Head) && "head".equals(raw))
      context_ = ParserContext.Page;

    else if (context_.equals(ParserContext.Head) && "body".equals(raw))
      context_ = ParserContext.Page;

    else if (context_.equals(ParserContext.Head) && "composer".equals(raw))
      context_ = ParserContext.Body;

    else if (context_.equals(ParserContext.Head) && "pagelet".equals(raw))
      context_ = ParserContext.Composer;

    super.endElement(uri, local, raw);
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