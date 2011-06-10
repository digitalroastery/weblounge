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

package ch.entwine.weblounge.contentrepository.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Test case for {@link LanguageIndex}.
 */
public class LanguageIndexTest {

  /** Language index */
  protected LanguageIndex idx = null;

  /** The index file */
  protected File indexFile = null;

  /** The index root directory */
  protected File indexRootDir = null;

  /** Number of entries per slot */
  protected int versionsPerEntry = 8;

  /** The expected number of bytes used for this index */
  protected long expectedSize = -1;

  /** English */
  protected Language english = LanguageUtils.getLanguage("en");

  /** German */
  protected Language german = LanguageUtils.getLanguage("de");

  /** Italian */
  protected Language french = LanguageUtils.getLanguage("fr");

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    indexRootDir = new File(System.getProperty("java.io.tmpdir"));
    indexFile = new File(indexRootDir, LanguageIndex.LANGUAGE_IDX_NAME);
    if (indexFile.exists())
      indexFile.delete();
    idx = new LanguageIndex(indexRootDir, false, versionsPerEntry);
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
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#close()}
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
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#size()}
   * .
   */
  @Test
  public void testSize() {
    assertEquals(expectedSize, idx.size());
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#getVersionsPerEntry()}
   * .
   */
  @Test
  public void testGetVersionsPerEntry() {
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#getEntries()}
   * .
   */
  @Test
  public void testGetEntries() {
    try {
      assertEquals(0, idx.getEntries());
      idx.set(UUID.randomUUID().toString(), toSet(english));
      assertEquals(1, idx.getEntries());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#set(java.lang.String, Language)}
   * .
   */
  @Test
  public void testAddStringLong() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String uuid3 = UUID.randomUUID().toString();
    try {
      idx.set(uuid1, toSet(english));
      idx.set(uuid2, toSet(english));
      assertEquals(2, idx.getEntries());
      int size = 28 + 2 * (uuid1.getBytes().length + 4 + versionsPerEntry * 8);
      assertEquals(size, idx.size());

      // test reusing slots
      idx.delete(0);
      assertEquals(size, idx.size());
      long address = idx.set(uuid3, toSet(english));
      assertEquals(0, address);
      assertEquals(size, idx.size());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Adds some entries while making sure that a resize operation is triggered,
   * then clears the index, adds the entries again and then makes sure
   * everything can be looked up as expected.
   */
  @Test
  public void testExercise() {
    int entries = 2 * (int)idx.getSlots() * idx.getEntriesPerSlot();
    
    List<Language> possibleLanguages = new ArrayList<Language>();
    possibleLanguages.add(english);
    possibleLanguages.add(german);
    possibleLanguages.add(french);

    List<Long> addresses = new ArrayList<Long>();
    List<String> ids = new ArrayList<String>();
    List<Set<Language>> languages = new ArrayList<Set<Language>>();
    
    // Add a number of entries to the index, clear and re-add
    for (int take = 0; take < 2; take++) {

      for (int i = 0; i < entries; i++) {
        String uuid = UUID.randomUUID().toString();
        Set<Language> languageSet = new HashSet<Language>();
        for (int j = 0; j < (i % 2) + 1; j++) {
          languageSet.add(possibleLanguages.get(j));
        }
        try {
          long address = idx.set(uuid, languageSet);
          ids.add(uuid);
          languages.add(languageSet);
          addresses.add(address);
          assertEquals(i + 1, idx.getEntries());
        } catch (IOException e) {
          e.printStackTrace();
          fail(e.getMessage());
        }
      }
      
      // After the first take, clear the index
      if (take == 0) {
        try {
          idx.clear();
          addresses.clear();
          ids.clear();
          languages.clear();
        } catch (IOException e) {
          e.printStackTrace();
          fail(e.getMessage());
        }
      }
      
    }
    
    // Close and reopen the index
    try {
      idx.close();
      idx = new LanguageIndex(indexRootDir, false, versionsPerEntry);
    } catch (IOException e) {
      fail("Error closing the index");
    }

    // Retrieve all of the entries
    for (int i = 0; i < addresses.size(); i++) {
      long address = addresses.get(i);
      Set<Language> languageSet = languages.get(i);
      try {
        assertTrue(idx.hasLanguage(address));
        for (Language language : possibleLanguages) {
          if (languageSet.contains(language)) {
            assertTrue(idx.hasLanguage(address, language));
          } else {
            assertFalse(idx.hasLanguage(address, language));
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#set(long, Language)}
   * .
   */
  @Test
  public void testSetLongLanguage() {
    String uuid1 = UUID.randomUUID().toString();
    try {
      long address = idx.set(uuid1, toSet(english));
      idx.add(address, german);
      assertEquals(2, idx.getEntries());
      int size = 28 + uuid1.getBytes().length + 4 + versionsPerEntry * 8;
      assertEquals(size, idx.size());
      assertEquals(2, idx.get(address).length);
      assertEquals(english, idx.get(address)[0]);
      assertEquals(german, idx.get(address)[1]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#set(long, Language)}
   * .
   */
  @Test
  public void testSetLongNull() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      long address1 = idx.set(uuid1, toSet(english));
      idx.set(address1, null);
      long address2 = idx.set(uuid2, null);
      assertEquals(0, idx.getEntries());
      assertEquals(0, idx.get(address1).length);
      assertEquals(0, idx.get(address2).length);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#delete(long)}
   * .
   */
  @Test
  public void testDeleteLong() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      idx.set(uuid1, toSet(english, german));
      idx.set(uuid2, toSet(english));
      int size = 28 + 2 * (uuid1.getBytes().length + 4 + versionsPerEntry * 8);
      idx.delete(0);
      assertEquals(size, idx.size());
      assertEquals(1, idx.getEntries());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#delete(long, Language)}
   * .
   */
  @Test
  public void testDeleteLongLanguage() {
    String uuid1 = UUID.randomUUID().toString();
    try {
      long address = idx.set(uuid1, toSet(english));
      idx.add(address, german);
      idx.delete(address, english);
      assertEquals(1, idx.get(address).length);
      assertEquals(german, idx.get(address)[0]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#clear()}
   * .
   */
  @Test
  public void testClear() {
    String uuid = UUID.randomUUID().toString();
    try {
      idx.set(uuid, toSet(german));
      idx.clear();
      assertEquals(0, idx.getEntries());
      assertEquals(versionsPerEntry, idx.getEntriesPerSlot());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#get(long)}
   * .
   */
  @Test
  public void testGetLanguages() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      long address1 = idx.set(uuid1, toSet(english, german));
      long address2 = idx.set(uuid2, toSet(english));
      assertEquals(2, idx.get(address1).length);
      assertEquals(english, idx.get(address1)[0]);
      assertEquals(german, idx.get(address1)[1]);
      assertEquals(1, idx.get(address2).length);
      assertEquals(english, idx.get(address2)[0]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#hasLanguage(long, Language)}
   * .
   */
  @Test
  public void testHasLanguages() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      long address1 = idx.set(uuid1, toSet(english, german));
      long address2 = idx.set(uuid2, toSet(german));
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
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#resize(int, int)}
   * .
   */
  @Test
  public void testResize() {
    String uuid = UUID.randomUUID().toString();
    long slotsInIndex = 0;
    int bytesPerId = uuid.getBytes().length;

    // Resize to the same size
    try {
      idx.set(uuid, toSet(german));
      slotsInIndex = idx.getEntries();
      idx.resize(bytesPerId, versionsPerEntry * 3);
      assertEquals(versionsPerEntry * 3, idx.getEntriesPerSlot());
      assertEquals(1, idx.getEntries());
      assertEquals(28 + (slotsInIndex * (bytesPerId + 4 + versionsPerEntry * 3 * 8)), idx.size());
      Language[] languages = idx.get(0);
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
   * {@link ch.entwine.weblounge.contentrepository.impl.index.LanguageIndex#getIndexVersion()}
   * .
   */
  @Test
  public void testGetIndexVersion() {
    assertEquals(VersionedContentRepositoryIndex.INDEX_VERSION, idx.getIndexVersion());
  }

  /**
   * Returns a set containing the languages.
   * 
   * @param languages
   *          the languages
   * @return the set
   */
  protected Set<Language> toSet(Language... languages) {
    Set<Language> set = new HashSet<Language>();
    for (Language l : languages) {
      set.add(l);
    }
    return set;
  }

}
