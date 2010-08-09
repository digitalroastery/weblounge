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

import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.contentrepository.VersionedContentRepositoryIndex;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Set;

/**
 * This index is part of the content repository index and maps page identifiers
 * to languages. In order to quickly get to an entry, use the <code>id</code> or
 * <code>path</code> index.
 * 
 * <pre>
 * | slot | id      | languages
 * |------------------------------------------
 * | 1    | a-b-c-d | de en
 * </pre>
 */
public class LanguageIndex implements VersionedContentRepositoryIndex {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(LanguageIndex.class);

  /** Name for the language index file */
  public static final String LANGUAGE_IDX_NAME = "language.idx";

  /** Location of the version header */
  protected static final long IDX_VERSION_HEADER_LOCATION = 0;

  /** Location of the bytes-per-id header */
  protected static final long IDX_BYTES_PER_ID_HEADER_LOCATION = 4;

  /** Location of the languages-per-entry header */
  protected static final long IDX_LANGUAGES_PER_ENTRY_HEADER_LOCATION = 8;

  /** Location of the entries header */
  protected static final long IDX_SLOTS_HEADER_LOCATION = 12;

  /** Location of the entries header */
  protected static final long IDX_ENTRIES_HEADER_LOCATION = 20;

  /** Number of bytes that are used for the index header */
  protected static final int IDX_HEADER_SIZE = 28;

  /** Default number of bytes used per id */
  private static final int IDX_BYTES_PER_ID = 36;

  /** Default number of languages per index entry */
  private static final int IDX_LANGUAGES_PER_ENTRY = 5;

  /** The index file */
  protected RandomAccessFile idx = null;

  /** The index file */
  protected File idxFile = null;

  /** True if this is a readonly index */
  protected boolean isReadOnly = false;

  /** The index version number */
  protected int indexVersion = -1;

  /** Number of bytes per id */
  protected int bytesPerId = IDX_BYTES_PER_ID;

  /** Number of languages per entry */
  protected int languagesPerEntry = IDX_LANGUAGES_PER_ENTRY;

  /** Number of bytes per language */
  protected int bytesPerLanguage = 2;

  /** Number of bytes per entry */
  protected int bytesPerEntry = bytesPerId + IDX_LANGUAGES_PER_ENTRY * bytesPerLanguage;

  /** Number of entries */
  protected long entries = 0;

  /** Number of slots (entries + empty space) */
  protected long slots = 0;

  /**
   * Creates an index inside the given directory. If the index does not exist,
   * it is created and initialized with the default index settings, which means
   * that uri identifiers are expected to be made out of 36 bytes (uuid) while
   * there is room for 10 languages with 8 bytes each.
   * <p>
   * Note that the number of languages will automatically be increased as soon
   * as an additional language is added, while the size of identifiers is fixed.
   * 
   * @param indexRootDir
   *          location of the index root directory
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @throws IOException
   *           if reading from the index fails
   */
  public LanguageIndex(File indexRootDir, boolean readOnly) throws IOException {
    this(indexRootDir, readOnly, IDX_BYTES_PER_ID, IDX_LANGUAGES_PER_ENTRY);
  }

  /**
   * Creates an index inside the given root directory. If the index does not
   * exist, it is created and initialized with the default index settings, which
   * means that uri identifiers are expected to be made out of 36 bytes (uuid).
   * <p>
   * Note that the number of languages will automatically be increased as soon
   * as an additional language is added, while the size of identifiers is fixed.
   * 
   * @param indexRootDirectory
   *          location of the index root directory
   * @param languages
   *          the number of languages per entry
   * @param readOnly
   *          <code>true</code> to indicate a read only index
   * @throws IOException
   *           if reading from the index fails
   */
  public LanguageIndex(File indexRootDirectory, boolean readOnly, int languages)
      throws IOException {
    this(indexRootDirectory, readOnly, IDX_BYTES_PER_ID, languages);
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
   * @param languages
   *          the number of languages per entry
   * @throws IOException
   *           if reading from the index fails
   */
  public LanguageIndex(File indexRootDir, boolean readOnly,
      int idLengthInBytes, int languages) throws IOException {

    this.idxFile = new File(indexRootDir, LANGUAGE_IDX_NAME);
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
        logger.warn("Language index version mismatch (found {}, expected {}), consider reindex", indexVersion, IDX_VERSION);
      this.bytesPerId = idx.readInt();
      this.languagesPerEntry = idx.readInt();
      this.slots = idx.readLong();
      this.entries = idx.readLong();
      this.bytesPerEntry = bytesPerId + 4 + languagesPerEntry * 8;
      if (this.bytesPerId != idLengthInBytes || this.languagesPerEntry != languages)
        resize(idLengthInBytes, languages);
    } catch (EOFException e) {
      if (readOnly) {
        throw new IllegalStateException("Readonly index cannot be empty");
      }
      init(idLengthInBytes, languages);
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
   * Returns the maximum number of languages per entry.
   * <p>
   * This number may change if a resize operation happens, e. g. as soon as an
   * additional language is added to a uri that already has the current maximum
   * number of languages associated with it.
   * 
   * @return the number of languages per entry
   */
  public int getLanguagesPerEntry() {
    return languagesPerEntry;
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
   * Adds id and language to the index and returns the index address.
   * 
   * @param id
   *          the identifier
   * @param language
   *          the resource language
   * @return the entry's address in this index
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized long add(String id, Language language) throws IOException {
    return add(slots, id, language);
  }

  /**
   * Adds id and a set of languages to the index and returns the index address.
   * 
   * @param id
   *          the identifier
   * @param languages
   *          the resource languages
   * @return the entry's address in this index
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized long setLanguages(String id, Set<Language> languages)
      throws IOException {
    if (languages.size() == 0)
      return add(slots, id, null);

    Iterator<Language> li = languages.iterator();
    long address = add(slots, id, li.next());
    while (li.hasNext()) {
      add(address, li.next());
    }
    return address;
  }

  /**
   * Adds the language to the entry that is located in slot <code>entry</code>
   * and returns the index address.
   * 
   * @param entry
   *          the entry where the language needs to be added
   * @param language
   *          the resource language
   * @return the entry's address in this index
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized long add(long entry, Language language)
      throws IOException {
    return add(entry, null, language);
  }

  /**
   * Adds the languages to the entry that is located in slot <code>entry</code>
   * and returns the index address.
   * 
   * @param entry
   *          the entry where the language needs to be added
   * @param languages
   *          the resource languages
   * @return the entry's address in this index
   * @throws IOException
   *           if writing to the index fails
   */
  public void setLanguages(long entry, Set<Language> languages) throws IOException {
    delete(entry, true);
    if (languages == null || languages.size() == 0)
      return;
    Iterator<Language> li = languages.iterator();
    long address = add(entry, li.next());
    while (li.hasNext()) {
      add(address, li.next());
    }
  }

  /**
   * Adds the language to the entry that is located in slot <code>address</code>
   * and returns the index address.
   * 
   * @param entry
   *          the entry where the language needs to be added
   * @param id
   *          the identifier
   * @param language
   *          the resource language
   * @return the entry's address in this index
   * @throws IOException
   *           if writing to the index fails
   */
  private long add(long entry, String id, Language language) throws IOException {
    if (id != null && id.getBytes().length != bytesPerId)
      throw new IllegalArgumentException(bytesPerId + " byte identifier required");

    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
    int existingLanguageCount = 0;

    // See if there is an empty slot (only if we are adding a whole new entry)
    if (entry >= slots) {
      long address = IDX_HEADER_SIZE;
      long e = 0;
      idx.seek(address);
      while (address < startOfEntry) {
        if (idx.read() == '\n') {
          logger.debug("Found orphan line for reuse");
          startOfEntry = address;
          entry = e;
          break;
        }
        idx.skipBytes(bytesPerEntry - 1);
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
      existingLanguageCount = idx.readInt();
      if (existingLanguageCount >= languagesPerEntry) {
        logger.info("Adding additional language, triggering index resize");
        resize(bytesPerId, languagesPerEntry * 2);
        startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
      }
    }

    // Add the new address
    idx.seek(startOfEntry);
    if (id != null)
      idx.write(id.getBytes());
    else
      idx.skipBytes(bytesPerId);

    if (language != null) {
      idx.writeInt(existingLanguageCount + 1);
      idx.skipBytes(existingLanguageCount * bytesPerLanguage);
      idx.write(language.getIdentifier().getBytes());
      entries++;
    } else {
      idx.writeInt(0);
    }

    // Update the file header
    if (entry >= slots) {
      slots++;
      idx.seek(IDX_SLOTS_HEADER_LOCATION);
      idx.writeLong(slots);
      idx.writeLong(entries);
      if (language != null)
        logger.debug("Added uri with id '{}' and initial language '{}' as entry no {}", new Object[] {
            id,
            language,
            entries });
      else
        logger.debug("Added uri with id '{}' and no initial language as entry no {}", new Object[] {
            id,
            entries });
    } else if (language != null) {
      idx.seek(IDX_ENTRIES_HEADER_LOCATION);
      idx.writeLong(entries);
      logger.debug("Added language '{}' to uri with id '{}'", language, id);
    }

    return entry;
  }

  /**
   * Removes all languages for the page uri that is located at slot
   * <code>entry</code> from the index.
   * 
   * @param entry
   *          start address of uri entry
   * @param keepSlot
   *          <code>true</code> to keep the slot assigned to the current id
   * @throws IOException
   *           if removing the entry from the index fails
   */
  public synchronized void delete(long entry) throws IOException {
    delete(entry, false);
  }

  /**
   * Removes all languages for the page uri that is located at slot
   * <code>entry</code> from the index. If <code>keepSlot</code> is set to
   * <code>true</code>, the languages will be removed but the slot is kept and
   * remains assigned to the current id.
   * 
   * @param entry
   *          start address of uri entry
   * @param keepSlot
   *          <code>true</code> to keep the slot assigned to the current id
   * @throws IOException
   *           if removing the entry from the index fails
   */
  protected synchronized void delete(long entry, boolean keepSlot)
      throws IOException {
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);

    // Remove the entry
    idx.seek(startOfEntry);
    if (keepSlot) {
      idx.skipBytes(IDX_BYTES_PER_ID);
      idx.writeInt(0);
      idx.write(new byte[languagesPerEntry * bytesPerEntry]);
      logger.debug("Removed all languages at address '{}' from index", entry);
    } else {
      idx.write('\n');
      idx.write(new byte[bytesPerEntry - 1]);
      entries--;
      idx.seek(IDX_ENTRIES_HEADER_LOCATION);
      idx.writeLong(entries);
      logger.debug("Removed entry at address '{}' from index", entry);
    }
  }

  /**
   * Removes the given language from the page uri that is located at slot
   * <code>entry</code>.
   * 
   * @param entry
   *          start address of uri entry
   * @param language
   *          the language to delete
   * @throws IOException
   *           if removing the entry from the index fails
   */
  public synchronized void delete(long entry, Language language)
      throws IOException {
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);

    // Remove the language from the indicated entry
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);
    int deleteEntry = -1;
    int v = idx.readInt();
    String[] languages = new String[v];
    for (int i = 0; i < v; i++) {
      byte[] l = new byte[bytesPerLanguage];
      idx.read(l);
      languages[i] = new String(l);
      if (languages[i].equals(language.getIdentifier())) {
        deleteEntry = i;
      }
    }

    // Everything ok?
    if (deleteEntry == -1)
      throw new IllegalStateException("Language '" + language + "' is not part of the index");

    // Drop the language
    idx.seek(startOfEntry + bytesPerId);
    idx.writeInt(v - 1);
    idx.skipBytes(deleteEntry * 8);
    for (int i = deleteEntry + 1; i < v; i++) {
      idx.write(languages[i].getBytes());
    }
    idx.write(new byte[(languagesPerEntry - v - 1) * 8]);

    logger.debug("Removed language '{}' from uri '{}' from index", language, entry);
  }

  /**
   * Removes all entries from the index.
   * 
   * @throws IOException
   *           if writing to the index fails
   */
  public synchronized void clear() throws IOException {
    init(bytesPerId, languagesPerEntry);
  }

  /**
   * Returns the uri's languages.
   * 
   * @param entry
   *          the entry at which the id is expected
   * @return the languages
   * @throws EOFException
   *           if the user tries to access a non-existing address
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized Language[] getLanguages(long entry) throws IOException,
      EOFException {
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);
    int v = idx.readInt();
    Language[] languages = new Language[v];
    for (int i = 0; i < v; i++) {
      byte[] language = new byte[bytesPerLanguage];
      idx.read(language);
      languages[i] = LanguageSupport.getLanguage(new String(language));
    }
    return languages;
  }

  /**
   * Returns <code>true</code> if the uri at address <code>entry</code> has the
   * indicated language.
   * 
   * @param entry
   *          the entry at which the id is expected
   * @param language
   *          the language to look up
   * @return <code>true</code> if language <code>language</code> exist
   * @throws EOFException
   *           if the user tries to access a non-existing address
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized boolean hasLanguage(long entry, Language language)
      throws IOException, EOFException {
    long startOfEntry = IDX_HEADER_SIZE + (entry * bytesPerEntry);
    idx.seek(startOfEntry);
    idx.skipBytes(bytesPerId);
    int v = idx.readInt();
    for (int i = 0; i < v; i++) {
      byte[] l = new byte[bytesPerLanguage];
      idx.read(l);
      if (language.getIdentifier().equals(new String(l)))
        return true;
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the uri at address <code>entry</code> has a
   * language associated.
   * 
   * @param entry
   *          the entry at which the id is expected
   * @return <code>true</code> if some language exists
   * @throws EOFException
   *           if the user tries to access a non-existing address
   * @throws IOException
   *           if reading from the index fails
   */
  public synchronized boolean hasLanguage(long entry) throws IOException,
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
  private void init(int bytesPerId, int languagesPerEntry) throws IOException {
    this.indexVersion = IDX_VERSION;
    this.bytesPerId = bytesPerId;
    this.languagesPerEntry = languagesPerEntry;
    this.bytesPerEntry = bytesPerId + 4 + languagesPerEntry * 8;

    logger.info("Creating language index with {} bytes per entry", bytesPerEntry);

    // Write header
    idx.seek(0);
    idx.writeInt(indexVersion);
    idx.writeInt(bytesPerId);
    idx.writeInt(languagesPerEntry);
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

    this.slots = 0;
    this.entries = 0;

    logger.debug("Language index created");
  }

  /**
   * Resizes the index file to the given number of bytes per entry.
   * 
   * @param newBytesPerId
   *          the number of bytes per id
   * @param newLanguagesPerEntry
   *          the number of languages per entry
   * @throws IOException
   *           writing to the index fails
   * @throws IllegalStateException
   *           if the index is read only or if the user tries to resize the
   *           number of slots while there are already entries in the index
   */
  public synchronized void resize(int newBytesPerId, int newLanguagesPerEntry)
      throws IOException {
    if (this.bytesPerId > newBytesPerId && this.entries > 0)
      throw new IllegalStateException("Cannot reduce the number of bytes per id when there are entries in the index");
    if (this.languagesPerEntry > newLanguagesPerEntry && this.entries > 0)
      throw new IllegalStateException("Cannot reduce the number of languages per entry when there are entries in the index");
    if (this.isReadOnly)
      throw new IllegalStateException("This index is readonly");

    int newBytesPerEntry = newBytesPerId + 4 + newLanguagesPerEntry * 8;

    logger.info("Resizing language index with {} entries to {} bytes per entry", entries, newBytesPerEntry);

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
    idxNew.writeInt(newLanguagesPerEntry);
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
    this.languagesPerEntry = newLanguagesPerEntry;
    this.idxFile = newIdxFile;

    time = System.currentTimeMillis() - time;
    logger.info("Language index resized in {}", ConfigurationUtils.toHumanReadableDuration(time));
  }

}
