/*
 * @(#)ListLayout.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/06/2001
 */

package net.osm.shell;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;

import net.osm.entity.*;

/**
 * The ListLayout class positions contained Items is a vertical left justified sequence.
 * Each item is positioned on the left below the proceeding item.
 */

class ListLayout implements LayoutManager
{

    public static final int EXPANDABLE = 0;

    public static final int SIMPLE = 1;

    private final int mode;

    //=============================================================
    // Constructors
    //=============================================================

   /**
    * Creation of a new ListLayout.
    */
    public ListLayout()
    {
        this( SIMPLE );
    }

   /**
    * Creation of a new ListLayout using the supplied <code>mode</code>.
    * @param mode value of ListLayout.EXPANDABLE or ListLayout.SIMPLE
    */
    public ListLayout( int mode )
    {
        this.mode = mode;
    }

    //=============================================================
    // LayoutManager
    //=============================================================

   /**
    * Adds the specified component with the specified name to
    * the layout. Not required by this layout manager.
    * @param name the component name
    * @param comp the component to be added
    */
    public void addLayoutComponent(String name, Component comp) {}

    /**
     * Removes the specified component from the layout.
     * Not required by this layout manager.
     * @param comp the component to be removed
     */
    public void removeLayoutComponent(Component comp){}

    /**
     * Calculates the preferred size dimensions for the specified 
     * panel given the components in the specified parent container.
     * @param parent the component to be laid out
     *  
     * @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container parent)
    {
	  Dimension dim = new Dimension(0, 0);
        synchronized (parent.getTreeLock()) 
        {
	      int n = parent.getComponentCount();
            for( int i=0; i<n; i++ )
		{
		    Component c = parent.getComponent(i);
		    if( c.isVisible() )
		    {
		        Dimension d = c.getPreferredSize();
			  dim.height = dim.height += d.height;
			  dim.width = Math.max(dim.width, d.width);
                }
            }
        }
	  return dim;
    }

    /** 
     * Calculates the minimum size dimensions for the specified 
     * panel given the components in the specified parent container.
     * @param parent the component to be laid out
     * @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        return preferredLayoutSize( parent );
    }

    /** 
     * Lays out the container in the specified panel.
     * @param parent the component which needs to be laid out 
     */
    public void layoutContainer(Container parent)
    {
	  Dimension dim = new Dimension(0,0);
        synchronized (parent.getTreeLock()) 
        {
	      int n = parent.getComponentCount();
            for( int i=0; i<n; i++ )
		{
		    Component c = parent.getComponent(i);
		    if( c.isVisible() )
		    {
		        Dimension d = c.getPreferredSize();
		        Rectangle r = new Rectangle( d );
			  c.setBounds( r );
			  c.setLocation( dim.width, dim.height );
			  dim.height += d.height;
                }
            }
        }
    }

    //=============================================================
    // ListLayout
    //=============================================================

   /**
    * Return the mode of this layout.
    */
    public int getMode()
    {
        return this.mode;
    }

}
