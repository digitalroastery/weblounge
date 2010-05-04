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
import static org.junit.Assert.fail;

import ch.o2it.weblounge.contentrepository.impl.index.IdIndex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Test cases for {@link IdIndex}.
 */
public class IdIndexTest {

  /** Id index */
  protected IdIndex idx = null;

  /** The index file */
  protected File indexFile = null;

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
    indexFile = new File(new File(System.getProperty("java.io.tmpdir")), "id.idx");
    if (indexFile.exists())
      indexFile.delete();
    idx = new IdIndex(indexFile, false, slotsInIndex, entriesPerSlot);
    expectedSize = 16 + (slotsInIndex * (4 + entriesPerSlot * 8));
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
   * {@link ch.o2it.weblounge.contentrepository.impl.index.IdIndex#size()}.
   */
  @Test
  public void testSize() {
    assertEquals(expectedSize, idx.size());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.IdIndex#getEntries()}
   * .
   */
  @Test
  public void testGetEntries() {
    assertEquals(0, idx.getEntries());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.IdIndex#getSlotSize()}
   * .
   */
  @Test
  public void testGetSlotSize() {
    assertEquals(entriesPerSlot, idx.getSlotSize());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.IdIndex#getLoadFactor()}
   * .
   */
  @Test
  public void testGetLoadFactor() {
    assertEquals(0, idx.getLoadFactor());

    // Fill half of the index
    long totalEntries = idx.getSlots() * idx.getSlotSize();
    for (long i = 0; i < totalEntries / 2; i++) {
      try {
        idx.add(UUID.randomUUID().toString(), i);
      } catch (IOException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }
    assertEquals(0.5, idx.getLoadFactor());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.IdIndex#add(java.lang.String, long)}
   * .
   */
  @Test
  public void testAdd() {
    String uuid = UUID.randomUUID().toString();
    long address = 2384762;
    try {
      idx.add(uuid, address);
      long[] candidates = idx.locate(uuid);
      assertEquals(1, candidates.length);
      assertEquals(address, candidates[0]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.IdIndex#delete(java.lang.String, long)}
   * .
   */
  @Test
  public void testDelete() {
    String uuid = UUID.randomUUID().toString();
    long address = 2384762;
    try {
      idx.add(uuid, address);
      idx.delete(uuid, address);
      long[] candidates = idx.locate(uuid);
      assertEquals(0, candidates.length);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.IdIndex#clear()}.
   */
  @Test
  public void testClear() {
    String uuid = UUID.randomUUID().toString();
    long address = 2384762;
    try {
      idx.add(uuid, address);
      idx.clear();
      assertEquals(0, idx.getEntries());
      assertEquals(slotsInIndex, idx.getSlots());
      assertEquals(entriesPerSlot, idx.getSlotSize());
      assertEquals(0.0, idx.getLoadFactor());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.IdIndex#locate(java.lang.String)}
   * .
   */
  @Test
  public void testGetPossibleSlots() {
    String uuid = UUID.randomUUID().toString();
    long address = 2384762;
    try {
      idx.add(uuid, address);
      long[] candidates = idx.locate(uuid);
      assertEquals(1, candidates.length);
      assertEquals(address, candidates[0]);
      idx.add(uuid, address + 1);
      candidates = idx.locate(uuid);
      assertEquals(2, candidates.length);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.IdIndex#resize()}.
   */
  @Test
  public void testResize() {
    String uuid = UUID.randomUUID().toString();
    long address = 2384762;
    
    // Resize to the same size
    try {
      idx.add(uuid, address);
      idx.resize(slotsInIndex, entriesPerSlot * 3);
      assertEquals(slotsInIndex, idx.getSlots());
      assertEquals(entriesPerSlot * 3, idx.getSlotSize());
      assertEquals(1, idx.getEntries());
      assertEquals(16 + (slotsInIndex * (4 + entriesPerSlot * 3 * 8)), idx.size());
      long[] candidates = idx.locate(uuid);
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

}
