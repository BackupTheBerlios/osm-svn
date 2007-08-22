/*
 * EditMenu.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 19/08/2001
 */

package net.osm.shell;

import java.util.List;
import java.util.Iterator;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Action;

import net.osm.util.ExceptionHelper;

/**
 * The create menu.
 */

final class CreateMenu extends JMenu
{

    private Panel panel;

   /**
    * Creates a new 'Create' menu without a key assignment.
    */
    public CreateMenu( String label )
    {
        this( label, -1 );
    }

   /**
    * Creates a new 'Create' menu with a supplied label and key assignment.
    */
    public CreateMenu( String label, int key )
    {
	  super( label );
        if( key > -1 ) setMnemonic( key );
        setFont( MGR.font );
        setEnabled( false );
    }

    public void setPanel( Panel panel )
    {
        removeAll();
	  try
	  {
		if( panel instanceof ActionHandler )
	      {
		    List list = ((ActionHandler)panel).getActions( );
		    if( list.size() > 0 )
		    {
			  Iterator iterator = list.iterator();
		 	  while( iterator.hasNext() )
			  {
			      add( new MenuItem( (Action) iterator.next() ));
			  }
		    }
	      }
	  }
	  catch( Exception e )
	  {
            final String error = "unexpected exception while retrieving panel factory actions";
		throw new RuntimeException( error, e );
	  }
        setEnabled( getItemCount() > 0 );
    }
}

