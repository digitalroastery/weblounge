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

package ch.entwine.weblounge.common.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.content.LocalizedModificationContext;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.impl.util.xml.XPathNamespaceContext;
import ch.entwine.weblounge.common.language.Language;

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
 * Test case for the implementation of {@link LocalizedModificationContext}.
 */
public class LocalizedModificationContextImplXmlTest extends LocalizedModificationContextImplTest {

  /** File path and name */
  protected String testFile = "/localizedmodificationcontext.xml";

  /**
   * Test setup.
   */
  @Before
  @Override
  public void setUp() throws Exception {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    URL testContext = this.getClass().getResource(testFile);
    Document doc = docBuilder.parse(testContext.openStream());
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(new XPathNamespaceContext(true));
    ctx = LocalizedModificationContext.fromXml(doc.getFirstChild(), xpath);
  }

  /**
   * Test method for {@link ch.entwine.weblounge.common.impl.content.LocalizedModificationContext#toXml()}.
   */
  @Test
  public void testToXml() {
    String testXml = TestUtils.loadXmlFromResource(testFile);
    ctx.enableLanguage(german);
    ctx.enableLanguage(french);
    StringBuffer content = new StringBuffer("<content>");
    for (Language l : ctx.languages()) {
      content.append("<locale language=\"");
      content.append(l.getIdentifier());
      content.append("\"");
      if (l.equals(ctx.getOriginalLanguage())) {
        content.append(" original=\"true\"");
      }
      content.append(">");
      content.append(ctx.toXml(l));
      content.append("</locale>");
    }
    content.append("</content>");
    try {
      assertEquals(testXml, new String(content.toString().getBytes("utf-8"), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      fail("Encoding to utf-8 failed");
    }
  }

}
