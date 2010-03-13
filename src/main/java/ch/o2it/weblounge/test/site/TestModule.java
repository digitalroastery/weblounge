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

package ch.o2it.weblounge.test.site;

import ch.o2it.weblounge.common.impl.site.ModuleImpl;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.Module;

/**
 * Test implementation of the {@link Module} interface.
 */
public class TestModule extends ModuleImpl {

  /**
   * Creates a new test module.
   */
  public TestModule() {
    setIdentifier("test");
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.impl.site.ModuleImpl#getActions()
   */
  @Override
  public Action[] getActions() {
    Action action = new GreeterAction();
    action.setIdentifier("greeter");
    action.setPath("greeting");
    action.setModule(this);
    action.setSite(getSite());
    return new Action[] { action };
  }
  
}
