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

import java.awt.Component;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import net.dpml.transit.model.HostModel;
import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.model.HostListener;
import net.dpml.transit.model.HostChangeEvent;
import net.dpml.transit.model.HostPriorityEvent;
import net.dpml.transit.model.HostLayoutEvent;
import net.dpml.transit.model.HostNameEvent;
import net.dpml.transit.model.LayoutModel;
import net.dpml.transit.model.LayoutRegistryListener;
import net.dpml.transit.model.LayoutRegistryEvent;

class LayoutComboBoxModel extends DefaultComboBoxModel
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final LayoutRegistryModel m_model;
    private final HostModel m_host;
    private final RemoteListener m_listener;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new combox box containing the available set of 
    * location resolvers declared by a location model and a current
    * selection declared by a supplied host model.
    *
    * @param locations the layout model
    * @param host the host model
    */
    public LayoutComboBoxModel( LayoutRegistryModel model, HostModel host ) throws Exception
    {
        super( model.getLayoutModels() );

        m_model = model;
        m_host = host;

        m_listener = new RemoteListener();
        model.addLayoutRegistryListener( m_listener );
        host.addHostListener( m_listener );
        LayoutModel layout = host.getLayoutModel();
        if( null != layout )
        {
            setSelectedItem( layout );
        }
    }

    public void dispose()
    {
        try
        {
            m_model.removeLayoutRegistryListener( m_listener );
            m_host.removeHostListener( m_listener );
        }
        catch( Throwable e )
        {
        }
    }

    //------------------------------------------------------------------------------
    // RemoteListener
    //------------------------------------------------------------------------------

    private class RemoteListener extends UnicastRemoteObject 
      implements HostListener, LayoutRegistryListener
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
            int n = m_model.getLayoutModels().length;
            fireContentsChanged( this, 0, n );
        }
    
       /**
        * Notify all listeners of the removal of a layout model.
        * @param event the layout registry event
        */
        public void layoutRemoved( LayoutRegistryEvent event ) throws RemoteException
        {
            int n = m_model.getLayoutModels().length;
            fireContentsChanged( this, 0, n );
        }

        //--------------------------------------------------------------
        // HostListener (listen for changes to the selected layout)
        //--------------------------------------------------------------

       /**
        * Notify a consumer of an aggregated set of changes.
        * @param event the host change event
        */
        public void hostChanged( HostChangeEvent event ) throws RemoteException
        {
        }

       /**
        * Notify a consumer of a change to the host priority.
        * @param event the host event
        */
        public void priorityChanged( HostPriorityEvent event ) throws RemoteException
        {
        }
    
       /**
        * Notify a consumer of a change to the host name.
        * @param event the host event
        */
        public void nameChanged( HostNameEvent event ) throws RemoteException
        {
        }

       /**
        * Notify a consumer of a change to the host priority.
        * @param event the host event
        */
        public void layoutChanged( HostLayoutEvent event ) throws RemoteException
        {
            LayoutModel layout = event.getLayoutModel();
            if( null != layout )
            {
                setSelectedItem( layout );
            }
        }
    }
}
