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
 * The HTMLEncoding encodes any text such that it may be displayed in an HTML
 * browser. This content encoder supports the character set ISO 8859-1 (Latin
 * 1).
 */
public class HTMLEncoding implements Encoding {

  /** Static instance */
  private static HTMLEncoding encoder_ = new HTMLEncoding();

  /** the encoding dictionary */
  private static Map<Character, String> encodingDict_ = new HashMap<Character, String>();

  /** the decoding dictionary */
  private static Map<String, Character> decodingDict_ = new HashMap<String, Character>();

  /**
   * Creates a new instance of HTMLEncoding.
   */
  public HTMLEncoding() { }

  /**
   * Encodes a text for proper display within an HTML browser.
   * 
   * @param text
   *          the element to be encoded
   * @return the encoded text
   */
  public String encode(String text) {
    return encode(new StringBuffer(text)).toString();
  }

  /**
   * Encodes a text for proper display within an HTML browser.
   * 
   * @param text
   *          the element to be encoded
   * @return the encoded text
   */
  public StringBuffer encode(StringBuffer text) {
    StringBuffer result = new StringBuffer("");
    for (int i = 0; i < text.length(); i++) {
      Character key = new Character(text.charAt(i));
      String replacement = encodingDict_.get(key);
      if (replacement != null) {
        for (int j = 0; j < replacement.length(); j++) {
          result.append(new Character(replacement.charAt(j)));
        }
      } else {
        result.append(key);
      }
    }
    return result;
  }

  /**
   * Decodes a text from html.
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
   * Static version of the HTML encoder.
   * 
   * @param text
   *          the input text
   * @return the html encoded text
   */
  public static String toHTML(String text) {
    return encoder_.encode(text);
  }

  /**
   * Static version of the HTML decoder.
   * 
   * @param text
   *          the input text
   * @return the html decoded text
   */
  public static String fromHTML(String text) {
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

    // punctuation signs

    register(new Character((char) 60), "&lt;");
    register(new Character((char) 62), "&gt;");
    register(new Character((char) 34), "&quot;");
    register(new Character((char) 39), "&apos;");
    register(new Character((char) 38), "&amp;");

    // special HTML charcters

    register(new Character('\n'), "<br />");

    // ISO 8859-1 (Latin 1)

    register(new Character((char) 160), "&nbsp;");
    register(new Character((char) 161), "&iexcl;");
    register(new Character((char) 162), "&cent;");
    register(new Character((char) 163), "&pound;");
    register(new Character((char) 164), "&curren;");
    register(new Character((char) 165), "&yen;");
    register(new Character((char) 166), "&brvbar;");
    register(new Character((char) 167), "&sect;");
    register(new Character((char) 168), "&uml;");
    register(new Character((char) 169), "&copy;");
    register(new Character((char) 170), "&ordf;");
    register(new Character((char) 171), "&laquo;");
    register(new Character((char) 172), "&not;");
    register(new Character((char) 173), "&shy;");
    register(new Character((char) 174), "&reg;");
    register(new Character((char) 175), "&macr;");
    register(new Character((char) 176), "&deg;");
    register(new Character((char) 177), "&plusmn;");
    register(new Character((char) 178), "&sup2;");
    register(new Character((char) 179), "&sup3;");
    register(new Character((char) 180), "&acute;");
    register(new Character((char) 181), "&micro;");
    register(new Character((char) 182), "&para;");
    register(new Character((char) 183), "&middot;");
    register(new Character((char) 184), "&cedil;");
    register(new Character((char) 185), "&sup1;");
    register(new Character((char) 186), "&ordm;");
    register(new Character((char) 187), "&raquo;");
    register(new Character((char) 188), "&frac14;");
    register(new Character((char) 189), "&frac12;");
    register(new Character((char) 190), "&frac34;");
    register(new Character((char) 191), "&iquest;");
    register(new Character((char) 192), "&Agrave;");
    register(new Character((char) 193), "&Aacute;");
    register(new Character((char) 194), "&Acirc;");
    register(new Character((char) 195), "&Atilde;");
    register(new Character((char) 196), "&Auml;");
    register(new Character((char) 197), "&Aring;");
    register(new Character((char) 198), "&AElig;");
    register(new Character((char) 199), "&Ccedil;");
    register(new Character((char) 200), "&Egrave;");
    register(new Character((char) 201), "&Eacute;");
    register(new Character((char) 202), "&Ecirc;");
    register(new Character((char) 203), "&Euml;");
    register(new Character((char) 204), "&Igrave;");
    register(new Character((char) 205), "&Iacute;");
    register(new Character((char) 206), "&Icirc;");
    register(new Character((char) 207), "&Iuml;");
    register(new Character((char) 208), "&ETH;");
    register(new Character((char) 209), "&Ntilde;");
    register(new Character((char) 210), "&Ograve;");
    register(new Character((char) 211), "&Oacute;");
    register(new Character((char) 212), "&Ocirc;");
    register(new Character((char) 213), "&Otilde;");
    register(new Character((char) 214), "&Ouml;");
    register(new Character((char) 215), "&times;");
    register(new Character((char) 216), "&Oslash;");
    register(new Character((char) 217), "&Ugrave;");
    register(new Character((char) 218), "&Uacute;");
    register(new Character((char) 219), "&Ucirc;");
    register(new Character((char) 220), "&Uuml;");
    register(new Character((char) 221), "&Yacute;");
    register(new Character((char) 222), "&THORN;");
    register(new Character((char) 223), "&szlig;");
    register(new Character((char) 224), "&agrave;");
    register(new Character((char) 225), "&aacute;");
    register(new Character((char) 226), "&acirc;");
    register(new Character((char) 227), "&atilde;");
    register(new Character((char) 228), "&auml;");
    register(new Character((char) 229), "&aring;");
    register(new Character((char) 230), "&aelig;");
    register(new Character((char) 231), "&ccedil;");
    register(new Character((char) 232), "&egrave;");
    register(new Character((char) 233), "&eacute;");
    register(new Character((char) 234), "&ecirc;");
    register(new Character((char) 235), "&euml;");
    register(new Character((char) 236), "&igrave;");
    register(new Character((char) 237), "&iacute;");
    register(new Character((char) 238), "&icirc;");
    register(new Character((char) 239), "&iuml;");
    register(new Character((char) 240), "&eth;");
    register(new Character((char) 241), "&ntilde;");
    register(new Character((char) 242), "&ograve;");
    register(new Character((char) 243), "&oacute;");
    register(new Character((char) 244), "&ocirc;");
    register(new Character((char) 245), "&otilde;");
    register(new Character((char) 246), "&ouml;");
    register(new Character((char) 247), "&divide;");
    register(new Character((char) 248), "&oslash;");
    register(new Character((char) 249), "&ugrave;");
    register(new Character((char) 250), "&uacute;");
    register(new Character((char) 251), "&ucirc;");
    register(new Character((char) 252), "&uuml;");
    register(new Character((char) 253), "&yacute;");
    register(new Character((char) 254), "&thorn;");
    register(new Character((char) 255), "&yuml;");
  }

}