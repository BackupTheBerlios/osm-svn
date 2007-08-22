/*
 * @(#)Menu.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.shell;

import java.awt.event.KeyEvent;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.osm.shell.Panel;
import net.osm.shell.Entity;
import net.osm.shell.MGR;
import net.osm.util.ExceptionHelper;


/**
 * A menu that handles dynamic content modification.
 */

abstract class Menu extends JMenu
{

    private int count = 0;

    private final Vector actions = new Vector();

    public Menu()
    {
        this( "Untitled" );
    }

    public Menu( String label )
    {
        this( label, KeyEvent.VK_UNDEFINED );
    }

    public Menu( String label, int key )
    {
	  super( label );
        setFont( MGR.font );
        if( key > -1 ) setMnemonic( key );
        getAccessibleContext().setAccessibleDescription( label + " menu");
        update();
    }

    //======================================================================
    // methods
    //======================================================================

    public void setPanel( )
    {
       setPanel( null );
    }

    public abstract void setPanel( Panel panel );

    //======================================================================
    // utilities
    //======================================================================

    protected void update()
    {
	  if( getItemCount() > 0 ) 
	  {
            setEnabled( true );
        }
	  else
	  {
		setEnabled( false );         
        }
    }

    public JMenuItem add( JMenuItem item )
    {
	  try
	  {
            JMenuItem mi = super.add( item );
            update();
            return mi;
	  }
	  catch( Exception e )
	  {
		e.printStackTrace();
	      return item;
	  }
    }

    public void remove( JMenuItem item )
    {
        super.remove( item );
        update();
    }

   /**
    * Remove any currently installed actions then install the 
    * actions supplied in the list.
    */
    protected void setActions( List list )
    {
	  if( list == null ) throw new RuntimeException("Menu, Null list argument in setActions.");
	  clearActions();

        //
        // install the supplied actions
	  //

	  Action action = null;
	  Iterator iterator = list.iterator();
        while( iterator.hasNext() )
	  {
		try
		{
		    //
		    // create a menuitem to hold the action
		    //

		    Object object = iterator.next();
		    if( object instanceof Action )
		    {
		        MenuItem item = new MenuItem();
		        action = (Action) object;
		        item.setAction( action );
		        add( item );
		        actions.add( item );
		    }
	          else
	          {
                    System.out.println("Menu, Warning, list entry is not an action: " + object );
	          }
	      }
	      catch( Exception e )
	      {
                ExceptionHelper.printException("Menu, Error while adding action: " + action , e );
	      }
	  }
        update();
    }

    protected void clearActions()
    {
        //
        // clear any currently installed actions
        //

        for( int i=0; i<actions.size(); i++ )
        {
	      try
	      {
	          remove( (JMenuItem) actions.get(i) );
	      }
	      catch( Exception e )
	      {
                ExceptionHelper.printException("Error removing action.", e );
	      }
	  }
        actions.removeAllElements();
        update();
    }
}

