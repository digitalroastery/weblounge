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

import ch.entwine.weblounge.common.content.ResourceUtils;
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
 * to versions. In order to quickly get to an entry, use the <code>id</code> or
 * <code>path</code> index.
 * 
 * Header:
 * <pre>
 * | index version | bytes per id | # of versions per entry | # of slots | # of entries |  
 * | (int)         | (int)        | (int)                   | (long)     | (long)       |  
 * </pre>
 * 
 * Entries:
 * <pre>
 * | id      | count | versions
 * |------------------------------------------
 * | a-b-c-d | 6     | 1 876876876
 * </pre>
 * 
 * <p>
 * Note that the current implementation is <b>not thread-safe</b> due to the
 * use of a single instance of the {@link RandomAccessFile}.
 */
public class VersionIndex implements VersionedContentRepositoryIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(VersionIndex.class);

  /** Name for the version index file */
  public static final String VERSION_IDX_NAME = "version.idx";

  
  /** Start of the index's header */
  protected static final long IDX_START_OF_HEADER = 0;

  /** Location of the version header */
  protected static final long IDX_HEADER_VERSION = IDX_START_OF_HEADER;

  /** Location of the bytes-per-id header */
  protected static final long IDX_HEADER_BYTES_PER_ID = IDX_HEADER_VERSION + 4;

  /** Location of the versions-per-entry header */
  protected static final long IDX_HEADER_VERSIONS_PER_ENTRY = IDX_HEADER_BYTES_PER_ID + 4;

  /** Location of the entries header */
  protected static final long IDX_HEADER_SLOTS = IDX_HEADER_VERSIONS_PER_ENTRY + 4;

  /** Location of the entries header */
  protected static final long IDX_HEADER_ENTRIES = IDX_HEADER_SLOTS + 8;

  /** Start of the index's body */
  protected static final long IDX_START_OF_CONTENT = IDX_HEADER_ENTRIES + 8;
  
  /** Default number of bytes used per id */
  private static final int DEFAULT_BYTES_PER_ID = 36;

  /** Default number of versions per index entry */
  private static final int DEFAULT_VERSIONS_PER_ENTRY = 10;

  /** Size of an entry (long) in bytes */
  protected static final int DEFAULT_ENTRY_SIZE = 8;

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

  /** Number of versions per entry */
  protected int versionsPerEntry = DEFAULT_VERSIONS_PER_ENTRY;

  /** Number of bytes per entry */
  protected int slotSizeInBytes = bytesPerId + DEFAULT_VERSIONS_PER_ENTRY * DEFAULT_ENTRY_SIZE;

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
    this(indexRootDir, readOnly, DEFAULT_BYTES_PER_ID, DEFAULT_VERSIONS_PER_ENTRY);
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
    this(indexRootDirectory, readOnly, DEFAULT_BYTES_PER_ID, versions);
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
      idx.seek(IDX_START_OF_HEADER);
      this.indexVersion = idx.readInt();
      if (indexVersion != INDEX_VERSION)
        logger.warn("Version index version mismatch (found {}, expected {}), consider reindex", indexVersion, INDEX_VERSION);
      this.bytesPerId = idx.readInt();
      this.versionsPerEntry = idx.readInt();
      this.slots = idx.readLong();
      this.entries = idx.readLong();
      
      // An entry consists of the id, the version count and the version entries
      this.slotSizeInBytes = bytesPerId + 4 + (versionsPerEntry * DEFAULT_ENTRY_SIZE);
      
      // If the index contains entries, we can't reduce the index size 
      if (this.entries > 0) {
        idLengthInBytes = Math.max(this.bytesPerId, idLengthInBytes);
        versions = Math.max(this.versionsPerEntry, versions);
      }

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
    return IDX_START_OF_CONTENT + (slots * slotSizeInBytes);
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
   * Returns the number of entries.
   * 
   * @return the number of entries
   */
  public synchronized long getEntries() {
    return entries;
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
  public synchronized int getEntriesPerSlot() {
    return versionsPerEntry;
  }

  /**
   * Returns the load factor for this index, which is determined by the number
   * of entries divided by the number of possible entries.
   * 
   * @return the load factor
   */
  public synchronized float getLoadFactor() {
    return (float) entries / (float) (slots * versionsPerEntry);
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
    long entry = slots;

    // See if there is an empty slot
    long address = IDX_START_OF_CONTENT;
    long e = 0;
    idx.seek(address);
    while (e < slots) {
      if (idx.readChar() == '\n') {
        logger.debug("Found orphan line for reuse");
        entry = e;
        break;
      }
      idx.skipBytes(slotSizeInBytes - 2);
      address += slotSizeInBytes;
      e++;
    }

    return add(entry, id, version);
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
  public synchronized long addVersion(long entry, long version)
      throws IOException {
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

    long startOfEntry = IDX_START_OF_CONTENT + (entry * slotSizeInBytes);
    int existingVersionCount = 0;

    // Make sure there is still room left for an additional entry
    idx.seek(startOfEntry);
    if (idx.getFilePointer() < idx.length() && idx.readChar() != '\n') {
      idx.skipBytes(bytesPerId - 2);
      existingVersionCount = idx.readInt();
      if (existingVersionCount >= versionsPerEntry) {
        logger.info("Adding additional language, triggering index resize");
        resize(bytesPerId, versionsPerEntry * 2);
        startOfEntry = IDX_START_OF_CONTENT + (entry * slotSizeInBytes);
      }
    } else if (id == null) {
      throw new IllegalArgumentException("Identifier required to create a new entry");
    }

    // Add the new address
    idx.seek(startOfEntry);
    if (id != null)
      idx.write(id.getBytes());
    else
      idx.skipBytes(bytesPerId);

    // Add the version
    idx.writeInt(existingVersionCount + 1);
    idx.skipBytes(existingVersionCount * DEFAULT_ENTRY_SIZE);
    idx.writeLong(version);
    entries++;

    // Update the file header
    if (entry >= slots) {
      slots++;
      idx.seek(IDX_HEADER_SLOTS);
      idx.writeLong(slots);
      idx.writeLong(entries);
      logger.debug("Added uri with id '{}' and initial version '{}' as entry no {}", new Object[] {
          id,
          ResourceUtils.getVersionString(version),
          entries });
    } else {
      idx.seek(IDX_HEADER_ENTRIES);
      idx.writeLong(entries);
      logger.debug("Added version '{}' to uri with id '{}'", ResourceUtils.getVersionString(version), id);
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
    long startOfEntry = IDX_START_OF_CONTENT + (entry * slotSizeInBytes);

    idx.seek(startOfEntry + bytesPerId);
    int existingVersions = idx.readInt();

    // Remove the entry
    idx.seek(startOfEntry);
    idx.writeChar('\n');
    idx.write(new byte[slotSizeInBytes - 2]);

    // Update the file header
    entries -= existingVersions;
    idx.seek(IDX_HEADER_ENTRIES);
    idx.writeLong(entries);

    logger.debug("Removed all versions at address '{}' from index", entry);
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
    long startOfEntry = IDX_START_OF_CONTENT + (entry * slotSizeInBytes);

    // Remove the version from the indicated entry
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);
    int deleteEntry = -1;
    int existingVersions = idx.readInt();

    // If this is the last version, simply remove the whole entry
    if (existingVersions == 1) {
      delete(entry);
      return;
    }

    long[] versions = new long[existingVersions];
    for (int i = 0; i < existingVersions; i++) {
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
    idx.writeInt(existingVersions - 1);
    idx.skipBytes(deleteEntry * DEFAULT_ENTRY_SIZE);
    for (int i = deleteEntry + 1; i < existingVersions; i++) {
      idx.writeLong(versions[i]);
    }
    idx.write(new byte[(versionsPerEntry - existingVersions - 1) * DEFAULT_ENTRY_SIZE]);

    // Adjust the header
    entries--;
    idx.seek(IDX_HEADER_ENTRIES);
    idx.writeLong(entries);

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
    long startOfEntry = IDX_START_OF_CONTENT + (entry * slotSizeInBytes);
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
    long startOfEntry = IDX_START_OF_CONTENT + (entry * slotSizeInBytes);
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
    long startOfEntry = IDX_START_OF_CONTENT + (entry * slotSizeInBytes);
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
    this.indexVersion = INDEX_VERSION;
    this.bytesPerId = bytesPerId;
    this.versionsPerEntry = versionsPerEntry;
    this.slotSizeInBytes = bytesPerId + 4 + versionsPerEntry * DEFAULT_ENTRY_SIZE;

    this.slots = 0;
    this.entries = 0;

    logger.info("Creating version index with {} bytes per entry", slotSizeInBytes);

    // Write header
    idx.seek(IDX_START_OF_HEADER);
    idx.writeInt(indexVersion);
    idx.writeInt(bytesPerId);
    idx.writeInt(versionsPerEntry);
    idx.writeLong(slots);
    idx.writeLong(entries);

    // If this file used to contain entries, we just null out the rest
    try {
      byte[] bytes = new byte[slotSizeInBytes - 2];
      while (idx.getFilePointer() < idx.length()) {
        idx.writeChar('\n');
        idx.write(bytes);
      }
    } catch (EOFException e) {
      // That's ok, we wanted to write until the very end
    }

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

    int newSlotSizeInBytes = newBytesPerId + 4 + (newVersionsPerEntry * DEFAULT_ENTRY_SIZE);

    logger.info("Resizing version index to {} ({}) slots and {} ({}) bytes per entry", new Object[] { slots, this.slots, newSlotSizeInBytes, this.slotSizeInBytes });

    String fileName = FilenameUtils.getBaseName(idxFile.getName());
    String fileExtension = FilenameUtils.getExtension(idxFile.getName());
    String idxFilenameNew = fileName + "_resized." + fileExtension;
    File idxNewFile = new File(idxFile.getParentFile(), idxFilenameNew);
    long time = System.currentTimeMillis();

    logger.debug("Creating resized index at " + idxNewFile);

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
    idxNew.writeInt(newVersionsPerEntry);
    idxNew.writeLong(slots);
    idxNew.writeLong(entries);

    // Position to read the whole content
    idx.seek(IDX_START_OF_CONTENT);

    // Write entries
    for (int i = 0; i < slots; i++) {
      byte[] bytes = new byte[newSlotSizeInBytes];
      if (i < this.slots) {
        idx.read(bytes, 0, this.slotSizeInBytes);
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

    this.slotSizeInBytes = newSlotSizeInBytes;
    this.bytesPerId = newBytesPerId;
    this.versionsPerEntry = newVersionsPerEntry;

    time = System.currentTimeMillis() - time;
    logger.info("Version index resized in {}", ConfigurationUtils.toHumanReadableDuration(time));
  }

}
