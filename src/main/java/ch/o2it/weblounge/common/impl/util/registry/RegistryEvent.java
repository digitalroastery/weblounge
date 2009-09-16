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

package ch.o2it.weblounge.common.impl.util.registry;

import java.util.EventObject;

/**
 * This event is created by a <code>Registry</code> and is passed to the
 * registered <code>RegistryListener</code>s.
 * 
 * @author Tobias Wunden
 * @version Jun 5, 2003
 */

public class RegistryEvent extends EventObject {

  /** Serial version UID */
  private static final long serialVersionUID = -6240990207250195768L;

  public static final int ADD = 0;

  public static final int REMOVE = 1;

  public static final int CLEAR = 2;

  /** the key of the changed object */
  private Object object_;

  /** the mode */
  private int mode_;

  /**
   * Creates a new RegistryEvent.
   * 
   * @param registry
   *          the registry that fired the event
   * @param object
   *          the object that caused this event
   * @param mode
   *          the mode
   */
  public RegistryEvent(Registry registry, Object object, int mode) {
    super(registry);
    object_ = object;
    mode_ = mode;
  }

  /*
   * ------------------------------------------------------------- P U B L I C M
   * E T H O D S -------------------------------------------------------------
   */

  /**
   * Returns the object that changed. This method returns <code>null</code> if
   * the event represents a <code>clear()</code> event on the registry.
   * 
   * @return the object that changed
   */
  public Object getObject() {
    return object_;
  }

  /**
   * Returns the mode of the change. The mode is either {@link #ADD} or
   * {@link #REMOVE}.
   * 
   * @return the mode
   */
  public int getMode() {
    return mode_;
  }

}