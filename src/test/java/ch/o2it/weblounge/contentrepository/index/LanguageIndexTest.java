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

package ch.o2it.weblounge.contentrepository.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Test case for {@link LanguageIndex}.
 */
public class LanguageIndexTest {
  
  /** Language index */
  protected LanguageIndex idx = null;

  /** The index file */
  protected File indexFile = null;

  /** Number of entries per slot */
  protected int versionsPerEntry = 8;

  /** The expected number of bytes used for this index */
  protected long expectedSize = -1;
  
  /** English */
  protected Language english = LanguageSupport.getLanguage("en"); 

  /** German */
  protected Language german = LanguageSupport.getLanguage("de"); 

  /** Italian */
  protected Language french = LanguageSupport.getLanguage("fr"); 

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    indexFile = new File(tmpDir, LanguageIndex.LANGUAGE_IDX_NAME);
    if (indexFile.exists())
      indexFile.delete();
    idx = new LanguageIndex(tmpDir, false, versionsPerEntry);
    expectedSize = 28;
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    idx.close();
    FileUtils.deleteQuietly(indexFile);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#close()}
   * .
   */
  @Test
  public void testClose() {
    try {
      idx.close();
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#size()}.
   */
  @Test
  public void testSize() {
    assertEquals(expectedSize, idx.size());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#getVersionsPerEntry()}
   * .
   */
  @Test
  public void testGetVersionsPerEntry() {
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#getEntries()}
   * .
   */
  @Test
  public void testGetEntries() {
    try {
      assertEquals(0, idx.getEntries());
      idx.add(UUID.randomUUID().toString(), english);
      assertEquals(1, idx.getEntries());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#add(java.lang.String, Language)}
   * .
   */
  @Test
  public void testAddStringLong() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String uuid3 = UUID.randomUUID().toString();
    try {
      idx.add(uuid1, english);
      idx.add(uuid2, english);
      assertEquals(2, idx.getEntries());
      int size = 28 + 2 * (uuid1.getBytes().length + 4 + versionsPerEntry  * 8);
      assertEquals(size, idx.size());
      
      // test reusing slots
      idx.delete(0);
      assertEquals(size, idx.size());
      long address = idx.add(uuid3, english);
      assertEquals(0, address);
      assertEquals(size, idx.size());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#add(long, Language)}
   * .
   */
  @Test
  public void testAddLongLong() {
    String uuid1 = UUID.randomUUID().toString();
    try {
      long address = idx.add(idx.add(uuid1, english), german);
      assertEquals(2, idx.getEntries());
      int size = 28 + uuid1.getBytes().length + 4 + versionsPerEntry  * 8;
      assertEquals(size, idx.size());
      assertEquals(2, idx.getLanguages(address).length);
      assertEquals(english, idx.getLanguages(address)[0]);
      assertEquals(german, idx.getLanguages(address)[1]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#delete(long)}
   * .
   */
  @Test
  public void testDeleteLong() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      idx.add(idx.add(uuid1, english), german);
      idx.add(uuid2, english);
      int size = 28 + 2 * (uuid1.getBytes().length + 4 + versionsPerEntry  * 8);
      idx.delete(0);
      assertEquals(size, idx.size());
      assertEquals(2, idx.getEntries());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#delete(long, Language)}
   * .
   */
  @Test
  public void testDeleteLongLanguage() {
    String uuid1 = UUID.randomUUID().toString();
    try {
      long address = idx.add(idx.add(uuid1, english), german);
      idx.delete(address, english);
      assertEquals(1, idx.getLanguages(address).length);
      assertEquals(german, idx.getLanguages(address)[0]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#clear()}
   * .
   */
  @Test
  public void testClear() {
    String uuid = UUID.randomUUID().toString();
    try {
      idx.add(uuid, german);
      idx.clear();
      assertEquals(0, idx.getEntries());
      assertEquals(versionsPerEntry, idx.getLanguagesPerEntry());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#getLanguages(long)}
   * .
   */
  @Test
  public void testGetLanguages() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      long address1 = idx.add(idx.add(uuid1, english), german);
      long address2 = idx.add(uuid2, english);
      assertEquals(2, idx.getLanguages(address1).length);
      assertEquals(english, idx.getLanguages(address1)[0]);
      assertEquals(german, idx.getLanguages(address1)[1]);
      assertEquals(1, idx.getLanguages(address2).length);
      assertEquals(english, idx.getLanguages(address2)[0]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#hasLanguage(long, Language)}
   * .
   */
  @Test
  public void testHasLanguages() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      long address1 = idx.add(idx.add(uuid1, english), german);
      long address2 = idx.add(uuid2, german);
      assertTrue(idx.hasLanguage(address1, english));
      assertTrue(idx.hasLanguage(address1, german));
      assertFalse(idx.hasLanguage(address1, french));
      assertTrue(idx.hasLanguage(address2, german));
      assertFalse(idx.hasLanguage(address2, english));
      idx.delete(address2, german);
      assertTrue(idx.hasLanguage(address1));
      assertFalse(idx.hasLanguage(address2));
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#resize(int, int)}
   * .
   */
  @Test
  public void testResize() {
    String uuid = UUID.randomUUID().toString();
    long slotsInIndex = 0;
    int bytesPerId = uuid.getBytes().length;
    
    // Resize to the same size
    try {
      idx.add(uuid, german);
      slotsInIndex = idx.getEntries();
      idx.resize(bytesPerId, versionsPerEntry * 3);
      assertEquals(versionsPerEntry * 3, idx.getLanguagesPerEntry());
      assertEquals(1, idx.getEntries());
      assertEquals(28 + (slotsInIndex * (bytesPerId + 4 + versionsPerEntry * 3 * 8)), idx.size());
      Language[] languages = idx.getLanguages(0);
      assertEquals(1, languages.length);
      assertEquals(german, languages[0]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    // Try to resize the number of slots (forbidden if there are already entries
    // in the index)
    try {
      idx.resize(bytesPerId - 1, versionsPerEntry);
      fail("Index did not prohibit resizing the slots");
    } catch (IOException e) {
      fail("Index tried resize operation, although it should have refused");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.LanguageIndex#getIndexVersion()}
   * .
   */
  @Test
  public void testGetIndexVersion() {
    assertEquals(VersionedContentRepositoryIndex.IDX_VERSION, idx.getIndexVersion());
  }

}
