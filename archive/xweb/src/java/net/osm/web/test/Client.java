/*
 * @(#)Client.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 03/04/2001
 */

package net.osm.web.test;

import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.ProcessorModel;
import org.omg.CollaborationFramework.ProcessorCriteria;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.GenericCriteria;
import org.omg.CommunityFramework.Problem;
import org.omg.CommunityFramework.ResourceFactoryProblem;
import org.omg.CommunityFramework.Community;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosLifeCycle.FactoryFinder;
import org.omg.CosLifeCycle.FactoryFinderHelper;
import org.omg.CosLifeCycle.NVP;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.IdentifiableDomainConsumer;
import org.omg.Session.IdentifiableDomainConsumerHelper;
import org.omg.Session.MessageBody;
import org.omg.Session.MessageBodyBase;
import org.omg.Session.MessageHeader;
import org.omg.Session.MessageHeaderBase;
import org.omg.Session.MessagePriority;
import org.omg.Session.MessageClassification;
import org.omg.Session.SystemMessageBase;
import org.omg.Session.SystemMessagesHolder;
import org.omg.Session.SystemMessageIterator;
import org.omg.Session.SystemMessage;
import org.omg.Session.TasksHolder;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.LinkHelper;
import org.omg.Session.User;
import org.omg.Session.UserHelper;
import org.omg.Session.Task;
import net.osm.dpml.criteria.UserCriteria;
import net.osm.dpml.criteria.MessageCriteria;
import net.osm.dpml.criteria.DPMLSingleton;
import net.osm.hub.home.Finder;
import net.osm.hub.home.FinderHelper;
import net.osm.hub.home.ResourceFactory;
import net.osm.hub.home.ResourceFactoryHelper;
import net.osm.hub.home.Principal;
import net.osm.hub.home.PrincipalHelper;
import net.osm.util.IOR;


/**
 * General test and validation client application that uses the Enterprise API to 
 * populate the gateway with users and add these users as members of the root community.
 */

public class Client
{
    
    protected static Thread thread;
    protected static ORB orb;
    protected static java.io.PrintWriter out;
    
    public static void main( String args[] )
    {
        
        print("\nCopyright, January 2000 - OSM SARL.\n");
        if( args.length < 1 ) throw new RuntimeException("bad argument set");
	  String FINDER_STRING = args[0];

        //
        // initialize client
        //
        
        orb = ORB.init( args,null );
        DPMLSingleton.init( orb );
        Finder finder = null;
        ResourceFactory factory = null;
	  Community community = null;
        
        // 
	  // locate the factory finder
        //

        try
        {
            print("\n\tLocating finder." );
            finder = FinderHelper.narrow( IOR.readIOR( orb, FINDER_STRING ) );

            print("\tResolving root Community." );
		community = finder.community();

            print("\tResolving resource factory." );
		factory = finder.resource_factory();

        }
        catch( Exception e )
        {
            print ("Unexpected exception while resolving finder/community/factory.");
            e.printStackTrace();
            System.exit(0);
        }
        
        //
        // commence bootstrap
        //

	  Client c = new Client();

	  try
	  {
	     c.bootstrap( finder, factory, community );
	  }
	  catch( Exception e )
	  {
	      e.printStackTrace();
	  }

	  System.exit(0);
    }


    public void bootstrap( FactoryFinder finder, ResourceFactory factory, Community community )
    {
        
	  Processor processor = null;
        Principal user = null;
        Task task = null;
	  AbstractResource r = null;

	  try
	  {

		ProcessorCriteria criteria = (ProcessorCriteria) factory.criterion( "hello" );
		processor = ProcessorHelper.narrow( factory.create( "Hello World Processor", criteria ));
		ProcessorModel model = (ProcessorModel) processor.model();

		// create a user from which we can create a task to coordinate the 
		// processor

		UserCriteria ucriteria = new UserCriteria();
            user = PrincipalHelper.narrow( factory.create( "John Smith", ucriteria ));
		user.connect();
            User user2 = PrincipalHelper.narrow( factory.create( "Armelle Gauffenic", ucriteria )).getUser();
            User user3 = PrincipalHelper.narrow( factory.create( "Tony Noninck", ucriteria )).getUser();
            User user4 = PrincipalHelper.narrow( factory.create( "Anne Meyrignac", ucriteria )).getUser();

		// create a resource, a task owned by the User, and bind the resource to 
            // the task as a consumed resource

		task = user.create_task( "Hello World", processor, null );
		r = factory.create("Hello World Contract Model", new GenericCriteria());
		task.add_consumed( r, "contract" );

		// put the resource into the public community, put reference to the 
            // resource in the user's desktop along with a reference to the
            // processor

		user.get_desktop().add_contains_resource( r );
		user.get_desktop().add_contains_resource( processor );
		community.add_contains_resource( r );
		community.add_contains_resource( user );
		community.add_contains_resource( user2 );
		community.add_contains_resource( user3 );
		community.add_contains_resource( user4 );
		community.add_contains_resource( user.get_desktop() );

		community.join( user, new String[]{"administrator"} );
		community.join( user2, new String[]{"guest"} );
		community.join( user3, new String[]{"guest"} );
		community.join( user4, new String[]{"guest"} );

		String subject = "Startup notification";
		String message = "Startup of the OSM Collaboration Platform complete.";
		user.enqueue( createMessage( subject, message, community ) );

		//SystemMessageIterator iterator = user.get_messages( 0, new SystemMessagesHolder() );
		//System.out.println( iterator.retrieve_element( new AnyHolder() ));

	  }
	  catch( Exception e )
	  {
		e.printStackTrace();
	  }
    }

    SystemMessage createMessage( String subject, String text, AbstractResource source )
    {
        MessageHeader header = new MessageHeaderBase( 
		subject, MessagePriority.NORMAL, 
		MessageClassification.INFORM,  
		source );
	  MessageBody body = new MessageBodyBase("TEXT", text );
	  return new SystemMessageBase( header, body );
    }
    
    public void printProblems( Problem[] problems )
    {
        for( int i = 0; i<problems.length; i++ )
        {
		Problem p = problems[i];
		p.print();
        }
    }

    public static void print( String s )
    {
       System.out.println( s );
    }
}
