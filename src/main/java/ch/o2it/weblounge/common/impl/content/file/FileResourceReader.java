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

package ch.o2it.weblounge.common.impl.content.file;

import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.file.FileContent;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.impl.content.AbstractResourceReaderImpl;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility class used to parse file data.
 */
public class FileResourceReader extends AbstractResourceReaderImpl<FileContent, FileResource> {

  /** The file content reader */
  private FileContentReader contentReader = new FileContentReader();

  /**
   * Creates a new file data reader that will parse the XML data and store it in
   * the <code>File</code> object that is returned by the {@link #read} method.
   * 
   * @throws ParserConfigurationException
   *           if the SAX parser setup failed
   * @throws SAXException
   *           if an error occurs while parsing
   */
  public FileResourceReader() throws ParserConfigurationException, SAXException {
    super(FileResource.TYPE);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.content.AbstractResourceReaderImpl#reset()
   */
  public void reset() {
    super.reset();
    contentReader.reset();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.content.AbstractResourceReaderImpl#createResource(ch.o2it.weblounge.common.content.ResourceURI)
   */
  @Override
  protected FileResource createResource(ResourceURI uri) {
    return new FileResourceImpl(uri);
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

    // read the file url
    if (FileResource.TYPE.equals(raw)) {
      parserContext = ParserContext.Resource;
      ((ResourceURIImpl) resource.getURI()).setIdentifier(attrs.getValue("id"));
      if (attrs.getValue("path") != null)
        ((ResourceURIImpl) resource.getURI()).setPath(attrs.getValue("path"));
    }

    // file content
    else if ("content".equals(raw) || parserContext.equals(ParserContext.Content)) {
      parserContext = ParserContext.Content;
      contentReader.startElement(uri, local, raw, attrs);
    }

    else {
      super.startElement(uri, local, raw, attrs);
    }
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.util.xml.WebloungeSAXHandler#characters(char[], int, int)
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
      resource.addContent(contentReader.getFileContent());
    }
    
    // file content
    else if (parserContext.equals(ParserContext.Content)) {
      contentReader.endElement(uri, local, raw);
    }

    else {
      super.endElement(uri, local, raw);
    }
  }

}
