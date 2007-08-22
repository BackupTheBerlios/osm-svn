
/*
 * CommunitySingleton.java
 *
 * Copyright 2000 OSM SARL All Rights Reserved.
 *
 * This software is the proprietary information of OSM SARL.
 * Use is subject to license terms.
 *
 * @author  Stephen McConnell
 * @version 1.0 24 DEC 2000
 */

package org.omg.CommunityFramework;

import org.omg.CORBA.ORB;
import org.omg.Session.SessionSingleton;
import org.omg.CosNotification.EventType;


/**
 * CommunitySingleton is a singleton class that provides a single static 
 * method through which valuetype and respective factories are registered 
 * with the current orb.
 */

public class CommunitySingleton extends SessionSingleton
{

   /**
    * The event type for a 'recruitment event change from a Community.
    */
    public static final EventType recruitmentEventType = new EventType( EVENT_DOMAIN, "recruitment" );

        
   /**
    * Static method to initialize value factories for each valuetype
    * with the ORB.  This method must be invoked during establishment
    * of any client or server using community or collaboration
    * valuetypes.
    */
    
    public static void init( final ORB current_orb ) {

	  SessionSingleton.init( current_orb );

        final org.omg.CORBA_2_3.ORB orb = (org.omg.CORBA_2_3.ORB) current_orb;

	  orb.register_value_factory( AgencyCriteriaHelper.id(), new AgencyCriteria());
	  orb.register_value_factory( CommunityCriteriaHelper.id(), new CommunityCriteria());
	  orb.register_value_factory( ControlHelper.id(), new Control());
	  orb.register_value_factory( CriteriaHelper.id(), new Criteria());
	  orb.register_value_factory( ExternalCriteriaHelper.id(), new ExternalCriteria());
	  orb.register_value_factory( GenericCriteriaHelper.id(), new GenericCriteria());
	  orb.register_value_factory( MemberHelper.id(), new Member());
	  orb.register_value_factory( MembershipCountHelper.id(), new MembershipCount());
	  orb.register_value_factory( MembershipModelHelper.id(), new MembershipModel());
	  orb.register_value_factory( MembershipPolicyHelper.id(), new MembershipPolicy());
	  orb.register_value_factory( ProblemHelper.id(), new Problem());
	  orb.register_value_factory( RecognizesHelper.id(), new Recognizes());
	  orb.register_value_factory( RoleHelper.id(), new Role());
	  orb.register_value_factory( RolePolicyHelper.id(), new RolePolicy());
	  orb.register_value_factory( RoleStatusHelper.id(), new RoleStatus());
	  orb.register_value_factory( UserCriteriaHelper.id(), new UserCriteria());
	  orb.register_value_factory( MessageCriteriaHelper.id(), new MessageCriteria());
    }
}
