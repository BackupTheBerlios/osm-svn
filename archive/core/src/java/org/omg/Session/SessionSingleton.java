/*
 * SessionSingleton.java
 *
 * Copyright 2000 OSM SARL All Rights Reserved.
 *
 * This software is the proprietary information of OSM SARL.
 * Use is subject to license terms.
 *
 * @author  Stephen McConnell
 * @version 1.0 24 DEC 2000
 */

package org.omg.Session;

import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;


/**
 * SessionSingleton is a singleton class that provides a single static 
 * method through which valuetype and respective factories are registered 
 * with the current orb.
 */

public class SessionSingleton
{

    //=======================================================================
    // static
    //=======================================================================
        
   /**
    * Name of the event domain classifying structured events produced by the 
    * AbstractResource type.
    */
    public static final String EVENT_DOMAIN = "org.omg.session";
    
   /**
    * The event type for a 'move' event.
    */
    public static final EventType moveEventType = new EventType( EVENT_DOMAIN, "move" );
    
   /**
    * The event type for a 'remove' event.
    */
    public static final EventType removeEventType = new EventType( EVENT_DOMAIN, "remove" );
    
   /**
    * The event type for a 'update' event.
    */
    public static final EventType updateEventType = new EventType( EVENT_DOMAIN,"update" );

   /**
    * The event type for a 'bind' event.
    */
    public static final EventType bindEventType = new EventType( EVENT_DOMAIN, "bind" );
    
   /**
    * The event type for a 'replace' event.
    */
    public static final EventType replaceEventType = new EventType( EVENT_DOMAIN, "replace" );

   /**
    * The event type for a 'release' event.
    */
    public static final EventType releaseEventType = new EventType( EVENT_DOMAIN, "release" );

   /**
    * The event type for a 'property' event from a User.
    */
    public static final EventType propertyEventType = new EventType( EVENT_DOMAIN, "property" );
    
   /**
    * The event type for a 'connected' event from a User.
    */
    public static final EventType connectedEventType = new EventType( EVENT_DOMAIN, "connected" );

   /**
    * Event type event containing changes in task state.
    */
    public static final EventType taskStateEventType = new EventType( EVENT_DOMAIN, "process_state" );

   /**
    * Event type event containing changes to Task ownership.
    */
    public static final EventType ownershipEventType = new EventType( EVENT_DOMAIN, "ownership" );

   /**
    * Event type event containing a new system message.
    */
    public static final EventType enqueueEventType = new EventType( EVENT_DOMAIN, "enqueue" );

   /**
    * Event type event containing a new system message.
    */
    public static final EventType dequeueEventType = new EventType( EVENT_DOMAIN, "dequeue" );

    //=======================================================================
    // valuetype initialization
    //=======================================================================

   /**
    * Static method to initialize value factories for each valuetype
    * with the ORB.  This method must be invoked during establishment
    * of any client or server using community or collaboration
    * valuetypes.
    */
    
    public static void init( ORB current_orb ) {
        
        final org.omg.CORBA_2_3.ORB orb = (org.omg.CORBA_2_3.ORB) current_orb;

        orb.register_value_factory( BaseBusinessObjectKeyHelper.id(), new BaseBusinessObjectKey());

        orb.register_value_factory( ProducesHelper.id(), new Produces());
        orb.register_value_factory( ProducedByHelper.id(), new ProducedBy());
        orb.register_value_factory( OwnsHelper.id(), new Owns());
        orb.register_value_factory( OwnedByHelper.id(), new OwnedBy());
        orb.register_value_factory( IsPartOfHelper.id(), new IsPartOf());
        orb.register_value_factory( ConsumesHelper.id(), new Consumes());
        orb.register_value_factory( ConsumedByHelper.id(), new ConsumedBy());
        orb.register_value_factory( ComposedOfHelper.id(), new ComposedOf());
        orb.register_value_factory( CollectsHelper.id(), new Collects());
        orb.register_value_factory( CollectedByHelper.id(), new CollectedBy());
        orb.register_value_factory( AdministersHelper.id(), new Administers());
        orb.register_value_factory( AdministeredByHelper.id(), new AdministeredBy());
        orb.register_value_factory( AccessesHelper.id(), new Accesses());
        orb.register_value_factory( AccessedByHelper.id(), new AccessedBy());
        orb.register_value_factory( SystemMessageHelper.id(), new SystemMessageBase());
        orb.register_value_factory( MessageHeaderHelper.id(), new MessageHeaderBase());
        orb.register_value_factory( MessageBodyHelper.id(), new MessageBodyBase());

    }
}
