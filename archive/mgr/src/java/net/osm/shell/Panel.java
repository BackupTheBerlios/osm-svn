/*
 * @(#)Panel.java
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

import net.osm.shell.Entity;

/**
 * A Panel is an Item container.
 */

public interface Panel extends ContextHandler
{

    public static final int SINGLE_SELECTION = 0;
    public static final int MULTIPLE_SELECTION = 1;

   /**
    * Return the name of the role that this panel represents
    * relative to its primary entity.
    * @return String the name of the role that this panel presents
    */
    public String getRole();

   /**
    * Return the base entity.
    *
    * @return Entity the entity that this item represents.
    */
    public Entity getEntity( );

   /**
    * Returns a possibly null value corresponding to an entity that is 
    * currently selected.
    * @return Entity the entity currently in focus or null if no entity
    */
    public Entity getDefaultEntity();

   /**
    * Returns the number of entries in the current selection.
    * @return int the number of entries in the current selection
    */
    public int getSelectionCount();
   
   /**
    * Clears the current selection.
    */
    public void clearSelection();
}
