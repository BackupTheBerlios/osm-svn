/*
 * @(#)EntityTable.java
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

import javax.swing.table.TableModel;

import net.osm.shell.Entity;

/**
 * An EntityTableModel extends TableModel to include an accessor to 
 * an Entity at a given row position.
 */

public interface EntityTable
{

   /**
    * Returns the entity at a particular row.
    * @return Entity the at the row
    */
    public Entity getEntityAtRow( int row );
   
}
