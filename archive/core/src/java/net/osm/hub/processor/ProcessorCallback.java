/*
 * @(#)ProcessorCallback.java
 *
 * Copyright 2001 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 19/03/2001
 */

package net.osm.hub.processor;

import org.omg.Session.SystemMessage;
import org.omg.Session.CannotStop;
import org.omg.Session.NotRunning;
import org.omg.Session.CannotSuspend;
import org.omg.Session.CurrentlySuspended;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorOperations;
import org.omg.PortableServer.Servant;

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
    public void suspend( SystemMessage message )
    throws CannotSuspend, CurrentlySuspended;

   /**
    * Request to a processor to terminate as a result of an error condition.  This method is 
    * invoked by an appliance to signal termination of the process as a result
    * of an internal exception.
    * @param throwable the exception causing the appliance to terminate
    * @exception  org.omg.Session.CannotStop
    * @exception  org.omg.Session.NotRunning
    */
    public void stop( Throwable throwable )
    throws CannotStop, NotRunning;

   /**
    * Method invoked by an appliance to signal normal completion of 
    * of the execution thread.
    */
    public void completion();

   /**
    * Method invoked by an appliance to signal normal completion of 
    * of the execution thread with a supplied completion message.
    */
    public void completion( SystemMessage message );

   /**
    * Returns a reference to the processor in the form of a CORBA object reference.
    * @return Processor a CORBA object reference to the delegate
    */
    public Processor getProcessorReference();

}



