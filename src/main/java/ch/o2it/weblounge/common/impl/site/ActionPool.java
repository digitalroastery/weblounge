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

import ch.o2it.weblounge.common.site.Action;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object pool for {@link Action} instances.
 */
public final class ActionPool extends GenericObjectPool {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(ActionPool.class);
  
  /** The action name */
  private String actionName = null;

  /**
   * Creates a new pool which will manage {@link Action} instances that are
   * created according to <code>configuration</code>.
   * 
   * @param action
   *          the action
   */
  public ActionPool(Action action) {
    if (action == null)
      throw new IllegalArgumentException("Action configuration must not be null");
    actionName = action.toString();
    setFactory(new ActionPoolFactory(action));
    setTestOnBorrow(false);
    setTestOnReturn(false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.impl.GenericObjectPool#borrowObject()
   */
  @Override
  public Object borrowObject() throws Exception {
    Action action = (Action) super.borrowObject();
    log_.debug("Received request to borrow action '{}', {} remaining", action.getIdentifier(), this.getNumIdle());
    log_.debug("Action pool '{}' has {} members active, {} idle", new Object[] {
        action.getIdentifier(),
        this.getNumActive(),
        this.getNumIdle() });
    return action;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.impl.GenericObjectPool#returnObject(java.lang.Object)
   */
  @Override
  public void returnObject(Object obj) throws Exception {
    Action action = (Action) super.borrowObject();
    log_.debug("Borrowed action '{}' returned to pool", action.getIdentifier());
    log_.debug("Action pool '{}' has {} members active, {} idle", new Object[] {
        action.getIdentifier(),
        this.getNumActive(),
        this.getNumIdle() });
    super.returnObject(obj);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.impl.GenericObjectPool#invalidateObject(java.lang.Object)
   */
  @Override
  public void invalidateObject(Object obj) throws Exception {
    Action action = (Action) super.borrowObject();
    log_.debug("Invalidating action '{}'", action.getIdentifier());
    super.invalidateObject(obj);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "action pool [" + actionName + "]";
  }

}
