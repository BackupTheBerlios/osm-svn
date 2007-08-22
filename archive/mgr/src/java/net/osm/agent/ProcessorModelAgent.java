
package net.osm.agent;

import java.awt.Color;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.omg.CommunityFramework.Control;
import org.omg.CollaborationFramework.ProcessorModel;

import net.osm.agent.util.SequenceIterator;
import net.osm.shell.MGR;
import net.osm.shell.Panel;
import net.osm.shell.View;
import net.osm.shell.ScrollView;
import net.osm.util.ActiveList;
import net.osm.util.ListEvent;
import net.osm.util.ListListener;

/**
 * ProcessorModelAgent is a an agent encapsulating a ProcessorModel valuetype.
 * @author Stephen McConnell
 */
public class ProcessorModelAgent extends ControlAgent implements ListListener, PropertyChangeListener
{

   /**
    * The object reference to the ProcessorModel that this agents 
    * represents.
    */
    protected ProcessorModel model;

   /**
    * Local cache reference to the sequence of usage descriptors.
    */
    private UsageDescriptorAgent[] array;
    private ActiveList descriptors;

   /**
    * Reference to the task that this model constrains.
    */
    TaskAgent task;

    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setPrimary( Object value ) 
    {
        super.setPrimary( value );
        try
        {
            this.model = (ProcessorModel) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"ProcessorModelAgent/setPrimary - bad type.");
        }
    }

   /**
    * Set the task that this model is enforcing.  The implementation
    * associates itself as a listener to changes on the tasks consumed
    * and produced resources lists and uses changes in the lists to update 
    * assignments of resource to model usage descriptors.
    */
    public void setTask( TaskAgent task )
    {
        this.task = task;
	  synchronized( task )
	  {
            List consumed = task.getConsumed();
            List produced = task.getProduced();

	      // for all of the usage constraints declared in this model, check
	      // if the supplied task has a bound value and if so, assign the value
            // to the respect usage descriptor so the user knowns what the status 
            // of assignments are

            UsageDescriptorAgent[] array = getDescriptorArray();
	      for( int i=0; i<array.length; i++ )
	      {
                UsageDescriptorAgent agent = array[i];
		    String tag = agent.getTag();
		    if( agent.isaInput() )
		    {
		        agent.setAssignment( getUsage( consumed, agent.getTag() ) );
		    }
	          else
	          {
		        agent.setAssignment( getUsage( produced, agent.getTag() ) );
		    }
	      }
            task.getConsumed().addListListener( this );
            task.getProduced().addListListener( this );
            putValue( "configured", new Boolean( verify() ));
	  }

    }

    private UsageAgent getUsage( List list, String tag )
    {
        Iterator iterator = list.iterator();
	  while( iterator.hasNext() )
	  {
		UsageAgent agent = (UsageAgent) iterator.next();
		if( agent.getTag().equals( tag ) ) return agent;
	  }
	  return null;
    }

    //======================================================================
    // PropertyChangeListener
    //======================================================================

   /**
    * Listens to changes in the assignment state of associated usage descriptors
    * and issues a <code>configured</code> property when the aggregated state of 
    * required usage descriptor associations change.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        putValue( "configured", new Boolean( verify() ) );
    }

    //=========================================================================
    // ProcessorModelAgent
    //=========================================================================

   /**
    * Returns an array of UsageDescriptorAgent instances.
    */
    public List getDescriptors()
    {
	  if( descriptors != null ) return descriptors;
	  descriptors = new ActiveList( getLogger(), getDescriptorArray() );
	  return descriptors;
    }

    private UsageDescriptorAgent[] getDescriptorArray()
    {
        if( array == null )
        {
            array = new UsageDescriptorAgent[ model.usage.length ];
            for( int i = 0; i<model.usage.length; i++ )
            {
                array[i] = new UsageDescriptorAgent( model.usage[i], getOrb(), getResolver() );
		    array[i].addPropertyChangeListener( this );
            }
        }
        return array;
    }

   /**
    * Returns a subset of the usage descriptors that can be considered as 
    * candidates given an supplied AbstractResourceAgent.
    * @param agent the candidate resource
    * @return List list of UsageDescriptorAgent instances that represent
    *    candidate associations
    */
    public List getCandidates( AbstractResourceAgent agent )
    {
	  if( agent == null ) throw new RuntimeException("Null agent argument to getCandidates/1");

        UsageDescriptorAgent[] array = getDescriptorArray();
	  List candidates = new LinkedList();
	  for( int i=0; i<array.length; i++ )
	  {
		UsageDescriptorAgent usage = array[i];
		if( usage.isaCandidate( agent ) ) candidates.add( usage );
	  }
	  return candidates;
    }

   /**
    * Returns a subset of the input usage descriptors that can be considered as 
    * candidates given an supplied AbstractResourceAgent.
    * @param agent the candidate resource
    * @return List list of InputUsageDescriptorAgent instances that represent
    *    candidate associations
    */
    public List getInputCandidates( AbstractResourceAgent agent )
    {
	  if( agent == null ) throw new RuntimeException("Null agent argument to getCandidates/1");

        UsageDescriptorAgent[] array = getDescriptorArray();
	  List candidates = new LinkedList();
	  for( int i=0; i<array.length; i++ )
	  {
		UsageDescriptorAgent usage = array[i];
		if( usage.isaCandidate( agent ) && usage.isaInput() ) candidates.add( usage );
	  }
	  return candidates;
    }

   /**
    * Verify that the processor/task configuration meets usage constraints.
    */
    public synchronized boolean verify()
    {
        UsageDescriptorAgent[] array = getDescriptorArray();
	  for( int i=0; i<array.length; i++ )
	  {
		UsageDescriptorAgent uda = array[i];
		if( uda.getRequired() ) if( !uda.isAssigned() ) return false;
	  }
	  return true;
    }

    //=========================================================================
    // ListListener
    //=========================================================================

   /**
    * Method invoked when an object is added to a consumes or produces list 
    * on the associated task.  
    */
    public void addObject( ListEvent event )
    {
	  UsageAgent link = (UsageAgent) event.getObject();
        UsageDescriptorAgent usage = lookupDescriptor( link.getTag() );
        if( usage != null )
	  {
		usage.setAssignment( link );
        }
    }

   /**
    * Method invoked when an object is removed from the list.  The implementation 
    * monitors the retraction of usage links from the assigned task.  If the 
    * tag value matches the tag for this usage descriptor the assignment value 
    * is set to null and the listener is removed.
    */
    public void removeObject( ListEvent event )
    {
	  UsageAgent link = (UsageAgent) event.getObject();
        UsageDescriptorAgent usage = lookupDescriptor( link.getTag() );
        if( usage != null )
	  {
		usage.setAssignment( null );
        }
    }

    public UsageDescriptorAgent lookupDescriptor( String tag )
    {
        Iterator iterator = getDescriptors().iterator();
	  while( iterator.hasNext() )
	  {
		UsageDescriptorAgent usage = (UsageDescriptorAgent) iterator.next();
		if( usage.getTag().equals( tag ) ) return usage;
	  }
	  return null;
    }
}
