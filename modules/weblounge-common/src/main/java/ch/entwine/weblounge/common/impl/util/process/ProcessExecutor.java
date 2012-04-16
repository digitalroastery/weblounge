/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.impl.util.process;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to execute processes on the host system and outside of the java
 * vm. Since there are problems with reading stdin, stdout and stderr that need
 * to be taken into account when running on various platforms, this helper class
 * is used to deal with those.
 * 
 * A generic Exception should be used to indicate what types of checked
 * exceptions might be thrown from this process.
 */
public class ProcessExecutor<T extends Exception> {

  /** True to redirect the error stream to std out */
  private boolean redirectErrorStream = true;

  /** Command line to call */
  private String[] commandLine;

  /**
   * Creates a process executor with the given command line.
   * 
   * @param commandLine
   *          the full command line
   */
  protected ProcessExecutor(String commandLine) {
    this.commandLine = commandLine.split("\\s+");
  }

  /**
   * Creates a process executor with the given command and command line options.
   * Options will be split by whitespace.
   * 
   * @param command
   *          the command
   * @param options
   *          the command options
   */
  protected ProcessExecutor(String command, String... options) {
    List<String> commandLineList = new ArrayList<String>();
    commandLineList.add(command);
    for (String s : options) {
      for (String t : s.split("\\s+"))
        commandLineList.add(t);
    }
    commandLine = commandLineList.toArray(new String[commandLineList.size()]);
  }

  /**
   * Creates a process executor with the given command line.
   * 
   * @param commandLine
   *          the full command line
   * @param redirectErrorStream
   *          <code>true</code> to redirect the error stream
   */
  protected ProcessExecutor(String commandLine, boolean redirectErrorStream) {
    this(commandLine);
    this.redirectErrorStream = redirectErrorStream;
  }

  /**
   * Creates a process executor for the given command and command line options.
   * 
   * @param command
   *          the command
   * @param redirectErrorStream
   *          <code>true</code> to redirect the error stream
   * @param options
   *          the command options
   */
  protected ProcessExecutor(String command, boolean redirectErrorStream,
      String... options) {
    this(command, options);
    this.redirectErrorStream = redirectErrorStream;
  }

  /**
   * Specifies whether the error stream should be directed to
   * <code>std out</code>.
   * 
   * @param redirect
   *          <code>true</code> to redirect the error stream
   */
  protected void setRedirectErrorStream(boolean redirect) {
    this.redirectErrorStream = redirect;
  }

  /**
   * Executes the process. During execution, {@link #onLineRead(String)} will be
   * called for process output. When finished, {@link #onProcessFinished(int)}
   * is called.
   * 
   * @throws ProcessExcecutorException
   *           if an error occurs during execution
   */
  public final void execute() throws ProcessExcecutorException {
    BufferedReader in = null;
    Process process = null;
    StreamHelper errorStreamHelper = null;
    try {
      // create process.
      // no special working dir is set which means the working dir of the
      // current java process is used.
      ProcessBuilder pbuilder = new ProcessBuilder(commandLine);
      pbuilder.redirectErrorStream(redirectErrorStream);
      process = pbuilder.start();
      // Consume error stream if necessary
      if (!redirectErrorStream) {
        errorStreamHelper = new StreamHelper(process.getErrorStream());
      }
      // Read input and
      in = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = in.readLine()) != null) {
        if (!onLineRead(line))
          break;
      }

      // wait until the task is finished
      process.waitFor();
      int exitCode = process.exitValue();
      onProcessFinished(exitCode);
    } catch (Throwable t) {
      String msg = null;
      if (errorStreamHelper != null) {
        msg = errorStreamHelper.contentBuffer.toString();
      } else {
        msg = t.getMessage();
      }

      // TODO: What if the error stream has been redirected? Can we still get
      // the error message?

      throw new ProcessExcecutorException(msg, t);
    } finally {
      if (process != null)
        process.destroy();
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Method that is intended to be overwritten by subclasses to catch a
   * processe's output.
   * 
   * @param line
   *          a line of console output
   * @return <code>true</code> to keep reading
   */
  protected boolean onLineRead(String line) {
    return false;
  }

  /**
   * Method that is intended to be overwritten by subclasses to catch the
   * processe's exit code.
   * 
   * @param exitCode
   *          the exit code
   * @throws T
   */
  protected void onProcessFinished(int exitCode) throws T {
  }

}
