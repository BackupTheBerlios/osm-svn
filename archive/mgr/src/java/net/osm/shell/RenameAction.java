/*
 * @(#)RenameAction.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.shell;

import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.math.BigInteger;
import java.awt.Frame;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JMenuBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.osm.util.ExceptionHelper;

import net.osm.shell.MGR;
import net.osm.shell.control.field.TextPanel;
import net.osm.shell.control.field.EqualValidator;
import net.osm.shell.control.field.FieldStatus;
import net.osm.shell.Entity;

/**
 * Action supporting the renaming of an <code>Entity</code>.
 */
final class RenameAction extends AbstractAction implements PropertyChangeListener
{
     
    private JDialog dialog;
    private Entity entity;
    private JButton ok;
    private JButton cancel;
    private TextPanel panel;
    private EqualValidator validator;
    private Action okAction;

    public RenameAction( String label, boolean enabled, int key )
    {
	  super( label );
	  setEnabled( enabled );
    }

    public void setEntity( Entity entity )
    {
	  if( this.entity == entity ) return;
        this.entity = entity;
        if( entity == null )
	  {
		setEnabled( false );
	  }
	  else
	  {
	      setEnabled( entity.renameable() );
	  }
    }

    public void actionPerformed( ActionEvent event )
    {
        if( dialog == null ) dialog = createRenameDialog( event );
        dialog.setVisible( true );
    }

    private JDialog createRenameDialog( ActionEvent event )
    {
	  Component source = (Component) event.getSource();

        //
        // Create the name field
        //

	  String name = entity.getName();
	  validator = new EqualValidator( name, false );
        panel = new TextPanel( "Renaming " + name, name, 20, validator );
        panel.addPropertyChangeListener( this );

        //
        // Create the button controls
        //

        cancel = new JButton( );
        ok = new JButton( );

        Action cancelAction = new HideWindowAction( "Cancel", cancel );
        okAction = new RenamePostAction( "OK", this );
        okAction.setEnabled( false );
        
        cancel.setAction( cancelAction );
        ok.setAction( okAction);

        JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        buttonPanel.add( cancel );
        buttonPanel.add( ok );
        Box buttonHolder = new Box( BoxLayout.Y_AXIS );
        buttonHolder.add( buttonPanel );

        //
        // package
        //
        
        JPanel holder = new JPanel( new BorderLayout() );
        holder.add( panel, BorderLayout.CENTER );
        holder.add( buttonHolder, BorderLayout.SOUTH );

	  dialog = new JDialog( getFrame( (Component) event.getSource() ), "Renaming: " + entity.getName(), true );
	  dialog.setContentPane( holder );
        dialog.setLocationRelativeTo( source );
        dialog.getRootPane().setDefaultButton( ok );
        dialog.pack();
	  return dialog;
    }

    public void doRename( ActionEvent event )
    {
        if( event.getSource() == ok ) 
        dialog.setVisible( false );
	  dialog = null;
	  entity.setName( panel.getText() );
    }

    //==========================================================
    // PropertyChangeListener
    //==========================================================

   /**
    * Listens to property changes from pages.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        // update the name
        if(event.getSource() == entity ) 
	  {
		if( event.getPropertyName().equals("name"))
	      {
		    String name = (String) event.getNewValue();
                if( validator != null ) validator.setBase( name );
                if( panel != null ) panel.setLabel( "Renaming: " + name );
		    if( dialog != null ) dialog.setTitle( "Renaming: " + name );
	      }
        }

        // rename dialog has valid content, enable ok action
        if( event.getSource() == panel )
	  {
		if(event.getPropertyName().equals("status")) 
	      {
		    FieldStatus status = (FieldStatus) event.getNewValue();
                okAction.setEnabled( status.getValue() );
	      }
	  }
    }

    //==========================================================
    // utilities
    //==========================================================

    public Frame getFrame( Component component )
    {
         if( component instanceof Frame ) return (Frame) component;
         return (Frame) SwingUtilities.getAncestorOfClass( Frame.class, component );
    }

}

