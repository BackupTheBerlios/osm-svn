/*
 * @(#)AuditService.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 12/03/2001
 */


package net.osm.audit;

import java.util.List;
import java.util.LinkedList;
import java.util.Hashtable;
import org.omg.CORBA_2_3.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
import org.omg.Session.AbstractResource;
import org.omg.NamingAuthority.AuthorityId;

/**
 *
 * 1. given an agent instance the service registers the agent instance in a table
 *    and attempts to establishes a subscription to the agent's primary resource
 * 2. on receipt of an event, the service locates registered listeners from the 
 *    table based on the source object reference contained within the event 
 *    structure, and issues a RomoteEvent to any listener registered for the 
 *    event.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface AuditService 
{

   /**
    * Associate the audit service with a remote abstract resource.
    * @param resource the primary <code>AbstractResource</code> event source
    * @param listener the <code>RemoteEventListener</code> to be notified of 
    *   incomming events and disconnection notifications
    */
    public void addRemoteEventListener( AbstractResource resource, RemoteEventListener listener );

   /**
    * Dissassociated a listener from the service.
    * @param resource the primary <code>AbstractResource</code> event source
    * @param listener the <code>RemoteEventListener</code> to be removed 
    * from the set of listeners.
    */
    public void removeRemoteEventListener( AbstractResource resource, RemoteEventListener listener );

   /**
    * Returns the boolean status of the connection between the supplied 
    * resource structured push supplier and the audit service consumer.
    * @return boolean tru if a subscription is established
    */
    public boolean getConnected( AbstractResource resource );

}



