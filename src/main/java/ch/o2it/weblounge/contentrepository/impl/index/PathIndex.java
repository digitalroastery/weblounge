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

import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.contentrepository.VersionedContentRepositoryIndex;

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
 * representations of paths to slot numbers in the <code>URI</code> or the
 * <code>Tree</code> index.
 * 
 * Header:
 * <pre>
 * | index version | # of entries per slot | # of slots | # of entries |  
 * | (int)         | (int)                 | (long)     | (long)       |  
 * </pre>
 * 
 * Entries:
 * <pre>
 * | entries | slot number in uri index
 * |-------------------------------------------------
 * | 2       | addressOf(/var), addressOf(/usr/share)
 * | 1       | addressOf(/etc)
 * </pre>
 * 
 * The index is made up of a header which consists of the number of slots (4
 * bytes) followed by the number of addresses per slot (4 bytes), the number of
 * entries currently in the index and then the slots containing the indicated
 * number of 64-bit addresses (8 bytes).
 */
public class PathIndex implements VersionedContentRepositoryIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(PathIndex.class);

  /** Name for the path index file */
  public static final String PATH_IDX_NAME = "path.idx";


  /** Start of the index's header */
  protected static final long IDX_START_OF_HEADER = 0;

  /** Location of the versions header */
  protected static final long IDX_HEADER_VERSION = IDX_START_OF_HEADER;

  /** Location of the entries-per-slot header */
  protected static final long IDX_HEADER_ENTRIES_PER_SLOTS = IDX_HEADER_VERSION + 4;

  /** Location of the slots header */
  protected static final long IDX_HEADER_SLOTS = IDX_HEADER_ENTRIES_PER_SLOTS + 4;

  /** Location of the entries header */
  protected static final long IDX_HEADER_ENTRIES = IDX_HEADER_SLOTS + 8;

  /** Start of the index's body */
  protected static final long IDX_START_OF_CONTENT = IDX_HEADER_ENTRIES + 8;

  
  /** Default number of entries in index */
  private static final int DEFAULT_SLOTS = 128;

  /** Default number of entries per slot in the index */
  private static final int DEFAULT_ENTRIES_PER_SLOT = 64;

  /** Size of an entry (long) in bytes */
  protected static final int DEFAULT_ENTRY_SIZE = 8;

  /** The index file */
  protected RandomAccessFile idx = null;

  /** The index file */
  protected File idxFile = null;

  /** The version number */
  protected int indexVersion = -1;

  /** True if this is a readonly index */
  protected boolean isReadOnly = false;

  /** Number of paths per slot */
  protected int entriesPerSlot = DEFAULT_ENTRIES_PER_SLOT;

  /** Number of paths per slot */
  protected int bytesPerSlot = 4 + (entriesPerSlot * DEFAULT_ENTRY_SIZE);

  /** Number of slots in the paths index */
  protected long slots = DEFAULT_SLOTS;

  /** Number of entries */
  protected long entries = 0;

  /**
   * Creates an index inside the given directory. If the index does not exist,
   * it is created and initialized with the default index settings.
   * 
   * @param indexRootDir
   *          location of the index root directory
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @throws IOException
   *           if reading from the index fails
   */
  public PathIndex(File indexRootDir, boolean readOnly) throws IOException {
    this(indexRootDir, readOnly, DEFAULT_SLOTS, DEFAULT_ENTRIES_PER_SLOT);
  }

  /**
   * Creates an index inside the given file. If the index does not exist, it is
   * created and initialized with the default index settings.
   * <p>
   * The number of slots and entries per slot defines the size of the index. At
   * creation time, the index implementation will allocate a file on disk which
   * is <code>slots * entriesPerSlot * DEFAULT_ENTRY_SIZE</code> bytes large,
   * plus 16 bytes of header information.
   * 
   * @param indexRootDir
   *          location of the index root directory
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @param slots
   *          the number of slots
   * @param entriesPerSlot
   *          the number of entries per slot
   * @throws IOException
   *           if reading from the index fails
   */
  public PathIndex(File indexRootDir, boolean readOnly, long slots,
      int entriesPerSlot) throws IOException {

    this.idxFile = new File(indexRootDir, PATH_IDX_NAME);
    this.isReadOnly = readOnly;

    String mode = readOnly ? "r" : "rwd";
    try {
      idxFile.getParentFile().mkdirs();
      if (!idxFile.exists())
        idxFile.createNewFile();
      idx = new RandomAccessFile(idxFile, mode);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Index file " + idxFile + " does not exist");
    }

    // Read index header information
    try {
      idx.seek(IDX_START_OF_HEADER);
      this.indexVersion = idx.readInt();
      if (indexVersion != INDEX_VERSION)
        logger.warn("Path index version mismatch (found {}, expected {}), consider reindex", indexVersion, INDEX_VERSION);
      this.entriesPerSlot = idx.readInt();
      this.slots = idx.readLong();
      this.entries = idx.readLong();
      this.bytesPerSlot = 4 + (this.entriesPerSlot * DEFAULT_ENTRY_SIZE);
      
      // If the index contains entries, we can't reduce the index size 
      if (this.entries > 0) {
        slots = Math.max(this.slots, slots);
        entriesPerSlot = Math.max(this.entriesPerSlot, entriesPerSlot);
      }

      if (this.slots != slots || this.entriesPerSlot != entriesPerSlot)
        resize(slots, entriesPerSlot);
    } catch (EOFException e) {
      if (readOnly) {
        throw new IllegalStateException("Readonly index cannot be empty");
      }
      init(slots, entriesPerSlot);
    } catch (IOException e) {
      logger.error("Error reading from path index: " + e.getMessage());
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
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.VersionedContentRepositoryIndex#getIndexVersion()
   */
  public int getIndexVersion() {
    return indexVersion;
  }

  /**
   * Returns the index size in bytes. The size is calculated from the size of
   * the header plus the number of slots multiplied by the size of one slot.
   * 
   * @return the index size
   */
  public synchronized long size() {
    return IDX_START_OF_CONTENT + (slots * bytesPerSlot);
  }

  /**
   * Returns the number of slots.
   * 
   * @return the number of slots
   */
  public synchronized long getSlots() {
    return slots;
  }

  /**
   * Returns the number of entries per slot;
   * 
   * @return the number of entries per slot
   */
  public synchronized int getEntriesPerSlot() {
    return entriesPerSlot;
  }

  /**
   * Returns the number of entries.
   * 
   * @return the number of entries
   */
  public synchronized long getEntries() {
    return entries;
  }

  /**
   * Returns the load factor for this index, which is determined by the number
   * of entries divided by the number of possible entries.
   * 
   * @return the load factor
   */
  public synchronized float getLoadFactor() {
    return entries / (slots * entriesPerSlot);
  }

  /**
   * Adds the path to the index.
   * @param addressOfPath
   *          slot number in main index
   * @param path
   *          the page path
   * 
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized void set(long addressOfPath, String path)
      throws IOException {

    long slot = findSlot(path);
    long startOfSlot = IDX_START_OF_CONTENT + (slot * bytesPerSlot);

    // Make sure there is still room left
    // for an additional address
    idx.seek(startOfSlot);
    int entriesInSlot = idx.readInt();
    if (entriesInSlot == entriesPerSlot) {
      logger.info("Maximum slot size reached, triggering index resize");
      resize(slots, entriesPerSlot * 2);
      slot = findSlot(path);
      startOfSlot = IDX_START_OF_CONTENT + (slot * bytesPerSlot);
      idx.seek(startOfSlot);
      entriesInSlot = idx.readInt();
    }

    // Add the new address at the end
    idx.skipBytes(entriesInSlot * DEFAULT_ENTRY_SIZE);
    idx.writeLong(addressOfPath);

    entries++;
    entriesInSlot++;

    // Update the number of entries in this slot
    idx.seek(startOfSlot);
    idx.writeInt(entriesInSlot);

    // Update the number of entries
    idx.seek(IDX_HEADER_ENTRIES);
    idx.writeLong(entries);

    logger.debug("Added address of path {} as entry no {} to slot {}", new Object[] {
        path,
        entriesInSlot,
        slot });
  }

  /**
   * Removes all entries for the given page uri from the index.
   * 
   * @param path
   *          the path
   * @param addressOfPath
   *          slot number in main index
   * @throws IOException
   *           if removing the entry from the index fails
   * @throws IllegalStateException
   *           if the path is not part of the index
   */
  public synchronized void delete(String path, long addressOfPath)
      throws IOException {

    // Move to the beginning of the slot
    long slot = findSlot(path);
    long startOfSlot = IDX_START_OF_CONTENT + (slot * bytesPerSlot);

    // Read all entries from the current slot
    idx.seek(startOfSlot);
    int entriesInSlot = idx.readInt();
    int deleteEntry = -1;
    long[] slotEntries = new long[entriesInSlot];
    for (int i = 0; i < entriesInSlot; i++) {
      long entry = idx.readLong();
      slotEntries[i] = entry;
      if (entry == addressOfPath)
        deleteEntry = i;
    }

    // See if we are trying to delete a ghost entry
    if (deleteEntry == -1)
      throw new IllegalStateException("Path '" + path + "' is not part of the index");

    // Write everything back but the entry that needs to be deleted
    entriesInSlot--;
    idx.seek(startOfSlot);
    idx.writeInt(entriesInSlot);
    idx.skipBytes(deleteEntry * DEFAULT_ENTRY_SIZE);
    for (int i = deleteEntry + 1; i < slotEntries.length; i++) {
      idx.writeLong(slotEntries[i]);
    }

    // Update the file header
    entries--;
    idx.seek(IDX_HEADER_ENTRIES);
    idx.writeLong(entries);

    logger.debug("Removed path '{}' from index", path);
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
   * Returns the slot number of the path's entry in the <code>paths</code>
   * index.
   * 
   * @param path
   *          the path
   * @return the slot number
   */
  private long findSlot(String path) {
    return Math.abs(path.hashCode()) % slots;
  }

  /**
   * Returns the path's possible slot numbers in the <code>URI</code> or
   * <code>Tree</code> index.
   * <p>
   * The reason for this method returning a number of addresses rather than one
   * is that a hashing algorithm is used to map paths to slot numbers in order
   * to keep the index small. Therefore, if you want to get to the final entry
   * in the tree or children index, you need to test all of the addresses
   * returned by this method.
   * <p>
   * 
   * @param path
   *          the path
   * @return the possible slots in the uri or tree index
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized long[] locate(String path) throws IOException {
    long slot = findSlot(path);
    long startOfSlot = IDX_START_OF_CONTENT + (slot * bytesPerSlot);
    idx.seek(startOfSlot);
    int entries = idx.readInt();

    long[] paths = new long[entries];
    for (int i = 0; i < entries; i++) {
      long address = idx.readLong();
      paths[i] = address;
    }
    return paths;
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
  private void init(long slots, int entriesPerSlot) throws IOException {
    this.indexVersion = INDEX_VERSION;
    this.entriesPerSlot = entriesPerSlot;
    this.bytesPerSlot = 4 + (entriesPerSlot * DEFAULT_ENTRY_SIZE);
    this.slots = slots;
    this.entries = 0;

    long totalEntries = slots * entriesPerSlot;

    logger.info("Creating path index with {} entries ({} slots, {} entries per slot)", new Object[] {
        totalEntries,
        slots,
        entriesPerSlot });

    // Write header
    idx.seek(IDX_START_OF_HEADER);
    idx.writeInt(indexVersion);
    idx.writeInt(entriesPerSlot);
    idx.writeLong(slots);
    idx.writeLong(entries);

    // Write entries
    byte[] entry = new byte[bytesPerSlot];
    for (int i = 0; i < slots; i++) {
      idx.write(entry);
    }

    logger.debug("Path index created");
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
  public synchronized void resize(long slots, int entriesPerSlot)
      throws IOException {
    if (this.slots != slots && this.entries > 0)
      throw new IllegalStateException("Cannot resize the number of slots when there are entries in the index");
    if (this.entriesPerSlot > entriesPerSlot && this.entries > 0)
      throw new IllegalStateException("Cannot reduce the number of entries per slot when there are entries in the index");
    if (this.isReadOnly)
      throw new IllegalStateException("This index is readonly");

    // Calculate new slot size
    int newBytesPerSlot = 4 + (entriesPerSlot * DEFAULT_ENTRY_SIZE);
    
    logger.info("Resizing path index to {} ({}) slots and {} ({}) entries per slot", new Object[] { slots, this.slots, entriesPerSlot, this.entriesPerSlot });

    String fileName = FilenameUtils.getBaseName(idxFile.getName());
    String fileExtension = FilenameUtils.getExtension(idxFile.getName());
    String idxFilenameNew = fileName + "_resized." + fileExtension;
    File idxNewFile = new File(idxFile.getParentFile(), idxFilenameNew);
    long time = System.currentTimeMillis();

    logger.debug("Creating resized index at " + idxNewFile);

    // Create the new index
    RandomAccessFile idxNew = null;
    try {
      idxNew = new RandomAccessFile(idxNewFile, "rwd");
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Index file " + idxNewFile + " cannot be created: " + e.getMessage(), e);
    }

    // Write header
    idxNew.seek(IDX_START_OF_HEADER);
    idxNew.writeInt(indexVersion);
    idxNew.writeInt(entriesPerSlot);
    idxNew.writeLong(slots);
    idxNew.writeLong(entries);

    // Position to read the whole content
    idx.seek(IDX_START_OF_CONTENT);
    
    // Write entries
    for (int i = 0; i < slots; i++) {
      byte[] bytes = new byte[newBytesPerSlot];
      if (i < this.slots) {
        idx.read(bytes, 0, this.bytesPerSlot);
        idxNew.write(bytes);
      } else {
        // Write an empty line
        idxNew.write(bytes);
      }
    }

    logger.debug("Removing old index at " + idxFile);

    // Close and delete the old index
    idx.close();
    if (!idxFile.delete())
      throw new IOException("Unable to delete old index file " + idxFile);

    // Close the new index, and move it into the old index' place
    logger.debug("Moving resized index into regular position at " + idxFile);
    idxNew.close();
    if (!idxNewFile.renameTo(idxFile))
      throw new IOException("Unable to move new index file to " + idxFile);

    try {
      idx = new RandomAccessFile(idxFile, "rwd");
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Index file " + idxNewFile + " cannot be created: " + e.getMessage(), e);
    }

    this.entriesPerSlot = entriesPerSlot;
    this.slots = slots;
    this.bytesPerSlot = newBytesPerSlot;
    long totalEntries = slots * entriesPerSlot;

    time = System.currentTimeMillis() - time;
    logger.info("Path index resized to {} entries ({} slots, {} entries per slot in {})", new Object[] {
        totalEntries,
        slots,
        entriesPerSlot,
        ConfigurationUtils.toHumanReadableDuration(time) });
  }

}
