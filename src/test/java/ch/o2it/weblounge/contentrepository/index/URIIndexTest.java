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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.o2it.weblounge.contentrepository.impl.index.URIIndex;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test case for {@link URIIndex}.
 */
public class URIIndexTest {

  /** Id index */
  protected URIIndex idx = null;

  /** The index file */
  protected File indexFile = null;

  /** The index root directory */
  protected File indexRootDir = null;

  /** Number of bytes for the header */
  protected int headerLength = 32;

  /** Number of bytes per id */
  protected int idLength = 36;

  /** Number of bytes per type */
  protected int typeLength = 8;

  /** Number of bytes per path */
  protected int pathLength = 128;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    indexRootDir = new File(System.getProperty("java.io.tmpdir"));
    indexFile = new File(indexRootDir, URIIndex.URI_IDX_NAME);
    if (indexFile.exists())
      indexFile.delete();
    idx = new URIIndex(indexRootDir, false, pathLength);
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
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#close()}.
   */
  @Test
  public void testClose() {
    try {
      idx.close();
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error while trying to close the index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#size()}.
   */
  @Test
  public void testSize() {
    assertEquals(headerLength, idx.size());
    String uuid = UUID.randomUUID().toString();
    try {
      idx.add(uuid, "page", "/");
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
    assertEquals(headerLength + uuid.getBytes().length + typeLength + pathLength, idx.size());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#getEntrySize()}
   * .
   */
  @Test
  public void testGetEntrySize() {
    assertEquals(idLength + typeLength + pathLength, idx.getEntrySize());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#getEntries()}
   * .
   */
  @Test
  public void testGetEntries() {
    assertEquals(0, idx.getEntries());
    try {
      idx.add(UUID.randomUUID().toString(), "page", "/");
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
    assertEquals(1, idx.getEntries());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#add(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testAdd() {
    try {
      String uuid = UUID.randomUUID().toString();
      String path = "/weblounge";
      long address = idx.add(uuid, "page", path);
      assertEquals(0, address);
      assertEquals(1, idx.getEntries());
      assertEquals(idLength + typeLength + pathLength, idx.getEntrySize());
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
  }

  /**
   * Adds some entries while making sure that a resize operation is triggered,
   * then clears the index, adds the entries again and then makes sure
   * everything can be looked up as expected.
   */
  @Test
  public void testExercise() {
    int entries = 2 * (int)idx.getSlots();
    String[] possibleTypes = new String[] { "page", "file", "image" };
 
    List<Long> addresses = new ArrayList<Long>();
    List<String> ids = new ArrayList<String>();
    List<String> types = new ArrayList<String>();
    List<String> paths = new ArrayList<String>();
    
    // Add a number of entries to the index, clear and re-add
    for (int take = 0; take < 2; take++) {

      for (int i = 0; i < entries; i++) {

        String id = UUID.randomUUID().toString();
        String type = possibleTypes[i % 3];
        StringBuffer b = new StringBuffer();
        for (int j = 0; j < (i % 4) + 1; j++) {
          b.append("/");
          b.append(UUID.randomUUID().toString());
        }
        String path = b.toString();
  
        try {
          long address = idx.add(id, type, path);
          assertEquals(i + 1, idx.getEntries());
  
          addresses.add(address);
          ids.add(id);
          types.add(type);
          paths.add(path);
          
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
          types.clear();
          paths.clear();
        } catch (IOException e) {
          e.printStackTrace();
          fail(e.getMessage());
        }
      }

    }
    
    // Close and reopen the index
    try {
      idx.close();
      idx = new URIIndex(indexRootDir, false, pathLength);
    } catch (IOException e) {
      fail("Error closing the index");
    }

    // Retrieve all of the entries
    for (int i = 0; i < addresses.size(); i++) {
      long address = addresses.get(i);
      try {
        assertEquals(ids.get(i), idx.getId(address));
        assertEquals(types.get(i), idx.getType(address));
        assertEquals(paths.get(i), idx.getPath(address));
      } catch (IOException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#delete(long)}
   * .
   */
  @Test
  public void testDelete() {
    long address = -1;
    String uuid = UUID.randomUUID().toString();
    String path = "/weblounge";
    try {
      address = idx.add(uuid, "page", path);
      idx.delete(address);
      assertEquals(0, idx.getEntries());
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }

    // Try to access the deleted data
    try {
      assertNull(idx.getId(address));
      fail("Accessing deleted data by id did not result in an exception");
    } catch (IllegalStateException e) {
      // Expected
    } catch (Throwable t) {
      t.printStackTrace();
      fail("Unexpected exception while trying to access deleted data");
    }
    try {
      assertNull(idx.getPath(address));
      fail("Accessing deleted data by address did not result in an exception");
    } catch (IllegalStateException e) {
      // Expected
    } catch (Throwable t) {
      t.printStackTrace();
      fail("Unexpected exception while trying to access deleted data");
    }
    
    // Add another entry and make sure it fills the empty slot
    try {
      long newAddress = idx.add(UUID.randomUUID().toString(), "page", "/");
      assertEquals(address, newAddress);
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding second entry to the index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#update(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testUpdate() {
    try {
      String uuid = UUID.randomUUID().toString();
      String path = "/weblounge";
      String newPath = "/etc/weblounge";
      StringBuffer newLongPathBuffer = new StringBuffer("/");
      for (int i = 0; i < pathLength + 1; i++)
        newLongPathBuffer.append("x");
      String newLongPath = newLongPathBuffer.toString();

      // Add the entry
      long address = idx.add(uuid, "page", path);

      // Update it
      idx.update(address, "page", newPath);
      assertEquals(1, idx.getEntries());
      assertEquals(uuid, idx.getId(address));
      assertEquals(newPath, idx.getPath(address));
      assertEquals(idLength + typeLength + pathLength, idx.getEntrySize());

      // ... again ...
      idx.update(address, "page", path);
      assertEquals(1, idx.getEntries());
      assertEquals(uuid, idx.getId(address));
      assertEquals(path, idx.getPath(address));
      assertEquals(idLength + typeLength + pathLength, idx.getEntrySize());

      // ... with a very long path
      idx.update(address, "page", newLongPath.toString());
      assertEquals(1, idx.getEntries());
      assertEquals(uuid, idx.getId(address));
      assertEquals(newLongPath, idx.getPath(address));
      assertEquals(idLength + typeLength + 2 * pathLength, idx.getEntrySize());

    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#clear()}.
   */
  @Test
  public void testClear() {
    String uuid = UUID.randomUUID().toString();
    try {
      idx.clear();
      assertEquals(0, idx.getEntries());
      assertEquals(headerLength, idx.size());

      idx.add(uuid, "page", "/");
      idx.clear();
      assertEquals(0, idx.getEntries());
      assertEquals(headerLength + uuid.getBytes().length + typeLength + pathLength, idx.size());
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error clearing the index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#getId(long)}
   * .
   */
  @Test
  public void testGetId() {
    long address = -1;
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String shortPath = "/weblounge";
    StringBuffer path = new StringBuffer("/");
    for (int i = 0; i < pathLength + 1; i++)
      path.append("x");
    try {
      address = idx.add(uuid1, "page", shortPath);
      assertEquals(uuid1, idx.getId(address));
      address = idx.add(uuid2, "page", path.toString());
      assertEquals(uuid2, idx.getId(address));
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#getPath(long)}
   * .
   */
  @Test
  public void testGetPath() {
    long address = -1;
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String shortPath = "/weblounge";
    StringBuffer path = new StringBuffer("/");
    for (int i = 0; i < pathLength + 1; i++)
      path.append("x");
    try {
      address = idx.add(uuid1, "page", shortPath);
      assertEquals(shortPath, idx.getPath(address));
      address = idx.add(uuid2, "page", path.toString());
      assertEquals(path.toString(), idx.getPath(address));
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#resize(int)}
   * .
   */
  @Test
  public void testResizeType() {
    long address = -1;
    String uuid = UUID.randomUUID().toString();
    
    // Test oversized type
    StringBuffer type = new StringBuffer();
    for (int i = 0; i < typeLength + 1; i++)
      type.append("x");
    try {
      address = idx.add(uuid, type.toString(), "/");
      assertEquals(1, idx.getEntries());
      assertEquals(idLength + 2 * typeLength + pathLength, idx.getEntrySize());
      assertEquals("/", idx.getPath(address));
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#resize(int)}
   * .
   */
  @Test
  public void testResizePath() {
    long address = -1;
    String uuid = UUID.randomUUID().toString();
    
    // Test oversized path
    StringBuffer path = new StringBuffer("/");
    for (int i = 0; i < pathLength + 1; i++)
      path.append("x");
    try {
      address = idx.add(uuid, "page", path.toString());
      assertEquals(1, idx.getEntries());
      assertEquals(idLength + typeLength + 2 * pathLength, idx.getEntrySize());
      assertEquals(path.toString(), idx.getPath(address));
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#resize(int)}
   * .
   */
  @Test
  public void testResizeTypeAndPath() {
    long address = -1;
    String uuid = UUID.randomUUID().toString();
    
    // Test oversized type and path
    StringBuffer type = new StringBuffer();
    for (int i = 0; i < typeLength + 1; i++)
      type.append("x");
    StringBuffer path = new StringBuffer("/");
    for (int i = 0; i < pathLength + 1; i++)
      path.append("x");
    try {
      address = idx.add(uuid, type.toString(), path.toString());
      assertEquals(1, idx.getEntries());
      assertEquals(idLength + 2 * typeLength + 2 * pathLength, idx.getEntrySize());
      assertEquals(path.toString(), idx.getPath(address));
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#getIndexVersion()}
   * .
   */
  @Test
  public void testGetIndexVersion() {
    assertEquals(VersionedContentRepositoryIndex.INDEX_VERSION, idx.getIndexVersion());
  }

}
