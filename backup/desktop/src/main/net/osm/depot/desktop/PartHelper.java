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
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.lang.reflect.Constructor;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import net.dpml.profile.model.ApplicationProfile;

import net.dpml.part.Part;
import net.dpml.part.PartContentHandler;
import net.dpml.part.PartHandler;
import net.dpml.part.PartEditor;

import net.dpml.transit.Logger;
import net.dpml.transit.Repository;
import net.dpml.transit.Transit;

import net.dpml.station.Application;

/**
 * Utility class that supports the construction of a components used to present
 * views of a part together with subsidiary nodes exposed by the part.  The 
 * implementation uses the codebase published by a part to locate and construct 
 * it's controller from which a part editor is resolved. The part editor is 
 * responsible for the construction of the component panels and nodes.
 *  
 * @see PartEditor
 */
final class PartHelper
{
    private final Application m_application;
    private final Logger m_logger;
    private final PartEditor m_editor;

    PartHelper( Logger logger, PartHandler handler, Application application ) throws Exception
    {
        m_application = application;
        m_logger = logger;
        
        URI codebase = getCodeBaseURI();
        Part part = handler.loadPart( codebase );
        m_editor = handler.loadPartEditor( part );
    }
    
    Component[] getPartPanels()
    {
        if( null != m_editor )
        {
            return m_editor.getPartPanels();
        }
        else
        {
            JPanel panel = new JPanel();
            panel.add( new JLabel( "Part type does not declare an editor." ) );
            return new Component[]{ panel };
        }
    }
    
    TreeNode[] getPartNodes()
    {
        if( null != m_editor )
        {
            return m_editor.getPartNodes();
        }
        else
        {
            return new TreeNode[0];
        }
    }

    private URI getCodeBaseURI()
    {
        try
        {
            return m_application.getProfile().getCodeBaseURI();
        }
        catch( Throwable e )
        {
            final String error =
              "Unable to resolve the application codebase.";
            throw new RuntimeException( error, e );
        }
    }
}
