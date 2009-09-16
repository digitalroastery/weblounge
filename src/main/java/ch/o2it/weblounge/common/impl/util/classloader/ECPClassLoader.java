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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Extended class path loader.
 * 
 * @version $Revision: 1090 $ $Date: 2009-09-05 02:45:07 +0200 (Sa, 05 Sep 2009)
 *          $
 * @author Daniel Steiner
 */

public class ECPClassLoader extends URLClassLoader {

  /** Logging facility */
  private final static Logger log = LoggerFactory.getLogger(ECPClassLoader.class);

  /** the name of packages that should be loaded by this class loader */
  private List<String> forcePackages = new ArrayList<String>();

  /**
   * Creates a new class loader using the specified parent class loader for
   * delegation.
   * 
   * @param parent
   *          the parent class loader
   */
  protected ECPClassLoader(ClassLoader parent) {
    super(new URL[0], parent);
  }

  /**
   * Creates a new class loader using the <code>ClassLoader</code> returned by
   * the method <code>getSystemClassLoader()</code> as the parent class loader.
   */
  protected ECPClassLoader() {
    super(new URL[0]);
  }

  /**
   * Adds an element to the class path. Note: this element has to be a valid
   * name of a directory or a ZIP/JAR file.
   * 
   * @param cp
   *          the element of the class path
   * @return true, if the classpath could be updated
   */
  protected boolean addClassPathElement(String cp) {

    /* we only support directories and ZIP/JAR files */
    String tmp = cp.toLowerCase();
    File file = new File(cp);
    if (!file.exists()) {
      log.info("Classpath element " + cp + " does not exist.");
      return false;
    }

    if (!(file.canRead() && (file.isDirectory() || (file.isFile() && (tmp.endsWith(".zip") || tmp.endsWith(".jar")))))) {
      log.warn("Classpath element " + cp + " is invalid.");
      return false;
    }

    /* everything seems to be ok. */
    log.debug("ECP add: " + cp);
    try {
      addURL(new URL("file:" + cp));
      return true;
    } catch (MalformedURLException e) {
      log.warn("Unable to add classpath element: " + e.getMessage());
      log.debug("Unable to add classpath element: " + e.getMessage(), e);
      return false;
    }
  }

  /**
   * Add all the zip/jar files of the specified directory to the CLASSPATH.
   * 
   * @param libDir
   *          the directory to search
   */
  protected void addLibRepository(String libDir) {
    // find all ZIP/JAR files in the lib directory
    File dir = new File(libDir);
    String[] files = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        name = name.toLowerCase();
        return name.endsWith(".zip") || name.endsWith(".jar");
      }
    });

    // add all the ZIP/JAR files to the CLASSPATH
    if (files != null) {
      for (int i = 0; i < files.length; i++)
        addClassPathElement(libDir + File.separatorChar + files[i]);
    }
  }

  /**
   * Add a directory containing a class and lib folder to the classpath.
   * 
   * @param cp
   *          the directory to add to the class path.
   */
  public void addExtendedClassPath(String cp) {
    if (!cp.endsWith(File.separator))
      cp += File.separatorChar;
    File f = new File(cp);
    if (!f.exists() || !f.isDirectory() || !f.canRead()) {
      log.warn("Extended classpath base " + cp + " does not exist");
      return;
    }
    log.info("ECP add base: " + cp);
    String classDir = cp + "classes" + File.separatorChar;
    String libDir = cp + "lib";

    // add the module's 'classes' directory to the CLASSPATH
    if (new File(classDir).exists())
      addClassPathElement(classDir);

    // find all ZIP/JAR files in the lib directory of the module
    addLibRepository(libDir);

  }

  /**
   * Force the class loader to load all classes in this package.
   * 
   * @param pkg
   *          the name of the package
   */
  public void forcePackage(String pkg) {
    forcePackages.add(pkg);
  }

  /**
   * @see java.net.URLClassLoader#findClass(java.lang.String)
   */
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    Class<?> c = findLoadedClass(name);
    if (c != null)
      return c;
    return super.findClass(name);
  }

  /**
   * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
   */
  @Override
  protected synchronized Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {

    // find loaded classes
    Class<?> c = findLoadedClass(name);
    if (c != null) {
      if (resolve)
        resolveClass(c);
      log.debug("ECP load [ C    ]: " + name);
      return c;
    }

    // load system classes with the system class loader
    if (name.startsWith("java.")) {
      c = getSystemClassLoader().loadClass(name);
      if (c != null) {
        if (resolve)
          resolveClass(c);
        log.debug("ECP load [  S   ]: " + name);
        return c;
      }
      throw new ClassNotFoundException(name);
    }

    boolean force = false;
    for (Iterator<String> i = forcePackages.iterator(); i.hasNext();)
      if (name.startsWith(i.next())) {
        force = true;
        break;
      }

    if (force) {
      // load our own classes
      try {
        c = findClass(name);
        if (c != null) {
          if (resolve)
            resolveClass(c);
          log.debug("ECP load [    RF]: " + name);
          return c;
        }
      } catch (ClassNotFoundException e) { /* ignore */
      }
    }

    // ask the parent class to load the classes
    ClassLoader parent = getParent();
    if (parent == null)
      parent = getSystemClassLoader();
    try {
      c = parent.loadClass(name);
    } catch (ClassNotFoundException e) { /* ignore */
    }
    if (c != null) {
      if (resolve)
        resolveClass(c);
      log.debug("ECP load [   P  ]: " + name);
      return c;
    }

    // load our own classes
    try {
      c = findClass(name);
      if (c != null) {
        if (resolve)
          resolveClass(c);
        log.debug("ECP load [    R ]: " + name);
        return c;
      }
    } catch (ClassNotFoundException e) { /* ignore */
    }

    // no class definition found
    log.debug("ECP load [E     ]: " + name);
    throw new ClassNotFoundException(name);
  }
}