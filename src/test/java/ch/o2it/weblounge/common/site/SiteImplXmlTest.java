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

package ch.o2it.weblounge.common.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.TestUtils;
import ch.o2it.weblounge.common.impl.site.SiteImpl;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * Test case for the xml capabilities of {@link SiteImpl}.
 */
public class SiteImplXmlTest extends SiteImplTest {

  /** Name of the test file */
  protected String testFile = "/site.xml";
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPrerequisites();
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    URL schemaUrl = SiteImpl.class.getResource("/xsd/site.xsd");
    Schema siteSchema = schemaFactory.newSchema(schemaUrl);
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setSchema(siteSchema);
    docBuilderFactory.setValidating(true);
    docBuilderFactory.setNamespaceAware(true);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    URL testContext = this.getClass().getResource(testFile);
    Document doc = docBuilder.parse(testContext.openStream());
    siteSchema.newValidator().validate(new DOMSource(doc.getFirstChild()));
    site = SiteImpl.fromXml(doc.getFirstChild());
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.PageImpl#toXml()}.
   */
  @Test
  public void testToXml() {
    String testXml = TestUtils.loadXmlFromFile(testFile);
    try {
      assertEquals(testXml, new String(site.toXml().getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      fail("Encoding to utf-8 failed");
    }
  }

}
