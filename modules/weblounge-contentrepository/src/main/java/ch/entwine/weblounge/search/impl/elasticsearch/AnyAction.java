/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2014 The Weblounge Team
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
package ch.entwine.weblounge.search.impl.elasticsearch;

import ch.entwine.weblounge.common.impl.security.ActionImpl;
import ch.entwine.weblounge.common.security.Action;

/**
 * This is a convenience implementation for any action in either all or inside
 * of a given context.
 */
public class AnyAction extends ActionImpl {

  /**
   * Creates an action that reflects any possible action inside of any possible
   * context.
   */
  public AnyAction() {
    super(Action.CONTEXT_WILDCARD, Action.ACTION_WILDCARD);
  }

  /**
   * Creates an action that reflects any possible action inside of the given
   * context.
   * 
   * @param context
   *          the context identifier
   */
  public AnyAction(String context) {
    super(context, Action.ACTION_WILDCARD);
  }

}
