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
 * Exception that is thrown during job executions.
 */
public class JobException extends Exception {

  /** The serial version uid */
  private static final long serialVersionUID = -8028700443165899080L;

  /** The job that caused this exception */
  protected Job job = null;

  /**
   * Creates a new exception that was thrown while executing <code>job</code>.
   * The exception contains an error message to indicate to the user what went
   * wrong.
   * 
   * @param job
   *          the job whose execution caused this exception
   * @param message
   *          error message
   */
  public JobException(Job job, String message) {
    this(job, message, null);
  }

  /**
   * Creates a new exception that was thrown while executing <code>job</code>.
   * The exception contains the original exception that caused the failure.
   * 
   * @param job
   *          the job whose execution caused this exception
   * @param cause
   *          the original exception
   */
  public JobException(Job job, Throwable cause) {
    this(job, null, cause);
  }

  /**
   * Creates a new exception that was thrown while executing <code>job</code>.
   * The exception contains information about the cause of failure as well as an
   * error message.
   * 
   * @param job
   *          the job whose execution caused this exception
   * @param message
   *          error message
   * @param cause
   *          the original exception
   */
  public JobException(Job job, String message, Throwable cause) {
    super(message, cause);
    this.job = job;
  }

  /**
   * Returns the job whose execution caused this exception.
   * 
   * @return the failing job
   */
  public Job getJob() {
    return job;
  }

}
