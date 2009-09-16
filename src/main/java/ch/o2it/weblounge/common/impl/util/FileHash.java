/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util;

import java.io.File;

/**
 * This class is used to easily compare two files, considering th file path, its
 * name, size and modification date. If all those file properties match, then
 * the <code>equals</code> method of this class returns <code>true</code>.
 * 
 * @author Tobias Wunden
 */

public class FileHash {

  /** The hash value */
  private long hash_;

  /**
   * Creates a new hash from the given file. Note that <code>file</code> must
   * not be <code>null</code> and must represent an existing and accessible
   * object in the file system.
   * 
   * @param file
   *          the file
   */
  public FileHash(File file) {
    Arguments.checkNull(file, "file");
    Arguments.checkValue(file.exists(), "file");
    long modified = file.lastModified();
    long size = file.length();
    hash_ = modified | size;
  }

  /**
   * Creates a <code>FileHash</code> from a previously calculated hash value.
   * 
   * @param hash
   *          the hash value
   */
  public FileHash(long hash) {
    hash_ = hash;
  }

  /**
   * Returns the hash code that has been computed from the file's properties.
   * 
   * @return the hash code
   */
  public long fileHashCode() {
    return hash_;
  }

  /**
   * Returns the <code>int</code> version of the file hash.
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return (new Long(hash_)).hashCode();
  }

  /**
   * Returns <code>true</code> if <code>o</code> is a <code>FileHash</code>
   * featuring the same hash code than this instance.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o instanceof FileHash) {
      return hash_ == ((FileHash) o).hash_;
    }
    return false;
  }

}