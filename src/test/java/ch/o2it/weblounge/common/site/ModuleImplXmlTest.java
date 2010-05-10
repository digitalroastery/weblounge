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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.TestUtils;
import ch.o2it.weblounge.common.impl.site.ModuleImpl;
import ch.o2it.weblounge.common.impl.site.SiteImpl;
import ch.o2it.weblounge.common.impl.util.xml.ValidationErrorHandler;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * Test case for the xml serialization of the {@link ModuleImpl}.
 */
public class ModuleImplXmlTest extends ModuleImplTest {

  /** Name of the test file */
  protected String testFile = "/module.xml";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setUpPreliminaries();

    // Schema validator setup
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    URL schemaUrl = SiteImpl.class.getResource("/xsd/module.xsd");
    Schema siteSchema = schemaFactory.newSchema(schemaUrl);
    
    // Site.xml document builder setup
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setSchema(siteSchema);
    docBuilderFactory.setNamespaceAware(true);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    // Validate and read the site descriptor
    URL testContext = this.getClass().getResource(testFile);
    ValidationErrorHandler errorHandler = new ValidationErrorHandler(testContext);
    docBuilder.setErrorHandler(errorHandler);
    Document doc = docBuilder.parse(testContext.openStream());
    assertFalse("Schema validation failed", errorHandler.hasErrors());

    module = ModuleImpl.fromXml(doc.getFirstChild());
    module.setSite(site);
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.ActionSupport#toXml()}.
   */
  @Test
  public void testToXml() {
    String testXml = TestUtils.loadXmlFromFile(testFile);
    try {
      assertEquals(testXml, new String(module.toXml().getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      fail("Encoding to utf-8 failed");
    }
  }

}
