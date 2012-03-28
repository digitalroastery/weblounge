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

package ch.entwine.weblounge.common.content.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import ch.entwine.weblounge.common.impl.content.file.FileContentImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Date;

/**
 * Test case for class
 * {@link ch.entwine.weblounge.common.impl.content.ResourceContentImpl}.
 */
public class FileContentImplTest {

  /** The file content object to test */
  protected FileContent content = null;

  /** The filename */
  protected String filename = "document.pdf";

  /** The author */
  protected String author = "Hans Muster";

  /** The German language */
  protected Language german = LanguageUtils.getLanguage("de");

  /** The file size */
  protected long size = 1408338L;

  /** The mime type */
  protected String mimetype = "application/pdf";

  /** The source file */
  protected String source = "http://entwinmedia.com/filexyz.ogg";

  /** The external location */
  protected String externalLocation = "http://www.youtube.com/watch?v=UF8uR6Z6KLc";

  /** The creation date */
  protected Date creationDate = new Date(1231358741000L);

  /** Some date after the latest modification date */
  protected Date futureDate = new Date(2000000000000L);

  /** The creation date */
  protected User amelie = new UserImpl("amelie", "testland", "Amélie Poulard");

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    content = new FileContentImpl(filename, german, mimetype, size);
    content.setMimetype(mimetype);
    content.setSource(source);
    content.setExternalLocation(new URL(externalLocation));
    content.setAuthor(author);
    ((FileContentImpl) content).setCreated(creationDate, amelie);
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.file.FileContentImpl#getLanguage()}
   * .
   */
  @Test
  public void testGetLanguage() {
    assertEquals(german, content.getLanguage());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.file.FileContentImpl#getMimetype()}
   * .
   */
  @Test
  public void testGetMimetype() {
    assertEquals(mimetype, content.getMimetype());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.file.FileContentImpl#getFilename()}
   * .
   */
  @Test
  public void testGetFilename() {
    assertEquals(filename, content.getFilename());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.file.FileContentImpl#getSource()}
   * .
   */
  @Test
  public void testGetSource() {
    assertEquals(source, content.getSource());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.file.FileContentImpl#getExternalLocation()}
   * .
   */
  @Test
  public void testGetExternalLocation() {
    assertEquals(externalLocation, content.getExternalLocation().toExternalForm());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.file.FileContentImpl#getAuthor()}
   * .
   */
  @Test
  public void testGetAuthor() {
    assertEquals(author, content.getAuthor());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.file.FileContentImpl#getSize()}
   * .
   */
  @Test
  public void testGetSize() {
    assertEquals(size, content.getSize());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.file.FileContentImpl#getCreationDate()}
   * .
   */
  @Test
  public void testGetCreationDate() {
    assertEquals(creationDate, content.getCreationDate());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.file.FileContentImpl#getCreator()}
   * .
   */
  @Test
  public void testGetCreator() {
    assertEquals(amelie, content.getCreator());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.common.impl.content.file.FileContentImpl#isCreatedAfter(Date)}
   * .
   */
  @Test
  public void testIsCreatedAfter() {
    assertFalse(content.isCreatedAfter(futureDate));
  }

}
