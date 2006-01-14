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

import net.dpml.component.info.PartReference;

import net.dpml.component.control.ClassLoaderManager;

import net.dpml.component.info.Type;
import net.dpml.component.info.InfoDescriptor;
import net.dpml.component.info.CollectionPolicy;
import net.dpml.component.info.LifestylePolicy;
import net.dpml.component.info.ContextDescriptor;
import net.dpml.component.data.ComponentDirective;
import net.dpml.component.data.ContextDirective;
import net.dpml.component.data.ClassLoaderDirective;
import net.dpml.component.data.ValueDirective;

import net.dpml.transit.Logger;

import net.osm.editor.PartException;
import net.osm.editor.PartEditor;

/**
 * ComponentDirective datatype editor. 
 */
public class ComponentDirectiveEditor extends DefaultMutableTreeNode implements PartEditor
{
    private Logger m_logger;
    private ClassLoaderManager m_manager;
    private ComponentDirective m_directive;
    private Class m_class;
    private Type m_type;
    private Component[] m_panels;
    private Component m_component;
    private DefaultMutableTreeNode[] m_nodes;

    ComponentDirectiveEditor( Logger logger, ClassLoaderManager manager, ComponentDirective directive )
      throws PartException
    {
        this( ComponentDirectiveEditor.class.getClassLoader(), logger, manager, directive );
    }

    ComponentDirectiveEditor( 
      ClassLoader anchor, Logger logger, ClassLoaderManager manager, ComponentDirective directive )
      throws PartException
    {
        m_directive = directive;
        m_manager = manager;
        m_logger = logger;
        
        String classname = directive.getClassname();
        ClassLoader classloader = m_manager.createClassLoader( anchor, directive );
        
        try
        {
            m_class = classloader.loadClass( classname );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to load component class: " + classname;
            throw new RuntimeException( error, e );
        }
        
        try
        {
            m_type = Type.decode( getClass().getClassLoader(), m_class );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to load component type: " + classname;
            throw new RuntimeException( error, e );
        }
        
        m_panels = buildPartPanels( classloader );
        m_nodes = buildPartNodes( classloader );
        m_component = buildPrimeComponent( m_panels );
    }

    public Part getPart()
    {
        // TODO: track changes and return an updated part suitable for 
        // subsequent externalization (currently we are just just returning
        // the original part)
        
        return m_directive;
    }

    public Component getComponent()
    {
        return m_component;
    }

    private Component buildPrimeComponent( Component[] panels )
    {
        JTabbedPane pane = new JTabbedPane();
        for( int i=0; i<panels.length; i++ )
        {
            pane.add( panels[i] );
        }
        return pane;
    }

    public Component[] getPartPanels()
    {
        return m_panels;
    }

    private Component[] buildPartPanels( ClassLoader classloader )
    {
        ArrayList list = new ArrayList();
        if( m_type.getContextDescriptor().getEntryDescriptors().length > 0 )
        {
            list.add( new ContextBuilder( m_type, m_directive ).getComponent() );
        }
        ClassLoaderDirective classloaderDirective = m_directive.getClassLoaderDirective();
        if( classloaderDirective.getClasspathDirectives().length > 0 )
        {
            list.add( buildClassLoaderComponent() );
        }
        list.add( buildTypeStaticComponent() );
        if( m_type.getCategoryDescriptors().length > 0 )
        {
            list.add( buildCategoriesComponent() );
        }
        if( m_type.getServiceDescriptors().length > 0 )
        {
            list.add( buildServicesComponent() );
        }
        return (Component[]) list.toArray( new Component[0] );
    }
    
    public TreeNode[] getPartNodes()
    {
        return getMutableNodes();
    }

    protected DefaultMutableTreeNode[] getMutableNodes()
    {
        return m_nodes;
    }

    private DefaultMutableTreeNode[] buildPartNodes( ClassLoader classloader )
    {
        ArrayList list = new ArrayList();
        PartReference[] references = m_type.getPartReferences();
        for( int i=0; i < references.length; i++ )
        {
            PartReference ref = references[i];
            String key = ref.getKey();
            Part part = ref.getPart();
            if( part instanceof ComponentDirective )
            {
                ComponentDirective directive = (ComponentDirective) part;
                try
                {
                    ComponentDirectiveAdapter adapter = 
                      new ComponentDirectiveAdapter( classloader, m_logger, m_manager, directive, key );
                    list.add( adapter );
                }
                catch( PartException e )
                {
                    final String error = 
                      "Unable to load part editor for: " + directive;
                    throw new RuntimeException( error, e );
                }
            }
            else if( part instanceof ValueDirective )
            {
                ValueDirective directive = (ValueDirective) part;
                list.add( new ValueDirectiveAdapter( classloader, m_logger, directive, key ) );
            }
            else
            {
                list.add( new PartReferenceTreeNode( ref ) );
            }
        }
        return (DefaultMutableTreeNode[]) list.toArray( new DefaultMutableTreeNode[0] );
    }

    private Component buildClassLoaderComponent() 
    {
        ClassLoaderDirective classloaderDirective = m_directive.getClassLoaderDirective();
        ClassLoaderBuilder builder = new ClassLoaderBuilder();
        return builder.buildPanel( classloaderDirective );
    }

    private Component buildCategoriesComponent() 
    {
        JPanel panel = new JPanel();
        panel.setName( "Categories" );
        return panel;
    }

    private Component buildServicesComponent() 
    {
        JPanel panel = new JPanel();
        panel.setName( "Services" );
        return panel;
    }

    private Component buildTypeStaticComponent() 
    {
        FormLayout layout = new FormLayout(
          "right:pref, 3dlu, fill:max(120dlu;pref)", 
          "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, "
             + "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref" );

        PanelBuilder builder = new PanelBuilder( layout );
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();

        //builder.addSeparator( "Type", cc.xyw( 1, 1, 3 ) );

        builder.addLabel( "Component:", cc.xy( 1, 3 ) ); 
        builder.add( getClassnameComponent(), cc.xy( 3, 3 ) );

        builder.addLabel( "Version:", cc.xy( 1, 5 ) ); 
        builder.add( getVersionComponent(), cc.xy( 3, 5 ) );
         
        builder.addLabel( "Name:", cc.xy( 1, 7 ) ); 
        builder.add( getPartNameComponent(), cc.xy( 3, 7 ) );
         
        builder.addLabel( "Lifestyle:", cc.xy( 1, 9 ) ); 
        builder.add( getLifestyleComponent(), cc.xy( 3, 9 ) );
         
        builder.addLabel( "Threadsafe:", cc.xy( 1, 11 ) ); 
        builder.add( getThreadSafeComponent(), cc.xy( 3, 11 ) );
         
        builder.addLabel( "Collection Policy:", cc.xy( 1, 13 ) ); 
        builder.add( getCollectionPolicyComponent(), cc.xy( 3, 13 ) );

        builder.addLabel( "Activation Policy:", cc.xy( 1, 15 ) ); 
        builder.add( getActivationPolicyComponent(), cc.xy( 3, 15 ) );

        JPanel panel = builder.getPanel();
        panel.setName( "Component" );
        return panel;
    }

    private Component getControllerURIComponent()
    {
        return new JLabel( m_directive.getPartHandlerURI().toString() );
    }

    private Component getActivationPolicyComponent()
    {
        return new JLabel( m_directive.getActivationPolicy().toString() );
    }

    private Component getClassnameComponent()
    {
        return new JLabel( m_type.getInfo().getClassname() );
    }

    private Component getVersionComponent()
    {
        return new JLabel( m_type.getInfo().getVersion().toString() );
    }

    private Component getPartNameComponent()
    {
        return new JLabel( m_type.getInfo().getName() );
    }

    private Component getLifestyleComponent()
    {
        return new JLabel( m_type.getInfo().getLifestyle().getName() );
    }

    private Component getThreadSafeComponent()
    {
        return new JLabel( "" + m_type.getInfo().isThreadsafe() );
    }

    private Component getCollectionPolicyComponent()
    {
        return new JLabel( m_type.getInfo().getCollectionPolicy().getName() );
    }

   /**
    * The PolicyComboBox is a volotile component that maintains the 
    * presentation of the application profile startup policy. 
    */
    /*
    private class ActivationPolicyComboBox extends JComboBox
    {
        private ActivationPolicyComboBox()
        {
            super( Control.ACTIVATION_POLICIES );
            setSelectedItem( m_directive.getActivationPolicy() );
        }

        public void selectedItemChanged()
        {
            super.selectedItemChanged();
            System.out.println( "TODO: " + getClass().getName() );
        }
    }
    */

    private class PartReferenceTreeNode extends DefaultMutableTreeNode
    {
        PartReference m_reference;
 
        PartReferenceTreeNode( PartReference ref )
        {
            m_reference = ref;
        }

        public String getName()
        {
            return m_reference.getKey();
        }

        public String toString()
        {
            return getName();
        }
    }
}
