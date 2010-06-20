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

import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.content.PageletURI;
import ch.o2it.weblounge.common.impl.content.WebloungeContentReader;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Utility class used to parse <code>Pagelet</code> data.
 */
public final class PageletReader extends WebloungeContentReader {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(PageletReader.class);

  /** The pagelet data */
  private PageletImpl pagelet = null;

  /** The pagelet location within a page */
  private PageletURI pageletLocation = null;

  /**
   * Creates a new pagelet reader that will parse serialized XML version of a
   * pagelet and store it in the {@link Pagelet} that is returned by the
   * {@link #read} method.
   * 
   * @see #read(InputStream)
   */
  public PageletReader() {
  }

  /**
   * This method is called to parse the serialized XML of a {@link Pagelet}.
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

    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.newSAXParser().parse(is, this);
    return pagelet;
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
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri, String local, String raw,
      Attributes attrs) throws SAXException {

    super.startElement(uri, local, raw, attrs);

    // start of a new pagelet
    if ("pagelet".equals(raw)) {
      String module = attrs.getValue("module");
      String id = attrs.getValue("id");
      pagelet = new PageletImpl(pageletLocation, module, id);
      log_.debug("Started reading pagelet {}", pagelet);
    }

    // locale
    else if ("locale".equals(raw)) {
      String language = attrs.getValue("language");
      Language l = LanguageSupport.getLanguage(language);
      if (l == null)
        throw new IllegalStateException("Found content without language");
      if ("true".equals(attrs.getValue("original")))
        pagelet.setOriginalLanguage(l);
      pagelet.modificationCtx.switchTo(l);
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
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // pagelet
    if ("pagelet".equals(raw)) {
      log_.debug("Finished reading pagelet {}", pagelet);
    }

    // text
    else if ("text".equals(raw)) {
      String id = (String) clipboard.get("text.id");
      Language l = (Language) clipboard.get("language");
      pagelet.setContent(id, getCharacters(), l);
      if (log_.isTraceEnabled())
        log_.trace("Adding " + l.toString().toLowerCase() + " text '" + getCharacters() + "'");
    }

    // property
    else if ("property".equals(raw)) {
      String id = (String) clipboard.get("property.id");
      pagelet.setProperty(id, getCharacters());
      if (log_.isTraceEnabled())
        log_.trace("Setting property '" + id + " to '" + getCharacters() + "'");
    }

    super.endElement(uri, local, raw);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  protected void setModified(User user, Date date) {
    Language language = (Language) clipboard.get("language");
    pagelet.setModified(user, date, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setOwner(ch.o2it.weblounge.common.user.User)
   */
  protected void setOwner(User user) {
    pagelet.setOwner(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setOriginalLanguage(ch.o2it.weblounge.common.language.Language)
   */
  protected void setOriginalLanguage(Language language) {
    pagelet.setOriginalLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#allow(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  @Override
  protected void allow(Permission permission, Authority authority) {
    pagelet.securityCtx.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setCreated(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  @Override
  protected void setCreated(User user, Date date) {
    pagelet.creationCtx.setCreator(user);
    pagelet.creationCtx.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setPublish(java.util.Date,
   *      java.util.Date)
   */
  @Override
  protected void setPublished(User publisher, Date startDate, Date endDate) {
    pagelet.publishingCtx.setPublished(publisher, startDate, endDate);
  }

}