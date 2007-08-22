/*
 * @(#)ContextAdapter.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/06/2001
 */

package net.osm.shell;

import java.util.Iterator;
import java.util.LinkedList;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.AWTEventMulticaster;

import net.osm.shell.Entity;
import net.osm.shell.ContextListener;
import net.osm.shell.ContextEvent;

/**
 * Utility class supporting content event management.
 */

class ContextAdapter implements ContextListener
{
    //======================================================================
    // state
    //======================================================================

    private final LinkedList listeners = new LinkedList();

    //======================================================================
    // constructor
    //======================================================================

   /**
    * Default constructor.
    */
    public ContextAdapter( )
    {
    }

   /**
    * Adds a <code>ContextListener</code>.
    */
    public void addContextListener( ContextListener listener )
    {
        if( listener == null ) throw new RuntimeException(
          "Cannot add a null listener.");
        listeners.add( listener );
    }

   /**
    * Removes a <code>ContextListener</code>.
    */
    public void removeContextListener( ContextListener listener )
    {
        if( listener != null ) listeners.remove( listener );
    }

    //======================================================================
    // FocusListener
    //======================================================================

    public void contextGained( ContextEvent event )
    {
        fireContextEvent( event );
    }

    public void contextLost( ContextEvent event )
    {
        fireContextEvent( event );
    }

   /**
    * Proceses context events on this handler by dispatching a 
    * ContextEvent to all registered listeners.
    */
    public void fireContextEvent( ContextEvent event )
    {
        LinkedList list = null;
        synchronized( listeners )
	  {
            list = (LinkedList) listeners.clone();
        }
        try
	  {
	      Iterator iterator = list.iterator();
            switch( event.getMode() )
            {
            case ContextEvent.CONTEXT_GAINED:
                while( iterator.hasNext() ) 
                {
                    ContextListener listener = (ContextListener) iterator.next();
	  	        listener.contextGained( event );
                }
		    break;
            case ContextEvent.CONTEXT_LOST:
                while( iterator.hasNext() ) 
                {
                    ContextListener listener = (ContextListener) iterator.next();
	  	        listener.contextLost( event );
                }
		    break;
            }
        }
        catch( Exception e )
        {
            System.out.println("Exception occured inside fireContextEvent");
		e.printStackTrace();
        }
    }
}
