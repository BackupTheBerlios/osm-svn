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

package net.osm.editor.composition;

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
import javax.swing.table.TableCellEditor;
import javax.swing.DefaultCellEditor;


import net.dpml.component.data.ComponentDirective;
import net.dpml.component.data.ContextDirective;
import net.dpml.component.data.ValueDirective;
import net.dpml.component.data.ReferenceDirective;
import net.dpml.component.info.Type;
import net.dpml.component.info.EntryDescriptor;

import net.dpml.part.Part;

/**
 * Application profile tree node. 
 */
public final class ContextBuilder
{
    private static final int NAME_COLUMN = 0;
    private static final int TYPE_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private static final int REQUIRED_COLUMN = 3;
    private static final int COLUMN_COUNT = 4;

    private static final TableColumnModel TABLE_COLUMN_MODEL = createColumnModel();

    private Type m_type;
    private ComponentDirective m_profile;
    private String m_id;
    private Component m_component;

    public ContextBuilder( Type type, ComponentDirective profile )
    {
        m_profile = profile;
        m_type = type;

        TableModel tableModel = new DataModel();
        JTable table = new JTable( tableModel, TABLE_COLUMN_MODEL );
        JScrollPane scrollPane = new JScrollPane( table );
        scrollPane.getViewport().setBackground( table.getBackground() );
        scrollPane.setBorder( null );
        m_component = scrollPane;
        m_component.setName( "Context" );
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
            return m_type.getContextDescriptor().getEntryDescriptors().length; 
        }

        public boolean isCellEditable( int row, int column )
        {
            if( column == VALUE_COLUMN )
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        public void setValueAt( Object value, int row, int col ) 
        {
            System.out.println( "# set (" + row + "," + col + ") " + value );
        }

        public Object getValueAt( int row, int column )
        {
            EntryDescriptor entry = getEntryDescriptor( row );
            if( null == entry )
            {
                return null;
            }
            switch( column )
            {
                case NAME_COLUMN :
                  return entry.getKey();
                case TYPE_COLUMN :
                  String classname = entry.getClassname();
                  if( classname.equals( "java.lang.String" ) )
                  {
                      return "String";
                  }
                  else if( classname.equals( "int" ) )
                  {
                      return "integer";
                  }
                  else
                  {
                      return classname;
                  }
                case VALUE_COLUMN :
                  Part part = getPartDirective( entry );
                  if( null == part )
                  {
                      return null;
                  }
                  else if( part instanceof ValueDirective )
                  {
                      ValueDirective directive = (ValueDirective) part;
                      if( directive.isCompound() )
                      {
                          return "Compound Value";
                      }
                      else
                      {
                          return directive.getBaseValue();
                      }
                  }
                  else if( part instanceof ReferenceDirective )
                  {
                      ReferenceDirective ref = (ReferenceDirective) part;
                      return ref.getURI();
                  }
                  else
                  {
                      return part.getClass().getName();
                  }
                case REQUIRED_COLUMN :
                  if( entry.isRequired() )
                  {
                      return "required";
                  }
                  else
                  {
                      return null;
                  }
                default: 
                  return null;
            }
        }

        private EntryDescriptor getEntryDescriptor( int index ) 
        {
            try
            {
                return m_type.getContextDescriptor().getEntryDescriptors()[ index ];
            }
            catch( Throwable e )
            {
                return null;
            }
        }

        private Part getPartDirective( EntryDescriptor entry ) 
        {
            String key = entry.getKey();
            return m_profile.getContextDirective().getPartDirective( key );
        }
    }

    //--------------------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------------------

    private static TableColumnModel createColumnModel()
    {
        TableColumnModel model = new DefaultTableColumnModel();
	  model.addColumn( createTableColumn( "Name", NAME_COLUMN, 100 ) );
	  model.addColumn( createTableColumn( "Type", TYPE_COLUMN, 150 ) );
	  model.addColumn( createTableColumn( "Value", VALUE_COLUMN, 150 ) );
	  model.addColumn( createTableColumn( "Required", REQUIRED_COLUMN, 60 ) );
	  return model;
    }

    private static TableColumn createTableColumn( String name, int index, int width )
    {
	  TableColumn column = 
          new TableColumn( index, width, new StandardCellRenderer(), null );
        column.setHeaderValue( name );
        return column;
    }

    private static class StandardCellRenderer extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(
          JTable table, Object object, boolean selected, boolean focus, int row, int column )
        {
            Object value = object;
            /*
            if( column == VALUE_COLUMN )
            {
                if( null != object )
                {
                    if( object instanceof ValueDirective )
                    {
                        ValueDirective directive = (ValueDirective) object;
                        value = directive.getLocalValue();
                        if( null == value )
                        {
                            value = "Compound Value";
                        }
                    }
                    else if( object instanceof ReferenceDirective )
                    {
                        ReferenceDirective ref = (ReferenceDirective) object;
                        value = ref.getURI();
                    }
                    else
                    {
                        value = object.getClass().getName();
                    }
                }
            }
            */
            JLabel label = 
              (JLabel) super.getTableCellRendererComponent( 
                table, value, selected, focus, row, column );
            label.setBorder( EMPTY_BORDER );
            label.setHorizontalAlignment( SwingConstants.LEFT );
            return label;
        }
    }

    private static final EmptyBorder EMPTY_BORDER = new EmptyBorder(1,5,1,3);
}