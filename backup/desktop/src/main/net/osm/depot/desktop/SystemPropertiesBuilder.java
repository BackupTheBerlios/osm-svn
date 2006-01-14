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

package net.dpml.depot.desktop;

import java.awt.Component;
import java.net.URL;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import net.dpml.profile.model.ApplicationRegistry;
import net.dpml.profile.model.ApplicationProfile;

/**
 * Application profile tree node. 
 */
public final class SystemPropertiesBuilder
{
    private static final int NAME_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;
    private static final int COLUMN_COUNT = 2;

    private static final TableColumnModel TABLE_COLUMN_MODEL = createColumnModel();

    private ApplicationProfile m_profile;
    private String m_id;
    private Component m_component;
    private Properties m_properties;

    public SystemPropertiesBuilder( ApplicationProfile profile )
    {
        m_profile = profile;
        try
        {
            m_properties = profile.getSystemProperties();
        }
        catch( Exception e )
        {
            m_properties = new Properties();
            e.printStackTrace();
        }

        TableModel tableModel = new DataModel();
        JTable table = new JTable( tableModel, TABLE_COLUMN_MODEL );
        JScrollPane scrollPane = new JScrollPane( table );
        scrollPane.getViewport().setBackground( table.getBackground() );
        scrollPane.setBorder( null );
        m_component = scrollPane;
    }

    Component getComponent()
    {
        return m_component;
    }

    private class DataModel extends AbstractTableModel
    {
        public int getColumnCount()
        { 
            return COLUMN_COUNT;
        }

        public int getRowCount()
        { 
            return m_properties.size();
        }

        public Object getValueAt( int row, int col ) 
        { 
            switch( col )
            {
                case NAME_COLUMN :
                  return m_properties.keySet().toArray()[ row ];
                case VALUE_COLUMN :
                  return m_properties.values().toArray()[ row ];
                default: 
                  return null;
            }
        }
    }

    private static TableColumnModel createColumnModel()
    {
        TableColumnModel model = new DefaultTableColumnModel();
	  model.addColumn( createTableColumn( "Name", NAME_COLUMN, 60 ) );
	  model.addColumn( createTableColumn( "Value", VALUE_COLUMN, 350 ) );
	  return model;
    }

    private static TableColumn createTableColumn( String name, int index, int width )
    {
	  TableColumn column = 
          new TableColumn( index, width, new StandardCellRenderer(), null );
        column.setHeaderValue( name );
        return column;
    }

}