/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.impl.util.xml.ValidationErrorHandler;
import ch.o2it.weblounge.common.site.Site;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * This reader reads site representations from a serialized xml form.
 */
public final class SiteReader {

  /**
   * Reads the site from the serialized xml representation.
   * 
   * @param siteXml
   *          the site xml
   * @return the site
   * @throws SAXException
   *           If <code>siteXml</code> is malformed
   * @throws ParserConfigurationException
   *           If setting up the parser fails
   * @throws IOException
   *           If reading from <code>siteXml</code> fails
   */
  public Site read(InputStream siteXml) throws SAXException,
      ParserConfigurationException, IOException {
    // Schema validator setup
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    URL schemaUrl = SiteImpl.class.getResource("/xsd/site.xsd");
    Schema siteSchema = schemaFactory.newSchema(schemaUrl);

    // Site.xml document builder setup
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setSchema(siteSchema);
    docBuilderFactory.setNamespaceAware(true);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    // Validate and read the site descriptor
    ValidationErrorHandler errorHandler = new ValidationErrorHandler("site");
    docBuilder.setErrorHandler(errorHandler);
    Document doc = docBuilder.parse(siteXml);

    Site site = SiteImpl.fromXml(doc.getFirstChild());
    return site;
  }

}
