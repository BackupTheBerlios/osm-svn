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

import java.util.Arrays;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import net.dpml.transit.Transit;
import net.dpml.transit.model.CacheDirectoryChangeEvent;
import net.dpml.transit.model.HostModel;
import net.dpml.transit.model.CacheEvent;
import net.dpml.transit.model.CacheListener;
import net.dpml.transit.model.CacheModel;

/**
 * Table model that maps table rows to child nodes of a supplied preferences node.
 */
class CacheTableModel extends AbstractTableModel
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
        CacheTableModel.class.getClassLoader(), ICON_PATH, "Features" );

    private final CacheModel m_manager;

    private final RemoteCacheListener m_cacheListener;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new table model that presents children of the supplied preference
    * node as table entries.
    *
    * @param manager the cache configuration manager
    */
    public CacheTableModel( CacheModel manager ) 
    {
        super();
        m_manager = manager;
        try
        {
            m_cacheListener = new RemoteCacheListener();
            manager.addCacheListener( m_cacheListener );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote exception occured while creating cache listener.";
            throw new RuntimeException( error, e );
        }
    }

    public void dispose()
    {
        try
        {
            m_manager.removeCacheListener( m_cacheListener );
        }
        catch( Throwable e )
        {
        }
    }

    //--------------------------------------------------------------------------
    // CacheListener
    //--------------------------------------------------------------------------

    private class RemoteCacheListener extends UnicastRemoteObject implements CacheListener
    {
        public RemoteCacheListener() throws RemoteException
        {
            super();
        }

       /**
        * Notify the listener of a change to the cache directory.
        * @param event the cache directory change event
        */
        public void cacheDirectoryChanged( CacheDirectoryChangeEvent event )
        {
            // ignore
        }

       /**
        * Notify the listener of the addition of a new host.
        * @param event the host added event
        */
        public void hostAdded( CacheEvent event )
        {
            fireTableStructureChanged();
        }
    
       /**
        * Notify the listener of the removal of a host.
        * @param event the host removed event
        */
        public void hostRemoved( CacheEvent event )
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
            return m_manager.getHostModels().length;
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
            default: 
              return getHostAtRow( row );
        }
    }

    private String getHostAtRow( int row )
    {
        try
        {
            HostModel[] hosts = m_manager.getHostModels();
            HostModel host = hosts[ row ];
            return hosts[ row ].getID();
        }
        catch( Throwable e )
        {
            Logger logger = Logger.getLogger( "depot.prefs" );
            logger.log( Level.SEVERE, "ERROR ON ROW: " + row, e );
            return "";
        }
    }
}
