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

import java.io.UnsupportedEncodingException;

/**
 * Generate downladable link files for various operating systems.
 * 
 * @version $Revision: 1059 $
 * @author Daniel Steiner
 */
public class DAVRepositoryLinkGenerator {

  /** the comment field embedded into all includes */
  private static final String COMMENT = "Access the Weblounge Repository using WebDAV";

  /*
   * The basic structure of a Windows shortcut to a web folder is as follows:
   * 
   * header: ------- 0x0000004c // file header "L" {0x01, 0x14, 0x02, 0x00,
   * 0x00, 0x00, 0x00, 0x00, 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46} //
   * Shortcut GUID {00021401-0000-0000-00C0-000000000046} 0x00000085 // flags:
   * 00000000 10000101 // BIT0 = shell item id list present // BIT1 = target is
   * a file or directory // BIT2 = comment present // BIT3 = relative path
   * string present // BIT4 = working directory present // BIT5 = command line
   * arguments present // BIT6 = custom icon // BIT7 = ?? 0x00000000 // target
   * file attributes 0x0000000000000000 // target file creation time
   * 0x0000000000000000 // target file modification time 0x0000000000000000 //
   * target file last access time 0x00000000 // target file length 0x00000000 //
   * icon number 0=default 0x00000001 // show window value: SW_NORMAL 0x00000000
   * // hot key: none 0x00000000 // reserved 0x00000000 // reserved
   * 
   * shell items: ------------ (0x0000) // shi_length = item_1 + item_2 + item_3
   * + item_4 // = 20 + 20 + item_3 + 2 0x0014 // 1st item length {0x1f, 0x50}
   * // ?? {0xe0, 0x4f, 0xd0, 0x20, 0xea, 0x3a, 0x69, 0x10, 0xa2, 0xd8, 0x08,
   * 0x00, 0x2b, 0x30, 0x30, 0x9d} // My Computer GUID
   * {20D04FE0-3AEA-1069-A2D8-08002B30309D} 0x0014 // 2nd item length {0x2e,
   * 0x80} // ?? alternatively {0x2e, 0x00} {0x00, 0xdf, 0xea, 0xbd, 0x65, 0xc2,
   * 0xd0, 0x11, 0xbc, 0xed, 0x00, 0xa0, 0xc9, 0x0a, 0xb5, 0x0f} // Web Folders
   * GUID {BDEADF00-C265-11d0-BCED-00A0C90AB50F} (0x0000) // 3rd item length = 2
   * + 26 + 2 + desc_length + 2 // + 2 + loc_length + 2 + 4 {0x4c, 0x50, 0x00,
   * 0x01, 0x42, 0x57, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
   * 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00} // ??
   * (0x0000) // length of resource description string "Text" // resource
   * description string in UTF-16lsb 0x0000 // separator (0x0000) // length of
   * resource location string "Text" // resource location string in UTF-16lsb
   * 0x0000 // separator 0x00000000 // item terminator 0x0000 // 4th item length
   * 
   * comment: -------- (0x0000) // length of comment string "Text" // comment
   * string in UTF-16lsb
   * 
   * footer: ------- 0x00000000 // file terminator
   * 
   * total_length = header_length + 2 + shi_length + 2 + comment_length +
   * footer_length = 76 + 2 + shi_length + 2 + comment_length + 4
   */
  /**
   * Generate a Windows link file to a web folder resource.
   * 
   * @param resName
   *          the name of the web folder resource
   * @param resURL
   *          the url of the web folder resource
   * @return the Windows link file
   */
  public static byte[] generateWindowsLink(String resName, String resURL) {
    resURL = "http://" + resURL;
    int resNameLength = resName.length(), resURLLength = resURL.length();
    int commentLength = COMMENT.length();
    int item3Length = 2 + 26 + 2 + resNameLength * 2 + 2 + 2 + resURLLength * 2 + 2 + 4;
    int shiLength = 20 + 20 + item3Length + 2;
    int length = 76 + 2 + shiLength + 2 + 2 * commentLength + 4;
    int pos = 0;
    byte buf[] = new byte[length];

    pos += append(buf, pos, new byte[] { 0x4c, 0x00, 0x00, 0x00, 0x01, 0x14, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46, (byte) 0x85, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });
    buf[pos++] = (byte) (shiLength & 0xff);
    buf[pos++] = (byte) (shiLength >> 8 & 0xff);
    pos += append(buf, pos, new byte[] { 0x14, 0x00, 0x1f, 0x50, (byte) 0xe0, 0x4f, (byte) 0xd0, 0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2, (byte) 0xd8, 0x08, 0x00, 0x2b, 0x30, 0x30, (byte) 0x9d, 0x14, 0x00, 0x2e, (byte) 0x80, 0x00, (byte) 0xdf, (byte) 0xea, (byte) 0xbd, 0x65, (byte) 0xc2, (byte) 0xd0, 0x11, (byte) 0xbc, (byte) 0xed, 0x00, (byte) 0xa0, (byte) 0xc9, 0x0a, (byte) 0xb5, 0x0f });
    buf[pos++] = (byte) (item3Length & 0xff);
    buf[pos++] = (byte) (item3Length >> 8 & 0xff);
    pos += append(buf, pos, new byte[] { 0x4c, 0x50, 0x00, 0x01, 0x42, 0x57, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00 });
    buf[pos++] = (byte) (resNameLength & 0xff);
    buf[pos++] = (byte) (resNameLength >> 8 & 0xff);
    try {
      pos += append(buf, pos, resName.getBytes("UTF-16LE"));
    } catch (UnsupportedEncodingException e) {
      assert false : "required encoding UTF-16LE not defined";
      return null;
    }
    buf[pos++] = 0;
    buf[pos++] = 0;
    buf[pos++] = (byte) (resURLLength & 0xff);
    buf[pos++] = (byte) (resURLLength >> 8 & 0xff);
    try {
      pos += append(buf, pos, resURL.getBytes("UTF-16LE"));
    } catch (UnsupportedEncodingException e1) {
      assert false : "required encoding UTF-16LE not defined";
      return null;
    }
    buf[pos++] = 0;
    buf[pos++] = 0;
    buf[pos++] = 0;
    buf[pos++] = 0;
    buf[pos++] = 0;
    buf[pos++] = 0;
    buf[pos++] = 0;
    buf[pos++] = 0;
    buf[pos++] = (byte) (commentLength & 0xff);
    buf[pos++] = (byte) (commentLength >> 8 & 0xff);
    try {
      pos += append(buf, pos, COMMENT.getBytes("UTF-16LE"));
    } catch (UnsupportedEncodingException e1) {
      assert false : "required encoding UTF-16LE not defined";
      return null;
    }
    buf[pos++] = 0;
    buf[pos++] = 0;
    buf[pos++] = 0;
    buf[pos++] = 0;
    return buf;
  }

  /**
   * Append some bytes to a buffer.
   * 
   * @param buf
   *          the buffer to append to
   * @param pos
   *          the current position in the buffer
   * @param data
   *          the bytes to append
   * @return the number of bytes appended
   */
  private static int append(byte buf[], int pos, byte data[]) {
    int len = data.length;
    System.arraycopy(data, 0, buf, pos, len);
    return len;
  }

  /*
   * A simple freedesktop.org .desktop file for a WebDAV resource:
   * 
   * [Desktop Entry] Encoding=UTF-8 Icon=unknown Name=Weblounge Repository on
   * www.test.org Type=Link URL=webdav://www.test.org/weblounge/repository/
   * Comment=Access the Weblounge Repository via DAV
   */
  /**
   * Generate a Freedesktop link file to a WebDAV resource.
   * 
   * @param resName
   *          the name of the WebDAV resource
   * @param resURL
   *          the url of the WebDAV resource
   * @return the Freedesktop link file
   */
  public static byte[] generateFreedesktopLink(String resName, String resURL) {
    StringBuffer sb = new StringBuffer();
    sb.append("[Desktop Entry]\n");
    sb.append("Encoding=UTF-8\n");
    sb.append("Icon=unknown\n");
    sb.append("Name=" + resName + "\n");
    sb.append("Type=Link\n");
    sb.append("URL=webdav://" + resURL + "\n");
    sb.append("Comment=" + COMMENT + "\n");
    try {
      return sb.toString().getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      assert false : "required encoding UTF-8 not defined";
      return null;
    }
  }

  /*
   * public static void main(String args[]) { try { FileOutputStream fos = new
   * FileOutputStream("/tmp/link.lnk");
   * fos.write(generateWindowsLink("Test Description",
   * "www/weblounge/repository")); fos.flush(); fos.close(); } catch
   * (IOException e) { System.err.println("Exception!");
   * e.printStackTrace(System.err); } try { FileOutputStream fos = new
   * FileOutputStream("/tmp/link.desktop");
   * fos.write(generateFreedesktopLink("Test Description",
   * "www/weblounge/repository")); fos.flush(); fos.close(); } catch
   * (IOException e) { System.err.println("Exception!");
   * e.printStackTrace(System.err); } }
   */
}
