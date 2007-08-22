/*
 * @(#)ListenerList.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.audit;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.StructuredPushSupplier;


/**
 * A list of remote event listeners that resgistered to receive 
 * events from a structured push supplier that this list holds a 
 * reference to.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class ListenerList extends AbstractLogEnabled
implements LogEnabled
{

    //==================================================
    // state
    //==================================================

    private StructuredPushSupplier m_supplier;

    private final LinkedList listeners = new LinkedList();

    //=======================================================================
    // Constructor
    //=======================================================================
        
   /**
    * Creation of a new listener list based on a supplier source resource.
    * @param resource the abstract resource that will be the source of events
    */
    public ListenerList( StructuredPushSupplier supplier )
    {
        if( supplier == null ) throw new NullPointerException(
	    "Null strucured push supplier supplied as a constructor argument.");

        m_supplier = supplier;
    }

    //=======================================================================
    // ListenerList
    //=======================================================================

   /**
    * Return the source <code>StructuredPushSupplier</code> that this set of 
    * listeners is listening to.
    */
    public StructuredPushSupplier getSupplier()
    {
        return m_supplier;
    }

    public void add( RemoteEventListener listener )
    {
        synchronized( listeners )
	  {
		listeners.add( listener );
	  }
    }

    public void remove( final RemoteEventListener listener )
    {
        synchronized( listeners )
	  {
		listeners.remove( listener );
	  }
    }

    public int locate( RemoteEventListener listener )
    {
        synchronized( listeners )
	  {
		return listeners.indexOf( listener );
	  }
    }

    public RemoteEventListener get( int index )
    {
        synchronized( listeners )
	  {
		return (RemoteEventListener) listeners.get( index );
	  }
    }

    public int size( )
    {
        synchronized( listeners )
	  {
		return listeners.size();
	  }
    }

   /**
    * Fire a remote event on all of the local listeners that are subscribed.
    * @param event the incomming <code>RemoteEvent</code> to be forwarded 
    * to the set of event listeners
    */
    public void fireStructuredEvent( final RemoteEvent event )
    {
        Thread thread = new Thread( 
            new Runnable()
            {
                public void run()
		    {
                    propergateEvent( event );
		    }
            }
        );
	  thread.start();
        if( getLogger().isDebugEnabled() ) getLogger().debug( "started remote event thread" );
   }

    protected void propergateEvent( final RemoteEvent event )
    {
	  final Iterator iterator  = getClonedList().iterator();
        while( iterator.hasNext() )
        {
		try
		{
		    //
		    // Need to check if the listener is subscribed to the 
		    // particular event type (not implemented yet)
	          //

		    final RemoteEventListener listener = (RemoteEventListener)iterator.next();
                if( getLogger().isDebugEnabled() ) getLogger().debug( 
			"issuing notification to listener: " 
			+ listener );
		    listener.remoteChange( event );
		}
		catch( Throwable e )
		{
		    //
		    // log the error but continue execution
		    //

		    final String warning = "Remote event listener raised an exception.";
		    if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
            }
        }
    }

    private List getClonedList()
    {
        synchronized( listeners )
	  {
	      return (List) listeners.clone();
        }
    }

    public boolean equals( Object other )
    {
	  if( !this.getClass().equals( other.getClass() )) return false;
	  return ( System.identityHashCode( this ) == System.identityHashCode( other ) );
    }

    public String toString()
    {
        return getClass().getName() + 
          "[" +	
          "id=" + System.identityHashCode( this ) + " " + 
          "size=" + size() +
          "]";
    }
}
