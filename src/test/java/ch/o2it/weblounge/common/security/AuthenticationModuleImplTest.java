/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.o2it.weblounge.common.impl.security.jaas.AuthenticationModuleImpl;
import ch.o2it.weblounge.common.security.AuthenticationModule.Relevance;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link AuthenticationModuleImpl}.
 */
public class AuthenticationModuleImplTest {

  /** The authentication module under test */
  protected AuthenticationModule module = null;
  
  /** The admin login module implementation */ 
  protected String adminLoginModuleClass = "ch.o2it.weblounge.common.impl.security.jaas.AdminLoginModule";

  /** The admin login module relevance */ 
  protected Relevance adminLoginModuleRelevance = Relevance.sufficient;
  
  /** Name of the option key */
  protected String optionKey = "key";

  /** Option value */
  protected String optionValue = "value";

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    module = new AuthenticationModuleImpl(adminLoginModuleClass, adminLoginModuleRelevance);
    module.setOption(optionKey, optionValue);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.security.jaas.AuthenticationModuleImpl#getModuleClass()}.
   */
  @Test
  public void testGetModuleClass() {
    assertEquals(adminLoginModuleClass, module.getModuleClass());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.security.jaas.AuthenticationModuleImpl#getRelevance()}.
   */
  @Test
  public void testGetRelevance() {
    assertEquals(adminLoginModuleRelevance, module.getRelevance());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.security.jaas.AuthenticationModuleImpl#getOptionValue(java.lang.String)}
   * .
   */
  @Test
  public void testGetOptionValueString() {
    assertEquals(optionValue, module.getOptionValue(optionKey));
    assertTrue(module.getOptionValue("test") == null);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.security.jaas.AuthenticationModuleImpl#getOptionValue(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testGetOptionValueStringString() {
    assertEquals(optionValue, module.getOptionValue(optionKey, "abc"));
    assertEquals(optionValue, module.getOptionValue("abc", optionValue));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.security.jaas.AuthenticationModuleImpl#getOptionValues(java.lang.String)}
   * .
   */
  @Test
  public void testGetOptionValues() {
    assertEquals(1, module.getOptionValues(optionKey).length);
    assertEquals(0, module.getOptionValues("test").length);
    module.setOption(optionKey, optionValue);
    assertEquals(1, module.getOptionValues(optionKey).length);
    module.setOption(optionKey, "abc");
    assertEquals(2, module.getOptionValues(optionKey).length);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.security.jaas.AuthenticationModuleImpl#getOptions()}.
   */
  @Test
  public void testGetOptions() {
    assertEquals(1, module.getOptions().size());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.security.jaas.AuthenticationModuleImpl#hasOption(java.lang.String)}
   * .
   */
  @Test
  public void testHasOption() {
    assertTrue(module.hasOption(optionKey));
    assertFalse(module.hasOption("test"));
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.security.jaas.AuthenticationModuleImpl#removeOption(java.lang.String)}
   * .
   */
  @Test
  public void testRemoveOption() {
    module.removeOption(optionKey);
    assertFalse(module.hasOption(optionKey));
  }

}
