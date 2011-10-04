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

package ch.entwine.weblounge.common.impl.content.audiovisual;

import ch.entwine.weblounge.common.content.audiovisual.AudioVisualContent;
import ch.entwine.weblounge.common.content.audiovisual.ScanType;
import ch.entwine.weblounge.common.impl.content.ResourceContentReaderImpl;
import ch.entwine.weblounge.common.language.Language;
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

/**
 * Utility class used to parse <code>Content</code> data for audio visual
 * objects.
 */
public class AudioVisualContentReader extends ResourceContentReaderImpl<AudioVisualContent> {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(AudioVisualContentReader.class);

  /** The audio stream */
  protected AudioStreamImpl audioStream = null;

  /** The video stream */
  protected VideoStreamImpl videoStream = null;

  private enum ParserContext {
    AudioVisual, AudioStream, VideoStream
  }

  /** The current parser context */
  private ParserContext context = ParserContext.AudioVisual;

  /**
   * Creates a new content reader that will parse serialized XML version of
   * audio visual content and store it in the
   * {@link ch.entwine.weblounge.common.content.AudioVisualContent} that is
   * returned by the {@link #read} method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   * 
   * @see #createFromXml(InputStream)
   */
  public AudioVisualContentReader() throws ParserConfigurationException,
      SAXException {
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
  public AudioVisualContent createFromXml(InputStream is) throws SAXException,
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
  public AudioVisualContent createFromContent(InputStream is, User user,
      Language language, long size, String fileName, String mimeType)
      throws IOException {

    AudioVisualContent content = new AudioVisualContentImpl(fileName, language, mimeType);

    // Use the logged in user as the author
    content.setCreator(user);

    // Set the creation date
    content.setCreationDate(new Date());

    // MemoryCacheSeekableStream mcss = new MemoryCacheSeekableStream(is);
    // UnclosableInputStream bis = new UnclosableInputStream(mcss);

    // Read the audio visual metadata

    // TODO: Call media info and extract the metadata

    // Close the input stream
    // IOUtils.closeQuietly(mcss);
    // bis = null;

    return content;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.ResourceContentReaderImpl#createContent()
   */
  @Override
  protected AudioVisualContent createContent() {
    return new AudioVisualContentImpl();
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
      context = ParserContext.AudioVisual;
    }

    // audio stream
    else if ("audio".equals(raw)) {
      audioStream = new AudioStreamImpl();
      context = ParserContext.AudioStream;
    }

    // video stream
    else if ("video".equals(raw)) {
      videoStream = new VideoStreamImpl();
      context = ParserContext.VideoStream;
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

    switch (context) {

      case AudioVisual:

        // file name
        if ("filename".equals(raw)) {
          content.setFilename(getCharacters());
          logger.trace("AudioVisuals's file name is '{}'", content.getMimetype());
        }

        // mime type
        if ("mimetype".equals(raw)) {
          content.setMimetype(getCharacters());
          logger.trace("AudioVisuals's content mimetype is '{}'", content.getMimetype());
        }

        // size
        else if ("size".equals(raw)) {
          content.setSize(Long.parseLong(getCharacters()));
          logger.trace("AudioVisual's content filesize is {} bytes", content.getSize());
        }

        // size
        else if ("duration".equals(raw)) {
          ((AudioVisualContentImpl) content).setDuration(Long.parseLong(getCharacters()));
          logger.trace("AudioVisual's duration is {} ms", content.getSize());
        }

        // Everything else
        else {
          super.endElement(uri, local, raw);
        }

        break;

      case AudioStream:

        if ("audio".equals(raw)) {
          ((AudioVisualContentImpl) content).addStream(audioStream);
          context = ParserContext.AudioVisual;
          audioStream = null;
        }

        // bit depth
        else if ("bitdepth".equals(raw)) {
          audioStream.setBitDepth(Integer.parseInt(getCharacters()));
          logger.trace("Audio stream has a bit depth of {}", audioStream.getBitDepth());
        }

        // bit rate
        else if ("bitrate".equals(raw)) {
          audioStream.setBitRate(Float.parseFloat(getCharacters()));
          logger.trace("Audio stream has a bit rate of {}", audioStream.getBitRate());
        }

        // channels
        else if ("channels".equals(raw)) {
          audioStream.setChannels(Integer.parseInt(getCharacters()));
          logger.trace("Audio stream has {} channels", audioStream.getChannels());
        }

        // format
        else if ("format".equals(raw)) {
          audioStream.setFormat(getCharacters());
          logger.trace("Audio stream is encoded as {}", audioStream.getFormat());
        }

        // sampling rate
        else if ("samplingrate".equals(raw)) {
          audioStream.setSamplingRate(Integer.parseInt(getCharacters()));
          logger.trace("Audio stream has a sampling rate of {}", audioStream.getSamplingRate());
        }

        break;

      case VideoStream:

        if ("video".equals(raw)) {
          ((AudioVisualContentImpl) content).addStream(videoStream);
          context = ParserContext.AudioVisual;
          videoStream = null;
        }

        // bit rate
        else if ("bitrate".equals(raw)) {
          videoStream.setBitRate(Float.parseFloat(getCharacters()));
          logger.trace("Video stream has a bit rate of {}", videoStream.getBitRate());
        }

        // format
        else if ("format".equals(raw)) {
          videoStream.setFormat(getCharacters());
          logger.trace("Video stream is encoded as {}", videoStream.getFormat());
        }

        // frame rate
        else if ("framerate".equals(raw)) {
          videoStream.setFrameRate(Float.parseFloat(getCharacters()));
          logger.trace("Video stream has a frame rate of {}", videoStream.getFrameRate());
        }

        // resolution
        else if ("resolution".equals(raw)) {
          String[] resolutionParts = getCharacters().split("x");
          if (resolutionParts.length != 2)
            throw new IllegalStateException("Resolution must be of form wxh");
          videoStream.setFrameWidth(Integer.parseInt(resolutionParts[0]));
          videoStream.setFrameHeight(Integer.parseInt(resolutionParts[1]));
          logger.trace("Video stream resolution is {}x{}", videoStream.getFrameWidth(), videoStream.getFrameHeight());
        }

        // scan type
        else if ("scantype".equals(raw)) {
          videoStream.setScanType(ScanType.fromString(getCharacters()));
          logger.trace("Video stream scan type is {}", videoStream.getScanType());
        }

        break;

      default:
        super.endElement(uri, local, raw);

    }

  }

}