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

package ch.o2it.weblounge.common.site;

/**
 * A <code>RendererException</code> is thrown if an exceptional state is reached
 * when executing a <code>Renderer</code> to create the output for either a page
 * or a single page name.
 */
public class RenderException extends RuntimeException {

  /** The serial version id */
  private static final long serialVersionUID = -857423335304601456L;

  /** Renderer name, e. g. <code>XSLRenderer</code> */
  protected Renderer renderer = null;

  /**
   * Creates a new <code>RendererException</code> that has been thrown by the
   * specified renderer.
   * 
   * @param renderer
   *          the renderer
   */
  public RenderException(Renderer renderer) {
    this.renderer = renderer;
  }

  /**
   * Creates a new <code>RendererException</code> that has been thrown by the
   * specified renderer.
   * 
   * @param renderer
   *          the renderer
   * @param t
   *          the exception caught when executing the renderer
   */
  public RenderException(Renderer renderer, Throwable t) {
    super(t);
    this.renderer = renderer;
  }

  /**
   * Creates a new <code>RendererException</code> that has been thrown by the
   * specified renderer.
   * 
   * @param renderer
   *          the renderer
   * @param message
   *          the error message
   */
  public RenderException(Renderer renderer, String message) {
    super(message);
    this.renderer = renderer;
  }


  /**
   * Returns the renderer that raised this exception.
   * 
   * @return the renderer
   */
  public Renderer getRenderer() {
    return renderer;
  }

  /**
   * Returns the exception detail message.
   * 
   * @see java.lang.Throwable#getMessage()
   */
  public String getMessage() {
    if (getCause() != null) {
      return getCause().getMessage();
    } else {
      return super.getMessage();
    }
  }

}