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

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.o2it.weblounge.contentrepository.impl.index.VersionIndex;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Test case for {@link VersionIndex}.
 */
public class VersionIndexTest {
  
  /** Version index */
  protected VersionIndex idx = null;

  /** The index file */
  protected File indexFile = null;

  /** Number of entries per slot */
  protected int versionsPerEntry = 8;

  /** The expected number of bytes used for this index */
  protected long expectedSize = -1;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    indexFile = new File(tmpDir, VersionIndex.VERSION_IDX_NAME);
    if (indexFile.exists())
      indexFile.delete();
    idx = new VersionIndex(tmpDir, false, versionsPerEntry);
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
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#close()}
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
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#size()}.
   */
  @Test
  public void testSize() {
    assertEquals(expectedSize, idx.size());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#getVersionsPerEntry()}
   * .
   */
  @Test
  public void testGetVersionsPerEntry() {
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#getEntries()}
   * .
   */
  @Test
  public void testGetEntries() {
    try {
      assertEquals(0, idx.getEntries());
      idx.add(UUID.randomUUID().toString(), Resource.LIVE);
      assertEquals(1, idx.getEntries());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#add(java.lang.String, long)}
   * .
   */
  @Test
  public void testAddStringLong() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String uuid3 = UUID.randomUUID().toString();
    try {
      idx.add(uuid1, Resource.LIVE);
      idx.add(uuid2, Resource.LIVE);
      assertEquals(2, idx.getEntries());
      int size = 28 + 2 * (uuid1.getBytes().length + 4 + versionsPerEntry  * 8);
      assertEquals(size, idx.size());
      
      // test reusing slots
      idx.delete(0);
      assertEquals(size, idx.size());
      long address = idx.add(uuid3, Resource.LIVE);
      assertEquals(0, address);
      assertEquals(size, idx.size());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#add(long, long)}
   * .
   */
  @Test
  public void testAddLongLong() {
    String uuid1 = UUID.randomUUID().toString();
    try {
      long address = idx.add(idx.add(uuid1, Resource.LIVE), Resource.WORK);
      assertEquals(2, idx.getEntries());
      int size = 28 + uuid1.getBytes().length + 4 + versionsPerEntry  * 8;
      assertEquals(size, idx.size());
      assertEquals(2, idx.getVersions(address).length);
      assertEquals(0, idx.getVersions(address)[0]);
      assertEquals(1, idx.getVersions(address)[1]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#delete(long)}
   * .
   */
  @Test
  public void testDeleteLong() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      idx.add(idx.add(uuid1, Resource.LIVE), Resource.WORK);
      idx.add(uuid2, Resource.LIVE);
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
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#delete(long, long)}
   * .
   */
  @Test
  public void testDeleteLongLong() {
    String uuid1 = UUID.randomUUID().toString();
    try {
      long address = idx.add(idx.add(uuid1, Resource.LIVE), Resource.WORK);
      idx.delete(address, Resource.LIVE);
      assertEquals(1, idx.getVersions(address).length);
      assertEquals(Resource.WORK, idx.getVersions(address)[0]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#clear()}
   * .
   */
  @Test
  public void testClear() {
    String uuid = UUID.randomUUID().toString();
    long address = 2384762;
    try {
      idx.add(uuid, address);
      idx.clear();
      assertEquals(0, idx.getEntries());
      assertEquals(versionsPerEntry, idx.getVersionsPerEntry());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#getVersions(long)}
   * .
   */
  @Test
  public void testGetVersions() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      long address1 = idx.add(idx.add(uuid1, Resource.LIVE), Resource.WORK);
      long address2 = idx.add(uuid2, Resource.LIVE);
      assertEquals(2, idx.getVersions(address1).length);
      assertEquals(Resource.LIVE, idx.getVersions(address1)[0]);
      assertEquals(Resource.WORK, idx.getVersions(address1)[1]);
      assertEquals(1, idx.getVersions(address2).length);
      assertEquals(Resource.LIVE, idx.getVersions(address2)[0]);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#hasVersion(long, long)}
   * .
   */
  @Test
  public void testHasVersion() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      long address1 = idx.add(idx.add(uuid1, Resource.LIVE), Resource.WORK);
      long address2 = idx.add(uuid2, Resource.WORK);
      assertTrue(idx.hasVersion(address1, Resource.LIVE));
      assertTrue(idx.hasVersion(address1, Resource.WORK));
      assertFalse(idx.hasVersion(address1, 27));
      assertTrue(idx.hasVersion(address2, Resource.WORK));
      assertFalse(idx.hasVersion(address2, Resource.LIVE));
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#hasVersions(long)}
   * .
   */
  @Test
  public void testHasVersions() {
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    try {
      long address1 = idx.add(idx.add(uuid1, Resource.LIVE), Resource.WORK);
      long address2 = idx.add(uuid2, Resource.WORK);
      idx.delete(address2, Resource.WORK);
      assertTrue(idx.hasVersions(address1));
      assertFalse(idx.hasVersions(address2));
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#resize(int, int)}
   * .
   */
  @Test
  public void testResize() {
    String uuid = UUID.randomUUID().toString();
    long version = 2384762;
    long slotsInIndex = 0;
    int bytesPerId = uuid.getBytes().length;
    
    // Resize to the same size
    try {
      idx.add(uuid, version);
      slotsInIndex = idx.getEntries();
      idx.resize(bytesPerId, versionsPerEntry * 3);
      assertEquals(versionsPerEntry * 3, idx.getVersionsPerEntry());
      assertEquals(1, idx.getEntries());
      assertEquals(28 + (slotsInIndex * (bytesPerId + 4 + versionsPerEntry * 3 * 8)), idx.size());
      long[] versions = idx.getVersions(0);
      assertEquals(1, versions.length);
      assertEquals(version, versions[0]);
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
   * {@link ch.o2it.weblounge.contentrepository.impl.index.VersionIndex#getIndexVersion()}
   * .
   */
  @Test
  public void testGetIndexVersion() {
    assertEquals(VersionedContentRepositoryIndex.IDX_VERSION, idx.getIndexVersion());
  }

}
