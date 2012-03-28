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

package ch.entwine.weblounge.workbench.suggest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the implementation at {@link SuggestionBase}.
 */
public class SuggestionBaseTest {

  /** The suggestion under test */
  protected SuggestionBase suggestion = null;

  /** The hint */
  protected String hint = "llo";

  /** The original text */
  protected String text = "Yellow melLow";

  /** The delimiter used for highlighting */
  protected String delimiter = "b";

  protected String highlightedText = "Ye<b>llo</b>w me<b>lLo</b>w";

  /**
   * Test setup.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    suggestion = new SuggestionBase() {
      public String toXml(String hint, String delimiter) {
        return "<hello-world/>";
      }
    };
  }

  /**
   * Test method for
   * {@link ch.entwine.weblounge.workbench.suggest.SuggestionBase#highlight(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testHighlight() {
    assertEquals(highlightedText, suggestion.highlight(text, hint, delimiter));
    assertEquals(text, suggestion.highlight(text, hint, null));
    assertNull(suggestion.highlight(null, hint, delimiter));
  }

}
