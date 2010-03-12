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

import ch.o2it.weblounge.common.scheduler.Job;
import ch.o2it.weblounge.common.scheduler.JobException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * Test job that will print a friendly greeting to <code>System.out</code>.
 */
@SuppressWarnings("unchecked")
public class GreeterJob implements Job {

  /** Logging facility */
  protected final static Logger logger = LoggerFactory.getLogger(GreeterJob.class);

  /** Name of the properties file that defines the greetings */
  public static final String GREETING_PROPS = "greetings.properties";

  /** Hello world in many languages */
  protected static Map.Entry<String, String>[] greetings = null;

  static {
    Map<String, String> hellos = new HashMap<String, String>();
    Properties props = new Properties();
    try {
      props.load(GreeterAction.class.getResourceAsStream(GREETING_PROPS));
      for (Entry<Object, Object> entry : props.entrySet()) {
        hellos.put((String)entry.getKey(), (String)entry.getValue());
      }
    } catch (IOException e) {
      logger.error("Error loading greetings from {}", GREETING_PROPS, e);
    }
    greetings = hellos.entrySet().toArray(new Map.Entry[hellos.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.scheduler.Job#execute(java.lang.String,
   *      java.util.Dictionary)
   */
  public void execute(String name, Dictionary<String, Serializable> ctx)
      throws JobException {
    int index = (int) ((greetings.length - 1)* Math.random());
    Map.Entry<String, String> entry = greetings[index];
    try {
      logger.info(new String(entry.getValue().getBytes("UTF-8")) + " (" + entry.getKey() + ")");
    } catch (UnsupportedEncodingException e) {
      logger.error("Cant' believe that utf-8 is not supported on this platform!", e);
    }
  }

}
