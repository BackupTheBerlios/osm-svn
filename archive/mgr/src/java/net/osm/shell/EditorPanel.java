/*
 * EditorPanel.java
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
import java.util.List;
import java.util.LinkedList;
import javax.swing.JPanel;
import javax.swing.JEditorPane;

/**
 * A panel that presents a heirachical breakdown of entities.
 */

public class EditorPanel extends JEditorPane implements Panel
{

    //======================================================================
    // state
    //======================================================================

   /**
    * The entity that this panel is presenting.
    */
    private Entity entity;

   /**
    * The role that this panel represents relative to the entity.
    */
    private String role;

   /**
    * Context listeners.
    */
    private ContextAdapter adapter = new ContextAdapter();

    //======================================================================
    // constructor
    //======================================================================

   /**
    * Creation of a new NavigatorPanel.
    * @param entity the entity that this table is presenting
    * @param role a label designating the view that this table presents of the entity
    */
    public EditorPanel( Entity entity, String role )
    {
        super();
        this.role = role;
        this.entity = entity;
    }

    //======================================================================
    // Panel
    //======================================================================

   /**
    * Return the name of the role that this panel represents
    * relative to its primary entity.
    * @return String the name of the role that this panel presents
    */
    public String getRole()
    {
        return role;
    }

   /**
    * Return the base entity.
    *
    * @return Entity the entity that this item represents.
    */
    public Entity getEntity( )
    {
        return entity;
    }

   /**
    * Returns a possibly null value corresponding to an entity that is 
    * currently selected.
    * @return Entity the entity currently in focus or null if no entity
    */
    public Entity getDefaultEntity()
    {
	  return null;
    }

    public int getSelectionCount()
    {
        return 0;
    }

    public void clearSelection()
    {
    }

    //============================================================================
    // ContextHandler
    //============================================================================

   /**
    * Adds a <code>ContextListener</code>.
    */
    public void addContextListener( ContextListener listener )
    {
        adapter.addContextListener( listener );
    }

   /**
    * Removes a <code>ContextListener</code>.
    */
    public void removeContextListener( ContextListener listener )
    {
        adapter.removeContextListener( listener );
    }
}
