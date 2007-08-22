
package net.osm.agent;

import java.util.Iterator;
import java.util.LinkedList;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.Link;
import org.omg.Session.LinkHelper;
import org.omg.Session.LinksHolder;
import org.omg.Session.LinkIterator;
import org.omg.Session.AbstractResource;
import org.omg.Session.User;
import org.omg.Session.SystemMessage;
import org.omg.Session.SystemMessageHelper;
import org.omg.Session.WorkspaceHelper;
import org.omg.CommunityFramework.CommunityHelper;
import org.omg.Session.Collects;

import org.apache.avalon.framework.logger.Logger;

import net.osm.shell.Entity;
import net.osm.util.ListEvent;
import net.osm.util.ListHandler;
import net.osm.util.ListListener;
import net.osm.util.ExceptionHelper;
import net.osm.agent.util.CollectionIterator;
import net.osm.agent.Agent;
import net.osm.entity.EntityService;
import net.osm.audit.RemoteEventListener;
import net.osm.audit.RemoteEvent;
import net.osm.audit.AuditService;
import net.osm.util.ActiveList;
import net.osm.util.List;

/**
 * The <code>MessageCollection</code> class maintains a list of messages 
 * that have been enqueued against a <code>User</code>.  The list is maintained
 * through tracking of "post" events from the target user.
 */
public class MessageCollection extends ActiveList implements RemoteEventListener
{

    //=========================================================================
    // static
    //=========================================================================

    private static final boolean trace = false;

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The service we use to create new message agents.
    */
    EntityService service;

   /**
    * The AbstractResource primary object reference.
    */
    User primary;

   /**
    * The audit service we are listening to changes from.
    */
    AuditService audit;

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>MessageCollection</code> based on supplied iterator
    * and the maintenance of that collection base on a supplied link class.
    *
    * @param logger - the logging channel to be used by the list
    * @param orb - object request broker
    * @param service - agent service used to resolve new message instances returned from the iterator
    * @param primary - the primary User object reference 
    * @param audit - the audit service producing remote post events
    * @param iterator a CosCollection iterator used to establish the initial collection
    */
    public MessageCollection( Logger logger, ORB orb, EntityService service,
      User primary, AuditService audit, org.omg.CosCollection.Iterator iterator )
    {

	  super( logger );

	  if( orb == null ) throw new RuntimeException(
	    "MessageCollection. Null orb argument to constructor.");
	  if( service == null ) throw new RuntimeException(
	    "MessageCollection. Null service argument to constructor.");
	  if( primary == null ) throw new RuntimeException(
	    "MessageCollection. Null primary argument to constructor.");
	  if( audit == null ) throw new RuntimeException(
	    "MessageCollection. Null audit argument to constructor.");
	  if( iterator == null ) throw new RuntimeException(
	    "MessageCollection. Null iterator argument to constructor.");

	  this.service = service;
	  this.primary = primary;
	  this.audit = audit;

	  try
	  {
            final org.omg.CORBA_2_3.ORB portableOrb = (org.omg.CORBA_2_3.ORB) orb;
	      audit.addRemoteEventListener( primary, this );
	      populate( new CollectionIterator( orb, service, iterator, true ));
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException(
		  "MessageCollection. Unable to establish collection.", e );
	  }
    }

    //=========================================================================
    // RemoteEventListener
    //=========================================================================

   /**
    * Method invoked when an an event has been received from a 
    * remote source signalling a state change in the source
    * object.
    */
    public void remoteChange( RemoteEvent event )
    {
	  if( event.getDomain().equals("org.omg.session") )
	  {
		if( event.getType().equals("enqueue") )
		{
		    handleEnqueueEvent( event );
		}
		if( event.getType().equals("dequeue") )
		{
		    handleDequeueEvent( event );
		}
	  }
    }

    private void handleEnqueueEvent( RemoteEvent event )
    {
        try
	  {
		SystemMessage message = SystemMessageHelper.extract( event.getProperty("message"));
            add( service.resolve( message ));
        }
	  catch( Exception e )
	  {
	      String error = "Bad message content received in remote event.";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
	      throw new RuntimeException( error, e );
	  }
    }

    private void handleDequeueEvent( RemoteEvent event )
    {
        try
	  {
            Any any = event.getProperty("identifier");
		long identifier = any.extract_longlong();
		System.out.println("\t" + identifier );
		synchronized( this )
		{
		    Iterator iterator = iterator();
		    while( iterator.hasNext() )
		    {
			  MessageAgent agent = (MessageAgent) iterator.next();
			  if( agent.getIdentifier() == identifier )
			  {
				remove( agent );
				break;
			  }
		    }
		}
        }
	  catch( Exception e )
	  {
	      String error = "bad dequeue event";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
	      throw new RuntimeException( error, e );
	  }
    }


    //=========================================================================
    // operations
    //=========================================================================

   /**
    * Populates the collection with elements extracted from the list supplied 
    * under the constructor. 
    */
    private void populate( CollectionIterator iterator )
    {
	  if( iterator == null ) throw new RuntimeException(
	    "MessageCollection. Null argument supplied to populate.");

	  try
	  {
	      while( iterator.hasNext() )
            {
		    add( (Entity)iterator.next() );
            }
        }
        catch( Exception e )
        {
		e.printStackTrace();
            throw new RuntimeException(
              "MessageCollection. Failed to populate the collection.", e );
        }
    }
}
