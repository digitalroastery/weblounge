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

package ch.o2it.weblounge.common.content.page;

/**
 * This interface encapsulates the information to include a script within the
 * &lt;head&gt; section of an html page. Relative paths are interpreted to be
 * relative to the weblounge folder <code>shared</code>.
 */
public interface Script extends HTMLInclude {

  /** The default script <code>type</code> */
  String TYPE_JAVASCRIPT = "text/javascript";

  /**
   * Sets the script type, e. g. <code>text/javascript</code> for scripts
   * written in javascript.
   * 
   * @param type
   *          the script type
   */
  void setType(String type);

  /**
   * Returns the script type or language, e. g. <code>text/javascript</code> for
   * scripts written in javascript.
   * <p>
   * Note that there used to be an additional attribute named
   * <code>language</code> that served the same purpose. This attribute,
   * however, has been deprecated in favor of <code>type</code> by the W3C.
   * 
   * @return the script type
   */
  String getType();

  /**
   * Sets the script's character set.
   * 
   * @param charset
   *          the character set
   * @see http://www.w3.org/TR/REC-html40/references.html#ref-RFC2045
   */
  void setCharset(String charset);

  /**
   * Returns the <code>RFC 2045</code> character set of the script. The default
   * character set is <code>ISO-8859-1</code>.
   * 
   * @return the character set
   * @see http://www.w3.org/TR/REC-html40/references.html#ref-RFC2045
   */
  String getCharset();

  /**
   * Set to <code>true</code> if the script should only be executed after the
   * whole page has been loaded, <code>false</code> otherwise.
   * <p>
   * If set to <code>true</code>, the script indicates that it is not going to
   * write anything to the document using <code>document.write</code>. The
   * default value is <code>false</code>.
   * 
   * @param deferred
   *          the deferred state of the page
   */
  void setDeferred(boolean deferred);

  /**
   * Returns <code>true</code> if the script is only executed after the page has
   * been loaded.
   * 
   * @return <code>true</code> if the script is deferred
   */
  boolean isDeferred();

}