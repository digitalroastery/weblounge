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

package ch.entwine.weblounge.common.content.audiovisual;

import static org.junit.Assert.assertEquals;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.audiovisual.AudioVisualResourceReader;
import ch.entwine.weblounge.common.impl.content.audiovisual.AudioVisualResourceURIImpl;
import ch.entwine.weblounge.common.impl.util.TestUtils;
import ch.entwine.weblounge.common.site.Site;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * Test case to test {@link AudioVisualResourceReader}.
 */
public class AudioVisualResourceReaderTest {

  /** The audio visual that was read in */
  protected AudioVisualResource audioVisual = null;

  /** The site */
  protected Site site = null;

  /** The audio visual uri */
  protected ResourceURIImpl avURI = null;

  /** Name of the test file */
  protected String testFile = "/av.xml";

  /** The audio visual resource reader */
  protected AudioVisualResourceReader reader = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    site = EasyMock.createNiceMock(Site.class);
    EasyMock.replay(site);
    avURI = new AudioVisualResourceURIImpl(site, "/test", Resource.LIVE);
    reader = new AudioVisualResourceReader();
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.audiovisual.AudioVisualResourceReader#read(ch.entwine.weblounge.common.content.ResourceURI, java.io.InputStream)}
   * .
   */
  @Test
  public void testRead() throws Exception {
    URL testContext = this.getClass().getResource(testFile);
    String testXml = TestUtils.loadXmlFromResource(testFile);
    audioVisual = reader.read(testContext.openStream(), site);
    assertEquals(testXml, new String(audioVisual.toXml().getBytes("utf-8"), "utf-8"));
  }

}