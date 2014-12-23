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

package ch.entwine.weblounge.common.impl.content.image;

import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.impl.content.ResourceContentReaderImpl;
import ch.entwine.weblounge.common.impl.util.WebloungeDateFormat;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

/**
 * Utility class used to parse <code>Content</code> data for simple files.
 */
public class ImageContentReader extends ResourceContentReaderImpl<ImageContent> {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(ImageContentReader.class);

  /**
   * Creates a new file content reader that will parse serialized XML version of
   * the file content and store it in the
   * {@link ch.entwine.weblounge.common.content.ResourceContent} that is
   * returned by the {@link #read} method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   * 
   * @see #createFromXml(InputStream)
   */
  public ImageContentReader() throws ParserConfigurationException, SAXException {
    parserRef = new WeakReference<SAXParser>(parserFactory.newSAXParser());
  }

  @Override
  public ImageContent createFromXml(InputStream is) throws SAXException,
      IOException, ParserConfigurationException {

    SAXParser parser = parserRef.get();
    if (parser == null) {
      parser = parserFactory.newSAXParser();
      parserRef = new WeakReference<SAXParser>(parser);
    }
    parser.parse(is, this);
    return content;
  }

  @Override
  public ImageContent createFromContent(InputStream is, User user,
      Language language, long size, String fileName, String mimeType)
      throws IOException {

    ImageContent content = new ImageContentImpl(fileName, language, mimeType);

    // Use the logged in user as the author
    content.setCreator(user);

    // Set the creation date
    content.setCreationDate(new Date());

    final byte[] imgData = IOUtils.toByteArray(is);

    try (final ByteArrayInputStream bais = new ByteArrayInputStream(imgData)) {
      readDimensions(content, bais);
    } catch (Throwable t) {
      logger.warn("Error extracting metadata using java advanced imaging (jai) from {}: {}", fileName, t.getMessage());
      throw new IOException(t);
    }

    try (final ByteArrayInputStream bais = new ByteArrayInputStream(imgData)) {
      readExifMetadata(content, bais);
    } catch (Throwable t) {
      logger.warn("Error extracting Exif metadata from {}: {}", fileName, t.getMessage());
    }

    return content;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.ResourceContentReaderImpl#createContent()
   */
  @Override
  protected ImageContent createContent() {
    return new ImageContentImpl();
  }

  /**
   * Resets the pagelet parser.
   */
  public void reset() {
    super.reset();
    content = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri, String local, String raw,
      Attributes attrs) throws SAXException {

    // start of a new content element
    if ("content".equals(raw)) {
      logger.debug("Started reading image content {}", content);
    }

    // gps position
    else if ("gps".equals(raw)) {
      double gpsLat = Double.parseDouble(attrs.getValue("lat"));
      double gpsLong = Double.parseDouble(attrs.getValue("lng"));
      content.setGpsPosition(gpsLat, gpsLong);
      logger.trace("Image's gps lat is '{}'", content.getGpsLat());
      logger.trace("Image's gps long is '{}'", content.getGpsLong());
    }

    super.startElement(uri, local, raw, attrs);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.WebloungeContentReader#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // mime type
    if ("mimetype".equals(raw)) {
      content.setMimetype(getCharacters());
      logger.trace("Images's content mimetype is '{}'", content.getMimetype());
    }

    // size
    else if ("size".equals(raw)) {
      content.setSize(Long.parseLong(getCharacters()));
      logger.trace("Image's content filesize is '{}'", content.getSize());
    }

    // height
    else if ("height".equals(raw)) {
      content.setHeight(Integer.parseInt(getCharacters()));
      logger.trace("Image's height is '{}'", content.getHeight());
    }

    // width
    else if ("width".equals(raw)) {
      content.setWidth(Integer.parseInt(getCharacters()));
      logger.trace("Image's width is '{}'", content.getWidth());
    }

    // date taken
    else if ("datetaken".equals(raw)) {
      try {
        content.setDateTaken(WebloungeDateFormat.parseStatic(getCharacters()));
      } catch (ParseException e) {
        throw new IllegalStateException("The date taken '" + getCharacters() + "' cannot be parsed", e);
      }
      logger.trace("Image's date taken is '{}'", content.getDateTaken());
    }

    // location
    else if ("location".equals(raw)) {
      content.setLocation(getCharacters());
      logger.trace("Image's location is '{}'", content.getLocation());
    }

    // filmspeed
    else if ("filmspeed".endsWith(raw)) {
      content.setFilmspeed(Integer.parseInt(getCharacters()));
      logger.trace("Image's filmspeed is '{}'", content.getFilmspeed());
    }

    // fnumber
    else if ("fnumber".equals(raw)) {
      content.setFNumber(Float.parseFloat(getCharacters()));
      logger.trace("Image's fnumber is '{}'", content.getFNumber());
    }

    // focal width
    else if ("focalwidth".equals(raw)) {
      content.setFocalWidth(Integer.parseInt(getCharacters()));
      logger.trace("Image's focalwidth is '{}'", content.getFocalWidth());
    }

    // exposure time
    else if ("exposuretime".equals(raw)) {
      content.setExposureTime(Float.parseFloat(getCharacters()));
      logger.trace("Image's exposuretime is '{}'", content.getExposureTime());
    }

    else {
      super.endElement(uri, local, raw);
    }
  }

  private void readExifMetadata(ImageContent content, InputStream is) {
    BufferedInputStream bis = new BufferedInputStream(is);
    ImageMetadata exifMetadata = ImageMetadataUtils.extractMetadata(bis);

    if (exifMetadata == null)
      return;

    if (exifMetadata.getDateTaken() != null) {
      content.setDateTaken(exifMetadata.getDateTaken());
    }

    if (!StringUtils.isBlank(exifMetadata.getPhotographer())) {
      content.setAuthor(exifMetadata.getPhotographer());
    }

    if (!StringUtils.isBlank(exifMetadata.getLocation())) {
      content.setLocation(exifMetadata.getLocation());
    }

    if (exifMetadata.getGpsLat() != 0 && exifMetadata.getGpsLong() != 0) {
      content.setGpsPosition(exifMetadata.getGpsLat(), exifMetadata.getGpsLong());
    }

    if (exifMetadata.getFilmspeed() != 0) {
      content.setFilmspeed(exifMetadata.getFilmspeed());
    }

    if (exifMetadata.getFNumber() != 0) {
      content.setFNumber(exifMetadata.getFNumber());
    }

    if (exifMetadata.getFocalWidth() != 0) {
      content.setFocalWidth(exifMetadata.getFocalWidth());
    }

    if (exifMetadata.getExposureTime() != 0) {
      content.setExposureTime(exifMetadata.getExposureTime());
    }
  }

  private void readDimensions(ImageContent content, InputStream is)
      throws IOException {
    BufferedImage bimg = ImageIO.read(is);
    if (bimg != null) {
      content.setWidth(bimg.getWidth());
      content.setHeight(bimg.getHeight());
    }
  }

}