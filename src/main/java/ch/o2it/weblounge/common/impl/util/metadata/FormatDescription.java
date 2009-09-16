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

package ch.o2it.weblounge.common.impl.util.metadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Data class to store information on a file format.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */

public class FormatDescription implements Comparable {

  private static List descriptions;

  private static int minBufferSize;

  private List fileExtensions;

  private String group;

  private String longName;

  private byte[] magicBytes;

  private List mimeTypes;

  private Integer minimumSize;

  private Integer offset;

  private String shortName;

  static {
    readFormats();
  }

  /**
   * Analyzes the given <code>byte[]</code> array of data and returns the
   * corresponding <code>FormatDescription</code>.
   * 
   * @param data
   *          the data
   * @return the data's format description
   */
  public static FormatDescription identify(byte[] data) {
    if (data == null || data.length < 1) {
      return null;
    }
    Iterator iter = descriptions.iterator();
    while (iter.hasNext()) {
      FormatDescription desc = (FormatDescription) iter.next();
      if (desc.matches(data)) {
        return desc;
      }
    }
    return null;
  }

  /**
   * Analyzes the first few bytes of the given file and returns the
   * corresponding <code>FormatDescription</code>.
   * 
   * @param file
   *          the data
   * @return the data's format description
   */
  public static FormatDescription identify(File file) {
    if (!file.isFile()) {
      return null;
    }
    long size = file.length();
    int numBytes;
    if (size > minBufferSize) {
      numBytes = minBufferSize;
    } else {
      numBytes = (int) size;
    }
    byte[] data = new byte[numBytes];
    RandomAccessFile in = null;
    try {
      in = new RandomAccessFile(file, "r");
      in.readFully(data);
      in.close();
    } catch (IOException ioe) {
      return null;
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ioe) {
        // 
      }
    }
    return identify(data);
  }

  /**
   * Loads the format descriptions from our <i>magic</i> file
   * <code>formats.txt</code>.
   */
  private static void readFormats() {
    descriptions = new ArrayList();
    minBufferSize = 1;
    try {
      InputStream input = FormatDescription.class.getResourceAsStream("/ch/o2it/weblounge/core/util/metadata/formats.txt");
      if (input == null) {
        return;
      }
      FormatDescriptionReader in = new FormatDescriptionReader(new InputStreamReader(input));
      FormatDescription desc;
      while ((desc = in.read()) != null) {
        byte[] magic = desc.getMagicBytes();
        Integer offset = desc.getOffset();
        if (magic != null && offset != null && offset.intValue() + magic.length > minBufferSize) {
          minBufferSize = offset.intValue() + magic.length;
        }
        descriptions.add(desc);
      }
      input.close();
    } catch (Exception e) {
    }
  }

  /**
   * Add a single file extension to the internal list of typical file extensions
   * for this format.
   * 
   * @param ext
   *          file extension
   */
  public void addFileExtension(String ext) {
    if (ext == null || ext.length() < 1) {
      return;
    }
    if (fileExtensions == null) {
      fileExtensions = new ArrayList();
    }
    fileExtensions.add(ext);
  }

  /**
   * Add all file extensions to the internal list of typical file extensions for
   * this format.
   * 
   * @param ext
   *          a comma-separated list of typical file extensions
   */
  public void addFileExtensions(String ext) {
    if (ext == null) {
      return;
    }
    if (ext.indexOf(',') == -1) {
      addFileExtension(ext);
      return;
    }
    String[] extensions = ext.split(",");
    for (int i = 0; i < extensions.length; i++) {
      addFileExtension(extensions[i]);
    }
  }

  /**
   * Add a single MIME type to the internal list of MIME types for this format.
   * 
   * @param mimeType
   *          the MIME type to be added
   */
  public void addMimeType(String mimeType) {
    if (mimeType == null || mimeType.length() < 1) {
      return;
    }
    if (mimeTypes == null) {
      mimeTypes = new ArrayList();
    }
    mimeTypes.add(mimeType);
  }

  /**
   * Add a list of MIME types to the internal list of MIME types for this
   * format.
   * 
   * @param mimeType
   *          a comma-separated list of MIME types to be added
   */
  public void addMimeTypes(String mimeType) {
    if (mimeTypes == null) {
      return;
    }
    if (mimeType.indexOf(',') == -1) {
      addMimeType(mimeType);
      return;
    }
    String[] types = mimeType.split(",");
    for (int i = 0; i < types.length; i++) {
      addMimeType(types[i]);
    }
  }

  public int compareTo(Object obj) {
    FormatDescription desc = (FormatDescription) obj;
    int relation = getGroup().compareTo(desc.getGroup());
    if (relation != 0) {
      return relation;
    }
    return getLongName().compareTo(desc.getLongName());
  }

  public List getFileExtensions() {
    return fileExtensions;
  }

  public String getGroup() {
    return group;
  }

  public String getLongName() {
    return longName;
  }

  public byte[] getMagicBytes() {
    return magicBytes;
  }

  public String getMimeType() {
    return getMimeType(0);
  }

  public String getMimeType(int index) {
    if (mimeTypes != null && index >= 0 && index < mimeTypes.size()) {
      return (String) mimeTypes.get(index);
    } else {
      return null;
    }
  }

  public List getMimeTypes() {
    return mimeTypes;
  }

  public Integer getMinimumSize() {
    return minimumSize;
  }

  public Integer getOffset() {
    return offset;
  }

  public String getShortName() {
    return shortName;
  }

  public boolean matches(byte[] data) {
    if (magicBytes == null || offset == null) {
      return false;
    }
    int index1 = 0;
    int index2 = offset.intValue();
    if (index2 + magicBytes.length > data.length) {
      return false;
    }
    int num = magicBytes.length;
    do {
      if (magicBytes[index1++] != data[index2++]) {
        return false;
      }
      num--;
    } while (num > 0);
    return true;
  }

  public void setGroup(String newValue) {
    group = newValue;
  }

  public void setLongName(String newValue) {
    longName = newValue;
  }

  public void setMagicBytes(byte[] newValue) {
    magicBytes = newValue;
  }

  public void setMagicBytes(String newValue) {
    if (newValue == null || newValue.length() < 1) {
      magicBytes = null;
      return;
    }
    if (newValue.length() > 2 && newValue.charAt(0) == '"' && newValue.charAt(newValue.length() - 1) == '"') {
      newValue = newValue.substring(1, newValue.length() - 1);
      try {
        magicBytes = newValue.getBytes("iso-8859-1");
      } catch (UnsupportedEncodingException uee) {
        magicBytes = null;
      }
      return;
    }
    if ((newValue.length() % 2) == 0) {
      newValue = newValue.toLowerCase();
      byte[] data = new byte[newValue.length() / 2];
      int byteValue = 0;
      for (int i = 0; i < newValue.length(); i++) {
        char c = newValue.charAt(i);
        int number;
        if (c >= '0' && c <= '9') {
          number = c - '0';
        } else if (c >= 'a' && c <= 'f') {
          number = 10 + c - 'a';
        } else {
          return;
        }
        if ((i % 2) == 0) {
          byteValue = number * 16;
        } else {
          byteValue += number;
          data[i / 2] = (byte) byteValue;
        }
      }
      magicBytes = data;
    }
  }

  public void setMinimumSize(Integer newValue) {
    minimumSize = newValue;
  }

  public void setOffset(Integer newValue) {
    offset = newValue;
  }

  public void setShortName(String newValue) {
    shortName = newValue;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer(80);
    sb.append(getGroup());
    sb.append(";");
    sb.append(getShortName());
    sb.append(";");
    sb.append(getLongName());
    sb.append(";");
    if (getMimeTypes() != null) {
      Iterator iter = getMimeTypes().iterator();
      while (iter.hasNext()) {
        sb.append(iter.next());
        sb.append(";");
      }
    } else {
      sb.append(";");
    }
    sb.append(";");
    if (getFileExtensions() != null) {
      Iterator iter = getFileExtensions().iterator();
      while (iter.hasNext()) {
        sb.append(iter.next());
        sb.append(";");
      }
    } else {
      sb.append(";");
    }
    sb.append(getOffset());
    sb.append(";");
    sb.append(";");
    return sb.toString();
  }

}