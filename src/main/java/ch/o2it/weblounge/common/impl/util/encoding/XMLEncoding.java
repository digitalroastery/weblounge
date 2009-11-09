/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.util.encoding;

import java.util.HashMap;
import java.util.Map;

/**
 * The XMLEncoding encodes any text such that it may be stored as part of an xml
 * document. This mainly means encoding &lt;, &gt;, &amp; and &quot;.
 */
public class XMLEncoding implements Encoding {

  /** Static instance */
  private static XMLEncoding encoder_ = new XMLEncoding();

  /** the encoding dictionary */
  private static Map<Character, String> encodingDict_ = new HashMap<Character, String>();

  /** the decoding dictionary */
  private static Map<String, Character> decodingDict_ = new HashMap<String, Character>();

  /**
   * Creates a new instance of XMLEncoding.
   */
  public XMLEncoding() {
  }

  /**
   * Encodes a text for proper storage within an xml document
   * 
   * @param text
   *          the element to be encoded
   * @return the encoded text
   */
  public String encode(String text) {
    StringBuffer original = new StringBuffer(text);
    StringBuffer result = new StringBuffer("");
    for (int i = 0; i < original.length(); i++) {
      Character key = new Character(original.charAt(i));
      String replacement = encodingDict_.get(key);
      if (replacement != null) {
        for (int j = 0; j < replacement.length(); j++) {
          result.append(new Character(replacement.charAt(j)));
        }
      } else {
        result.append(key);
      }
    }
    return result.toString();
  }

  /**
   * Static version of the XML encoder.
   * 
   * @param text
   *          the input text
   * @return the xml encoded text
   */
  public static String toXML(String text) {
    return encoder_.encode(text);
  }

  /**
   * Decodes a text from xml decoding.
   * 
   * @param text
   *          the element to be decoded
   * @return the decoded text
   */
  public String decode(String text) {
    StringBuffer original = new StringBuffer(text);
    StringBuffer result = new StringBuffer("");
    StringBuffer buf = null;
    Character amp = new Character('&');

    for (int i = 0; i < original.length(); i++) {
      Character key = new Character(original.charAt(i));
      if (key.equals(amp)) {
        buf = new StringBuffer("&");
        continue;
      }
      if (buf != null) {
        buf.append(key);
        if (decodingDict_.get(buf.toString()) != null) {
          result.append(decodingDict_.get(buf.toString()));
          buf = null;
          continue;
        } else if (buf.length() > 6) {
          result.append(buf);
          buf = null;
        }
      } else {
        result.append(key);
      }
    }
    return result.toString();
  }

  /**
   * Static version of the XML decoder.
   * 
   * @param text
   *          the input text
   * @return the xml decoded text
   */
  public static String fromXML(String text) {
    return encoder_.decode(text);
  }

  /**
   * Registers the combination with encoding and decoding dictionary.
   * 
   * @param c
   *          the character
   * @param s
   *          the corresponding string
   */
  private static void register(Character c, String s) {
    encodingDict_.put(c, s);
    decodingDict_.put(s, c);
  }

  static {
    register(new Character('<'), "&lt;");
    register(new Character('>'), "&gt;");
    register(new Character('\"'), "&quot;");
    register(new Character('&'), "&amp;");
  }

}