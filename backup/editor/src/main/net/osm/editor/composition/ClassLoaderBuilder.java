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

import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.net.URI;
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
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import net.dpml.component.data.ClassLoaderDirective;
import net.dpml.component.data.ClasspathDirective;

import net.dpml.transit.Repository;
import net.dpml.transit.Transit;

/**
 * Application profile tree node. 
 */
public final class ClassLoaderBuilder
{
    JComponent buildPanel( ClassLoaderDirective directive )
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode( directive );
        DefaultTreeModel model = new DefaultTreeModel( root );

        ClasspathDirective[] directives = directive.getClasspathDirectives();
        for( int i=0; i < directives.length; i++ )
        {
            ClasspathDirective d = directives[i];
            root.add( new ClasspathDirectiveTreeNode( d ) );
        }

        JTree tree = new JTree( model );
        tree.setRootVisible( false );
        tree.setShowsRootHandles( true );
        tree.setCellRenderer( new CellRenderer() );
        expand( tree, new TreePath( root ), true );
        JScrollPane scroller = new JScrollPane( tree );
        scroller.setName( "Classloader" );
        return scroller;
    }

    private void expand( JTree tree, TreePath parent, boolean expand )
    {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if( node.getChildCount() >= 0 )
        {
            Enumeration enummeration = node.children();
            while( enummeration.hasMoreElements() )
            {
                TreeNode n = (TreeNode) enummeration.nextElement();
                TreePath path = parent.pathByAddingChild( n );
                expand( tree, path, expand );
            }
        }

        if( expand )
        {
            tree.expandPath( parent );
        }
        else
        {
            tree.collapsePath( parent );
        }
    }

    private class ClasspathDirectiveTreeNode extends DefaultMutableTreeNode
    {
        private final ClasspathDirective m_value;

        private ClasspathDirectiveTreeNode( final ClasspathDirective value )
        {
            super( value );

            m_value = value;

            //
            // add classpath entries
            //

            URI[] uris = value.getURIs();
            for( int i=0; i< uris.length; i++ )
            {
                URI uri = uris[i];
                add( new StringTreeNode( uri.toString() ) );
            }
        }

        public String toString()
        {
            return m_value.getCategory().toString();
        }
    }

    private class StringTreeNode extends DefaultMutableTreeNode
    {
        private StringTreeNode( final String value )
        {
            super( value );
        }
    }

    private class CellRenderer extends DefaultTreeCellRenderer
    {
        public Component getTreeCellRendererComponent( 
          JTree tree, Object value, boolean selected, boolean expanded, 
          boolean leaf, int row, boolean focus )
        {
            if( value instanceof ClasspathDirectiveTreeNode )
            {
                setClosedIcon( CLASSPATH_ICON );
                setOpenIcon( CLASSPATH_ICON );
            }
            else
            {
                setClosedIcon( JAR_ICON );
                setOpenIcon( JAR_ICON );
                setLeafIcon( JAR_ICON );
            }
            return super.getTreeCellRendererComponent( 
                tree, value, selected, expanded, leaf, row, focus );
        }
    }

    protected static ImageIcon readImageIcon( final String filename ) 
    {
        return readImageIcon( BASE_IMAGE_PATH, filename );
    }

    protected static ImageIcon readImageIcon( final String base, final String filename ) 
    {
        final String path = base + filename;
        URL url = ClassLoaderBuilder.class.getClassLoader().getResource( path );
        if( null == url )
        {
           final String error = "Invalid icon path: " + path;
           throw new IllegalArgumentException( error );
        }
        return new ImageIcon( url );
    }

    private static final String BASE_IMAGE_PATH = "net/dpml/composition/edit/images/";

    private static final ImageIcon JAR_ICON = readImageIcon( "16/binary.png" );

    private static final ImageIcon CLASSPATH_ICON = readImageIcon( "16/classpath.png" );

}
