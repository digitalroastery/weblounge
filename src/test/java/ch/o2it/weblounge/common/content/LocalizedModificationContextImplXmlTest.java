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

package ch.o2it.weblounge.common.content;

import static org.junit.Assert.fail;

import static org.junit.Assert.assertEquals;

import ch.o2it.weblounge.common.TestUtils;
import ch.o2it.weblounge.common.impl.content.LocalizedModificationContext;
import ch.o2it.weblounge.common.language.Language;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
    ctx = LocalizedModificationContext.fromXml(doc.getFirstChild());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.LocalizedModificationContext#toXml()}.
   */
  @Test
  public void testToXml() {
    String testXml = TestUtils.loadXmlFromFile(testFile);
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
      assertEquals(testXml, new String(content.toString().getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      fail("Encoding to utf-8 failed");
    }
  }

}
