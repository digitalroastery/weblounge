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

package ch.o2it.weblounge.common.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

/**
 * This listener keeps track of the work that the Quartz scheduler is doing.
 */
final class TestTriggerListener implements TriggerListener {

  /** The listener name */
  private String name = null;

  /** Veto job executions? */
  private boolean veto = false;

  /** Number of expected firings */
  private int expectedFirings = 0;

  /** Number of trigger completions */
  private int triggerCompleted = 0;

  /** Number of trigger firings */
  private int triggerFired = 0;

  /** Number of failed trigger firings */
  private int triggerMisfired = 0;

  /** Number of triggers that were vetoed */
  private int triggerVetoed = 0;

  /** The monitor */
  private Object monitor = null;

  /**
   * Creates a new trigger listener that will notify listeners on the monitor
   * as soon as the expected number of firings has been reached.
   * 
   * @param site
   *          the site
   */
  TestTriggerListener(Object monitor, int expectedFirings) {
    this.name = "quartz trigger listener";
    this.monitor = monitor;
    this.expectedFirings = expectedFirings;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.TriggerListener#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the number of trigger completions.
   * 
   * @return the number of trigger completions
   */
  int getCompletedCount() {
    return triggerCompleted;
  }

  /**
   * Returns the number of trigger firings.
   * 
   * @return the number of trigger firings
   */
  int getFiredCount() {
    return triggerFired;
  }

  /**
   * Returns the number of failed trigger firings.
   * 
   * @return the number of failed trigger firings
   */
  int getMisfiredCount() {
    return triggerMisfired;
  }

  /**
   * Returns the number of times that the trigger was not fired due to a veto.
   * 
   * @return the number of vetoed firings
   */
  int getVetoedCount() {
    return triggerVetoed;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.TriggerListener#triggerComplete(org.quartz.Trigger,
   *      org.quartz.JobExecutionContext, int)
   */
  public void triggerComplete(Trigger trigger, JobExecutionContext ctx, int a) {
    triggerCompleted++;
    
    // If we have reached the number of expected firings (including vetoed
    // ones), tell waiting objects
    if (triggerVetoed + triggerCompleted == expectedFirings) {
      synchronized (monitor) {
        monitor.notifyAll();
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.TriggerListener#triggerFired(org.quartz.Trigger,
   *      org.quartz.JobExecutionContext)
   */
  public void triggerFired(Trigger trigger, JobExecutionContext ctx) {
    triggerFired++;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.TriggerListener#triggerMisfired(org.quartz.Trigger)
   */
  public void triggerMisfired(Trigger trigger) {
    triggerMisfired++;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.quartz.TriggerListener#vetoJobExecution(org.quartz.Trigger,
   *      org.quartz.JobExecutionContext)
   */
  public boolean vetoJobExecution(Trigger trigger, JobExecutionContext ctx) {
    if (veto)
      triggerVetoed++;

    // If we have reached the number of expected firings (including vetoed
    // ones), tell waiting objects
    if (triggerVetoed + triggerCompleted == expectedFirings) {
      synchronized (monitor) {
        monitor.notifyAll();
      }
    }

    return veto;
  }

  /**
   * Sets the trigger to either veto or allow a trigger firing.
   * 
   * @param veto
   *          the veto setting
   */
  void setVeto(boolean veto) {
    this.veto = veto;
  }

}