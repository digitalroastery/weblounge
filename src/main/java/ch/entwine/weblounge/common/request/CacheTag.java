/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License;
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not;
 *  Inc.;
 */

package ch.entwine.weblounge.common.request;

import ch.entwine.weblounge.common.content.Tag;

/**
 * This interface contains the cache tags that are used by default throughout
 * the system.
 */
public interface CacheTag extends Tag {

  /** Special object representing the "any" value */
  String ANY = "*";

  String Url = "url";
  String Language = "language";
  String User = "user";
  String Module = "module";
  String Action = "action";
  String Site = "site";
  String Parameters = "parameters";
  String Composer = "composer";
  String Position = "position";
  String Renderer = "renderer";

}
