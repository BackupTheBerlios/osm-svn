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

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

class ClassicCellRenderer extends DefaultTableCellRenderer 
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private static final EmptyBorder border = new EmptyBorder(1,5,1,3);

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new cell renderer using the default font.
    */
    public ClassicCellRenderer( )
    {
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    public Component getTableCellRendererComponent(
      JTable table, Object object, boolean selected, boolean focus, int row, int column )
    {
        JLabel label = 
          (JLabel) super.getTableCellRendererComponent( 
            table, object, selected, focus, row, column );
        label.setBorder( border );
        label.setHorizontalAlignment( SwingConstants.LEFT );

        if( object == null )
        {
            label.setIcon( null );
            label.setText( null );
            return label;
        }
      
        if( object instanceof ImageIcon )
        {
            label.setText( null );
            label.setIcon( (ImageIcon) object );
            return label;
        }
        else if( object instanceof Boolean ) 
        {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected( ((Boolean)object).booleanValue() );
            if( selected )
            {
                checkBox.setForeground(table.getSelectionForeground());
                checkBox.setBackground(table.getSelectionBackground());
            }
            else
            {
                checkBox.setForeground(table.getForeground());
                checkBox.setBackground(table.getBackground());
            }
            checkBox.setBorder( new EmptyBorder(0,20,0,10) );
            checkBox.setHorizontalAlignment(JCheckBox.LEFT);
            return checkBox;
        }
        else
        {
            return label;
        }
    }
}

