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

import org.apache.commons.io.FilenameUtils;
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
 * | slot | entries | slot number in tree.idx
 * |------------------------------------------
 * | 1    | 2       | addressOf(a-b-c-d), addressOf(e-f-g-h)
 * | 2    | 1       | addressOf(u-v-w-y)
 * </pre>
 * 
 * The index is made up of a header which consists of the number of slots (4
 * bytes) followed by the number of addresses per slot (4 bytes), the number of
 * entries currently in the index and then the slots containing the indicated
 * number of 64-bit addresses (8 bytes).
 */
public class IdIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(IdIndex.class);

  /** Number of bytes that are used for the index header */
  protected static final int IDX_HEADER_SIZE = 16;

  /** Default number of entries in index */
  private static final int IDX_ENTRIES = 128;

  /** Default number of addresses per index entries */
  private static final int IDX_ADDRESSES_PER_ENTRY = 64;

  /** Size of an entry (long) in bytes */
  protected static final int IDX_ENTRY_SIZE = 8;

  /** Location of the slots header */
  protected static final long IDX_SLOTS_HEADER_LOCATION = 0;

  /** Location of the entries-per-slot header */
  protected static final long IDX_ENTRIES_PER_SLOT_HEADER_LOCATION = 4;

  /** Location of the entries header */
  protected static final long IDX_ENTRIES_HEADER_LOCATION = 8;

  /** Location of the first entry */
  protected static final long IDX_ENTRIES_LOCATION = 16;

  /** The index file */
  protected RandomAccessFile idx = null;

  /** The index file */
  protected File idxFile = null;

  /** True if this is a readonly index */
  protected boolean isReadOnly = false;

  /** Number of slots in the ids index */
  protected int slots = IDX_ENTRIES;

  /** Number of paths per slot */
  protected int entriesPerSlot = IDX_ADDRESSES_PER_ENTRY;

  /** Number of paths per slot */
  protected long slotSizeInBytes = 4 + (entriesPerSlot * IDX_ENTRY_SIZE);

  /** Number of entries */
  protected long entries = 0;

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
    this(indexFile, readOnly, IDX_ENTRIES, IDX_ADDRESSES_PER_ENTRY);
  }

  /**
   * Creates an index from the given file. If the file does not exist, it is
   * created and initialized with the default index settings.
   * <p>
   * The number of slots and entries per slot defines the size of the index. At
   * creation time, the index implementation will allocate a file on disk which
   * is <code>slots * entriesPerSlot * 8</code> bytes large, plus 16 bytes of
   * header information.
   * 
   * @param indexFile
   *          location of the index file
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @param slots
   *          the number of slots
   * @param entriesPerSlot
   *          the number of entries per slot
   * @throws IOException
   *           if reading from the index fails
   */
  public IdIndex(File indexFile, boolean readOnly, int slots, int entriesPerSlot)
      throws IOException {

    this.idxFile = indexFile;
    this.isReadOnly = readOnly;

    String mode = readOnly ? "r" : "rwd";
    try {
      idx = new RandomAccessFile(indexFile, mode);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Index file " + indexFile + " does not exist");
    }

    // Read index header information
    try {
      this.slots = idx.readInt();
      this.entriesPerSlot = idx.readInt();
      this.entries = idx.readLong();
      this.slotSizeInBytes = 4 + (entriesPerSlot * IDX_ENTRY_SIZE);
      if (this.slots != slots || this.entriesPerSlot != entriesPerSlot)
        resize(slots, entriesPerSlot);
    } catch (EOFException e) {
      if (readOnly) {
        throw new IllegalStateException("Readonly index cannot be empty");
      }
      logger.info("Initializing index with default index values");
      init(slots, entriesPerSlot);
    } catch (IOException e) {
      logger.error("Error reading from id index: " + e.getMessage());
      throw e;
    }
  }

  /**
   * Closes this index.
   * 
   * @throws IOException
   *           if closing the index file fails
   */
  public void close() throws IOException {
    idx.close();
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
   * Returns the number of slots.
   * 
   * @return the number of slots
   */
  public int getSlots() {
    return slots;
  }

  /**
   * Returns the number of entries per slot;
   * 
   * @return the number of entries per slot
   */
  public int getSlotSize() {
    return entriesPerSlot;
  }

  /**
   * Returns the number of entries.
   * 
   * @return the number of entries
   */
  public long getEntries() {
    return entries;
  }

  /**
   * Returns the load factor for this index, which is determined by the number
   * of entries divided by the number of possible entries.
   * 
   * @return the load factor
   */
  public float getLoadFactor() {
    return entries / (slots * entriesPerSlot);
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
    int slot = findSlot(id);
    long startOfSlot = IDX_HEADER_SIZE + (slot * slotSizeInBytes);

    // Make sure there is still room left
    // for an additional address
    idx.seek(startOfSlot);
    int entriesInSlot = idx.readInt();
    if (entriesInSlot == entriesPerSlot) {
      logger.info("Maximum slot size reached, triggering index resize");
      resize(slots, entriesPerSlot * 2);
      startOfSlot = IDX_HEADER_SIZE + (slot * slotSizeInBytes);
      idx.seek(startOfSlot);
    }

    // Add the new address at the end
    idx.skipBytes(entriesInSlot * IDX_ENTRY_SIZE);
    idx.writeLong(addressOfId);

    entries++;
    entriesInSlot++;

    // Update the number of entries in this slot
    idx.seek(startOfSlot);
    idx.writeInt(entriesInSlot);

    // Update the file header
    idx.seek(IDX_ENTRIES_HEADER_LOCATION);
    idx.writeLong(entries);

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
   * @throws IllegalStateException
   *           if the id is not part of the index
   */
  public synchronized void delete(String id, long addressOfId)
      throws IOException {

    // Move to the beginning of the slot
    int slot = findSlot(id);
    long startOfSlot = IDX_HEADER_SIZE + (slot * slotSizeInBytes);

    // Read all entries from the current slot
    idx.seek(startOfSlot);
    int entriesInSlot = idx.readInt();
    int deleteEntry = -1;
    long[] slotEntries = new long[entriesInSlot];
    for (int i = 0; i < entriesInSlot; i++) {
      long entry = idx.readLong();
      slotEntries[i] = entry;
      if (entry == addressOfId)
        deleteEntry = i;
    }

    // See if we are trying to delete a ghost entry
    if (deleteEntry == -1)
      throw new IllegalStateException("Id '" + id + "' is not part of the index");

    // Write everything back but the entry that needs to be deleted
    entriesInSlot--;
    idx.seek(startOfSlot);
    idx.writeInt(entriesInSlot);
    idx.skipBytes((deleteEntry + 1) * IDX_ENTRY_SIZE);
    for (int i = deleteEntry + 1; i < slotEntries.length; i++) {
      idx.writeLong(slotEntries[i]);
    }
    idx.writeLong(0);

    // Update the file header
    entries--;
    idx.seek(IDX_ENTRIES_HEADER_LOCATION);
    idx.writeLong(entries);

    logger.debug("Removed id '{}' from index", id);
  }

  /**
   * Removes all entries from the index.
   * 
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized void clear() throws IOException {
    init(slots, entriesPerSlot);
  }

  /**
   * Returns the slot number of the id's entry in the <code>ids</code> index.
   * 
   * @param id
   *          the uuid
   * @return the slot number
   */
  private int findSlot(String id) {
    int slot = Math.abs(id.hashCode()) % slots;
    return slot;
  }

  /**
   * Returns the id's possible slot numbers in the <code>URI</code> or
   * <code>Tree</code> index.
   * <p>
   * The reason for this method returning a number of addresses rather than one
   * is that a hashing algorithm is used to map ids to slot numbers in order to
   * keep the index small. Therefore, if you want to get to the final entry in
   * the tree or children index, you need to test all of the addresses returned
   * by this method.
   * <p>
   * 
   * @param id
   *          the uuid
   * @return the possible slots in the id or tree index
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized long[] locate(String id) throws IOException {
    int slot = findSlot(id);
    long startOfSlot = IDX_HEADER_SIZE + (slot * slotSizeInBytes);
    idx.seek(startOfSlot);
    int entries = idx.readInt();

    long[] ids = new long[entries];
    for (int i = 0; i < entries; i++) {
      long address = idx.readLong();
      ids[i++] = address;
    }
    return ids;
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
    this.slotSizeInBytes = 4 + (entriesPerSlot * IDX_ENTRY_SIZE);
    this.entries = 0;

    long totalEntries = slots * entriesPerSlot;

    idx.seek(0);

    // Write header
    idx.writeInt(slots);
    idx.writeInt(entriesPerSlot);
    idx.writeLong(entries);

    // Write entries
    for (int i = 0; i < slots; i++) {
      idx.writeInt(0);
      for (int j = 0; j < entriesPerSlot; j++) {
        idx.writeLong(0);
      }
    }

    logger.info("Id index initialized to {} entries ({} slots, {} entries per slot)", new Object[] {
        totalEntries,
        slots,
        entriesPerSlot });
  }

  /**
   * Resizes the index file to the given number of slots and number of entries
   * per slot.
   * <p>
   * Note that it is only possible to change the entries per slot if there are
   * already entries in the index. If you want to change the number of slots as
   * well, you need to <code>clear</code> the index first.
   * 
   * @param slots
   *          the number of slots
   * @param entriesPerSlot
   *          the number of <code>long</code> entries per slot
   * @throws IOException
   *           writing to the index fails
   * @throws IllegalStateException
   *           if the index is read only or if the user tries to resize the
   *           number of slots while there are already entries in the index
   */
  public synchronized void resize(int slots, int entriesPerSlot)
      throws IOException {
    if (this.slots != slots && this.entries > 0)
      throw new IllegalStateException("Cannot resize the number of slots when there are entries in the index");
    if (this.entriesPerSlot > entriesPerSlot && this.entries > 0)
      throw new IllegalStateException("Cannot reduce the number of entries per slot when there are entries in the index");
    if (this.isReadOnly)
      throw new IllegalStateException("This index is readonly");

    String idxFilename = idxFile.getName();
    String fileName = FilenameUtils.getBaseName(idxFilename);
    String fileExtension = FilenameUtils.getExtension(idxFilename);
    String idxFilenameNew = fileName + "_resized." + fileExtension;
    File newIdxFile = new File(idxFile.getParentFile(), idxFilenameNew);

    logger.debug("Creating resized index at " + newIdxFile);

    RandomAccessFile idxNew = null;
    try {
      idxNew = new RandomAccessFile(newIdxFile, "rwd");
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Index file " + newIdxFile + " cannot be created: " + e.getMessage(), e);
    }

    // Copy the current index to the new one

    idx.seek(IDX_ENTRIES_LOCATION);
    idxNew.seek(0);

    // Write header
    idxNew.writeInt(slots);
    idxNew.writeInt(entriesPerSlot);
    idxNew.writeLong(entries);

    // Write entries
    for (int i = 0; i < slots; i++) {
      if (i < this.slots)
        idxNew.writeInt(idx.readInt());
      else
        idxNew.writeInt(0);
      for (int j = 0; j < entriesPerSlot; j++) {
        if (j < this.entriesPerSlot)
          idxNew.writeLong(idx.readLong());
        else
          idxNew.writeLong(0);
      }
    }

    idxNew.close();

    logger.debug("Removing old index at " + idxFile);
    idxFile.delete();
    logger.debug("Moving resized index into regular position at " + idxFile);
    newIdxFile.renameTo(idxFile);

    try {
      idx = new RandomAccessFile(idxFile, "rwd");
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Index file " + newIdxFile + " cannot be created: " + e.getMessage(), e);
    }

    this.entriesPerSlot = entriesPerSlot;
    this.idxFile = newIdxFile;
    this.slots = slots;
    this.slotSizeInBytes = 4 + (entriesPerSlot * IDX_ENTRY_SIZE);
    long totalEntries = slots * entriesPerSlot;

    logger.info("Id index resized to {} entries ({} slots, {} entries per slot)", new Object[] {
        totalEntries,
        slots,
        entriesPerSlot });
  }

}
