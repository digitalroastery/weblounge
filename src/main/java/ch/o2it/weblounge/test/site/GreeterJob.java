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

import ch.o2it.weblounge.common.scheduler.JobWorker;
import ch.o2it.weblounge.common.scheduler.JobException;
import ch.o2it.weblounge.test.util.TestSiteUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Dictionary;
import java.util.Map;

/**
 * Test job that will print a friendly greeting to <code>System.out</code>.
 */
@SuppressWarnings("unchecked")
public class GreeterJob implements JobWorker {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(GreeterJob.class);

  /** Hello world in many languages */
  protected static Map.Entry<String, String>[] greetings = null;

  static {
    Map<String, String> hellos = TestSiteUtils.loadGreetings();
    greetings = hellos.entrySet().toArray(new Map.Entry[hellos.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.scheduler.JobWorker#execute(java.lang.String,
   *      java.util.Dictionary)
   */
  public void execute(String name, Dictionary<String, Serializable> ctx)
      throws JobException {
    int index = (int) ((greetings.length - 1) * Math.random());
    Map.Entry<String, String> entry = greetings[index];
    try {
      logger.info(new String(entry.getValue().getBytes("utf-8")) + " (" + entry.getKey() + ")");
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
