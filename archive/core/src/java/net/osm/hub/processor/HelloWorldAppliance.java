/*
 * @(#)HelloWorldAppliance.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 23/03/2001
 */

package net.osm.hub.processor;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.omg.CollaborationFramework.StateDescriptor;
import org.omg.CosLifeCycle.NVP;
import org.omg.Session.SystemMessage;
import org.omg.Session.MessageClassification;

import net.osm.util.ExceptionHelper;

/**
 * The <code>HelloWorldAppliance</code> is an example of a back-end process
 * implementation. 
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class HelloWorldAppliance extends AbstractAppliance
implements Configurable
{

   /**
    * Message based on local process factory configuration.
    */
    private String message;

   /**
    * Message based on supplied context.
    */
    private String location = "Unknown location";

   /**
    * The number of steps to execute.
    */
    private int count = 10;

   /**
    * Internal counter of the progress of the process.
    */
    private int countdown;

   /**
    * Duration in millisecond of the pause between each step in the process.
    */
    private int pause = 100;

   /**
    * Process constructor.
    */

    //=======================================================================
    // Configurable
    //=======================================================================

   /**
    * Configuration of the process based on a supplied Configuration arguments.
    *
    * @param config Configuration argument supplied by the process factory.
    * @exception ConfigurationException if the supplied configuration is incomplete or badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
	  System.out.println("\t[HELLO] configure" );
	  this.message = config.getChild( "message" , true ).getAttribute( "value", "unknown" );
	  this.count = config.getChild( "count" , true ).getAttributeAsInteger( "value", count );
	  this.pause = config.getChild( "pause" , true ).getAttributeAsInteger( "value", pause );
	  this.countdown = count;
    }

   /**
    * Initate the thread - the implementation simply executes a countdown, printing out
    * value to System.out until it reaches 0.
    */
    public void run()
    {
	  setState( RUNNING );
	  while( getState() == RUNNING )
	  {
		try
		{
		    if( count == 0 )
		    {
			  SystemMessage completion = createMessage( 
			    MessageClassification.INFORM, 
			    "Hello World (Completion)",
			    "<html><body>" +
			    "<h1>Hello World</h1><br><h2>Completion</h2></br>" +
			    "<p>The hello world processor has completed.</p>" +
			    "</body></html>" 
			  );
			  signalCompletion( completion );
			  return;
		    }
		    else if( count == 10 )
		    {
			  count--;
			  SystemMessage suspension = createMessage( 
			    MessageClassification.INFORM, 
			    "Hello World (Suspension)",
			    "<html><body>" +
			    "<h1>Hello World</h1><br><h2>Suspension</h2></br>" +
			    "<p>The hello world processor has suspended.</p>" +
			    "</body></html>"
			  );
			  signalSuspension( suspension );
			  return;
		    }
		    else
		    {
			  try
		        {
		            System.out.println("\tcount: " + (count--));
                        Thread.currentThread().sleep( pause );
			  }
		        catch( Throwable wakeup )
			  {
			  }
		    }
		}
		catch( Throwable e )
		{
		    signalTermination( e );
	          return;
		}
	  }
    }
}
