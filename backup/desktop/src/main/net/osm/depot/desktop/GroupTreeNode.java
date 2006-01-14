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
import java.net.URI;
import java.util.Properties;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Application profile tree node. 
 */
public class GroupTreeNode extends Node
{
    private static final ImageIcon COLLAPSED = readImageIcon( "16/folder.png" );
    private static final ImageIcon EXPANDED = readImageIcon( "16/folder_open.png" );

    public static final Icon DPML_DESKTOP_LEAF_ICON = COLLAPSED;
    public static final Icon DPML_DESKTOP_EXPANDED_ICON = EXPANDED;
    public static final Icon DPML_DESKTOP_COLLAPSED_ICON = COLLAPSED;

    private final String m_name;

    public GroupTreeNode( final String name )
    {
        super( name );

        m_name = name;
    }

    public String getName()
    {
        return m_name;
    }
}
