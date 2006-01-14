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
import javax.swing.AbstractListModel;

import net.dpml.transit.Transit;
import net.dpml.transit.model.TransitRegistryListener;
import net.dpml.transit.model.TransitRegistryEvent;
import net.dpml.transit.model.TransitRegistryModel;

/**
 * Table model that maps table rows to child nodes of a supplied preferences node.
 */
class TransitListModel extends AbstractListModel
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final TransitRegistryModel m_model;
    private final RemoteListener m_listener;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new table model that presents children of the supplied preference
    * node as table entries.
    *
    * @param model the registry of application profiles
    */
    public TransitListModel( TransitRegistryModel model ) throws Exception
    {
        super();
        m_model = model;
        m_listener = new RemoteListener();
        model.addTransitRegistryListener( m_listener );
    }

    protected void dispose()
    {
        try
        {
            m_model.removeTransitRegistryListener( m_listener );
        }
        catch( Throwable e )
        {
        }
    }

    //--------------------------------------------------------------------------
    // DepotListener
    //--------------------------------------------------------------------------

    private class RemoteListener extends UnicastRemoteObject implements TransitRegistryListener
    {
        public RemoteListener() throws RemoteException
        {
            super();
        }

       /**
        * Notify all listeners of the addition of an application profile.
        * @param event the depot event
        */
        public void modelAdded( TransitRegistryEvent event ) throws RemoteException
        {
            fireContentsChanged( this, -1, getSize() );
        }
    
       /**
        * Notify all listeners of the removal of an application profile.
        * @param event the depot event
        */
        public void modelRemoved( TransitRegistryEvent event ) throws RemoteException
        {
            fireContentsChanged( this, -1, getSize() );
        }
    }

    //--------------------------------------------------------------------------
    // ListModel
    //--------------------------------------------------------------------------

   /**
    * Returns the number of rows in the model.
    */
    public int getSize()
    { 
        try
        {
            return m_model.getTransitModelCount();
        }
        catch( Throwable e )
        {
            return 0;
        }
    }

   /**
    * Returns the activation profile at the requested index.
    * @param index the list index
    * @return Object
    */
    public Object getElementAt( int index ) 
    {
        try
        {
            return m_model.getTransitModels()[ index ];
        }
        catch( Throwable e )
        {
            return null;
        }
    }

}
