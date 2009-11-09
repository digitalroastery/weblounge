/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.util.classloader;

import ch.o2it.weblounge.common.impl.url.PathSupport;
import ch.o2it.weblounge.common.impl.util.Env;

import java.io.File;

/**
 * SiteClassLoader, used to load module classes.
 */
public class SiteClassLoader extends ECPClassLoader {

  /** the parent class loader */
  private WebloungeClassLoader parent;

  /** the module base directory */
  private String moduleBase;

  /** the shared module base directory */
  private static String sharedModuleBase;

  static {
    sharedModuleBase = PathSupport.trim(Env.getRealPath("/shared/module"));
  }

  /**
   * Creates a new <code>SiteClassLoader</code> using the <code>
	* SharedClassLoader</code>
   * as its parent.
   * 
   * @param siteBase
   *          the site base directory
   * @param moduleBase
   *          the site module base directory
   */
  public SiteClassLoader(String siteBase, String moduleBase) {
    super(WebloungeClassLoader.getInstance());
    parent = WebloungeClassLoader.getInstance();
    setSiteBase(siteBase);
    setSiteModuleBase(moduleBase);
  }

  /**
   * Add a new module to the configuration.
   * 
   * @param moduleName
   *          the module name (case sensitive)
   */
  public void addModule(String moduleName) {
    if (moduleBase != null) {
      addExtendedClassPath(PathSupport.concat(new String[] { moduleBase, moduleName }));
    }
  }

  /**
   * Add a new shared module to the configuration.
   * 
   * @param moduleName
   *          the module name (case sensitive)
   */
  public void addSharedModule(String moduleName) {
    addExtendedClassPath(PathSupport.concat(new String[] { sharedModuleBase, moduleName }));
  }

  /**
   * @see ch.o2it.weblounge.common.impl.util.classloader.ECPClassLoader#addExtendedClassPath(java.lang.String)
   */
  @Override
  public void addExtendedClassPath(String cp) {
    parent.addExtendedClassPath(cp);
  }

  /**
   * Sets the base directory for modules.
   * 
   * @param base
   *          the base directory for the modules
   * @throws NullPointerException
   *           if base == <code>null</code>
   * @throws IllegalArgumentException
   *           if base does not exist
   */
  protected void setSiteBase(String base) {
    if (base == null)
      throw new IllegalArgumentException("Site base directory may not be null!");
    if (!base.endsWith(File.separator)) {
      base += File.separatorChar;
    }
    addExtendedClassPath(base);
  }

  /**
   * Sets the base directory for modules.
   * 
   * @param base
   *          the base directory for the modules
   * @throws NullPointerException
   *           if base == <code>null</code>
   * @throws IllegalArgumentException
   *           if base does not exist
   */
  protected void setSiteModuleBase(String base) {
    if (base == null)
      throw new NullPointerException();
    File f = new File(base);
    if (f.exists() && f.isDirectory() && f.canRead()) {
      moduleBase = base;
      if (!moduleBase.endsWith(File.separator)) {
        moduleBase += File.separatorChar;
      }
    }
  }

}