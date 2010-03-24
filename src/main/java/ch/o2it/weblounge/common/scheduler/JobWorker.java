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

package ch.o2it.weblounge.common.scheduler;

import java.io.Serializable;
import java.util.Dictionary;

/**
 * A <code>Job</code> provides an implementation of the work that needs to be
 * done as well as a list of triggers that determine when job execution is due.
 */
public interface JobWorker {

  /** For site and module jobs, the <code>Site</code> is part of the context */
  String CTXT_SITE = "webl:site";

  /**
   * This method is called every time the job execution triggers fire. The
   * context dictionary passed to this method contains both the configuration
   * data found at instantiation time as well as any data that previous
   * executions might have added to it.
   * 
   * @param name
   *          the job name
   * @param ctx
   *          the job context
   * @throws JobException
   *           if job execution fails
   */
  void execute(String name, Dictionary<String, Serializable> ctx) throws JobException;

}
