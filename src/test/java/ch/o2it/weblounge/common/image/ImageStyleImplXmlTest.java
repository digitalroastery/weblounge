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

package ch.o2it.weblounge.common.image;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.TestUtils;
import ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.o2it.weblounge.common.impl.util.xml.XPathNamespaceContext;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Tests loading and serializing of {@link ImageStyleImpl} objects from and to
 * <code>XML</code>.
 */
public class ImageStyleImplXmlTest extends ImageStyleImplTest {

  /** File path and name */
  protected String testFile = "/imagestyle.xml";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    URL testContext = this.getClass().getResource(testFile);
    Document doc = docBuilder.parse(testContext.openStream());
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(new XPathNamespaceContext(true));
    imageStyle = ImageStyleImpl.fromXml(doc.getFirstChild(), xpath);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.image.ImageStyleImpl#toXml()} .
   */
  @Test
  public void testToXml() {
    String testXml = TestUtils.loadXmlFromResource(testFile);
    try {
      assertEquals(testXml, new String(imageStyle.toXml().getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      fail("Encoding to utf-8 failed");
    }
  }

}
