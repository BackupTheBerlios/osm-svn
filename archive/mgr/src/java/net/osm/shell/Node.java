
package net.osm.shell;

import java.util.List;
import java.util.Iterator;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import net.osm.util.ListHandler;
import net.osm.util.ListListener;
import net.osm.util.ListEvent;


/**
 * A tree node representing a Entity.
 */
public class Node extends DefaultMutableTreeNode implements ListListener, PropertyChangeListener
{
    //=========================================================================
    // static
    //=========================================================================

    private static final String padding = " ";

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The Entity that this node represents.
    */
    private Entity entity;

   /**
    * The list of child entities.
    */
    private List list;

    private NavigatorModel model;

    private boolean policy = false;

    private boolean populated = false;

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>Node</code> based on a supplied
    * entity.
    * @param entity - the entity backing this node.
    */
    public Node( Entity entity )
    {
        this( entity, null );
	  this.policy = true;
    }

   /**
    * Constructor of a new <code>Node</code> based on a supplied
    * entity.
    * @param entity the entity backing this node.
    * @param model the data model
    */
    public Node( Entity entity, NavigatorModel model )
    {
        super( entity );
	  this.entity = entity;
	  this.model = model;
	  this.policy = policy;
	  entity.addPropertyChangeListener( this );
        list = entity.getChildren();
	  if( !entity.isaLeaf() )
	  {
	      setAllowsChildren( true );
		if( list instanceof ListHandler ) ((ListHandler)list).addListListener( this );
	  }
    }

    public void setModel( NavigatorModel model )
    {
        this.model = model;
    }

    //=========================================================================
    // ListListener
    //=========================================================================

   /**
    * Method invoked when an object is added to a list.  
    */
    public synchronized void addObject( ListEvent event )
    {
        if( !populated ) 
	  {
	      populate();
	  }
	  else
	  {
		int j = super.getChildCount();
	      Entity entity = (Entity) event.getObject();
		if( entity instanceof Pointer ) entity = ((Pointer)entity).getTarget();
            final Node node = new Node( entity, model );
            insert( node, j );
	      if( model != null ) model.notifyTreeNodesInserted( 
		  this, getPath(), new int[]{j}, new Object[]{ node } );
        }
    }

   /**
    * Method invoked when an object is removed from the list.  
    */
    public synchronized void removeObject( ListEvent event )
    {
        if( !populated ) 
	  {
		populate();
	  }
	  else
	  {
	      Entity entity = (Entity) event.getObject();
            if( entity instanceof Pointer ) entity = ((Pointer)entity).getTarget();
            int index = locateNode( entity );
	      if( index > -1 ) 
            {
                final Node node = (Node) getChildAt( index );
 		    remove( index );
	          if( model != null ) model.notifyTreeNodesRemoved( 
		      this, getPath(), new int[]{index}, new Object[]{ node } );
	      }
	  }
    }

    private int locateNode( Object object )
    {
	  int i = 0;
        Enumeration enum = children();
        while( enum.hasMoreElements() )
	  {
		if( ((Node)enum.nextElement()).getUserObject() == object ) return i;
		i++;
	  }
        return -1;
    }

    private void populate( )
    {
        populate( true );
    }

    private void populate( boolean notify )
    {
        populated = true;
        try
	  {
		synchronized( list )
		{
		    int p = -1;
		    final int[] ints = new int[ list.size() ];
		    Object[] nodes = new Object[ list.size() ];
	          Iterator iterator = list.iterator();
		    while( iterator.hasNext() )
		    {
		        p++;
		        Entity entity = (Entity) iterator.next();
		        if( entity instanceof Pointer ) entity = ((Pointer)entity).getTarget();
		        final Node node = new Node( entity, model );
		        nodes[p] = node;
		        ints[p] = p;
		        insert( node, p );
		    }
	          if( notify ) if( model != null ) model.notifyTreeNodesInserted( 
			this, getPath(), ints, nodes );
		}
	  }
	  catch( Throwable unexpected )
	  {
		removeAllChildren();
		populated = false;
		final String error = "unable to populate a node in the navigator";
		throw new RuntimeException( error, unexpected );
	  }
    }

    //=========================================================================
    // PropertyChangeListener
    //=========================================================================
    
   /**
    * Property event change handler that propergates name and icon changes.
    * @param event the property change event
    */
    public void propertyChange( PropertyChangeEvent event )
    {
	  String name = event.getPropertyName();
	  if( name.equals("name") || name.equals("icon") )
	  {
	      if( model != null ) model.notifyTreeNodeChanges( 
		  this, getPath(), null, null );
	  }
    }

    //=========================================================================
    // TreeModel
    //=========================================================================

    public boolean isLeaf()
    {
	  if( policy ) return entity.isaLeaf();
	  return super.isLeaf();
    }

    public TreeNode getChildAt( int index )
    {
	  if( !populated ) populate( false );
        return super.getChildAt( index );
    }

    public int getChildCount()
    {
        if( populated ) return super.getChildCount();
        return list.size();
    }

    public String toString()
    {
        return padding + entity.getName() + padding;
    }

}
