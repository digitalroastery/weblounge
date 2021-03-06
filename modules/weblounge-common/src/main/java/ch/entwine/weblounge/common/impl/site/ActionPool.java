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

package ch.entwine.weblounge.common.impl.site;

import ch.entwine.weblounge.common.site.Action;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object pool for {@link Action} instances.
 */
public final class ActionPool extends GenericObjectPool<Action> {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ActionPool.class);
  
  /** The action name */
  private String actionName = null;
  
  /** The last reported limit */
  private int reportedLimit = 0;

  /**
   * Creates a new pool which will manage {@link Action} instances that are
   * created according to <code>configuration</code>.
   * 
   * @param action
   *          the action
   */
  public ActionPool(Action action) {
    super(new ActionPoolFactory(action), -1);

    if (action == null)
      throw new IllegalArgumentException("Action configuration must not be null");

    actionName = action.toString();

    setTestOnBorrow(false);
    setTestOnReturn(false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.impl.GenericObjectPool#borrowObject()
   */
  @Override
  public Action borrowObject() throws Exception {
    Action action = super.borrowObject();
    logger.debug("Received request to borrow action '{}', {} remaining", action.getIdentifier(), this.getNumIdle());

    if (getNumActive() > reportedLimit + 10) {
      reportedLimit += 10;
      logger.debug("Action pool '{}' grew above {}", new Object[] {
          action,
          reportedLimit }
      );
    } else {
      logger.debug("Action pool '{}' has {} members active, {} idle", new Object[] {
          action,
          this.getNumActive(),
          this.getNumIdle() });
    }
    return action;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.impl.GenericObjectPool#returnObject(java.lang.Object)
   */
  @Override
  public void returnObject(Action action) throws Exception {
    super.returnObject(action);
    logger.debug("Borrowed action '{}' returned to pool", action.getIdentifier());
    logger.debug("Action pool '{}' has {} members active, {} idle", new Object[] {
        action.getIdentifier(),
        this.getNumActive(),
        this.getNumIdle() });
    if (getNumActive() < reportedLimit - 10) {
      reportedLimit -= 10;
      logger.debug("Action pool '{}' dropped below {}", new Object[] {
          action,
          reportedLimit }
      );
    } else {
      logger.debug("Action pool '{}' has {} members active, {} idle", new Object[] {
          action,
          this.getNumActive(),
          this.getNumIdle() });
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.impl.GenericObjectPool#invalidateObject(java.lang.Object)
   */
  @Override
  public void invalidateObject(Action obj) throws Exception {
    Action action = super.borrowObject();
    logger.debug("Invalidating action '{}'", action.getIdentifier());
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
