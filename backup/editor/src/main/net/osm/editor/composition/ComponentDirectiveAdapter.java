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
import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import net.dpml.part.Controller;
import net.dpml.part.Part;

import net.dpml.component.control.ClassLoaderManager;

import net.dpml.component.info.Type;
import net.dpml.component.info.PartReference;
import net.dpml.component.info.InfoDescriptor;
import net.dpml.component.data.ComponentDirective;
import net.dpml.component.data.ClassLoaderDirective;

import net.dpml.transit.Logger;

import net.osm.edit.PartException;
import net.osm.edit.PartEditor;

/**
 * ComponentDirective datatype editor. 
 */
public final class ComponentDirectiveAdapter extends ComponentDirectiveEditor
{
    private String m_key;

    ComponentDirectiveAdapter( 
      ClassLoader classloader, Logger logger, ClassLoaderManager manager, 
      ComponentDirective directive, String key )
      throws PartException
    {
        super( classloader, logger, manager, directive );

        m_key = key;

        DefaultMutableTreeNode[] nodes = getMutableNodes();
        for( int i=0; i < nodes.length; i++ )
        {
            DefaultMutableTreeNode node = nodes[i];
            add( node );
        }
    }

    public String toString()
    {
        return m_key;
    }
}
