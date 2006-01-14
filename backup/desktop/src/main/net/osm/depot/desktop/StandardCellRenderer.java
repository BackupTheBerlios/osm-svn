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

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

class StandardCellRenderer extends DefaultTableCellRenderer 
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private static final EmptyBorder EMPTY_BORDER = new EmptyBorder(1,5,1,3);

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    public Component getTableCellRendererComponent(
      JTable table, Object object, boolean selected, boolean focus, int row, int column )
    {
        JLabel label = 
          (JLabel) super.getTableCellRendererComponent( 
            table, object, selected, focus, row, column );
        label.setBorder( EMPTY_BORDER );
        label.setHorizontalAlignment( SwingConstants.LEFT );
        return label;
    }
}

