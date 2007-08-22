
package net.osm.agent;

import java.util.Iterator;
import java.util.LinkedList;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.Session.Link;
import org.omg.Session.LinkHelper;
import org.omg.Session.LinksHolder;
import org.omg.Session.AbstractResource;

import org.apache.avalon.framework.logger.Logger;

import net.osm.shell.Entity;
import net.osm.util.ListEvent;
import net.osm.util.ListHandler;
import net.osm.util.ListListener;
import net.osm.util.ExceptionHelper;
import net.osm.agent.Agent;
import net.osm.entity.EntityService;
import net.osm.audit.RemoteEventListener;
import net.osm.audit.RemoteEvent;
import net.osm.audit.AuditService;
import net.osm.util.ActiveList;
import net.osm.util.List;

/**
 * The <code>LinkCollection</code> class maintains a list of entities 
 * based on a collection establised by a romote invocation using 
 * returning a CosCollection iterator, and maintained by notification
 * events from the source object.
 *
 * @osm.warning does not support logging
 */
public class LinkCollection extends ActiveList implements RemoteEventListener
{

    //=========================================================================
    // static
    //=========================================================================

    private static final boolean trace = false;

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The service we use to create new agents.
    */
    EntityService resolver;

   /**
    * The audit service providing remote events to this instance.
    */
    AuditService m_audit;

   /**
    * The AbstractResource primary object reference.
    */
    AbstractResource primary;

    TypeCode type;

    Class clazz;

    Exception exception;

    ORB orb;

    String filter;

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>LinkCollection</code> based on supplied 
    * link type. 
    *
    * @param logger - the log channel
    * @param orb - the current orb
    * @param resolver - agent resolver
    * @param primary - the primary AbstractResource object reference 
    * @param audit - the audit service producing remote events (may be null)
    * @param type - a TypeCode of a link instance
    * @param clazz - the class of link (native equivilent of the typecode)
    */
    public LinkCollection( Logger log, ORB orb, EntityService resolver,
      AbstractResource primary, AuditService audit, TypeCode type, Class clazz )
    {
        this( log, orb, resolver, primary, audit, type, clazz, null );
    }

   /**
    * Constructor of a new <code>LinkCollection</code> based on supplied 
    * link type. 
    *
    * @param logger - the log channel
    * @param orb - the current orb
    * @param resolver - agent resolver
    * @param primary - the primary AbstractResource object reference 
    * @param audit - the audit service producing remote events (may be null)
    * @param type - a TypeCode of a link instance
    * @param clazz - the class of link (native equivilent of the typecode)
    * @param filter - IDL repository identifier of the link target type
    */
    public LinkCollection( Logger log, ORB orb, EntityService resolver,
      AbstractResource primary, AuditService audit, TypeCode type, Class clazz, String filter )
    {
	  super( log );
	  this.orb = orb;
	  this.resolver = resolver;
	  this.primary = primary;
	  this.m_audit = audit;
	  this.type = type;
	  this.clazz = clazz;
	  this.filter = filter;

	  if( log.isDebugEnabled() ) log.debug(
		"LinkCollection created\n\tcollection: " + this );

	  populate( );
	
	  if( audit != null ) 
	  {
		try
		{
	          if( log.isDebugEnabled() ) log.debug(
		      "registering collection as a listener" );
		    audit.addRemoteEventListener( primary, this );
		}
		catch( Throwable e )
		{
		    final String error = "Unable to register the collection as a listener.";
		    throw new RuntimeException( error, e );
		}
	  }
	  else
	  {
	      if( log.isDebugEnabled() ) log.debug(
		  "bypassing listener registration" );
	  }
    }

    //=========================================================================
    // RemoteEventListener
    //=========================================================================

   /**
    * Method invoked when an an event has been received from a 
    * remote source signalling a state change in the source
    * object.
    * @osm.warning error and log management pending
    */
    public void remoteChange( RemoteEvent event )
    {
	  if( event.getDomain().equals("org.omg.session") )
	  {
		if( event.getType().equals("bind") )
		{
		    handleAddition( LinkHelper.extract( event.getProperty("link")));
		}
		else if( event.getType().equals("release") )
		{
		    handleRemoval( LinkHelper.extract( event.getProperty("link")));
		}
		else if( event.getType().equals("replace") )
		{
		    boolean result = handleRemoval( LinkHelper.extract( event.getProperty("old")));
		    if( result ) handleAddition( LinkHelper.extract( event.getProperty("new")));
		}
            else if( event.getType().equals("remove") )
		{
		    // this event is signallying the removal of the entity
		    // from which this collection is maintained - its the responsibility 
		    // of the entity to destroy the collections it creates so we don't need
	          // to take any action here
		}
	  }
    }

    private boolean handleAddition( Link link )
    {
	  if( !clazz.isInstance( link ) ) return false;
        if( filter != null )
        {
	      AbstractResource r = link.resource();
	      if( !r._is_a( filter ) ) return false;
	  }
	  try
	  {
	      return add( resolver.resolve( link ) );
	  }
	  catch( Exception e )
	  {
		return false;
	  }
    }

    private boolean handleRemoval( Link link )
    {
	  if( !clazz.isInstance( link ) ) return false;

	  //
	  // locate a linkAgent in this list that matches the 
	  // link supplied under the remote event
	  //

	  LinkAgent agent = locateLinkAgent( link );
 	  if( agent == null ) return false;
	  return remove( agent );
    }

    private synchronized LinkAgent locateLinkAgent( Link link )
    {
        Iterator iterator = iterator();
	  while( iterator.hasNext() )
	  {
		LinkAgent source = (LinkAgent) iterator.next();
		if( source.getPrimaryTarget() == null )
		{
		    final String debug = "removing null link ";
		    if( getLogger().isDebugEnabled() ) getLogger().debug( debug + source );
		    remove( source );
		}
		else
		{
                if( source.equivalent( link.resource() )) return source;
		}
	  }
        return null;
    }

    //=========================================================================
    // operations
    //=========================================================================

   /**
    * Populates the collection with elements extracted from the list supplied 
    * under the constructor. 
    */
    public void populate()
    {
        try
        {
		LinksHolder holder = new LinksHolder();
		org.omg.Session.LinkIterator linkIt = primary.expand( type, 0, holder );
            Iterator iterator = new LinkIterator( orb, linkIt );
	      while( iterator.hasNext() )
            {
		    handleAddition( (Link) iterator.next() );
            }
        }
        catch( Exception e )
        {
            throw new RuntimeException("Unable to populate the collection.", e );
        }
    }

   /**
    * Returns a string representation of the event.
    */
    public String toString()
    {
        return getClass().getName() + 
          "[id: " + System.identityHashCode( this ) + 
          " size: " + size() + 
          " class: " + clazz.getName() +
          " filter: " + filter + 
          "]";
    }

}
