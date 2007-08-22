/*
 * Copyright 2001 OSM All Rights Reserved.
 * 
 * This software is the proprietary information of OSM.
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 2.0 28-MAR-2002
 */

package net.osm.session.processor;

import org.omg.Session.CannotStop;
import org.omg.Session.NotRunning;
import org.omg.Session.CannotSuspend;
import org.omg.Session.CurrentlySuspended;

import net.osm.session.message.SystemMessage;

/**
 * The ProcessorCallback interface exposes operations through which an appliance
 * can trigger suspension, termination and completion states of the hosting processor.
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface ProcessorCallback extends ProcessorOperations
{

   /**
    * Operation invoked by an appliance to request suspension of the process
    * for reasons detailed under the supplied message.  The implementation 
    * directs the message to the owner of the coordinating task.
    * @param message the message to be directed to the owner of the coordinating task
    */
    public void suspended( SystemMessage message )
    throws CannotSuspend, CurrentlySuspended;

   /**
    * Request to a processor to terminate as a result of an error condition.  This method is 
    * invoked by an appliance to signal termination of the process as a result
    * of an internal exception.
    * @param throwable the exception causing the appliance to terminate
    * @exception  org.omg.Session.CannotStop
    * @exception  org.omg.Session.NotRunning
    */
    public void stopped( Throwable throwable )
    throws CannotStop, NotRunning;

   /**
    * Method invoked by an appliance to signal normal completion of 
    * of the execution thread.
    */
    public void completed();

   /**
    * Method invoked by an appliance to signal normal completion of 
    * of the execution thread with a supplied completion message.
    */
    public void completed( SystemMessage message );

}



