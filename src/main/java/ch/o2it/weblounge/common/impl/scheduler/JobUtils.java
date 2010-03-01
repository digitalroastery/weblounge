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

package ch.o2it.weblounge.common.impl.scheduler;

import ch.o2it.weblounge.common.scheduler.JobTrigger;

/**
 * Helper class that makes creating special job triggers easy.
 */
public class JobUtils {

  /**
   * Returns a job trigger that is executed on site restart.
   * 
   * @return the restart job trigger
   */
  public static JobTrigger createRestartTrigger() {
    return new CronJobTrigger("@restart");
  }

  /**
   * Returns a job trigger that is executed once every hour. It's
   * <code>cron</code> equivalent is <code>0 * * * *</code>.
   * 
   * @return the hourly job trigger
   */
  public static JobTrigger createHourlyTrigger() {
    return new CronJobTrigger("@hourly");
  }

  /**
   * Returns a job trigger that is executed once every day. It's
   * <code>cron</code> equivalent is <code>0 0 * * *</code>.
   * 
   * @return the daily job trigger
   */
  public static JobTrigger createDailyTrigger() {
    return new CronJobTrigger("@daily");
  }

  /**
   * Returns a job trigger that is executed every Sunday. It's <code>cron</code>
   * equivalent is <code>0 0 * * 0</code>.
   * 
   * @return the weekly job trigger
   */
  public static JobTrigger createWeeklyTrigger() {
    return new CronJobTrigger("@weekly");
  }

  /**
   * Returns a job trigger that is executed once every month. It's
   * <code>cron</code> equivalent is <code>0 0 1 * *</code>.
   * 
   * @return the monthly job trigger
   */
  public static JobTrigger createMonthlyTrigger() {
    return new CronJobTrigger("@monthly");
  }

  /**
   * Returns a job trigger that is executed once every year. It's
   * <code>cron</code> equivalent is <code>0 0 1 1 *</code>.
   * 
   * @return the yearly job trigger
   */
  public static JobTrigger createYearlyTrigger() {
    return new CronJobTrigger("@yearly");
  }

}
