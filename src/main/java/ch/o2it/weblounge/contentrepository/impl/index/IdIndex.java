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

package ch.o2it.weblounge.contentrepository.impl.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This index is part of the content repository index and maps <code>int</code>
 * representations of page identifiers to slot numbers in the <code>URI</code>
 * or the <code>Tree</code> index.
 * 
 * <pre>
 * | slot | slot number in tree.idx
 * |------------------------------------------
 * | 1    | addressOf(a-b-c-d), addressOf(e-f-g-h)
 * | 2    | addressOf(u-v-w-y)
 * </pre>
 * 
 * The index is made up of a header which consists of the number of slots (4
 * bytes) followed by the number of addresses per slot (4 bytes) and then the
 * slots containing the indicated number of 64-bit addresses (8 bytes).
 * <p>
 * Within each slot, there will be a sentinel of value <code>-1</code> so we
 * don't have to read the whole line.
 */
public class IdIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(IdIndex.class);

  /** Number of bytes that are used for the index header */
  protected static final int IDX_HEADER_SIZE = 8;

  /** Default number of entries in index */
  private static final int IDX_ENTRIES = 128;

  /** Default number of addresses per index entries */
  private static final int IDX_ADDRESSES_PER_ENTRY = 64;

  /** The index file */
  protected RandomAccessFile idx = null;

  /** Number of slots in the ids index */
  protected int slots = IDX_ENTRIES;

  /** Number of paths per slot */
  protected int entriesPerSlot = IDX_ADDRESSES_PER_ENTRY;

  /** Number of paths per slot */
  protected long slotSizeInBytes = entriesPerSlot * 8;

  /**
   * Creates an index from the given file. If the file does not exist, it is
   * created and initialized with the default index settings.
   * 
   * @param indexFile
   *          location of the index file
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @throws IOException
   *           if reading from the index fails
   */
  public IdIndex(File indexFile, boolean readOnly) throws IOException {
    String mode = readOnly ? "r" : "rwd";
    try {
      idx = new RandomAccessFile(indexFile, mode);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Index file " + indexFile + " does not exist");
    }

    // Read index header information
    try {
      slots = idx.readInt();
      entriesPerSlot = idx.readInt();
    } catch (EOFException e) {
      if (readOnly) {
        throw new IllegalStateException("Readonly index cannot be empty");
      }
      logger.info("Initializing index with default index values");
      init(IDX_ENTRIES, IDX_ADDRESSES_PER_ENTRY);
    } catch (IOException e) {
      logger.error("Error reading from id index: " + e.getMessage());
      throw e;
    }
    slotSizeInBytes = entriesPerSlot * 8;
  }

  /**
   * Returns the index size in bytes. The size is calculated from the size of
   * the header plus the number of slots multiplied by the size of one slot.
   * 
   * @return the index size
   */
  public long size() {
    return IDX_HEADER_SIZE + (slots * slotSizeInBytes);
  }

  /**
   * Adds the id to the index.
   * 
   * @param id
   *          the page identifier
   * @param addressOfId
   *          slot number in main index
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized void add(String id, long addressOfId) throws IOException {
    int slot = locate(id);
    long startOfSlot = IDX_HEADER_SIZE + (slot * slotSizeInBytes);
    long lastEntryInSlot = startOfSlot + slotSizeInBytes - 8;

    idx.seek(startOfSlot);

    // skip to the end of the slot and make sure there is still room left
    // for an additional address
    long address = -1;
    int entriesInSlot = 0;
    while ((address = idx.readLong()) != -1)
      entriesInSlot++;

    // is there room for an additional address?
    if (address > lastEntryInSlot)
      // TODO: trigger reindex
      throw new IllegalStateException("Maximum slot size reached");

    // add the new address and rewrite the sentinel
    idx.seek(address - 8);
    idx.writeLong(addressOfId);
    idx.writeLong(-1);

    entriesInSlot++;
    logger.debug("Added address of id {} as entry no {} to slot {}", new Object[] {
        id,
        entriesInSlot,
        slot });
  }

  /**
   * Removes all entries for the given page uri from the index.
   * 
   * @param id
   *          the identifier
   * @param addressOfId
   *          slot number in main index
   * @throws IOException
   *           if removing the entry from the index fails
   */
  public synchronized void delete(String id, long addressOfId) throws IOException {
    long[] entries = new long[entriesPerSlot];

    // Move to the beginning of the slot
    int slot = locate(id);
    long startOfSlot = IDX_HEADER_SIZE + (slot * slotSizeInBytes);
    idx.seek(startOfSlot);

    // Read the existing entries into memory
    long address = -1;
    int i = 0;
    while ((address = idx.readLong()) != -1) {
      entries[i++] = address;
    }
    entries[i] = -1;
    
    // Write all of them back except for the one that needs to be deleted
    idx.seek(startOfSlot);
    for (long entry : entries) {
      if (entry == addressOfId)
        continue;
      if (entry == -1) {
        idx.writeLong(entry);
        break;
      }
      idx.writeLong(entry);
    }
    
    logger.debug("Removed id '{}' from index", id);
  }

  /**
   * Removes all entries from the index.
   * 
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized void clear() throws IOException {
    init(IDX_ENTRIES, IDX_ADDRESSES_PER_ENTRY);
  }

  /**
   * Initializes the index file to the given size using <code>0</code> byte
   * values and writes the header information to it.
   * 
   * @param slots
   *          the number of slots
   * @param entriesPerSlot
   *          the number of <code>long</code> entries per slot
   * @throws IOException
   *           writing to the index fails
   */
  private void init(int slots, int entriesPerSlot) throws IOException {
    this.slots = slots;
    this.entriesPerSlot = entriesPerSlot;
    this.slotSizeInBytes = entriesPerSlot * 8;

    long totalSizeInBytes = entriesPerSlot * slotSizeInBytes;
    long totalEntries = slots * entriesPerSlot;

    idx.seek(0);
    idx.writeInt(IDX_ENTRIES);
    idx.writeInt(IDX_ADDRESSES_PER_ENTRY);
    for (long b = 0; b < totalSizeInBytes; b++) {
      if (b % entriesPerSlot == 0)
        idx.write(-1);
      else
        idx.writeByte(0);
    }

    logger.info("Id index initialized to {} entries ({} per slot)", totalEntries, entriesPerSlot);
  }

  /**
   * Returns the slot number of the id's entry in the <code>ids</code> index.
   * 
   * @param id
   *          the uuid
   * @return the slot number
   */
  private int locate(String id) {
    int slot = id.hashCode() % slots;
    return slot;
  }

  /**
   * Returns the id's possible slot numbers in the <code>URI</code> or
   * <code>Tree</code> index. This involves doing a lookup in the
   * <code>Id</code> index in order to map the identifier to the correct slot
   * number.
   * 
   * @param id
   *          the uuid
   * @return the possible slot numbers in the id or tree index
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized long[] getPossibleSlots(String id) throws IOException {
    int slot = locate(id);
    long startOfSlot = IDX_HEADER_SIZE + (slot * slotSizeInBytes);
    idx.seek(startOfSlot);
    long[] ids = new long[entriesPerSlot];
    long address = -1;
    int i = 0;
    while ((address = idx.readLong()) != -1) {
      ids[i++] = address;
    }
    return ids;
  }

}
