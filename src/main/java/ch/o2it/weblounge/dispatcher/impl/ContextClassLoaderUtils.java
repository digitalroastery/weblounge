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

package ch.o2it.weblounge.dispatcher.impl;

import java.util.concurrent.Callable;

/**
 * Utilities for class loading.
 */
public class ContextClassLoaderUtils {

  private ContextClassLoaderUtils() {
  }

  /**
   * Executes a piece of code (callable.call) using a specific class loader set
   * as context class loader. If the current thread context class loader is
   * already set, it will be restored after execution.
   * 
   * @param classLoader
   *          class loader to be used as context class loader during call.
   * @param callable
   *          piece of code to be executed using the class loader
   * @return return from callable
   * @throws Exception
   *           re-thrown from callable
   */
  public static <V> V doWithClassLoader(final ClassLoader classLoader,
      final Callable<V> callable) throws Exception {
    Thread currentThread = null;
    ClassLoader backupClassLoader = null;
    try {
      if (classLoader != null) {
        currentThread = Thread.currentThread();
        backupClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(classLoader);
      }
      return callable.call();
    } finally {
      if (backupClassLoader != null) {
        currentThread.setContextClassLoader(backupClassLoader);
      }
    }
  }

}
