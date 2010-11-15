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

package ch.o2it.weblounge.common.content.file;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertEquals;

import ch.o2it.weblounge.common.impl.content.ResourceContentImpl;
import ch.o2it.weblounge.common.impl.content.file.FileContentImpl;
import ch.o2it.weblounge.common.impl.language.LanguageUtils;
import ch.o2it.weblounge.common.impl.user.UserImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.user.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Test case for class {@link ResourceContentImpl}.
 */
public class FileContentImplTest {
  
  /** The file content object to test */
  protected FileContent content = null;

  /** The filename */
  protected String filename = "document.pdf";
  
  /** The German language */
  protected Language german = LanguageUtils.getLanguage("de");
  
  /** The file size */
  protected long size = 1408338L;
  
  /** The mime type */
  protected String mimetype = "application/pdf";
  
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
    ((FileContentImpl)content).setCreated(creationDate, amelie);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceContentImpl#getLanguage()}.
   */
  @Test
  public void testGetLanguage() {
    assertEquals(german, content.getLanguage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceContentImpl#getMimetype()}.
   */
  @Test
  public void testGetMimetype() {
    assertEquals(mimetype, content.getMimetype());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceContentImpl#getFilename()}.
   */
  @Test
  public void testGetFilename() {
    assertEquals(filename, content.getFilename());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceContentImpl#getSize()}.
   */
  @Test
  public void testGetSize() {
    assertEquals(size, content.getSize());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceContentImpl#getCreationDate()}.
   */
  @Test
  public void testGetCreationDate() {
    assertEquals(creationDate, content.getCreationDate());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceContentImpl#getCreator()}.
   */
  @Test
  public void testGetCreator() {
    assertEquals(amelie, content.getCreator());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.content.ResourceContentImpl#isCreatedAfter(java.util.Date)}.
   */
  @Test
  public void testIsCreatedAfter() {
    assertFalse(content.isCreatedAfter(futureDate));
  }

}
