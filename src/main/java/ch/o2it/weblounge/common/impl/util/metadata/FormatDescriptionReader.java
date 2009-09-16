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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Read {@link FormatDescription}objects from a semicolon-separated text file.
 * 
 * @author Tobias Wunden
 * @version 1.0
 */
public class FormatDescriptionReader {

  private BufferedReader in;

  public FormatDescriptionReader(Reader reader) {
    in = new BufferedReader(reader);
  }

  public FormatDescription read() throws IOException {
    String line;
    do {
      line = in.readLine();
      if (line == null) {
        return null;
      }
    } while (line.length() < 1 || line.charAt(0) == '#');
    String[] items = line.split(";");
    FormatDescription desc = new FormatDescription();
    desc.setGroup(items[0]);
    desc.setShortName(items[1]);
    desc.setLongName(items[2]);
    desc.addMimeTypes(items[3]);
    desc.addFileExtensions(items[4]);
    desc.setOffset(new Integer(items[5]));
    desc.setMagicBytes(items[6]);
    desc.setMinimumSize(new Integer(items[7]));
    return desc;
  }

}