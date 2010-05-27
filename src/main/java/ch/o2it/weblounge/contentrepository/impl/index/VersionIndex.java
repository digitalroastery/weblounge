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

import ch.o2it.weblounge.common.impl.content.PageUtils;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;

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
 * to versions. In order to quickly get to an entry, use the <code>id</code> or
 * <code>path</code> index.
 * 
 * <pre>
 * | slot | id      | versions
 * |------------------------------------------
 * | 1    | a-b-c-d | 1 876876876
 * </pre>
 */
public class VersionIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(VersionIndex.class);

  /** Name for the version index file */
  public static final String VERSION_IDX_NAME = "version.idx";

  /** Default number of bytes used per id */
  private static final int IDX_BYTES_PER_ID = 36;

  /** Default number of versions per index entry */
  private static final int IDX_VERSIONS_PER_ENTRY = 10;

  /** Location of the bytes-per-id header */
  protected static final long IDX_BYTES_PER_ID_HEADER_LOCATION = 0;

  /** Location of the versions-per-entry header */
  protected static final long IDX_VERSIONS_PER_ENTRY_HEADER_LOCATION = 4;

  /** Location of the entries header */
  protected static final long IDX_SLOTS_HEADER_LOCATION = 8;

  /** Location of the entries header */
  protected static final long IDX_ENTRIES_HEADER_LOCATION = 16;

  /** Number of bytes that are used for the index header */
  protected static final int IDX_HEADER_SIZE = 24;

  /** Location of the first entry */
  protected static final long IDX_ENTRIES_LOCATION = IDX_HEADER_SIZE;

  /** The index file */
  protected RandomAccessFile idx = null;

  /** The index file */
  protected File idxFile = null;

  /** True if this is a readonly index */
  protected boolean isReadOnly = false;

  /** Number of bytes per id */
  protected int bytesPerId = IDX_BYTES_PER_ID;

  /** Number of versions per entry */
  protected int versionsPerEntry = IDX_VERSIONS_PER_ENTRY;

  /** Number of bytes per entry */
  protected int bytesPerEntry = bytesPerId + IDX_VERSIONS_PER_ENTRY * 8;

  /** Number of entries */
  protected long entries = 0;

  /** Number of slots (entries + empty space) */
  protected long slots = 0;

  /**
   * Creates an index inside the given directory. If the index does not exist,
   * it is created and initialized with the default index settings, which means
   * that uri identifiers are expected to be made out of 36 bytes (uuid) while
   * there is room for 10 versions with 8 bytes each.
   * <p>
   * Note that the number of versions will automatically be increased as soon as
   * an additional version is added, while the size of identifiers is fixed.
   * 
   * @param indexRootDir
   *          location of the index root directory
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @throws IOException
   *           if reading from the index fails
   */
  public VersionIndex(File indexRootDir, boolean readOnly) throws IOException {
    this(indexRootDir, readOnly, IDX_BYTES_PER_ID, IDX_VERSIONS_PER_ENTRY);
  }

  /**
   * Creates an index inside the given root directory. If the index does not
   * exist, it is created and initialized with the default index settings, which
   * means that uri identifiers are expected to be made out of 36 bytes (uuid).
   * <p>
   * Note that the number of versions will automatically be increased as soon as
   * an additional version is added, while the size of identifiers is fixed.
   * 
   * @param indexRootDirectory
   *          location of the index root directory
   * @param versions
   *          the number of versions per entry
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @throws IOException
   *           if reading from the index fails
   */
  public VersionIndex(File indexRootDirectory, boolean readOnly, int versions)
      throws IOException {
    this(indexRootDirectory, readOnly, IDX_BYTES_PER_ID, versions);
  }

  /**
   * Creates an index inside the given directory. If the index does not exist,
   * it is created and initialized with the default index settings.
   * 
   * @param indexRootDir
   *          location of the index root directory
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @param idLengthInBytes
   *          the number of bytes per id
   * @param versions
   *          the number of versions per entry
   * @throws IOException
   *           if reading from the index fails
   */
  public VersionIndex(File indexRootDir, boolean readOnly, int idLengthInBytes,
      int versions) throws IOException {

    this.idxFile = new File(indexRootDir, VERSION_IDX_NAME);
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
      this.bytesPerId = idx.readInt();
      this.versionsPerEntry = idx.readInt();
      this.entries = idx.readLong();
      this.bytesPerEntry = bytesPerId + 4 + versionsPerEntry * 8;
      if (this.bytesPerId != idLengthInBytes || this.versionsPerEntry != versions)
        resize(idLengthInBytes, versions);
    } catch (EOFException e) {
      if (readOnly) {
        throw new IllegalStateException("Readonly index cannot be empty");
      }
      init(idLengthInBytes, versions);
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
   * Returns the index size in bytes. The size is calculated from the size of
   * the header plus the number of slots multiplied by the size of one slot.
   * 
   * @return the index size
   */
  public long size() {
    return IDX_HEADER_SIZE + (slots * bytesPerEntry);
  }

  /**
   * Returns the maximum number of versions per entry.
   * <p>
   * This number may change if a resize operation happens, e. g. as soon as an
   * additional version is added to a uri that already has the current maximum
   * number of versions associated with it.
   * 
   * @return the number of versions per entry
   */
  public int getVersionsPerEntry() {
    return versionsPerEntry;
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
   * Adds id and version to the index and returns the index address.
   * 
   * @param id
   *          the identifier
   * @param version
   *          the page version
   * @return the entry's address in this index
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized long add(String id, long version) throws IOException {
    return add(entries, id, version);
  }

  /**
   * Adds the version to the entry that is located in slot <code>address</code>
   * and returns the index address.
   * 
   * @param entry
   *          the entry where the version needs to be added
   * @param version
   *          the page version
   * @return the entry's address in this index
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized long add(long entry, long version) throws IOException {
    return add(entry, null, version);
  }

  /**
   * Adds the version to the entry that is located in slot <code>address</code>
   * and returns the index address.
   * 
   * @param entry
   *          the entry where the version needs to be added
   * @param id
   *          the identifier
   * @param version
   *          the page version
   * @return the entry's address in this index
   * @throws IOException
   *           if writing to the index fails
   */
  private long add(long entry, String id, long version) throws IOException {
    if (id != null && id.getBytes().length != bytesPerId)
      throw new IllegalArgumentException(bytesPerId + " byte identifier required");

    boolean reusingSlot = false;
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
    int existingVersions = 0;

    // See if there is an empty slot (only if we are adding a whole new entry)
    if (entry == entries) {
      long address = IDX_HEADER_SIZE;
      long e = 0;
      idx.seek(address);
      while (address < startOfEntry) {
        if (idx.read() == '\n') {
          logger.debug("Found orphan line for reuse");
          startOfEntry = address;
          reusingSlot = true;
          entry = e;
          break;
        }
        idx.skipBytes(bytesPerEntry - 2);
        address += bytesPerEntry;
        e++;
      }
    }

    // Make sure there is still room left for an additional entry
    else {
      idx.seek(startOfEntry);
      byte[] bytes = new byte[bytesPerId];
      idx.read(bytes);
      id = new String(bytes);
      existingVersions = idx.readInt();
      if (existingVersions >= versionsPerEntry) {
        logger.info("Adding additional version, triggering index resize");
        resize(bytesPerId, versionsPerEntry * 2);
        startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
      }
    }

    // Add the new address
    idx.seek(startOfEntry);
    if (id != null)
      idx.write(id.getBytes());
    else
      idx.skipBytes(bytesPerId);
    idx.writeInt(existingVersions + 1);
    idx.skipBytes(existingVersions * 8);
    idx.writeLong(version);

    // Update the file header
    if (entry == entries) {
      if (!reusingSlot)
        slots++;
      entries++;
      idx.seek(IDX_SLOTS_HEADER_LOCATION);
      idx.writeLong(slots);
      idx.writeLong(entries);
      logger.debug("Added uri with id '{}' and initial version '{}' as entry no {}", new Object[] {
          id,
          PageUtils.getVersionString(version),
          entries });
    } else {
      logger.debug("Added version '{}' to uri with id '{}'", PageUtils.getVersionString(version), id);
    }

    return entry;
  }

  /**
   * Removes all versions for the page uri that is located at slot
   * <code>entry</code> from the index.
   * 
   * @param entry
   *          start address of uri entry
   * @throws IOException
   *           if removing the entry from the index fails
   */
  public synchronized void delete(long entry) throws IOException {
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);

    // Remove the entry
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
   * Removes the given version from the page uri that is located at slot
   * <code>entry</code>.
   * 
   * @param entry
   *          start address of uri entry
   * @param version
   *          the version to delete
   * @throws IOException
   *           if removing the entry from the index fails
   */
  public synchronized void delete(long entry, long version) throws IOException {
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);

    // Remove the version from the indicated entry
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);
    int deleteEntry = -1;
    int v = idx.readInt();
    long[] versions = new long[v];
    for (int i = 0; i < v; i++) {
      versions[i] = idx.readLong();
      if (versions[i] == version) {
        deleteEntry = i;
      }
    }

    // Everything ok?
    if (deleteEntry == -1)
      throw new IllegalStateException("Version '" + version + "' is not part of the index");

    // Drop the version
    idx.seek(startOfEntry + bytesPerId);
    idx.writeInt(v - 1);
    idx.skipBytes(deleteEntry * 8);
    for (int i = deleteEntry + 1; i < v; i++) {
      idx.writeLong(versions[i]);
    }
    idx.write(new byte[(versionsPerEntry - v - 1) * 8]);

    logger.debug("Removed version '{}' from uri '{}' from index", version, entry);
  }

  /**
   * Removes all entries from the index.
   * 
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized void clear() throws IOException {
    init(bytesPerId, versionsPerEntry);
  }

  /**
   * Returns the uri's versions.
   * 
   * @param entry
   *          the entry at which the id is expected
   * @return the versions
   * @throws EOFException
   *           if the user tries to access a non-existing address
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized long[] getVersions(long entry) throws IOException,
      EOFException {
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);
    int v = idx.readInt();
    long[] versions = new long[v];
    for (int i = 0; i < v; i++) {
      versions[i] = idx.readLong();
    }
    return versions;
  }

  /**
   * Returns <code>true</code> if the uri at address <code>entry</code> has the
   * indicated version.
   * 
   * @param entry
   *          the entry at which the id is expected
   * @param version
   *          the version to look up
   * @return <code>true</code> if version <code>version</code> exist
   * @throws EOFException
   *           if the user tries to access a non-existing address
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized boolean hasVersion(long entry, long version)
      throws IOException, EOFException {
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);
    int v = idx.readInt();
    for (int i = 0; i < v; i++) {
      if (version == idx.readLong())
        return true;
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the uri at address <code>entry</code> has a
   * version associated.
   * 
   * @param entry
   *          the entry at which the id is expected
   * @return <code>true</code> if some <code>version</code> exists
   * @throws EOFException
   *           if the user tries to access a non-existing address
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized boolean hasVersions(long entry) throws IOException,
      EOFException {
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);
    return idx.readInt() > 0;
  }

  /**
   * Initializes the index file to the given size using <code>0</code> byte
   * values and writes the header information to it.
   * 
   * @param bytesPerEntry
   *          the number of bytes per entry
   * @throws IOException
   *           writing to the index fails
   */
  private void init(int bytesPerId, int versionsPerEntry) throws IOException {
    this.bytesPerId = bytesPerId;
    this.versionsPerEntry = versionsPerEntry;
    this.bytesPerEntry = bytesPerId + 4 + versionsPerEntry * 8;

    logger.info("Creating version index with {} bytes per entry", bytesPerEntry);

    // Write header
    idx.seek(0);
    idx.writeInt(bytesPerId);
    idx.writeInt(versionsPerEntry);
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

    logger.debug("Version index created");
  }

  /**
   * Resizes the index file to the given number of bytes per entry.
   * 
   * @param newBytesPerId
   *          the number of bytes per id
   * @param newVersionsPerEntry
   *          the number of versions per entry
   * @throws IOException
   *           writing to the index fails
   * @throws IllegalStateException
   *           if the index is read only or if the user tries to resize the
   *           number of slots while there are already entries in the index
   */
  public synchronized void resize(int newBytesPerId, int newVersionsPerEntry)
      throws IOException {
    if (this.bytesPerId > newBytesPerId && this.entries > 0)
      throw new IllegalStateException("Cannot reduce the number of bytes per id when there are entries in the index");
    if (this.versionsPerEntry > newVersionsPerEntry && this.entries > 0)
      throw new IllegalStateException("Cannot reduce the number of versions per entry when there are entries in the index");
    if (this.isReadOnly)
      throw new IllegalStateException("This index is readonly");

    int newBytesPerEntry = newBytesPerId + 4 + newVersionsPerEntry * 8;

    logger.info("Resizing version index with {} entries to {} bytes per entry", entries, newBytesPerEntry);

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
    idxNew.writeInt(newBytesPerId);
    idxNew.writeInt(newVersionsPerEntry);
    idxNew.writeLong(slots);
    idxNew.writeLong(entries);

    // Copy the current index to the new one
    idx.seek(IDX_ENTRIES_LOCATION);
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
    this.versionsPerEntry = newVersionsPerEntry;
    this.idxFile = newIdxFile;

    time = System.currentTimeMillis() - time;
    logger.info("Version index resized in {}", ConfigurationUtils.toHumanReadableDuration(time));
  }

}
