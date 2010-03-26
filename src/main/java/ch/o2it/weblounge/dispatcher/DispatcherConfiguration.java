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

package ch.o2it.weblounge.dispatcher;

/**
 * This interface defines configuration values for the dispatcher.
 */
public interface DispatcherConfiguration {

  /** Property name for the web application context root (mountpoint) */
  public static final String WEBAPP_CONTEXT_ROOT = "weblounge.http.CONTEXT_ROOT";
  
  /** Property name for the bundles (sites) root path element */
  public static final String BUNDLE_CONTEXT_ROOT_URI = "weblounge.http.BUNDLE_CONTEXT_ROOT_URI";
  
  /**
   * Property name for the bundle context root path (including the webapp
   * context root)
   */
  public static final String BUNDLE_CONTEXT_ROOT = "weblounge.http.BUNDLE_CONTEXT_ROOT";
  
  /**
   * Property name for the bundle root path (including the webapp context root
   * and the bundle context root)
   */
  public static final String BUNDLE_ROOT = "weblounge.http.BUNDLE_ROOT";
  
  /** Property name for the bundle name */
  public static final String BUNDLE_NAME = "weblounge.http.BUNDLE_NAME";
  
  /** Property name for the bundle (site) path element */
  public static final String BUNDLE_URI = "weblounge.http.BUNDLE_URI";
  
  /** Property name for the entry path into the bundle resources */
  public static final String BUNDLE_ENTRY = "weblounge.http.BUNDLE_ENTRY";
  
  /** Semicolon delimited list of welcome files */
  public static final String WELCOME_FILES = "weblounge.http.WELCOME_FILES";

}
