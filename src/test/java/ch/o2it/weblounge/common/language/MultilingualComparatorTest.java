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

package ch.o2it.weblounge.common.language;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.language.MultilingualComparator;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * This test suite tests the implementation of {@link MultilingualComparator}.
 */
public class MultilingualComparatorTest {
  
  /** The comparator to test */
  protected MultilingualComparator<Localizable> englishComparator = null;

  /** The comparator to test */
  protected MultilingualComparator<Localizable> germanComparator = null;

  /** The comparator to test */
  protected MultilingualComparator<Localizable> frenchComparator = null;

  /** The list of localizable objects */
  protected List<Localizable> localizables = null;
  
  /** The localizable that will be first when sorted in English */
  protected LocalizableContent<String> englishFirst = null;

  /** The localizable that will be first when sorted in German */
  protected LocalizableContent<String> germanFirst = null;

  /** The English language */
  protected final Language english = new LanguageImpl(new Locale("en"));

  /** The German language */
  protected final Language german = new LanguageImpl(new Locale("de"));

  /** The French language */
  protected final Language french = new LanguageImpl(new Locale("fr"));

  /** The language */
  protected Language language = null;
  
  /**
   * Prepares a collection of localizable objects.
   * 
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    englishComparator = new MultilingualComparator<Localizable>(english);
    germanComparator = new MultilingualComparator<Localizable>(german);
    frenchComparator = new MultilingualComparator<Localizable>(french);

    localizables = new ArrayList<Localizable>();
    
    englishFirst = new LocalizableContent<String>();
    englishFirst.put("art", english);
    englishFirst.put("kunst", german);
    localizables.add(englishFirst);

    germanFirst = new LocalizableContent<String>();
    germanFirst.put("deutsch", english);
    germanFirst.put("german", german);
    localizables.add(germanFirst);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.MultilingualComparator#getLanguage()}.
   */
  @Test
  public void testGetLanguage() {
    assertEquals(english, englishComparator.getLanguage());
    assertEquals(german, germanComparator.getLanguage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.MultilingualComparator#setLanguage(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testSetLanguage() {
    englishComparator.setLanguage(french);
    assertEquals(french, englishComparator.getLanguage());
    try {
      englishComparator.setLanguage(null);
      fail("Setting the language to null should not be allowed");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.language.MultilingualComparator#compare(ch.o2it.weblounge.common.language.Localizable, ch.o2it.weblounge.common.language.Localizable)}.
   */
  @Test
  public void testCompare() {
    Collections.sort(localizables, englishComparator);
    assertEquals(englishFirst, localizables.get(0));
    Collections.sort(localizables, germanComparator);
    assertEquals(germanFirst, localizables.get(0));
    try {
      Collections.sort(localizables, frenchComparator);
    } catch (Exception e) {
      fail("Sorting with an unknown language failed");
    }
  }

}
