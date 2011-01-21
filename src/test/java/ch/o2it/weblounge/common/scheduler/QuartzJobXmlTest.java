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

package ch.o2it.weblounge.common.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.scheduler.QuartzJob;
import ch.o2it.weblounge.common.impl.util.TestUtils;
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
 * Test case for the part of the implementation of {@link QuartzJob}
 * that deals with <code>XML</code> serialization and deserialization.
 */
public class QuartzJobXmlTest extends QuartzJobTest {

  /** Test file */
  protected String testFile = "/job.xml";

  /**
   * @throws java.lang.Exception
   */
  @Before
  @Override
  public void setUp() throws Exception {
    setupPreliminaries();
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    URL testContext = this.getClass().getResource(testFile);
    Document doc = docBuilder.parse(testContext.openStream());
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(new XPathNamespaceContext(true));
    quartzJob = (QuartzJob)QuartzJob.fromXml(doc.getFirstChild(), xpath);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.PublishingContext#fromXml(org.w3c.dom.Node)}
   * .
   */
  @Test
  public void testFromXmlNode() {
    String testXml = TestUtils.loadXmlFromResource(testFile);
    try {
      assertEquals(testXml, new String(quartzJob.toXml().getBytes("utf-8"), "utf-8"));
    } catch (UnsupportedEncodingException e) {
      fail("Encoding to utf-8 failed");
    }
  }

}
