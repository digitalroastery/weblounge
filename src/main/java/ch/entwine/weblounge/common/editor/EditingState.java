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

package ch.entwine.weblounge.common.editor;

/**
 * This interface holds constants used to record the editing state.
 */
public interface EditingState {

  /** Name of the cookie that is used to maintain the editing state */
  String STATE_COOKIE = "weblounge.editor";

  /** Name of the request parameter that will trigger editing support */
  String WORKBENCH_PARAM = "edit";

  /** Name of the request parameter for the environment mode */
  String WORKBENCH_ENVIRONMENT_PARAM = "environment";

  /** Name of the request parameter for the editor uri */
  String WORKBENCH_EDITOR_PARAM = "editor";

  /** Name of the request parameter for the editor preview uri */
  String WORKBENCH_PREVIEW_PARAM = "preview";

}
