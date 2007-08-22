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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.CascadingError;
import org.apache.avalon.framework.CascadingThrowable;

import org.omg.CommunityFramework.Community;
import org.omg.CORBA.ORB;
import net.osm.dpml.criteria.DPMLSingleton;
import net.osm.hub.home.Finder;
import net.osm.hub.home.FinderHelper;
import net.osm.util.IOR;
import net.osm.agent.*;

/**
 * Client application that is redundant pending setup of a lauchable agent framework client application.
 */

public class Test
{
    
    protected static ORB orb;
    
    public void execute( CommunityAgent community )
    {
	  try
	  {
		System.out.println("listing members:");
		AgentIterator members = community.getMembers( );
		while( members.hasNext() )
	      {
		    UserAgent user = (UserAgent) members.next();
		    System.out.println( "user: " + user.getName() );
/*
		    AgentIterator roles = community.getMemberRoles( user );
		    while( roles.hasNext() )
	          {
		        RoleAgent role = (RoleAgent) roles.next();
		        System.out.println( "\trole: " + role.getLabel() );
		    }
*/
		}
	  }
	  catch( Throwable e )
	  {
		System.out.println("\nException:\n" );
		doReport( e );
	  }
    }

    public static void doReport( Throwable throwable )
    {
	  System.out.println( "  cause:\n" );

	  String name = throwable.getClass().getName();
	  String msg = throwable.getMessage();
	  if( msg == null ) msg = "no message";

	  System.out.println( "\tname: " + name );
	  System.out.println( "\tmessage: " + msg + "\n");

        if( throwable instanceof CascadingThrowable )
        {
		Throwable cause = ((CascadingThrowable)throwable).getCause();
		doReport( cause );
        }
    }

    public static void main( String args[] )
    {
        
        System.out.println("\nCopyright, January 2000 - OSM SARL.\n");
        if( args.length < 2 )
        {
            System.out.println("\n" +
		"To execute this test you need to provide the path to the finder IOR \n" +
		"as the first argument.  The test assumes that the root community has already \n" +
	      "been initalized with a membership.  The test creates a role filtered iterator \n" +
            "using the second command line argument as the role name.\n"
		);
            System.exit(0);
        }
	  String FINDER_STRING = args[0];
	  String ROLE_NAME = args[1];

        orb = ORB.init( args,null );
        DPMLSingleton.init( orb );
        Finder finder = null;
	  Community community = null;

        try
        {
		AgentServer.boot( );
            finder = FinderHelper.narrow( IOR.readIOR( orb, FINDER_STRING ) );
	      community = finder.community();
	      Test c = new Test();
	      c.execute( new CommunityAgent( orb, community ));
        }
        catch( Exception e )
        {
		System.out.println("\nException:\n" );
		doReport( e );
        }
    	  System.exit(0);
    }


}
