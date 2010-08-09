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
 * This index is part of the content repository index and maps page identifiers
 * to paths. In order to quickly get to an entry, use the <code>id</code> or
 * <code>path</code> index.
 * 
 * <pre>
 * | slot | id      | path
 * |------------------------------------------
 * | 1    | a-b-c-d | /etc/weblounge
 * </pre>
 */
public class URIIndex implements VersionedContentRepositoryIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(URIIndex.class);

  /** Name for the uri index file */
  public static final String URI_IDX_NAME = "uri.idx";

  /** Default number of bytes used per id */
  private static final int IDX_BYTES_PER_ID = 36;

  /** Default number of bytes per type */
  private static final int IDX_BYTES_PER_TYPE = 8;

  /** Default number of bytes per path */
  private static final int IDX_BYTES_PER_PATH = 128;

  /** Location of the bytes-per-id header */
  protected static final long IDX_VERSION_HEADER_LOCATION = 0;

  /** Location of the bytes-per-id header */
  protected static final long IDX_BYTES_PER_ID_HEADER_LOCATION = 4;

  /** Location of the bytes-per-id header */
  protected static final long IDX_BYTES_PER_TYPE_HEADER_LOCATION = 8;

  /** Location of the bytes-per-path header */
  protected static final long IDX_BYTES_PER_PATH_HEADER_LOCATION = 12;

  /** Location of the entries header */
  protected static final long IDX_SLOTS_HEADER_LOCATION = 16;

  /** Location of the entries header */
  protected static final long IDX_ENTRIES_HEADER_LOCATION = 24;

  /** Number of bytes that are used for the index header */
  protected static final int IDX_HEADER_SIZE = 32;

  /** The index file */
  protected RandomAccessFile idx = null;

  /** The index file */
  protected File idxFile = null;

  /** True if this is a readonly index */
  protected boolean isReadOnly = false;

  /** The version number */
  protected int indexVersion = -1;

  /** Number of bytes per id */
  protected int bytesPerId = IDX_BYTES_PER_ID;

  /** Number of bytes per type */
  protected int bytesPerType = IDX_BYTES_PER_TYPE;

  /** Number of bytes per path */
  protected int bytesPerPath = IDX_BYTES_PER_PATH;

  /** Number of bytes per entry */
  protected int bytesPerEntry = bytesPerId + bytesPerType + bytesPerPath;

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
    this(indexRootDir, readOnly, IDX_BYTES_PER_ID, IDX_BYTES_PER_TYPE, IDX_BYTES_PER_PATH);
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
    this(indexRootDir, readOnly, IDX_BYTES_PER_ID, IDX_BYTES_PER_TYPE, pathLengthInBytes);
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
      this.indexVersion = idx.readInt();
      if (indexVersion != IDX_VERSION)
        logger.warn("URI index version mismatch (found {}, expected {}), consider reindex", indexVersion, IDX_VERSION);
      this.bytesPerId = idx.readInt();
      this.bytesPerType = idx.readInt();
      this.bytesPerPath = idx.readInt();
      this.entries = idx.readLong();
      this.bytesPerEntry = bytesPerId + bytesPerType + bytesPerPath;
      if (this.bytesPerId != idLengthInBytes || this.bytesPerType != typeLengthInBytes || this.bytesPerPath != pathLengthInBytes)
        bytesPerEntry = resize(idLengthInBytes, typeLengthInBytes, pathLengthInBytes);
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
  public long size() {
    return IDX_HEADER_SIZE + (slots * bytesPerEntry);
  }

  /**
   * Returns the number of bytes per entry;
   * 
   * @return the number of bytes per entry
   */
  public int getEntrySize() {
    return bytesPerEntry;
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
    if (type == null)
      throw new IllegalArgumentException("Type cannot be null");
    if (path == null)
      throw new IllegalArgumentException("Path cannot be null");

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

      bytesPerEntry = resize(bytesPerId, newBytesPerType, newBytesPerPath);
    }

    // See if there is an empty slot
    long startOfEntry = IDX_HEADER_SIZE + (entries * bytesPerEntry);
    long address = IDX_HEADER_SIZE;
    long e = 0;
    boolean reusingSlot = false;
    idx.seek(address);
    while (address < startOfEntry) {
      if (idx.read() == '\n') {
        logger.debug("Found orphan line for reuse");
        startOfEntry = address;
        reusingSlot = true;
        entry = e;
        break;
      }
      idx.skipBytes(bytesPerEntry - 1);
      address += bytesPerEntry;
      e++;
    }

    // Add the new address at the end
    idx.seek(startOfEntry);
    idx.write(id.getBytes());
    idx.write(type.getBytes());
    for (int i = 0; i < bytesPerType - type.length(); i++)
      idx.writeByte(0);
    idx.write(path.getBytes());
    idx.write('\n');
    long remainingBytes = bytesPerEntry - IDX_BYTES_PER_ID - pathLengthInBytes - 1;
    for (int i = 0; i < remainingBytes; i++) {
      idx.writeByte(0);
    }

    if (!reusingSlot)
      slots++;
    entries++;

    // Update the file header
    idx.seek(IDX_SLOTS_HEADER_LOCATION);
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
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);

    // Remove the entry by writing a '\n' to the first byte
    idx.seek(startOfEntry);
    idx.write('\n');
    idx.write(new byte[bytesPerEntry - 1]);

    // Update the file header
    entries--;
    idx.seek(IDX_ENTRIES_HEADER_LOCATION);
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

      bytesPerEntry = resize(bytesPerId, newBytesPerType, newBytesPerPath);
    }

    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);

    // Write the path to the index
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);
    for (int i = 0; i < bytesPerType - type.length(); i++)
      idx.writeByte(0);
    idx.write(type.getBytes());
    idx.write(path.getBytes());
    idx.write('\n');
    for (int i = 1; i < bytesPerEntry; i++) {
      idx.writeLong(0);
    }

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
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
    idx.seek(startOfEntry);
    byte[] bytes = new byte[bytesPerId];
    int bytesRead = idx.read(bytes);
    if (bytesRead < bytesPerId || bytes[0] == '\n')
      throw new IllegalStateException("No data at address " + entry);
    return new String(bytes);
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
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);

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
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId + bytesPerType);

    byte[] bytes = new byte[bytesPerPath];
    idx.read(bytes);
    for (int i = 0; i < bytes.length; i++) {
      if (bytes[i] == '\n')
        return new String(bytes, 0, i);
    }

    throw new IllegalStateException("Found path without delimiter");
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
    this.indexVersion = IDX_VERSION;
    this.bytesPerId = bytesPerId;
    this.bytesPerType = bytesPerType;
    this.bytesPerPath = bytesPerPath;
    this.bytesPerEntry = bytesPerId + bytesPerType + bytesPerPath;

    logger.info("Creating uri index with {} bytes per entry", bytesPerEntry);

    // Write header
    idx.seek(0);
    idx.writeInt(indexVersion);
    idx.writeInt(bytesPerId);
    idx.writeInt(bytesPerType);
    idx.writeInt(bytesPerPath);
    idx.writeLong(0);
    idx.writeLong(0);

    // If this file used to contain entries, we just null out the rest
    try {
      byte[] bytes = new byte[bytesPerEntry - 1];
      while (idx.getFilePointer() < idx.length()) {
        idx.write('\n');
        idx.write(bytes);
      }
    } catch (EOFException e) {
      // That's ok, we wanted to write until the very end
    }

    this.entries = 0;

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

    int newBytesPerEntry = newBytesPerId + newBytesPerType + newBytesPerPath;

    logger.info("Resizing uri index with {} entries to {} bytes per entry", entries, newBytesPerEntry);

    String idxFilename = idxFile.getName();
    String fileName = FilenameUtils.getBaseName(idxFilename);
    String fileExtension = FilenameUtils.getExtension(idxFilename);
    String idxFilenameNew = fileName + "_resized." + fileExtension;
    File newIdxFile = new File(idxFile.getParentFile(), idxFilenameNew);
    long time = System.currentTimeMillis();

    logger.debug("Creating resized index at " + newIdxFile);

    RandomAccessFile idxNew = null;
    try {
      idxNew = new RandomAccessFile(newIdxFile, "rwd");
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Index file " + newIdxFile + " cannot be created: " + e.getMessage(), e);
    }

    // Write header
    idxNew.seek(0);
    idxNew.writeInt(indexVersion);
    idxNew.writeInt(newBytesPerId);
    idxNew.writeInt(newBytesPerType);
    idxNew.writeInt(newBytesPerPath);
    idxNew.writeLong(slots);
    idxNew.writeLong(entries);

    // Copy the current index to the new one
    idx.seek(IDX_HEADER_SIZE);
    for (int i = 0; i < this.entries; i++) {
      byte[] bytes = new byte[this.bytesPerEntry];
      idx.read(bytes);
      idxNew.write(bytes);
      for (int j = this.bytesPerEntry; j < newBytesPerEntry; j++)
        idxNew.write(0);
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

    this.bytesPerEntry = newBytesPerEntry;
    this.bytesPerId = newBytesPerId;
    this.bytesPerType = newBytesPerType;
    this.bytesPerPath = newBytesPerPath;
    this.idxFile = newIdxFile;

    time = System.currentTimeMillis() - time;
    logger.info("Uri index resized in {}", ConfigurationUtils.toHumanReadableDuration(time));
    return newBytesPerEntry;
  }

}
