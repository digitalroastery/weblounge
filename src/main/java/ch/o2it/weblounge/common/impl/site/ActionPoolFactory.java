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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.site.ActionConfiguration;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

/**
 * The action pool factory will create action objects according to the action
 * configuration that is passed in at construction time.
 */
public class ActionPoolFactory extends BaseKeyedPoolableObjectFactory {

  /** The action configuration */
  protected ActionConfiguration configuration = null;

  /**
   * Creates a new action pool factory that will create action objects according
   * to the configuration.
   * 
   * @param configuration
   *          the action configuration
   */
  public ActionPoolFactory(ActionConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#makeObject(java.lang.Object)
   */
  @Override
  public Object makeObject(Object key) throws Exception {
    // TODO Create action handler with id "key"
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#activateObject(java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void activateObject(Object key, Object obj) throws Exception {
    // TODO Auto-generated method stub
    super.activateObject(key, obj);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#passivateObject(java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void passivateObject(Object key, Object obj) throws Exception {
    // TODO Auto-generated method stub
    super.passivateObject(key, obj);
  }

}
