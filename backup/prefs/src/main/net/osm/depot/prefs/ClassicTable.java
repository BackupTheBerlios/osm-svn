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

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * A table mapping the collection of preference nodes to hosts.
 */
public class ClassicTable extends JTable implements ListSelectionListener, FocusListener
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

   /**
    * The enabled color.
    */
    private static final Color ENABLED_COLOR = new Color( 204, 204, 255 );

   /**
    * The disabled color.
    */
    private static final Color DISABLED_COLOR = new Color( 228, 228, 255 );

   /**
    * Double-click count.
    */
    private static final int TWO = 2;

   /**
    * Row height.
    */
    private static final int ROW_HEIGHT = 18;

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

   /**
    * The currently selected item if only 1 instance is selected.
    */
    private Object m_selection;

   /**
    * The table data model.
    */
    private TableModel m_data;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new table.
    * @param data a table model containing the data to layout in view
    * @param columns the table column model to apply
    */
    public ClassicTable( TableModel data, TableColumnModel columns )
    {
        super( data, columns );

        m_data = data;
        setShowGrid( false );
        setRowHeight( ROW_HEIGHT );
        setShowVerticalLines( false );
        setShowHorizontalLines( false );
        getTableHeader().setReorderingAllowed( false );
        MouseListener popupListener = new PopupListener();
        addMouseListener( popupListener );
        addFocusListener( this );
    }

    //--------------------------------------------------------------------------
    // ListSelectionListener
    //--------------------------------------------------------------------------

   /**
    * Listens to changes in the selected state of the table and 
    * propergates a <code>ContextEvent</code> referencing this table as 
    * the event's panel when the table selection changes.
    * @param event a list selection event
    */
    public void valueChanged( ListSelectionEvent event )
    {
        super.valueChanged( event );
        if( !event.getValueIsAdjusting() ) 
        {
            ListSelectionModel model = getSelectionModel();
            synchronized( model )
            {
                Object old = m_selection;
                int n = model.getMinSelectionIndex();
                if( ( n == model.getMaxSelectionIndex() ) && ( n > -1 ) ) 
                {
                    m_selection = m_data.getValueAt( n, -1 );
                }
                else if( m_selection != null ) 
                {
                    m_selection = null;
                }
                firePropertyChange( "selection", old, m_selection );
            }
        }
    }

    //--------------------------------------------------------------------------
    // FocusListener
    //--------------------------------------------------------------------------

   /**
    * Handle the focus gained event.
    * @param event the focus event
    */
    public void focusGained( FocusEvent event )
    {
        //setSelectionBackground( ENABLED_COLOR );
    }

   /**
    * Handle the focus lost event.
    * @param event the focus event
    */
    public void focusLost( FocusEvent event )
    {
        //setSelectionBackground( DISABLED_COLOR );
    }

   /**
    * Mouse adapter.
    */
    private class PopupListener extends MouseAdapter 
    {
       /**
        * Handle a mouse event.
        * @param event the mouse pressed event
        */
        public void mousePressed( MouseEvent event ) 
        {
            handleMouseEvent( event );
        }

       /**
        * Handle a mouse event.
        * @param event the mouse released event
        */
        public void mouseReleased( MouseEvent event ) 
        {
            handleMouseEvent( event );
        }
    }

   /**
    * Handle a mouse event within the table.
    * @param event the mouse event
    */
    protected void handleMouseEvent( MouseEvent event )
    {
        if( event.isPopupTrigger() )
        {
            int j = rowAtPoint( event.getPoint() );
            if( j > -1 ) 
            {
                if( !isRowSelected( j ) ) 
                {
                    clearSelection();
                    setRowSelectionInterval( j, j );
                }
                firePropertyChange( "popup", null, m_selection );
            }
            else
            {
                clearSelection();
            }
        }
        else if( SwingUtilities.isLeftMouseButton( event ) && event.getClickCount() == TWO ) 
        {
            int j = rowAtPoint( event.getPoint() );
            if( j > -1 ) 
            {
                if( !isRowSelected( j ) ) 
                {
                    clearSelection();
                    setRowSelectionInterval( j, j );
                }
                if( false == m_armed )
                {
                    m_armed = true;
                }
                else
                {
                    m_armed = false;
                    firePropertyChange( "doubleclick", null, m_selection );
                }
            }
            else
            {
                clearSelection();
            }
        }
    }

    private boolean m_armed = false;
}
