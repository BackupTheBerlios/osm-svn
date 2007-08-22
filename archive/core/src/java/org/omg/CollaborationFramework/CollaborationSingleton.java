
/*
 * CollaborationSingleton.java
 *
 * Copyright 2000-2001 OSM SARL All Rights Reserved.
 *
 * This software is the proprietary information of OSM SARL.
 * Use is subject to license terms.
 *
 * @author  Stephen McConnell <mailto:mcconnell@osm.net>
 * @version 1.0 24 DEC 2000
 */

package org.omg.CollaborationFramework;

import org.omg.CORBA.ORB;
import org.omg.CommunityFramework.CommunitySingleton;
import org.omg.CosNotification.EventType;


/**
 * CollaborationSingleton is a singleton class that provides a single static 
 * method through which valuetype and respective factories are registered 
 * with the current orb.
 */

public class CollaborationSingleton extends CommunitySingleton
{

   /**
    * Name of the event domain classifying structured events produced by the 
    * AbstractResource type.
    */
    public static final String EVENT_DOMAIN = "org.omg.collaboration";

   /**
    * The event type for a processor 'state' event.
    */
    public static final EventType stateEventType = new EventType( EVENT_DOMAIN, "state" );
        
   /**
    * Static method to initialize value factories for each valuetype
    * with the ORB.  This method must be invoked during establishment
    * of any client or server using community or collaboration
    * valuetypes.
    */
    
    public static void init( final ORB current_orb ) {

	  CommunitySingleton.init( current_orb );

	  // WARNING: Timeout, VoteCount, VoteReceipt, VoteStatement not impemented yet

	  try
	  {

            final org.omg.CORBA_2_3.ORB orb = (org.omg.CORBA_2_3.ORB) current_orb;


	      // declare valuetype factories for the CollaborationFramework
	      orb.register_value_factory( ApplyArgumentHelper.id(), new ApplyArgument());
	      orb.register_value_factory( ClockHelper.id(), new Clock());
	      orb.register_value_factory( CollaborationModelHelper.id(), new CollaborationModel());
	      orb.register_value_factory( CompletionHelper.id(), new Completion());
	      orb.register_value_factory( CompoundTransitionHelper.id(), new CompoundTransition());
	      orb.register_value_factory( ConstructorHelper.id(), new Constructor());
	      orb.register_value_factory( ControlledByHelper.id(), new ControlledBy());
	      orb.register_value_factory( ControlsHelper.id(), new Controls());
	      orb.register_value_factory( CoordinatedByHelper.id(), new CoordinatedBy());
	      orb.register_value_factory( CoordinatesHelper.id(), new Coordinates());
	      orb.register_value_factory( DuplicateHelper.id(), new Duplicate());
	      orb.register_value_factory( DurationHelper.id(), new Duration());
	      orb.register_value_factory( EncounterCriteriaHelper.id(), new EncounterCriteria());
	      orb.register_value_factory( EngagementModelHelper.id(), new EngagementModel());
	      orb.register_value_factory( InitializationHelper.id(), new Initialization());
	      orb.register_value_factory( InputDescriptorHelper.id(), new InputDescriptor());
	      orb.register_value_factory( LaunchHelper.id(), new Launch());
	      orb.register_value_factory( LocalTransitionHelper.id(), new LocalTransition());
	      orb.register_value_factory( MapHelper.id(), new Map());
	      orb.register_value_factory( MonitorsHelper.id(), new Monitors());
	      orb.register_value_factory( MoveHelper.id(), new Move());
	      orb.register_value_factory( OutputDescriptorHelper.id(), new OutputDescriptor());
	      orb.register_value_factory( ProcessorCriteriaHelper.id(), new ProcessorCriteria());
	      orb.register_value_factory( ProcessorModelHelper.id(), new ProcessorModel());
	      orb.register_value_factory( ReferralHelper.id(), new Referral());
	      orb.register_value_factory( RemoveHelper.id(), new Remove());
	      //orb.register_value_factory( ResultClassHelper.id(), new ResultClass()); 
	      orb.register_value_factory( SimpleTransitionHelper.id(), new SimpleTransition());
	      orb.register_value_factory( StateHelper.id(), new State());
	      orb.register_value_factory( StateDescriptorHelper.id(), new StateDescriptor());
	      orb.register_value_factory( TerminalTransitionHelper.id(), new TerminalTransition());
	      orb.register_value_factory( TimeoutHelper.id(), new Timeout()); //##
	      orb.register_value_factory( TransitionHelper.id(), new Transition());
	      orb.register_value_factory( TriggerHelper.id(), new Trigger());
	      orb.register_value_factory( VoteCountHelper.id(), new VoteCount());
	      orb.register_value_factory( VoteModelHelper.id(), new VoteModel());
	      orb.register_value_factory( VoteReceiptHelper.id(), new VoteReceipt());
	      orb.register_value_factory( VoteStatementHelper.id(), new VoteStatement());
	  }
	  catch(Throwable t)
	  {
		System.out.println("message: " + t.getMessage());
		t.printStackTrace();
		throw new RuntimeException( t.getMessage() );
	  }
    }
}
