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

package ch.entwine.weblounge.contentrepository.impl.index;

import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.contentrepository.VersionedContentRepositoryIndex;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This index is part of the content repository index and maps page identifiers
 * to paths. In order to quickly get to an entry, use the <code>id</code> or
 * <code>path</code> index.
 * 
 * Header:
 * <pre>
 * | index version | bytes per id | bytes per type | bytes per path | # of slots | # of entries |  
 * | (int)         | (int)        | (int)          | (int)          | (long)     | (long)       |  
 * </pre>
 * 
 * Entries:
 * <pre>
 * | id      | path
 * |------------------------------------------
 * | a-b-c-d | /etc/weblounge
 * </pre>
 * 
 * <p>
 * Note that the current implementation is <b>not thread-safe</b> due to the
 * use of a single instance of the {@link RandomAccessFile}.
 */
public class URIIndex implements VersionedContentRepositoryIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(URIIndex.class);

  /** Name for the uri index file */
  public static final String URI_IDX_NAME = "uri.idx";
  
  /** Start of the index's header */
  protected static final long IDX_START_OF_HEADER = 0;

  /** Location of the bytes-per-id header */
  protected static final long IDX_HEADER_VERSION = IDX_START_OF_HEADER;

  /** Location of the bytes-per-id header */
  protected static final long IDX_HEADER_BYTES_PER_ID = IDX_HEADER_VERSION + 4;

  /** Location of the bytes-per-id header */
  protected static final long IDX_HEADER_BYTES_PER_TYPE = IDX_HEADER_BYTES_PER_ID + 4;

  /** Location of the bytes-per-path header */
  protected static final long IDX_HEADER_BYTES_PER_PATH = IDX_HEADER_BYTES_PER_TYPE + 4;

  /** Location of the entries header */
  protected static final long IDX_HEADER_SLOTS = IDX_HEADER_BYTES_PER_PATH + 4;

  /** Location of the entries header */
  protected static final long IDX_HEADER_ENTRIES = IDX_HEADER_SLOTS + 8;

  /** Start of the index's body */
  protected static final long IDX_START_OF_CONTENT = IDX_HEADER_ENTRIES + 8;

  /** Default number of bytes used per id */
  private static final int DEFAULT_BYTES_PER_ID = 36;

  /** Default number of bytes per type */
  private static final int DEFAULT_BYTES_PER_TYPE = 8;

  /** Default number of bytes per path */
  private static final int DEFAULT_BYTES_PER_PATH = 128;

  /** The index file */
  protected RandomAccessFile idx = null;

  /** The index file */
  protected File idxFile = null;

  /** True if this is a readonly index */
  protected boolean isReadOnly = false;

  /** The version number */
  protected int indexVersion = -1;

  /** Number of bytes per id */
  protected int bytesPerId = DEFAULT_BYTES_PER_ID;

  /** Number of bytes per type */
  protected int bytesPerType = DEFAULT_BYTES_PER_TYPE;

  /** Number of bytes per path */
  protected int bytesPerPath = DEFAULT_BYTES_PER_PATH;

  /** Number of bytes per entry */
  protected int bytesPerSlot = bytesPerId + bytesPerType + bytesPerPath;

  /** Number of entries */
  protected long entries = 0;

  /** Number of slots (entries + empty space) */
  protected long slots = 0;

  /**
   * Creates an index inside the given directory. If the index does not exist,
   * it is created and initialized with the default index settings, which means
   * that uri identifiers are expected to be made out of 36 bytes (uuid) while
   * paths are allowed up to 128 bytes.
   * <p>
   * Note that the path length will automatically be increased as soon as longer
   * paths are added, while the size of identifiers is fixed.
   * 
   * @param indexRootDir
   *          location of the index root directory
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @throws IOException
   *           if reading from the index fails
   */
  public URIIndex(File indexRootDir, boolean readOnly) throws IOException {
    this(indexRootDir, readOnly, DEFAULT_BYTES_PER_ID, DEFAULT_BYTES_PER_TYPE, DEFAULT_BYTES_PER_PATH);
  }

  /**
   * Creates an index inside the given directory. If the index does not exist,
   * it is created and initialized with the default index settings, which means
   * that uri identifiers are expected to be made out of 36 bytes (uuid).
   * <p>
   * Note that the path length will automatically be increased as soon as longer
   * paths are added, while the size of identifiers is fixed.
   * 
   * @param indexRootDir
   *          location of the index root directory
   * @param pathLengthInBytes
   *          the number of bytes per path
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @throws IOException
   *           if reading from the index fails
   */
  public URIIndex(File indexRootDir, boolean readOnly, int pathLengthInBytes)
      throws IOException {
    this(indexRootDir, readOnly, DEFAULT_BYTES_PER_ID, DEFAULT_BYTES_PER_TYPE, pathLengthInBytes);
  }

  /**
   * Creates an index inside the given directory. If the index does not exist,
   * it is created and initialized with the default index settings.
   * <p>
   * The number of bytes per entry defines the size of the index.
   * 
   * @param indexRootDir
   *          location of the index root directory
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @param idLengthInBytes
   *          the number of bytes per id
   * @param typeLengthInBytes
   *          the number of bytes per type
   * @param pathLengthInBytes
   *          the number of bytes per path
   * @throws IOException
   *           if reading from the index fails
   */
  public URIIndex(File indexRootDir, boolean readOnly, int idLengthInBytes,
      int typeLengthInBytes, int pathLengthInBytes) throws IOException {

    this.idxFile = new File(indexRootDir, URI_IDX_NAME);
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
        logger.warn("URI index version mismatch (found {}, expected {}), consider reindex", indexVersion, INDEX_VERSION);
      this.bytesPerId = idx.readInt();
      this.bytesPerType = idx.readInt();
      this.bytesPerPath = idx.readInt();
      this.slots = idx.readLong();
      this.entries = idx.readLong();
      this.bytesPerSlot = bytesPerId + bytesPerType + bytesPerPath;
      
      // If the index contains entries, we can't reduce the index size 
      if (this.entries > 0) {
        idLengthInBytes = Math.max(this.bytesPerId, idLengthInBytes);
        typeLengthInBytes = Math.max(this.bytesPerType, typeLengthInBytes);
        pathLengthInBytes = Math.max(this.bytesPerPath, pathLengthInBytes);
      }

      if (this.bytesPerId != idLengthInBytes || this.bytesPerType != typeLengthInBytes || this.bytesPerPath != pathLengthInBytes)
        bytesPerSlot = resize(idLengthInBytes, typeLengthInBytes, pathLengthInBytes);
    } catch (EOFException e) {
      if (readOnly) {
        throw new IllegalStateException("Readonly index cannot be empty");
      }
      init(idLengthInBytes, typeLengthInBytes, pathLengthInBytes);
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
   * @see ch.entwine.weblounge.contentrepository.VersionedContentRepositoryIndex#getIndexVersion()
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
   * Returns the number of bytes per entry;
   * 
   * @return the number of bytes per entry
   */
  public synchronized int getEntrySize() {
    return bytesPerSlot;
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
    return (float)entries / (float)slots;
  }

  /**
   * Adds id and path to the index and returns the index address.
   * 
   * @param id
   *          the identifier
   * @param type
   *          the resource type
   * @param path
   *          the page path
   * @return the entry's address in this index
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized long add(String id, String type, String path)
      throws IOException {
    if (id == null)
      throw new IllegalArgumentException("Id cannot be null");
    if (type == null)
      throw new IllegalArgumentException("Type cannot be null");
    if (path == null)
      throw new IllegalArgumentException("Path cannot be null");

    // Make sure this is a regular id
    if (id.getBytes().length != bytesPerId)
      throw new IllegalArgumentException(bytesPerId + " byte identifier required");

    long entry = entries;

    // Get the required entry size
    int typeLengthInBytes = type.getBytes().length;
    int pathLengthInBytes = path.getBytes().length;

    // Make sure there is still room left for an additional entry. One entry
    // consists of the uuid, the path and a closing '\n'
    if (typeLengthInBytes >= bytesPerType || pathLengthInBytes >= bytesPerPath) {

      // Make sure the type field is long enough
      int newBytesPerType = bytesPerType;
      if (typeLengthInBytes >= bytesPerType) {
        logger.info("Type doesn't fit, triggering index resize");
        while (newBytesPerType < typeLengthInBytes)
          newBytesPerType *= 2;
      }

      // Make sure the path field is long enough
      int newBytesPerPath = bytesPerPath;
      if (pathLengthInBytes >= bytesPerPath) {
        logger.info("Path doesn't fit, triggering index resize");
        while (newBytesPerPath < pathLengthInBytes)
          newBytesPerPath *= 2;
      }

      bytesPerSlot = resize(bytesPerId, newBytesPerType, newBytesPerPath);
    }

    // See if there is an empty slot
    long startOfEntry = IDX_START_OF_CONTENT + (entries * bytesPerSlot);
    long address = IDX_START_OF_CONTENT;
    long e = 0;
    boolean reusingSlot = false;
    idx.seek(address);
    while (address < startOfEntry) {
      if (idx.readChar() == '\n') {
        logger.debug("Found orphan line for reuse");
        startOfEntry = address;
        reusingSlot = true;
        entry = e;
        break;
      }
      idx.skipBytes(bytesPerSlot - 2);
      address += bytesPerSlot;
      e++;
    }

    // Add the new address at the end
    idx.seek(startOfEntry);
    idx.write(id.getBytes());
    idx.write(type.getBytes());
    idx.write(new byte[bytesPerType - type.getBytes().length]);
    idx.write(path.getBytes());
    idx.writeChar('\n');
    idx.write(new byte[bytesPerPath - pathLengthInBytes - 2]);

    if (!reusingSlot)
      slots++;
    entries++;

    // Update the file header
    idx.seek(IDX_HEADER_SLOTS);
    idx.writeLong(slots);
    idx.writeLong(entries);

    logger.debug("Added uri with id '{}', type '{}' and path '{}' as entry no {}", new Object[] {
        id,
        type,
        path,
        entries });
    return entry;
  }

  /**
   * Removes all entries for the given page uri from the index.
   * 
   * @param entry
   *          start address of uri entry
   * @throws IOException
   *           if removing the entry from the index fails
   */
  public synchronized void delete(long entry) throws IOException {
    long startOfEntry = IDX_START_OF_CONTENT + (entry * bytesPerSlot);

    // Remove the entry by writing a '\n' to the first byte
    idx.seek(startOfEntry);
    idx.writeChar('\n');
    idx.write(new byte[bytesPerSlot - 2]);

    // Update the file header
    entries--;
    idx.seek(IDX_HEADER_ENTRIES);
    idx.writeLong(entries);

    logger.debug("Removed uri at address '{}' from index", entry);
  }

  /**
   * Updates the path on the uri located at <code>entry</code>.
   * 
   * @param entry
   *          start address of uri
   * @param type
   *          the new type
   * @param path
   *          the new path
   * @throws IOException
   *           if updating the path in the index fails
   */
  public synchronized void update(long entry, String type, String path)
      throws IOException {
    if (type == null)
      throw new IllegalArgumentException("Type cannot be null");
    if (path == null)
      throw new IllegalArgumentException("Path cannot be null");
    
    // Check if the new path fits the current index
    int typeLengthInBytes = type.getBytes().length;
    int pathLengthInBytes = path.getBytes().length;
    
    // Make sure there is still room left for an additional entry. One entry
    // consists of the uuid, the path and a closing '\n'
    if (typeLengthInBytes >= bytesPerType || pathLengthInBytes >= bytesPerPath) {

      // Make sure the type field is long enough
      int newBytesPerType = bytesPerType;
      if (typeLengthInBytes >= bytesPerType) {
        logger.info("Type doesn't fit, triggering index resize");
        while (newBytesPerType < typeLengthInBytes)
          newBytesPerType *= 2;
      }

      // Make sure the path field is long enough
      int newBytesPerPath = bytesPerPath;
      if (pathLengthInBytes >= bytesPerPath) {
        logger.info("Path doesn't fit, triggering index resize");
        while (newBytesPerPath < pathLengthInBytes)
          newBytesPerPath *= 2;
      }

      bytesPerSlot = resize(bytesPerId, newBytesPerType, newBytesPerPath);
    }

    long startOfEntry = IDX_START_OF_CONTENT + (entry * bytesPerSlot);

    // Write the path to the index
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);
    idx.write(type.getBytes());
    idx.write(new byte[bytesPerType - type.getBytes().length]);
    idx.write(path.getBytes());
    idx.writeChar('\n');
    idx.write(new byte[bytesPerPath - pathLengthInBytes - 2]);

    logger.debug("Updated uri at address '{}' to {}", entry, path);
  }

  /**
   * Removes all entries from the index.
   * 
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized void clear() throws IOException {
    init(bytesPerId, bytesPerType, bytesPerPath);
  }

  /**
   * Returns the uri's id.
   * 
   * @param entry
   *          the entry at which the id is expected
   * @return the id
   * @throws IllegalStateException
   *           if the user tries to access an address with no data
   * @throws EOFException
   *           if the user tries to access a non-existing address
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized String getId(long entry) throws IOException, EOFException {
    long startOfEntry = IDX_START_OF_CONTENT + (entry * bytesPerSlot);
    idx.seek(startOfEntry);
    byte[] bytes = new byte[bytesPerId];
    int bytesRead = idx.read(bytes);
    if (bytesRead < bytesPerId || bytes[1] == '\n')
      throw new IllegalStateException("No data at address " + entry);
    return new String(bytes, "utf-8");
  }

  /**
   * Returns the uri's type.
   * 
   * @param entry
   *          the entry at which the id is expected
   * @return the type
   * @throws EOFException
   *           if the user tries to access a non-existing address
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized String getType(long entry) throws IOException,
      EOFException {
    long startOfEntry = IDX_START_OF_CONTENT + (entry * bytesPerSlot);
    idx.seek(startOfEntry);
    if (idx.readChar() == '\n')
      throw new IllegalStateException("No data at address " + entry);
    idx.skipBytes(bytesPerId - 2);

    byte[] bytes = new byte[bytesPerType];
    idx.read(bytes);
    for (int i = 0; i < bytes.length; i++) {
      if (bytes[i] == 0)
        return new String(bytes, 0, i);
    }

    throw new IllegalStateException("Found type without delimiter");
  }

  /**
   * Returns the uri's path.
   * 
   * @param entry
   *          the entry at which the id is expected
   * @return the path
   * @throws EOFException
   *           if the user tries to access a non-existing address
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized String getPath(long entry) throws IOException,
      EOFException {
    long startOfEntry = IDX_START_OF_CONTENT + (entry * bytesPerSlot);
    idx.seek(startOfEntry);
    if (idx.readChar() == '\n')
      throw new IllegalStateException("No data at address " + entry);
    idx.skipBytes(bytesPerId + bytesPerType - 2);

    byte[] bytes = new byte[bytesPerPath];
    idx.read(bytes);
    String line = new String(bytes, "utf-8");
    int delimiter = line.indexOf('\n');
    if (delimiter < 1)
      throw new IllegalStateException("Found path without delimiter");
    return new String(bytes, 0, delimiter - 1);
  }

  /**
   * Initializes the index file to the given size using <code>0</code> byte
   * values and writes the header information to it.
   * 
   * @param bytesPerId
   *          the number of bytes per identifier
   * @param bytesPerType
   *          the number of bytes per type
   * @param bytesPerPath
   *          the number of bytes per path
   * @throws IOException
   *           writing to the index fails
   */
  private void init(int bytesPerId, int bytesPerType, int bytesPerPath) throws IOException {
    this.indexVersion = INDEX_VERSION;
    this.bytesPerId = bytesPerId;
    this.bytesPerType = bytesPerType;
    this.bytesPerPath = bytesPerPath;
    this.bytesPerSlot = bytesPerId + bytesPerType + bytesPerPath;
    this.entries = 0;

    logger.info("Creating uri index with {} bytes per entry", bytesPerSlot);

    // Write header
    idx.seek(IDX_START_OF_HEADER);
    idx.writeInt(indexVersion);
    idx.writeInt(bytesPerId);
    idx.writeInt(bytesPerType);
    idx.writeInt(bytesPerPath);
    idx.writeLong(slots);
    idx.writeLong(entries);

    // If this file used to contain entries, we just null out the rest
    try {
      byte[] bytes = new byte[bytesPerSlot - 2];
      while (idx.getFilePointer() < idx.length()) {
        idx.writeChar('\n');
        idx.write(bytes);
      }
    } catch (EOFException e) {
      // That's ok, we wanted to write until the very end
    }

    logger.debug("Uri index created");
  }

  /**
   * Resizes the index file to the given number of bytes per entry.
   * 
   * @param newBytesPerId
   *          the number of bytes per id
   * @param newBytesPerType
   *          the number of bytes per type
   * @param newBytesPerPath
   *          the number of bytes per entry
   * @throws IOException
   *           writing to the index fails
   * @throws IllegalStateException
   *           if the index is read only or if the user tries to resize the
   *           number of slots while there are already entries in the index
   */
  public synchronized int resize(int newBytesPerId, int newBytesPerType, int newBytesPerPath)
      throws IOException {
    if (this.bytesPerId > newBytesPerId && this.entries > 0)
      throw new IllegalStateException("Cannot reduce the number of bytes per id when there are entries in the index");
    if (this.bytesPerType > newBytesPerType && this.entries > 0)
      throw new IllegalStateException("Cannot reduce the number of bytes per type when there are entries in the index");
    if (this.bytesPerPath > newBytesPerPath && this.entries > 0)
      throw new IllegalStateException("Cannot reduce the number of bytes per path when there are entries in the index");
    if (this.isReadOnly)
      throw new IllegalStateException("This index is readonly");

    int newBytesPerSlot = newBytesPerId + newBytesPerType + newBytesPerPath;

    logger.info("Resizing uri index to {} ({}) slots and {} ({}) bytes per entry", new Object[] { slots, this.slots, newBytesPerSlot, this.bytesPerSlot });

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
    idxNew.writeInt(newBytesPerId);
    idxNew.writeInt(newBytesPerType);
    idxNew.writeInt(newBytesPerPath);
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

    this.bytesPerSlot = newBytesPerSlot;
    this.bytesPerId = newBytesPerId;
    this.bytesPerType = newBytesPerType;
    this.bytesPerPath = newBytesPerPath;

    time = System.currentTimeMillis() - time;
    logger.info("Uri index resized in {}", ConfigurationUtils.toHumanReadableDuration(time));
    return newBytesPerSlot;
  }

}
