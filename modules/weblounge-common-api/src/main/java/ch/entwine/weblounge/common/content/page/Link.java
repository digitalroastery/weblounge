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

package ch.entwine.weblounge.common.content.page;

/**
 * This class encapsulates the information to include a link element within the
 * &lt;head&gt; section of an <code>HTML</code> page.
 */
public interface Link extends HTMLInclude {

  /** The default link <code>type</code> */
  String TYPE_CSS = "text/css";

  /** The default link <code>media</code> */
  String MEDIA_ALL = "all";

  /** The default link <code>rel</code> */
  String REL_CSS = "stylesheet";

  /**
   * Sets the mime type for the link.
   * 
   * @param type
   *          the mime type
   */
  void setType(String type);

  /**
   * Returns the link mimetype.
   * 
   * @return the mime type
   */
  String getType();

  /**
   * Sets the link's character set.
   * 
   * @param charset
   *          the character set
   * @see http://www.w3.org/TR/REC-html40/references.html#ref-RFC2045
   */
  void setCharset(String charset);

  /**
   * Returns the <code>RFC 2045</code> character set of the link. The default
   * character set is <code>ISO-8859-1</code>.
   * 
   * @return the character set
   * @see http://www.w3.org/TR/REC-html40/references.html#ref-RFC2045
   */
  String getCharset();

  /**
   * Specifies on what device the linked document will be displayed. The
   * following media devices are defined as of <code>HTML 4.1</code>:
   * <ul>
   * <li>screen</li>
   * <li>tty</li>
   * <li>tv</li>
   * <li>projection</li>
   * <li>handheld</li>
   * <li>print</li>
   * <li>braille</li>
   * <li>aural</li>
   * <li>all</li>
   * </ul>
   * 
   * @param media
   *          the media
   */
  void setMedia(String media);

  /**
   * Returns the device on which the linked document will be displayed.
   * 
   * @return the device
   * @see #setMedia(String)
   */
  String getMedia();

  /**
   * Specifies the relationship between the current document and the linked
   * document. As of <code>HTML 4.1</code>, the following relationships are
   * defined:
   * <ul>
   * <li>alternate</li>
   * <li>appendix</li>
   * <li>bookmark</li>
   * <li>chapter</li>
   * <li>contents</li>
   * <li>copyright</li>
   * <li>glossary</li>
   * <li>help</li>
   * <li>home</li>
   * <li>index</li>
   * <li>next</li>
   * <li>prev</li>
   * <li>section</li>
   * <li>start</li>
   * <li>stylesheet</li>
   * <li>subsection</li>
   * </ul>
   * 
   * @param relation
   */
  void setRelation(String relation);

  /**
   * Returns the relationship between the current and the linked document.
   * 
   * @return the relationship
   * @see #setRelation(String)
   */
  String getRelation();

  /**
   * Specifies the relationship between the linked document and the current one.
   * As of <code>HTML 4.1</code>, the following relationships are
   * defined:
   * <ul>
   * <li>alternate</li>
   * <li>appendix</li>
   * <li>bookmark</li>
   * <li>chapter</li>
   * <li>contents</li>
   * <li>copyright</li>
   * <li>glossary</li>
   * <li>help</li>
   * <li>home</li>
   * <li>index</li>
   * <li>next</li>
   * <li>prev</li>
   * <li>section</li>
   * <li>start</li>
   * <li>stylesheet</li>
   * <li>subsection</li>
   * </ul>
   * 
   * @param relation
   */
  void setReverseRelation(String relation);

  /**
   * Returns the relationship between the linked document and the current one.
   * 
   * @return the relationship
   * @see #setRelation(String)
   */
  String getReverseRelation();

}
