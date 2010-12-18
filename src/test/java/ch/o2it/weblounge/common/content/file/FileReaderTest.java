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

package ch.o2it.weblounge.common.content.file;

import static org.junit.Assert.assertEquals;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.impl.content.ResourceURIImpl;
import ch.o2it.weblounge.common.impl.content.file.FileResourceReader;
import ch.o2it.weblounge.common.impl.content.file.FileResourceURIImpl;
import ch.o2it.weblounge.common.impl.util.TestUtils;
import ch.o2it.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Test case to test {@link FileResourceReader}.
 */
public class FileReaderTest {

  /** The file that was read in */
  protected FileResource file = null;

  /** The site */
  protected Site site = null;

  /** The file uri */
  protected ResourceURIImpl fileURI = null;

  /** Name of the test file */
  protected String testFile = "/file.xml";

  /** Name of the other test file */
  protected String otherTestFile = "/file2.xml";

  /** The file reader */
  protected FileResourceReader reader = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.replay(site);
    fileURI = new FileResourceURIImpl(site, "/test", Resource.LIVE);
    reader = new FileResourceReader();
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.content.file.FileResourceReader#read(ch.o2it.weblounge.common.content.ResourceURI, java.io.InputStream)}
   * .
   */
  @Test
  public void testResetFileReader() throws Exception {
    URL testContext = this.getClass().getResource(testFile);
    String testXml = TestUtils.loadXmlFromResource(testFile);

    URL otherTestContext = this.getClass().getResource(otherTestFile);
    String otherTestXml = TestUtils.loadXmlFromResource(otherTestFile);

    // Read test file
    file = reader.read(testContext.openStream(), site);
    assertEquals(testXml, new String(file.toXml().getBytes("UTF-8")));

    // Read other test file
    file = reader.read(otherTestContext.openStream(), site);
    assertEquals(otherTestXml, new String(file.toXml().getBytes("UTF-8")));

    // Read test file again
    file = reader.read(testContext.openStream(), site);
    assertEquals(testXml, new String(file.toXml().getBytes("UTF-8")));
  }

}