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
import ch.entwine.weblounge.common.content.audiovisual.AudioVisualResource;
import ch.entwine.weblounge.common.impl.content.AbstractResourceReaderImpl;
import ch.entwine.weblounge.common.site.Site;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility class used to parse audio visual data.
 */
public class AudioVisualResourceReader extends AbstractResourceReaderImpl<AudioVisualContent, AudioVisualResource> {

  /** The audio visual content reader */
  private AudioVisualContentReader contentReader = new AudioVisualContentReader();

  /**
   * Creates a new reader that will parse the XML data and store it in the
   * <code>AudioVisual</code> object that is returned by the {@link #read}
   * method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public AudioVisualResourceReader() throws ParserConfigurationException,
      SAXException {
    super(AudioVisualResource.TYPE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.AbstractResourceReaderImpl#reset()
   */
  public void reset() {
    super.reset();
    contentReader.reset();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.content.AbstractResourceReaderImpl#createResource(ch.entwine.weblounge.common.site.Site)
   */
  protected AudioVisualResource createResource(Site site) {
    return new AudioVisualResourceImpl(new AudioVisualResourceURIImpl(site));
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

    // resource content
    if ("content".equals(raw) || parserContext.equals(ParserContext.Content)) {
      parserContext = ParserContext.Content;
      contentReader.startElement(uri, local, raw, attrs);
    }

    // other stuff, most likely head elements
    else {
      super.startElement(uri, local, raw, attrs);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.util.xml.WebloungeSAXHandler#characters(char[],
   *      int, int)
   */
  @Override
  public void characters(char[] chars, int start, int end) throws SAXException {
    if (parserContext.equals(ParserContext.Content))
      contentReader.characters(chars, start, end);
    super.characters(chars, start, end);
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String local, String raw)
      throws SAXException {

    // File content
    if ("content".equals(raw)) {
      contentReader.endElement(uri, local, raw);
      parserContext = ParserContext.Resource;
      resource.addContent(contentReader.getContent());
    } else if (parserContext.equals(ParserContext.Content)) {
      contentReader.endElement(uri, local, raw);
    } else {
      super.endElement(uri, local, raw);
    }
  }

}
