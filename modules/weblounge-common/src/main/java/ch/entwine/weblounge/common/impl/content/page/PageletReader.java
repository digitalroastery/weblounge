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

package ch.entwine.weblounge.common.impl.content.page;

import ch.entwine.weblounge.common.content.page.PageletURI;
import ch.entwine.weblounge.common.impl.content.WebloungeContentReader;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Securable.Order;
import ch.entwine.weblounge.common.security.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Utility class used to parse <code>Pagelet</code> data.
 */
public final class PageletReader extends WebloungeContentReader {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PageletReader.class);

  /** Parser factory */
  private static final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

  /** The SAX parser */
  private WeakReference<SAXParser> parserRef = null;

  /** The pagelet data */
  private PageletImpl pagelet = null;

  /** The pagelet location within a page */
  private PageletURI pageletLocation = null;

  /**
   * Creates a new pagelet reader that will parse serialized XML version of a
   * pagelet and store it in the
   * {@link ch.entwine.weblounge.common.content.page.Pagelet} that is returned
   * by the {@link #read} method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   * 
   * @see #read(InputStream)
   */
  public PageletReader() throws ParserConfigurationException, SAXException {
    parserRef = new WeakReference<SAXParser>(parserFactory.newSAXParser());
  }

  /**
   * This method is called to parse the serialized XML of a
   * {@link ch.entwine.weblounge.common.content.page.Pagelet}.
   * 
   * @param is
   *          the pagelet data
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws IOException
   *           if reading the input stream fails
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public PageletImpl read(InputStream is) throws SAXException, IOException,
      ParserConfigurationException {

    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return pagelet;
  }

  /**
   * Resets the pagelet parser.
   */
  public void reset() {
    pagelet = null;
    pageletLocation = null;
    SAXParser parser = parserRef.get();
    if (parser != null)
      parser.reset();
  }

  /**
   * Returns the pagelet that has been read in.
   * 
   * @return the pagelet
   */
  PageletImpl getPagelet() {
    return pagelet;
  }

  /**
   * The location of the pagelet within the page. Note that when using this
   * reader as a helper for the {@link PageReader}, you need to set the location
   * prior to passing the first SAX event, otherwise it won't be taken into
   * account.
   * 
   * @param location
   *          the pagelet's location on the page
   */
  public void setPageletLocation(PageletURI location) {
    this.pageletLocation = location;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri, String local, String raw,
      Attributes attrs) throws SAXException {

    super.startElement(uri, local, raw, attrs);

    // start of a new pagelet
    if ("pagelet".equals(raw)) {
      String module = attrs.getValue("module");
      String id = attrs.getValue("id");
      pagelet = new PageletImpl(pageletLocation, module, id);
      logger.debug("Started reading pagelet {}", pagelet);
    }

    // locale
    else if ("locale".equals(raw)) {
      String language = attrs.getValue("language");
      Language l = LanguageUtils.getLanguage(language);
      if (l == null)
        throw new IllegalStateException("Found content without language");
      if ("true".equals(attrs.getValue("original")))
        pagelet.setOriginalLanguage(l);
      pagelet.modificationCtx.switchTo(l);
      pagelet.enableLanguage(l);
      clipboard.put("language", l);
    }

    // text
    else if ("text".equals(raw)) {
      clipboard.put("text.id", attrs.getValue("id"));
    }

    // property
    else if ("property".equals(raw)) {
      clipboard.put("property.id", attrs.getValue("id"));
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // pagelet
    if ("pagelet".equals(raw)) {
      logger.debug("Finished reading pagelet {}", pagelet);
    }

    // text
    else if ("text".equals(raw)) {
      String id = (String) clipboard.get("text.id");
      Language l = (Language) clipboard.get("language");
      pagelet.setContent(id, getCharacters(), l);
      if (logger.isTraceEnabled())
        logger.trace("Adding " + l.toString().toLowerCase() + " text '" + getCharacters() + "'");
    }

    // property
    else if ("property".equals(raw)) {
      String id = (String) clipboard.get("property.id");
      pagelet.addProperty(id, getCharacters());
      if (logger.isTraceEnabled())
        logger.trace("Setting property '" + id + " to '" + getCharacters() + "'");
    }

    super.endElement(uri, local, raw);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setModified(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  protected void setModified(User user, Date date) {
    if (pagelet == null)
      return;
    Language language = (Language) clipboard.get("language");
    pagelet.setModified(user, date, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setOwner(ch.entwine.weblounge.common.security.User)
   */
  protected void setOwner(User user) {
    if (pagelet == null)
      return;
    pagelet.setOwner(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setOriginalLanguage(ch.entwine.weblounge.common.language.Language)
   */
  protected void setOriginalLanguage(Language language) {
    if (pagelet == null)
      return;
    pagelet.setOriginalLanguage(language);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setAllowDenyOrder(ch.entwine.weblounge.common.security.Securable.Order)
   */
  @Override
  protected void setAllowDenyOrder(Order order) {
    if (pagelet == null)
      return;
    pagelet.securityCtx.setAllowDenyOrder(order);
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#allow(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  protected void allow(Action action, Authority authority) {
    if (pagelet == null)
      return;
    pagelet.securityCtx.allow(action, authority);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#deny(ch.entwine.weblounge.common.security.Action, ch.entwine.weblounge.common.security.Authority)
   */
  @Override
  protected void deny(Action action, Authority authority) {
    if (pagelet == null)
      return;
    pagelet.securityCtx.deny(action, authority);
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setCreated(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  @Override
  protected void setCreated(User user, Date date) {
    if (pagelet == null)
      return;
    pagelet.creationCtx.setCreator(user);
    pagelet.creationCtx.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#setPublish(java.util.Date,
   *      java.util.Date)
   */
  @Override
  protected void setPublished(User publisher, Date startDate, Date endDate) {
    if (pagelet == null)
      return;
    pagelet.publishingCtx.setPublished(publisher, startDate, endDate);
  }

}