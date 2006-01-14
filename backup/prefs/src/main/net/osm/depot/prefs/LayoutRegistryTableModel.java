/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.depot.prefs;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import net.dpml.transit.Transit;
import net.dpml.transit.model.LayoutRegistryEvent;
import net.dpml.transit.model.LayoutRegistryListener;
import net.dpml.transit.model.LayoutRegistryModel;

/**
 * Table model that maps table rows to child nodes of a supplied preferences node.
 */
class LayoutRegistryTableModel extends AbstractTableModel
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

   /**
    * Default small icon path.
    */
    private static final String ICON_PATH = "net/dpml/depot/prefs/images/item.gif";

   /**
    * Constant row identifier for the name.
    */
    public static final int VALUE = 0;

   /**
    * Number of columns.
    */
    private static final int COLUMN_COUNT = 1;

   /**
    * Default small icon.
    */
    private static final Icon FEATURE_ICON = 
      IconHelper.createImageIcon( 
        LayoutRegistryTableModel.class.getClassLoader(), ICON_PATH, "Features" );

    private final LayoutRegistryModel m_manager;
    private final RemoteListener m_listener;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new table model that presents children of the supplied preference
    * node as table entries.
    *
    * @param manager the registry configuration manager
    */
    public LayoutRegistryTableModel( LayoutRegistryModel manager ) throws Exception
    {
        super();
        m_manager = manager;
        m_listener = new RemoteListener();
        manager.addLayoutRegistryListener( m_listener );
    }

    protected void dispose()
    {
        try
        {
            m_manager.removeLayoutRegistryListener( m_listener );
        }
        catch( Throwable e )
        {
        }
    }

    //------------------------------------------------------------------------------
    // RemoteListener
    //------------------------------------------------------------------------------

    private class RemoteListener extends UnicastRemoteObject 
      implements LayoutRegistryListener
    {
        public RemoteListener() throws RemoteException
        {
            super();
        }

        //--------------------------------------------------------------------------
        // LayoutRegistryListener (listen to changes to the available layouts)
        //--------------------------------------------------------------------------

       /**
        * Notify all listeners of the addition of a layout registry model.
        * @param event the layout registry event
        */
        public void layoutAdded( LayoutRegistryEvent event ) throws RemoteException
        {
            fireTableStructureChanged();
        }
    
       /**
        * Notify all listeners of the removal of a layout model.
        * @param event the layout registry event
        */
        public void layoutRemoved( LayoutRegistryEvent event ) throws RemoteException
        {
            fireTableStructureChanged();
        }
    }

    //--------------------------------------------------------------------------
    // NodeTableModel
    //--------------------------------------------------------------------------

   /**
    * Returns the number of model columns.
    * @return int the number of columns maintained by the model
    */
    public int getColumnCount()
    { 
        return COLUMN_COUNT;
    }

   /**
    * Returns the number of rows in the model.  The value returned is
    * equivilent to the number of elements in the list backing the model.
    * @return int the number of rows maintained by the model
    */
    public int getRowCount()
    {
        try
        {
            return m_manager.getLayoutModels().length;
        }
        catch( Throwable e )
        {
            return 0;
        }
    }

   /**
    * Returns the feature object at the request column and row combination.
    * If the col index is out of range the method returns the agent corresponding
    * to the row identifier.
    * @param row the row index
    * @param col the column index
    * @return Object
    */
    public Object getValueAt( int row, int col ) 
    { 
        Object result = "";
        if( row > getRowCount() )
        {
            return result;
        }

        switch( col )
        {
            case VALUE :
              return getResolverAtRow( row );
            default: 
              return getResolverAtRow( row );
        }
    }

    private String getResolverAtRow( int row )
    {
        try
        {
            return m_manager.getLayoutModels()[ row ].getID();
        }
        catch( Throwable e )
        {
            return "";
        }
    }
}
