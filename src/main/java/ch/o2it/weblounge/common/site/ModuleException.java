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

package ch.o2it.weblounge.common.site;

/**
 * This exception is thrown if a module cannot be properly started or shut down
 * or exhibits any other misbehavior.
 */
public class ModuleException extends Exception {

  /**
   * Serial version uid
   */
  private static final long serialVersionUID = 5173735923703880699L;

  /**
   * The module in question
   */
  protected Module module = null;

  /**
   * Creates a new exception, caused by the given module.
   * 
   * @param module
   *          the module
   */
  public ModuleException(Module module) {
    this.module = module;
  }

  /**
   * Creates a new exception, caused by the given module.
   * 
   * @param module
   *          the module
   * @param message
   *          the error message
   */
  public ModuleException(Module module, String message) {
    super(message);
    this.module = module;
  }

  /**
   * Creates a new exception, caused by the given module.
   * 
   * @param module
   *          the module
   * @param cause
   *          the original cause
   */
  public ModuleException(Module module, Throwable cause) {
    super(cause);
    this.module = module;
  }

  /**
   * Creates a new exception, caused by the given module.
   * 
   * @param module
   *          the module
   * @param message
   *          the error message
   * @param cause
   *          the original cause
   */
  public ModuleException(Module module, String message, Throwable cause) {
    super(message, cause);
    this.module = module;
  }

  /**
   * Returns the module that caused the exception.
   * 
   * @return the module
   */
  public Module getModule() {
    return module;
  }

}
