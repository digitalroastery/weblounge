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
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Date;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

/**
 * Utility class used to parse <code>Content</code> data for simple files.
 */
public class ImageContentReader extends ResourceContentReaderImpl<ImageContent> {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ImageContentReader.class);

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

  /**
   * This method is called to parse the serialized XML of a
   * {@link ch.entwine.weblounge.common.content.ResourceContent}.
   * 
   * @param is
   *          the content data
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws IOException
   *           if reading the input stream fails
   * @throws SAXException
   *           if an error occurs while parsing
   */
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

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.ResourceContentReader#createFromContent(java.io.InputStream,
   *      ch.entwine.weblounge.common.language.Language, long, java.lang.String)
   */
  public ImageContent createFromContent(InputStream is, User user,
      Language language, long size, String fileName, String mimeType)
      throws IOException {
    ImageContent content = new ImageContentImpl(fileName, language, mimeType);
    content.setCreator(user);
    content.setCreationDate(new Date());

    // Fork the input stream so that it can be consumed twice
//    PipedOutputStream outputStream = new PipedOutputStream();
//    PipedInputStream pipedInputStream = new PipedInputStream(outputStream);
//    final InputStream tis = new TeeInputStream(is, outputStream);

//    ImageMetadataExtractor extractor = new ImageMetadataExtractor(tis);
//    Thread extractorThread = new Thread(extractor);
//    extractorThread.start();

    InputStream pipedInputStream = is;

    // Read the image metadata
    SeekableStream imageInputStream = null;
    try {
      imageInputStream = new MemoryCacheSeekableStream(pipedInputStream);
      RenderedOp image = JAI.create("stream", imageInputStream);
      content.setWidth(image.getWidth());
      content.setHeight(image.getHeight());
    } finally {
      IOUtils.closeQuietly(imageInputStream);
    }

    // Read the image's EXIF data
    ImageMetadata imageMetadata = null;
//    synchronized (extractor) {
//      while ((imageMetadata = extractor.getMetadata()) == null) {
//        try {
//          extractor.wait();
//        } catch (InterruptedException e) {
//          logger.warn("Interrupted while waiting for image metadata extraction");
//          break;
//        }
//      }
//    }

    if (imageMetadata == null)
      return content;

    // Add metadata
    if (!StringUtils.isBlank(imageMetadata.getPhotographer())) {
      content.setCreator(new UserImpl(user.getLogin(), user.getRealm(), imageMetadata.getPhotographer()));
    }
    if (imageMetadata.getDateTaken() != null) {
      content.setCreationDate(imageMetadata.getDateTaken());
    }
    if (!StringUtils.isBlank(imageMetadata.getLocation())) {
      content.setLocation(imageMetadata.getLocation());
    }
    if (imageMetadata.getGpsLat() != 0 && imageMetadata.getGpsLong() != 0) {
      content.setGpsPosition(imageMetadata.getGpsLat(), imageMetadata.getGpsLong());
    }
    if (imageMetadata.getFilmspeed() != 0) {
      content.setFilmspeed(imageMetadata.getFilmspeed());
    }
    if (imageMetadata.getFNumber() != 0) {
      content.setFNumber(imageMetadata.getFNumber());
    }
    if (imageMetadata.getFocalWidth() != 0) {
      content.setFocalWidth(imageMetadata.getFocalWidth());
    }
    if (imageMetadata.getExposureTime() != 0) {
      content.setExposureTime(imageMetadata.getExposureTime());
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

    // mimetype
    if ("mimetype".equals(raw)) {
      content.setMimetype(getCharacters());
      logger.trace("Images's content mimetype is '{}'", content.getMimetype());
    }

    // size
    else if ("size".equals(raw)) {
      content.setSize(Long.parseLong(getCharacters()));
      logger.trace("Image's content filesize is '{}'", content.getSize());
    }

    // width
    else if ("width".equals(raw)) {
      content.setWidth(Integer.parseInt(getCharacters()));
      logger.trace("Image's width is '{}'", content.getWidth());
    }

    // height
    else if ("height".equals(raw)) {
      content.setHeight(Integer.parseInt(getCharacters()));
      logger.trace("Image's height is '{}'", content.getHeight());
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

    // focalwidth
    else if ("focalwidth".equals(raw)) {
      content.setFocalWidth(Integer.parseInt(getCharacters()));
      logger.trace("Image's focalwidth is '{}'", content.getFocalWidth());
    }

    // exposuretime
    else if ("exposuretime".equals(raw)) {
      content.setExposureTime(Float.parseFloat(getCharacters()));
      logger.trace("Image's exposuretime is '{}'", content.getExposureTime());
    }

    else {
      super.endElement(uri, local, raw);
    }
  }

  /**
   * Implements a separate thread that extracts the image metadata.
   */
  private static class ImageMetadataExtractor implements Runnable {

    /** The input stream */
    private InputStream imageInputStream = null;

    /** The image metadata */
    private ImageMetadata metadata = null;

    ImageMetadataExtractor(InputStream is) {
      imageInputStream = is;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
      BufferedInputStream bis = new BufferedInputStream(imageInputStream);
      try {
        metadata = ImageMetadataUtils.extractMetadata(bis);
        while (bis.available() > 0) {
          bis.read();
        }
      } catch (Throwable t) {
        synchronized (this) {
          this.notify();
        }
      }
    }

    /**
     * Returns the extracted metadata.
     * 
     * @return the metadata
     */
    ImageMetadata getMetadata() {
      return metadata;
    }

  }

}