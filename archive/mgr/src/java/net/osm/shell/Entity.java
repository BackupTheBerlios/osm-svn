
package net.osm.shell;

import java.util.List;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.Icon;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;

import net.osm.shell.View;

/**
 * The <code>Entity</code> interface defines the set of operations 
 * that a object must support to be presentable within the shell. These 
 * operations include access to an expandable item presentation, 
 * properties and other related views.
 *
 * @author  Stephen McConnell
 * @version 1.0 20 JUN 2001
 */
public interface Entity extends Action, Component, Disposable, EntityHandler
{

    //==============================================================
    // constants
    //==============================================================

   /**
    * Constant used to declare a SMALL icon.
    */
    public static final int SMALL = 0;

   /**
    * Constant used to declare a LARGE icon.
    */
    public static final int LARGE = 1;

    //==============================================================
    // operations
    //==============================================================

   /**
    * Returns the renamable state of the Entity.
    * @return boolean true if this entity is renamable.
    */
    public boolean renameable();

   /**
    * Returns the removable state of the Entity.
    * @return boolean true if this entity is removable.
    */
    public boolean removable();

   /**
    * Returns the <code>Action</code> object that will be handle
    * an action event signalling removal of the instance.
    * @return Action an <code>Action</code> that will remove the 
    * <code>Entity</code> 
    */
    public Action getRemoveAction();

   /**
    * The <code>getName</code> method returns the name of the entity.
    * @param String the entity name
    */
    public String getName();

   /**
    * Set the name of the entity to the supplied <code>String</code>..
    * @param name the new entity name
    */
    public void setName( String name );

   /**
    * Returns a list of Action instances to be installed as 
    * menu items within the desktop actions menu group when 
    * the entity is selected.
    */
    public List getActions( );

   /**
    * Returns a list of <code>Features</code> instances to be added 
    * to the Properties dialog features panel.
    * @return List the list of <code>Feature</code> instances
    */
    public List getFeatures();

   /**
    * Returns a list of named <code>Component</code> instances to be added 
    * to the Properties dialog.
    * @return List the list of property panels
    */
    List getPropertyPanels();

   /**
    * Returns the icon representing the entity.
    * @param size a value of LARGE or SMALL
    * @return Icon iconic representation of the entity
    */
    public Icon getIcon( int size );

   /**
    * Return the default <code>View</code> of this <code>Entity</code>
    * @return View the default view of the entity
    */
    public View getView();

   /**
    * Returns list of the primary views of this <code>Entity</code>
    * @return List list of <code>View</code> instances.
    */
    public List getViews();

   /**
    * Test if this entity if a leaf or a composite
    * @return boolean true if this is a leaf entity
    */
    public boolean isaLeaf( );

   /**
    * Returns a list of entities that represents the navigatable content
    * of the target entity. 
    * @return List the navigatable content
    */
    public List getChildren( );

   /**
    * Addition of a property change listener to the entity.
    * @param listener a property change listener to add to the entity
    */
    public void addPropertyChangeListener( PropertyChangeListener listener );

   /**
    * Remove a property change listener from the entity.
    * @param listener a property change listener to remove from the entity
    */
    public void removePropertyChangeListener( PropertyChangeListener listener );

   /**
    * Returns the disposed state of the entity.
    */
    public boolean isDisposed();

}
