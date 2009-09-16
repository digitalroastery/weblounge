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
 * Holds all the information about a single uploaded file.
 * 
 * @version 1.0
 * @author Daniel Steiner
 * @see ch.ch.o2it.weblounge.common.util.http.MultipartFormdataDecoder#getFiles()
 */

public class UploadedFile {

  /** the local file name */
  protected String fileName;

  /** the original file name */
  protected String origName;

  /** the name of the form field that contained the file */
  protected String fieldName;

  /** the mime type of the file */
  protected String contentType;

  /** the owner of the file */
  protected String owner;

  /**
   * Creates a new description of an uploaded file.
   * 
   * @param fileName
   *          the local name of the file
   * @param origName
   *          the original name of the file
   * @param fieldName
   *          the name of the form field
   * @param contentType
   *          the content type of the file
   * @param owner
   *          the owner of the file
   */
  public UploadedFile(String fileName, String origName, String fieldName,
      String contentType, String owner) {
    this.fileName = fileName;
    this.origName = origName;
    this.fieldName = fieldName;
    this.contentType = contentType;
    this.owner = owner;
  }

  /**
   * Creates a new description of an uploaded file.
   * 
   * @param fileName
   *          the local name of the file
   * @param origName
   *          the original name of the file
   * @param fieldName
   *          the name of the form field
   * @param contentType
   *          the content type of the file
   */
  public UploadedFile(String fileName, String origName, String fieldName,
      String contentType) {
    this.fileName = fileName;
    this.origName = origName;
    this.fieldName = fieldName;
    this.contentType = contentType;
  }

  /**
   * Returns the MIME type of the file.
   * 
   * @return the content type of the file
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Returns the name of the form field that contained the file.
   * 
   * @return the name of the foem field
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Returns the name of the local file where the download is stored. This will
   * usually point to a file in the system's temp directory.
   * 
   * @return the local file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Returns the original name of the file.
   * 
   * @return the original name of the file
   */
  public String getOriginalName() {
    return origName;
  }

  /**
   * Returns the owner of the uploaded file.
   * 
   * @return the owner of the resource
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Removes the temporary files.
   * 
   * @return <code>true</code> if the file could be deleted
   */
  public boolean delete() {
    return new File(fileName).delete();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (fileName != null) ? fileName : super.toString();
  }

}