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

import java.util.ArrayList;
import java.util.List;

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
    List<Action> actions = new ArrayList<Action>();
    
    // HTML greeter action
    Action greeterHTMLAction = new GreeterHTMLAction();
    greeterHTMLAction.setIdentifier("greeter.html");
    greeterHTMLAction.setPath("greeting");
    greeterHTMLAction.setModule(this);
    greeterHTMLAction.setSite(getSite());
    actions.add(greeterHTMLAction);

    // XML greeter action
    Action greeterXMLAction = new GreeterXMLAction();
    greeterXMLAction.setIdentifier("greeter.xml");
    greeterXMLAction.setPath("greeting");
    greeterXMLAction.setModule(this);
    greeterXMLAction.setSite(getSite());
    actions.add(greeterXMLAction);

    // XML greeter action
    Action greeterJSONAction = new GreeterJSONAction();
    greeterJSONAction.setIdentifier("greeter.json");
    greeterJSONAction.setPath("greeting");
    greeterJSONAction.setModule(this);
    greeterJSONAction.setSite(getSite());
    actions.add(greeterJSONAction);

    return actions.toArray(new Action[actions.size()]);
  }
  
}
