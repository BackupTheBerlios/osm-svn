/*
 * @(#)TablePanel.java
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

import java.awt.Font;
import java.util.LinkedList;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;

import net.osm.shell.MGR;
import net.osm.shell.ContextEvent;
import net.osm.shell.ContextListener;
import net.osm.shell.Entity;
import net.osm.shell.FeatureTableModel;
import net.osm.shell.Panel;


/**
 * A panel presenting entities in the form of a table.
 */

public class FeaturesPanel extends TablePanel
{

    //======================================================================
    // static
    //======================================================================

    private static final boolean trace = false;

    public static TableColumnModel newFeaturesColumnModel( Font font )
    {
        //
	  // create a column model
	  //

	  TableColumn iconColumn = new TableColumn( 0, 30, new DefaultCellRenderer( font ), null );
        iconColumn.setHeaderValue("");
	  iconColumn.setMaxWidth( 30 );
	  iconColumn.setMinWidth( 30 );

	  TableColumn nameColumn = new TableColumn( 1, 100, new DefaultCellRenderer( font ), null );
        nameColumn.setHeaderValue("Name");
	  TableColumn valueColumn = new TableColumn( 2, 210, new DefaultCellRenderer( font ), null );
        valueColumn.setHeaderValue("Value");

        TableColumnModel columnModel = new DefaultTableColumnModel();
	  columnModel.addColumn( iconColumn  );
	  columnModel.addColumn( nameColumn  );
	  columnModel.addColumn( valueColumn );

	  return columnModel;
    }

    private static FeatureTableModel newFeatureTableModel( Entity entity )
    {
        return new FeatureTableModel( entity );
    }

    //======================================================================
    // constructor
    //======================================================================

   /**
    * Creation of a new FeaturesPanel.
    */
    public FeaturesPanel( Entity entity, String role )
    {
	  this( entity, role, MGR.font );
    }

   /**
    * Creation of a new FeaturesPanel.
    */
    public FeaturesPanel( Entity entity, String role, Font font )
    {
	  super( entity, role, 
	    newFeatureTableModel( entity ), 
	    newFeaturesColumnModel( font ) 
        );
    }

   /**
    * Creation of a new FeaturesPanel.
    */
    public FeaturesPanel( Entity entity, String role, TableColumnModel model )
    {
	  super( entity, role, newFeatureTableModel( entity ), model );
    }

}
