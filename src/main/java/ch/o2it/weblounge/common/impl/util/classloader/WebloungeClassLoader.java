/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util.classloader;

import java.io.File;

/**
 * WebloungeClassLoader
 * 
 * NOTE: This class MUST NOT depend on any weblounge classes except
 * ECPClassLoader! This class MUST NOT depend on any weblounge classes except
 * ECPClassLoader! This class MUST NOT depend on any weblounge classes except
 * ECPClassLoader! This class MUST NOT depend on any weblounge classes except
 * ECPClassLoader! This class MUST NOT depend on any weblounge classes except
 * ECPClassLoader! This class MUST NOT depend on any weblounge classes except
 * ECPClassLoader! This class MUST NOT depend on any weblounge classes except
 * ECPClassLoader! This class MUST NOT depend on any weblounge classes except
 * ECPClassLoader!
 * 
 * @version $Revision: 1059 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */
public class WebloungeClassLoader extends ECPClassLoader {

  /** the only instance of <code>WebloungeClassLoader</code> */
  private static WebloungeClassLoader loader;

  /**
   * Creates a new <code>WebloungeClassLoader</code> with the class loader that
   * loaded this class as its parent.
   * 
   * @param base
   *          the phisical base directory of the weblounge installation
   */
  private WebloungeClassLoader(String base) {
    super(WebloungeClassLoader.class.getClassLoader());
    addExtendedClassPath(base + File.separatorChar + "shared");
    addClassPathElement(base + File.separatorChar + "classes" + File.separatorChar);
    addLibRepository(base + File.separatorChar + "lib" + File.separatorChar + "endorsed");
    addLibRepository(base + File.separatorChar + "lib" + File.separatorChar + "core");
    addLibRepository(base + File.separatorChar + "lib" + File.separatorChar + "optional");
  }

  /**
   * <code>WebloungeClassLoader</code> is a singleton. Use this method to get
   * the single instance of this class loader.
   * 
   * @return a <code>WebloungeClassLoader</code> instance
   */
  public static WebloungeClassLoader getInstance() {
    return loader;
  }

  /**
   * This methos is needed during bootstrapping.
   * 
   * @param base
   *          the phisical base directory of the weblounge installation
   * @return a <code>WebloungeClassLoader</code> instance
   */
  public static WebloungeClassLoader getInstance(String base) {
    if (loader == null)
      loader = new WebloungeClassLoader(base);
    return loader;
  }

}
