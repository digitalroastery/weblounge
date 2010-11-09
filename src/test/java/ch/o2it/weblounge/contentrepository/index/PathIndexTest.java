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

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.fail;

import ch.o2it.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.o2it.weblounge.contentrepository.impl.index.PathIndex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test cases for {@link PathIndex}.
 */
public class PathIndexTest {

  /** Path index */
  protected PathIndex idx = null;

  /** The index file */
  protected File indexFile = null;

  /** The index root directory */
  protected File indexRootDir = null;

  /** The number of slots in this index */
  protected int slotsInIndex = 16;

  /** Number of entries per slot */
  protected int entriesPerSlot = 8;

  /** The expected number of bytes used for this index */
  protected long expectedSize = -1;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    indexRootDir = new File(System.getProperty("java.io.tmpdir"));
    indexFile = new File(indexRootDir, PathIndex.PATH_IDX_NAME);
    if (indexFile.exists())
      indexFile.delete();
    idx = new PathIndex(indexRootDir, false, slotsInIndex, entriesPerSlot);
    expectedSize = 24 + (slotsInIndex * (4 + entriesPerSlot * 8));
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
   * {@link ch.o2it.weblounge.contentrepository.impl.index.PathIndex#size()}.
   */
  @Test
  public void testSize() {
    assertEquals(expectedSize, idx.size());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.PathIndex#getEntries()}
   * .
   */
  @Test
  public void testGetEntries() {
    assertEquals(0, idx.getEntries());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.PathIndex#getEntriesPerSlot()}
   * .
   */
  @Test
  public void testGetSlotSize() {
    assertEquals(entriesPerSlot, idx.getEntriesPerSlot());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.PathIndex#getLoadFactor()}
   * .
   */
  @Test
  public void testGetLoadFactor() {
    assertEquals(0, idx.getLoadFactor());

    // Fill half of the index
    long totalEntries = idx.getSlots() * idx.getEntriesPerSlot();
    for (long i = 0; i < totalEntries / 2; i++) {
      try {
        idx.set(i, UUID.randomUUID().toString());
      } catch (IOException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }
    assertEquals(0.5, idx.getLoadFactor());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.PathIndex#set(long, java.lang.String)}
   * .
   */
  @Test
  public void testAdd() {
    String path = UUID.randomUUID().toString();
    long address = 2384762;
    try {
      idx.set(address, path);
      long[] candidates = idx.locate(path);
      assertEquals(1, candidates.length);
      assertEquals(address, candidates[0]);
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
    int entries = 2 * (int) idx.getSlots() * idx.getEntriesPerSlot();
    List<String> paths = new ArrayList<String>(entries);

    // Add a number of entries to the index, clear and re-add
    for (int take = 0; take < 2; take++) {

      for (int i = 0; i < entries; i++) {
        StringBuffer b = new StringBuffer();
        for (int j = 0; j < (i % 4) + 1; j++) {
          b.append("/");
          b.append(UUID.randomUUID().toString());
        }
        String path = b.toString();
        paths.add(path);
        long address = path.hashCode();
        try {
          idx.set(address, path);
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
      idx = new PathIndex(indexRootDir, false, slotsInIndex, entriesPerSlot);
    } catch (IOException e) {
      fail("Error closing the index");
    }
    

    // Retrieve all of the entries
    for (String path : paths) {
      long address = path.hashCode();
      long[] possibleAddresses;
      try {
        possibleAddresses = idx.locate(path);
        boolean found = false;
        for (long a : possibleAddresses) {
          if (a == address) {
            found = true;
            break;
          }
        }
        if (!found)
          fail("Address not found in candidates returned by index");
      } catch (IOException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }
    
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.PathIndex#delete(java.lang.String, long)}
   * .
   */
  @Test
  public void testDelete() {
    String path = UUID.randomUUID().toString();
    long address = 2384762;
    try {
      idx.set(address, path);
      idx.delete(path, address);
      long[] candidates = idx.locate(path);
      assertEquals(0, candidates.length);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.PathIndex#clear()}.
   */
  @Test
  public void testClear() {
    String path = UUID.randomUUID().toString();
    long address = 2384762;
    try {
      idx.set(address, path);
      idx.clear();
      assertTrue(idx.getEntries() == 0);
      assertEquals(slotsInIndex, idx.getSlots());
      assertEquals(entriesPerSlot, idx.getEntriesPerSlot());
      assertEquals(0.0, idx.getLoadFactor());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.PathIndex#locate(java.lang.String)}
   * .
   */
  @Test
  public void testGetPossibleSlots() {
    String path = UUID.randomUUID().toString();
    long address = 2384762;
    try {
      idx.set(address, path);
      long[] candidates = idx.locate(path);
      assertEquals(1, candidates.length);
      assertEquals(address, candidates[0]);
      idx.set(address + 1, path);
      candidates = idx.locate(path);
      assertEquals(2, candidates.length);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.PathIndex#resize()}.
   */
  @Test
  public void testResize() {
    String path = UUID.randomUUID().toString();
    long address = 2384762;

    // Resize to the same size
    try {
      idx.set(address, path);
      idx.resize(slotsInIndex, entriesPerSlot * 3);
      assertEquals(slotsInIndex, idx.getSlots());
      assertEquals(entriesPerSlot * 3, idx.getEntriesPerSlot());
      assertEquals(1, idx.getEntries());
      assertEquals(24 + (slotsInIndex * (4 + entriesPerSlot * 3 * 8)), idx.size());
      long[] candidates = idx.locate(path);
      assertEquals(1, candidates.length);
      assertEquals(address, candidates[0]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    // Try to resize the number of slots (forbidden if there are already entries
    // in the index)
    try {
      idx.resize(slotsInIndex - 1, entriesPerSlot);
      fail("Index did not prohibit resizing the slots");
    } catch (IOException e) {
      fail("Index tried resize operation, although it should have refused");
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.PathIndex#getIndexVersion()}
   * .
   */
  @Test
  public void testGetIndexVersion() {
    assertEquals(VersionedContentRepositoryIndex.INDEX_VERSION, idx.getIndexVersion());
  }

}
