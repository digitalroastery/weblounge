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
package ch.entwine.weblounge.kernel.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * Implementation of a security manager that will allow anything and everything
 * to pass.
 */
public class NullSecurityManager extends SecurityManager {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(NullSecurityManager.class);

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
   */
  @Override
  public void checkPermission(Permission perm) {
    logger.trace("Passing relaxed permission security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkPermission(java.security.Permission,
   *      java.lang.Object)
   */
  @Override
  public void checkPermission(Permission perm, Object context) {
    logger.trace("Passing relaxed permission security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkCreateClassLoader()
   */
  @Override
  public void checkCreateClassLoader() {
    logger.trace("Passing relaxed create classloader security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkAccess(java.lang.Thread)
   */
  @Override
  public void checkAccess(Thread t) {
    logger.trace("Passing relaxed access security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkAccess(java.lang.ThreadGroup)
   */
  @Override
  public void checkAccess(ThreadGroup g) {
    logger.trace("Passing relaxed access security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkExit(int)
   */
  @Override
  public void checkExit(int status) {
    logger.trace("Passing relaxed exit security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkExec(java.lang.String)
   */
  @Override
  public void checkExec(String cmd) {
    logger.trace("Passing relaxed exec security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkLink(java.lang.String)
   */
  @Override
  public void checkLink(String lib) {
    logger.trace("Passing relaxed link security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkRead(java.io.FileDescriptor)
   */
  @Override
  public void checkRead(FileDescriptor fd) {
    logger.trace("Passing relaxed read security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkRead(java.lang.String)
   */
  @Override
  public void checkRead(String file) {
    logger.trace("Passing relaxed read security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkRead(java.lang.String,
   *      java.lang.Object)
   */
  @Override
  public void checkRead(String file, Object context) {
    logger.trace("Passing relaxed read security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkWrite(java.io.FileDescriptor)
   */
  @Override
  public void checkWrite(FileDescriptor fd) {
    logger.trace("Passing relaxed write security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkWrite(java.lang.String)
   */
  @Override
  public void checkWrite(String file) {
    logger.trace("Passing relaxed write security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkDelete(java.lang.String)
   */
  @Override
  public void checkDelete(String file) {
    logger.trace("Passing relaxed delete security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkConnect(java.lang.String, int)
   */
  @Override
  public void checkConnect(String host, int port) {
    logger.trace("Passing relaxed connect security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkConnect(java.lang.String, int,
   *      java.lang.Object)
   */
  @Override
  public void checkConnect(String host, int port, Object context) {
    logger.trace("Passing relaxed connect security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkListen(int)
   */
  @Override
  public void checkListen(int port) {
    logger.trace("Passing relaxed listen security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkAccept(java.lang.String, int)
   */
  @Override
  public void checkAccept(String host, int port) {
    logger.trace("Passing relaxed accept security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkMulticast(java.net.InetAddress)
   */
  @Override
  public void checkMulticast(InetAddress maddr) {
    logger.trace("Passing relaxed multicast security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkMulticast(java.net.InetAddress, byte)
   */
  @Override
  public void checkMulticast(InetAddress maddr, byte ttl) {
    logger.trace("Passing relaxed multicast security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkPropertiesAccess()
   */
  @Override
  public void checkPropertiesAccess() {
    logger.trace("Passing relaxed properties access security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
   */
  @Override
  public void checkPropertyAccess(String key) {
    logger.trace("Passing relaxed property access security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkTopLevelWindow(java.lang.Object)
   */
  @Override
  public boolean checkTopLevelWindow(Object window) {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkPrintJobAccess()
   */
  @Override
  public void checkPrintJobAccess() {
    logger.trace("Passing relaxed print job access security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkSystemClipboardAccess()
   */
  @Override
  public void checkSystemClipboardAccess() {
    logger.trace("Passing relaxed system clipboard access security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkAwtEventQueueAccess()
   */
  @Override
  public void checkAwtEventQueueAccess() {
    logger.trace("Passing relaxed awt event queue access security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkPackageAccess(java.lang.String)
   */
  @Override
  public void checkPackageAccess(String pkg) {
    logger.trace("Passing relaxed package access security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkPackageDefinition(java.lang.String)
   */
  @Override
  public void checkPackageDefinition(String pkg) {
    logger.trace("Passing relaxed package definition security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkMemberAccess(java.lang.Class, int)
   */
  @Override
  public void checkMemberAccess(Class<?> clazz, int which) {
    logger.trace("Passing relaxed member access security check");
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.SecurityManager#checkSecurityAccess(java.lang.String)
   */
  @Override
  public void checkSecurityAccess(String target) {
    logger.trace("Passing relaxed security access security check");
  }

}
