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

package ch.o2it.weblounge.common.impl.content.image;

import ch.o2it.weblounge.common.content.ResourceReader;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.image.ImageContent;
import ch.o2it.weblounge.common.content.image.ImageResource;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
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
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Utility class used to parse image data.
 */
public final class ImageResourceReader extends WebloungeContentReader implements ResourceReader<ImageContent, ImageResource> {

  /** Logging facility */
  private final static Logger logger = LoggerFactory.getLogger(ImageResourceReader.class);

  /** Parser factory */
  private static final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

  /** The SAX parser */
  private WeakReference<SAXParser> parserRef = null;

  /** The image object */
  private ImageResourceImpl image = null;

  /** Current parser context */
  private enum ParserContext {
    Document, File, Head
  };

  /** The parser context */
  private ParserContext parserContext = ParserContext.Document;

  /**
   * Creates a new image data reader that will parse the XML data and store it in
   * the <code>File</code> object that is returned by the {@link #read} method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public ImageResourceReader() throws ParserConfigurationException, SAXException {
    parserRef = new WeakReference<SAXParser>(parserFactory.newSAXParser());
  }

  /**
   * This method is called when a <code>File</code> object is instantiated.
   * @param uri
   *          the image uri
   * @param is
   *          the xml input stream
   * 
   * @throws IOException
   *           if reading the input stream fails
   */
  public ImageResource read(ResourceURI uri, InputStream is) throws SAXException,
      IOException, ParserConfigurationException {
    reset();
    image = new ImageResourceImpl(uri);
    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return image;
  }

  /**
   * Sets the image that needs to be further enriched with content from an xml
   * document.
   * 
   * @param image
   *          the image
   */
  public void init(ImageResourceImpl image) {
    this.image = image;
  }

  /**
   * Resets this parser instance.
   */
  void reset() {
    this.image = null;
    this.parserContext = ParserContext.Document;
    SAXParser parser = parserRef.get();
    if (parser != null)
      parser.reset();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setOwner(ch.o2it.weblounge.common.user.User)
   */
  @Override
  protected void setOwner(User owner) {
    image.setOwner(owner);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#allow(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  @Override
  protected void allow(Permission permission, Authority authority) {
    image.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setCreated(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  @Override
  protected void setCreated(User user, Date date) {
    image.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  @Override
  protected void setModified(User modifier, Date date) {
    image.setModified(modifier, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.content.WebloungeContentReader#setPublished(ch.o2it.weblounge.common.user.User,
   *      java.util.Date, java.util.Date)
   */
  @Override
  protected void setPublished(User publisher, Date startDate, Date endDate) {
    image.setPublished(publisher, startDate, endDate);
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

    // read the image url
    if ("resource".equals(raw)) {
      parserContext = ParserContext.File;
      ((ResourceURIImpl) image.getURI()).setIdentifier(attrs.getValue("id"));
      if (attrs.getValue("path") != null)
        ((ResourceURIImpl) image.getURI()).setPath(attrs.getValue("path"));
    }

    // in the header
    else if ("head".equals(raw)) {
      parserContext = ParserContext.Head;
    }

    // title, subject and the like
    else if ("title".equals(raw) || "subject".equals(raw) || "description".equals(raw) || "coverage".equals(raw) || "rights".equals(raw)) {
      String language = attrs.getValue("language");
      if (language != null) {
        Language l = LanguageSupport.getLanguage(language);
        clipboard.put("language", l);
      } else {
        clipboard.remove("language");
      }
    }

    super.startElement(uri, local, raw, attrs);
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    if (parserContext.equals(ParserContext.Head)) {

      // Indexed
      if ("index".equals(raw)) {
        image.setIndexed("true".equals(characters.toString()));
      }

      // Promote
      else if ("promote".equals(raw)) {
        image.setPromoted("true".equals(characters.toString()));
      }

      // Type
      else if ("type".equals(raw)) {
        image.setType(characters.toString());
      }

      // Title
      else if ("title".equals(raw)) {
        Language l = (Language) clipboard.get("language");
        image.setTitle(characters.toString(), l);
      }

      // Description
      else if ("description".equals(raw)) {
        Language l = (Language) clipboard.get("language");
        image.setDescription(characters.toString(), l);
      }

      // Coverage
      else if ("coverage".equals(raw)) {
        Language l = (Language) clipboard.get("language");
        image.setCoverage(characters.toString(), l);
      }

      // Rights
      else if ("rights".equals(raw)) {
        Language l = (Language) clipboard.get("language");
        image.setRights(characters.toString(), l);
      }

      // Subject
      else if ("subject".equals(raw)) {
        image.addSubject(characters.toString());
      }

      // Filelock
      else if ("locked".equals(raw)) {
        User user = (User) clipboard.get("user");
        if (user != null)
          image.setLocked(user);
      }

    }

    // Head
    if ("head".equals(raw)) {
      parserContext = ParserContext.File;
    }

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
    logger.warn("Warning while reading {}: {}", image, e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The error is printed out and
   * the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void error(SAXParseException e) {
    logger.warn("Error while reading {}: {}", image, e.getMessage());
  }

  /**
   * The parser encountered problems while parsing. The fatal error is printed
   * out and the parsing process is stopped.
   * 
   * @param e
   *          information about the error
   */
  public void fatalError(SAXParseException e) {
    logger.warn("Fatal error while reading {}: {}", image, e.getMessage());
  }

}