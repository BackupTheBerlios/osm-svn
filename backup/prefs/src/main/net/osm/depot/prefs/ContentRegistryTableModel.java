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
import net.dpml.transit.model.ContentRegistryEvent;
import net.dpml.transit.model.ContentRegistryListener;
import net.dpml.transit.model.ContentRegistryModel;

/**
 * Table model that maps table rows to child nodes of a supplied preferences node.
 */
class ContentRegistryTableModel extends AbstractTableModel
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

   /**
    * Default small icon path.
    */
    private static final String ICON_PATH = "net/dpml/depot/prefs/images/item.gif";

   /**
    * Constant row identifier for the icon.
    */
    //public static final int ICON = 0;

   /**
    * Constant row identifier for the name.
    */
    //public static final int VALUE = 1;
    public static final int VALUE = 0;

   /**
    * Constant row identifier for the name.
    */
    public static final int CODEBASE = 1;

   /**
    * Number of columns.
    */
    private static final int COLUMN_COUNT = 2;

   /**
    * Default small icon.
    */
    private static final Icon FEATURE_ICON = 
      IconHelper.createImageIcon( 
        ContentRegistryTableModel.class.getClassLoader(), ICON_PATH, "Features" );

    private final ContentRegistryModel m_manager;

    private final RemoteContentRegistryListener m_listener;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new table model that presents children of the supplied preference
    * node as table entries.
    *
    * @param manager the registry configuration manager
    */
    public ContentRegistryTableModel( ContentRegistryModel manager ) 
    {
        super();
        m_manager = manager;
        try
        {
            m_listener = new RemoteContentRegistryListener();
            manager.addRegistryListener( m_listener );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote exception while establishing content registry listener.";
            throw new RuntimeException( error, e );
        }
    }

    public void dispose()
    {
        try
        {
            m_manager.removeRegistryListener( m_listener );
        }
        catch( Throwable e )
        {
        }
    }

    protected void finalize()
    {
        dispose();
    }

    //--------------------------------------------------------------------------
    // ContentRegistryListener
    //--------------------------------------------------------------------------

    private class RemoteContentRegistryListener extends UnicastRemoteObject 
      implements ContentRegistryListener
    {
        public RemoteContentRegistryListener() throws RemoteException
        {
            super();
        }

       /**
        * Notify all listeners of the addition of a content model.
        * @param event the registry event
        */
        public void contentAdded( ContentRegistryEvent event ) throws RemoteException
        {
            fireTableStructureChanged();
        }
    
       /**
        * Notify all listeners of the removal of a content model.
        * @param event the registry event
        */
        public void contentRemoved( ContentRegistryEvent event ) throws RemoteException
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
            return m_manager.getContentModels().length;
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
              return getHostAtRow( row );
            case CODEBASE :
              return getCodeBase( row );
            default: 
              return getHostAtRow( row );
        }
    }

    private String getHostAtRow( int row )
    {
        try
        {
            return m_manager.getContentModels()[ row ].getContentType();
        }
        catch( Throwable e )
        {
            return "";
        }
    }

    private String getCodeBase( int row )
    {
        try
        {
            return m_manager.getContentModels()[ row ].getCodeBaseURI().toString();
        }
        catch( Throwable e )
        {
            return "";
        }
    }

}
