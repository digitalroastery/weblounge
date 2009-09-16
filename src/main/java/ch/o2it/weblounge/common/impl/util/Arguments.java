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

/**
 * @author Tobias Wunden
 * @version Jul 31, 2003
 * 
 *          This class contains a collection of useful methods that are being
 *          used many times throughout a larger programming project.
 */

public final class Arguments {

  /**
   * Utility method used for <code>null</code> argument checking. The method
   * throws a <code>IllegalArgumentException</code> if <code>argument</code> is
   * null.
   * <p>
   * The exception message is:
   * 
   * <pre>
   * 	&lt;tt&gt;Argument &lt;i&gt;name&lt;/i&gt; must not be null!&lt;/tt&gt;
   * </pre>
   * 
   * @param argument
   *          the argument
   * @param name
   *          the argument name
   * @throws IllegalArgumentException
   *           if <code>argument</code> is <code>null</code>
   */
  public static void checkNull(Object argument, String name) {
    if (argument == null) {
      String s = "Argument '" + name + "' must not be null!";
      throw new IllegalArgumentException(s);
    }
  }

  /**
   * Utility method used for general argument checking. An
   * <code>IllegalArgumentException</code> is thrown if <code>condition</code>
   * does not hold.
   * <p>
   * The exception message is:
   * 
   * <pre>
   * 	&lt;tt&gt;Argument &lt;i&gt;name&lt;/i&gt; does not meet requirements!&lt;/tt&gt;
   * </pre>
   * 
   * @param condition
   *          the condition that must hold
   * @param name
   *          the argument name
   * @throws IllegalArgumentException
   *           if <code>condition</code> is not <code>true</code>
   */
  public static void check(boolean condition, String name) {
    if (!condition) {
      String s = "Argument '" + name + "' does not meet requirements!";
      throw new IllegalArgumentException(s);
    }
  }

  /**
   * Utility method used for argument bounds checking. An
   * <code>IndexOutOfBoundsException</code> is thrown if <code>condition</code>
   * does not hold.
   * <p>
   * The exception message is:
   * 
   * <pre>
   * 	&lt;tt&gt;Argument &lt;i&gt;name&lt;/i&gt; is out of bounds!&lt;/tt&gt;
   * </pre>
   * 
   * @param condition
   *          the condition that must hold
   * @param name
   *          the argument name
   * @throws IndexOutOfBoundsException
   *           if <code>condition</code> is not <code>true</code>
   */
  public static void checkBounds(boolean condition, String name) {
    if (!condition) {
      String s = "Argument '" + name + "' is out of bounds!";
      throw new IndexOutOfBoundsException(s);
    }
  }

  /**
   * Utility method used for argument checking. An
   * <code>IllegalArgumentException</code> is thrown if <code>condition</code>
   * does not hold.
   * <p>
   * The exception message is:
   * 
   * <pre>
   * 	&lt;tt&gt;Value of argument &lt;i&gt;name&lt;/i&gt; is unknown!&lt;/tt&gt;
   * </pre>
   * 
   * @param condition
   *          the condition that must hold
   * @param name
   *          the argument name
   * @throws IllegalArgumentException
   *           if <code>condition</code> is not <code>true</code>
   */
  public static void checkValue(boolean condition, String name) {
    if (!condition) {
      String s = "Value of argument '" + name + "' is does not satisfy condition!";
      throw new IllegalArgumentException(s);
    }
  }

  /**
   * Utility method used for argument checking. An
   * <code>IllegalArgumentException</code> is thrown if <code>condition</code>
   * does not hold.
   * <p>
   * The exception message is:
   * 
   * <pre>
   * 	&lt;tt&gt;Value &lt;i&gt;value&lt;/i&gt; of argument &lt;i&gt;name&lt;/i&gt; is unknown!&lt;/tt&gt;
   * </pre>
   * 
   * @param condition
   *          the condition that must hold
   * @param name
   *          the argument name
   * @param value
   *          the argument value
   * @throws IllegalArgumentException
   *           if <code>condition</code> is not <code>true</code>
   */
  public static void checkValue(boolean condition, String name, Object value) {
    if (!condition) {
      String s = "Value '" + value + "' of argument '" + name + "' is unknown!";
      throw new IllegalArgumentException(s);
    }
  }
}