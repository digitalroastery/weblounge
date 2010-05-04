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

import ch.o2it.weblounge.contentrepository.impl.index.URIIndex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Test case for {@link URIIndex}.
 */
public class URIIndexTest {

  /** Id index */
  protected URIIndex idx = null;

  /** The index file */
  protected File indexFile = null;

  /** Number of bytes per entry */
  protected int pathLength = 128;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    indexFile = new File(new File(System.getProperty("java.io.tmpdir")), "id.idx");
    if (indexFile.exists())
      indexFile.delete();
    idx = new URIIndex(indexFile, false, pathLength);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    indexFile.delete();
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
    assertEquals(24, idx.size());
    String uuid = UUID.randomUUID().toString();
    try {
      idx.add(uuid, "/");
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
    assertEquals(24 + uuid.getBytes().length + pathLength, idx.size());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.URIIndex#getEntrySize()}
   * .
   */
  @Test
  public void testGetEntrySize() {
    assertEquals(pathLength, idx.getEntrySize());
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
      idx.add(UUID.randomUUID().toString(), "/");
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
      long address = idx.add(uuid, path);
      assertEquals(0, address);
      assertEquals(1, idx.getEntries());
      assertEquals(pathLength, idx.getEntrySize());
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
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
      address = idx.add(uuid, path);
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
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception while trying to access deleted data");
    }
    try {
      assertNull(idx.getPath(address));
      fail("Accessing deleted data by address did not result in an exception");
    } catch (IllegalStateException e) {
      // Expected
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception while trying to access deleted data");
    }
    
    // Add another entry and make sure it fills the empty slot
    try {
      long newAddress = idx.add(UUID.randomUUID().toString(), "/");
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
      long address = idx.add(uuid, path);

      // Update it
      idx.update(address, newPath);
      assertEquals(1, idx.getEntries());
      assertEquals(uuid, idx.getId(address));
      assertEquals(newPath, idx.getPath(address));
      assertEquals(pathLength, idx.getEntrySize());

      // ... again ...
      idx.update(address, path);
      assertEquals(1, idx.getEntries());
      assertEquals(uuid, idx.getId(address));
      assertEquals(path, idx.getPath(address));
      assertEquals(pathLength, idx.getEntrySize());

      // ... with a very long path
      idx.update(address, newLongPath.toString());
      assertEquals(1, idx.getEntries());
      assertEquals(uuid, idx.getId(address));
      assertEquals(newLongPath, idx.getPath(address));
      assertEquals(2 * pathLength, idx.getEntrySize());

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
      assertEquals(24, idx.size());

      idx.add(uuid, "/");
      idx.clear();
      assertEquals(0, idx.getEntries());
      assertEquals(24 + uuid.getBytes().length + pathLength, idx.size());
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
      address = idx.add(uuid1, shortPath);
      assertEquals(uuid1, idx.getId(address));
      address = idx.add(uuid2, path.toString());
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
      address = idx.add(uuid1, shortPath);
      assertEquals(shortPath, idx.getPath(address));
      address = idx.add(uuid2, path.toString());
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
  public void testResize() {
    long address = -1;
    String uuid = UUID.randomUUID().toString();
    StringBuffer path = new StringBuffer("/");
    for (int i = 0; i < pathLength + 1; i++)
      path.append("x");
    try {
      address = idx.add(uuid, path.toString());
      assertEquals(1, idx.getEntries());
      assertEquals(pathLength * 2, idx.getEntrySize());
      assertEquals(path.toString(), idx.getPath(address));
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error adding entry to the index");
    }
  }

}
