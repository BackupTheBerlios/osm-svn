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
import net.dpml.transit.model.ProxyListener;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.model.ProxyEvent;

/**
 * Table model that presents the set of excluded hosts.
 */
class ProxyExcludesTableModel extends AbstractTableModel
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final ProxyModel m_model;
    private final RemoteListener m_listener;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

    public ProxyExcludesTableModel( ProxyModel model ) throws Exception
    {
        super();
        m_model = model;
        m_listener = new RemoteListener();
        model.addProxyListener( m_listener );
    }

    protected void dispose()
    {
        try
        {
            m_model.removeProxyListener( m_listener );
        }
        catch( Throwable e )
        {
        }
    }

    //--------------------------------------------------------------------------
    // ProxyListener
    //--------------------------------------------------------------------------

    private class RemoteListener extends UnicastRemoteObject implements ProxyListener
    {
        public RemoteListener() throws RemoteException
        {
            super();
        }

       /**
        * Notify a listener of the change to Transit proxy settings.
        * @param event the proxy change event
        */
        public void proxyChanged( ProxyEvent event )
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
        return 1;
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
            return m_model.getExcludes().length;
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
        try
        {
            String[] excludes = m_model.getExcludes();
            return excludes[ row ];
        }
        catch( Throwable e )
        {
            return null;
        }
    }
}
