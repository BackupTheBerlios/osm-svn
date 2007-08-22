

package net.osm.hub.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.JarURLConnection;
import java.util.jar.Attributes;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CosTime.TimeService;
import org.omg.CollaborationFramework.Controls;
import org.omg.CollaborationFramework.ControlsHelper;
import org.omg.CollaborationFramework.ControlledBy;
import org.omg.CollaborationFramework.ControlledByHelper;
import org.omg.CollaborationFramework.Coordinates;
import org.omg.CollaborationFramework.CoordinatedBy;
import org.omg.CollaborationFramework.CoordinatedByHelper;
import org.omg.CollaborationFramework.ProcessorOperations;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorModel;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.StateDescriptor;
import org.omg.CollaborationFramework.Master;
import org.omg.CollaborationFramework.MasterHelper;
import org.omg.CollaborationFramework.UsageDescriptor;
import org.omg.CollaborationFramework.InputDescriptor;
import org.omg.CollaborationFramework.OutputDescriptor;
import org.omg.CollaborationFramework.Slave;
import org.omg.CollaborationFramework.SlaveHelper;
import org.omg.CollaborationFramework.SlaveIterator;
import org.omg.CollaborationFramework.SlaveIteratorPOA;
import org.omg.CollaborationFramework.SlaveIteratorPOATie;
import org.omg.CollaborationFramework.SlavesHolder;
import org.omg.CollaborationFramework.CollaborationSingleton;
import org.omg.CollaborationFramework.ProcessorStateHelper;
import org.omg.CollaborationFramework.StateDescriptor;
import org.omg.CollaborationFramework.StateDescriptorHelper;
import org.omg.CommunityFramework.Model;
import org.omg.CommunityFramework.Problem;
import org.omg.CommunityFramework.GenericResource;
import org.omg.CommunityFramework.GenericResourceHelper;
import org.omg.PortableServer.POA;
import org.omg.Session.AbstractResource;
import org.omg.Session.BaseBusinessObjectKey;
import org.omg.Session.Link;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinkIteratorPOA;
import org.omg.Session.LinkIteratorPOATie;
import org.omg.Session.LinksHolder;
import org.omg.Session.LinkHolder;
import org.omg.Session.LinkIteratorHolder;
import org.omg.Session.ResourceUnavailable;
import org.omg.Session.Task;
import org.omg.Session.TaskHelper;
import org.omg.Session.ProcessorConflict;
import org.omg.Session.SemanticConflict;
import org.omg.Session.Consumes;
import org.omg.Session.ConsumesHelper;
import org.omg.Session.CannotStart; 
import org.omg.Session.AlreadyRunning;
import org.omg.Session.CurrentlySuspended;
import org.omg.Session.CannotSuspend;
import org.omg.Session.NotRunning;
import org.omg.Session.CannotStop;
import org.omg.Session.task_state;

import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.Manager;
import net.osm.hub.resource.StructuredEventUtilities;
import net.osm.hub.resource.AbstractResourceDelegate;
import net.osm.hub.resource.LinkIteratorDelegate;
import net.osm.hub.pss.LinkStorage;
import net.osm.hub.pss.ProcessorStorage;
import net.osm.hub.home.TaskCallback;
import net.osm.hub.home.TaskCallbackHelper;
import net.osm.hub.gateway.CannotTerminate;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;
import net.osm.realm.StandardPrincipal;


/**
 * <p>The <code>AbstractProcessorDelegate</code> class manages the bridge between the external view of
 * a enterprise business process and the internal implementation of that service.   To the 
 * external client the Processor interface presents a view of formal connections of a processor
 * to a user's task, the task associated consumption and production relationships, the owner
 * of the Task and so forth.  Internally, a Processor implementation maintains persistent 
 * state execution state and acts as the coordinator of an internal enterprise service in its
 * delivery and execution of the published process lifecycle and process policy.</>
 */

public abstract class AbstractProcessorDelegate extends AbstractResourceDelegate 
implements Initializable
{
    
    //=====================================================================
    // static
    //=====================================================================
    
   /**
    * Event type event containing changes in task state.
    */
    public static final EventType stateEventType = CollaborationSingleton.stateEventType;

    //=====================================================================
    // state
    //=====================================================================

   /**
    * Storage object representing this Processor.
    */
    protected ProcessorStorage store;
    
   /**
    * CORBA object reference to this Processor.
    */
    private Processor m_processor;
     

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Resolves the processor PSS storage object under which processor state 
    * is maintained (enabling processor execution beyond the duration of 
    * transient server activations). 
    */
    public void initialize()
    throws Exception
    {
	  super.initialize();
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization (processor)" );
        this.store = (ProcessorStorage) super.getContext().getStorageObject();
        setProcessorReference( ProcessorHelper.narrow( getManager().getReference( 
          store.get_pid(), ProcessorHelper.id() ) ) );
    }

   /**
    * Clean up state members. 
    */ 
    public synchronized void dispose()
    {
 	  if( getLogger().isDebugEnabled() ) getLogger().debug("dispose (processor)");
        this.typeCode = null;
        this.m_processor = null;
        this.store = null;
	  super.dispose();
    }
  
    // ==========================================================================
    // Structured Events
    // ==========================================================================

   /**
    * Creation of a new 'state' StructuredEvent.
    * @param state the task state
    * @return StructuredEvent the structured event
    */
    
    public StructuredEvent newStateEvent( StateDescriptor state )
    {
        Property sourceProp = super.createSourceProperty( );
        Any stateHolder = orb.create_any();
	  
        StateDescriptorHelper.insert( stateHolder, state );
        Property stateProp = new Property( "value", stateHolder );
        return StructuredEventUtilities.createEvent( orb, stateEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( orb ), 
			sourceProp, 
			stateProp 
		}
	  );
    }
           
    // ==========================================================================
    // Processor
    // ==========================================================================
        
   /**
    * Declaration of the state of a Processor
    * @return  org.omg.CollaborationFramework.StateDescriptor
    */
    public StateDescriptor state()
    {
        touch( store );
        return store.processor_state();
    }
    
   /**
    * The coordinator operation returns the Task acting
    * as coordinator of the processor. If no task is
    * associated to the processor, the operation raises
    * the ResourceUnavailable exception.
    * @return  Task
    */
    public org.omg.Session.Task coordinator()
    throws ResourceUnavailable
    {
        touch( store );
        AbstractResource r = store.coordinated_by().resource();
        if( r == null ) throw new ResourceUnavailable();
        return TaskHelper.narrow( r );
    }
    
   /**
    * Operations returns a sequence of Problem
    * instances concerning configuration of a processor
    * relative to the constraints defined under the
    * associated ProcessorModel.  Specializations of ProcessorDelegate are
    * responsible for overriding this method to provide supplimentary assesment
    * of the inital state of the processor specilizations.  The bdefault
    * implemenmtation supports validation of the association of the required
    * input parameters prior to an invocation of the start method.
    *
    * @return  org.omg.CommunityFramework::Problem[]
    */
    public Problem[] verify()
    {
        return validate_usage_descriptors();
    }
    
   /**
    * The Processor start operation is invoked by a client to initate the execution of 
    * a process thread.  A Processor may be started if it is in either the 'notstarted' 
    * or 'suspended' state.  In either case, validation of the processor will be 
    * undertaken.  If the validation fails a CannotStart exception will be thrown.
    * A client can access supplementary information concerning a CannotStart condition 
    * by invoking the <code>verify</code> operation against the Processor interface.
    *
    * @exception  org.omg.Session.CannotStart
    * @exception  org.omg.Session.AlreadyRunning
    */
    public abstract void start()
    throws CannotStart, AlreadyRunning;

   /**
    * Moves a processor into a suspended state.
    * Semantically equivalent to the Task suspend
    * operation.
    *
    * @exception  org.omg.Session.CannotSuspend
    * @exception  org.omg.Session.CurrentlySuspended
    */
    public abstract void suspend()
    throws CannotSuspend, CurrentlySuspended;
    
   /**
    * Stops a processor. Semantically equivalent to the
    * Task stop operation (refer Task and Session, Task
    * specification).
    *
    * @exception  org.omg.Session.CannotStop
    * @exception  org.omg.Session.NotRunning
    */
    public abstract void stop()
    throws CannotStop, NotRunning;

    // ==========================================================================
    // Slave
    // ==========================================================================
    
   /**
    * Returns the Master controlling this Slave.
    * @return  Master controller
    */
    public org.omg.CollaborationFramework.Master master()
    {
        return MasterHelper.narrow( store.controlled_by().resource());
    }
    
    // ==========================================================================
    // Master
    // ==========================================================================
    
   /**
    * Reurns an interator and slave sequence representing the slaves
    * that this master is coordinating.
    * @return  SlaveIterator iterator of types supporting slave that
    *          are coordinated by this master.
    * @param   max_number int number of slave instances to include in the
    *          slaves inout value.
    * @param   slaves SlavesHolder sequence of Slave references.
    */
    public SlaveIterator slaves(int max_number, SlavesHolder slaves)
    {
	  SlaveIterator iterator = null;
        LinkedList list = store.controls();
        synchronized( list )
        {
	      try
	      {
	          SlaveIteratorDelegate delegate = new SlaveIteratorDelegate( orb, list.iterator() );
	          SlaveIteratorPOA servant = new SlaveIteratorPOATie( delegate );
		    iterator = servant._this( orb );

		    AnySequenceHolder anysHolder = new AnySequenceHolder();
		    BooleanHolder booleanHolder = new BooleanHolder();
		    if( iterator.retrieve_next_n_elements( max_number, anysHolder, booleanHolder ) )
		    {
			  int k = anysHolder.value.length;
			  Slave[] sequence = new Slave[k];
		        for( int i=0; i<k; i++ )
			  {
				sequence[i] = SlaveHelper.extract( anysHolder.value[i]);
		        }
			  slaves.value = sequence;
		    }
		    else
		    {
		        slaves.value = new Slave[0];
		    }
		}
		catch( Exception e )
		{
		    String error = "failed to establish SlaveIterator" ;
	          if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		    throw new org.omg.CORBA.INTERNAL( error );
		}
        }
	  return iterator;
    }

    // ==========================================================================
    // Simulator
    // ==========================================================================
    
   /**
    * A model valuetype defines the constraints and operational semantics of this
    * processor.  Implementations of concrete simulators are responsible
    * for ensuring that the appropriate type of model is returned through
    * to the client.
    * @return Model the model this processor is simulating
    */
    public Model model()
    {
        touch( store );
        if( getLogger().isDebugEnabled() ) getLogger().debug("model");
        return store.model();
    }
    
    // ==========================================================================
    // AbstractResource
    // ==========================================================================
    
   /**
    *  The most derived type that this <code>AbstractResource</code> represents.
    */
    public TypeCode resourceKind()
    {
        touch( store );
        return ProcessorHelper.type();
    }
    
   /**
    * Extension of the AbstractResource bind operation to support reception of the
    * notification of association dependencies from external resources on this
    * Processor.
    *
    * @param link Link notification of an association dependency
    */
    public synchronized void bind(Link link)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        synchronized( store )
        {
            
            if( getLogger().isDebugEnabled() ) getLogger().debug("[PRO] bind");
            touch( store );
            
            if( link instanceof CoordinatedBy )
            {
                // Notification to this Processor that it is coordinated by supplied Task
                if( store.coordinated_by().resource() != null ) throw new SemanticConflict();
                store.coordinated_by( (CoordinatedBy) link );
                if( getLogger().isDebugEnabled() ) getLogger().debug("[PRO] bind "
                + "\n\tSOURCE: " + store.name()
                + "\n\tLINK: " + link.getClass().getName()
                + "\n\tTARGET: " + link.resource().name() );
                post( newBindEvent( link ));
            }
            else if( link instanceof ControlledBy)
            {
                // Notification to this Processor that it is under the control of a
                // parent processor
                
                if( store.controlled_by().resource() != null ) throw new SemanticConflict();
                store.controlled_by( (ControlledBy) link );
                if( getLogger().isDebugEnabled() ) getLogger().debug("[PRO] bind "
                + "\n\tSOURCE: " + store.name()
                + "\n\tLINK: " + link.getClass().getName()
                + "\n\tTARGET: " + link.resource().name() );
                post( newBindEvent( link ));
            }
            else if( link instanceof Controls )
            {
                
                // Notification to this Processor that a subsidary processor has been added to
                // the set of processors it is controlling.
                
                LinkedList list = store.controls();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( getProcessorReference(), list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else
            {
                super.bind( link );
            }
        }
    }
    
   /**
    * Replaces an existing Processor dependency with another.
    *
    * @param  old the Link to replace
    * @param  new the replacement Link
    * @exception  <code>ResourceUnavailable</code>
    * if the resource cannot accept the new link binding
    * @exception  <code>ProcessorConflict</code>
    * if a processor is unable or unwilling to provide processing services to a Task.
    * @exception  <code>SemanticConflict</code>
    * if the resource cannot accept the link binding due to a cardinality constraint.
    */
    public synchronized void replace(Link old, Link _new)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        synchronized( store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[PRO] replace");
            touch( store );
            if(( old instanceof CoordinatedBy ) && ( _new instanceof CoordinatedBy ))
            {
                
                // client is requesting the replacement of an exiting CoordinatedBy
                // association from a Task to another Task
                
                if( store.coordinated_by().resource() != null )
                {
                    if( store.coordinated_by().resource().get_key().equal( old.resource().get_key() ))
                    {
                        store.coordinated_by( (CoordinatedBy) _new );
                        post( newReplaceEvent( old, _new ));
                        modify( store );
                        return;
                    }
                    else
                    {
                        if( getLogger().isDebugEnabled() ) getLogger().debug(
			        "supplied 'old' value does not match current 'OwnedBy' link");
                        throw new SemanticConflict();
                    }
                }
                else if( old.resource() == null )
                {
                    store.coordinated_by( (CoordinatedBy) _new );
                    post( newReplaceEvent( old, _new ));
                    modify(store);
                    return;
                }
                else
                {
                    if( getLogger().isDebugEnabled() ) getLogger().debug(
			    "old argument cannot replace a non assigned link");
                    throw new SemanticConflict();
                }
            }
            else if(( old instanceof ControlledBy ) && ( _new instanceof ControlledBy ))
            {
                
                // client is requesting a change in the processor managing this processor
                
                if( store.controlled_by().resource() != null )
                {
                    if( store.controlled_by().resource().get_key().equal( old.resource().get_key() ))
                    {
                        store.controlled_by( (ControlledBy) _new );
                        post( newReplaceEvent( old, _new ));
                        modify( store );
                        return;
                    }
                    else
                    {
                        if( getLogger().isDebugEnabled() ) getLogger().debug(
				  "supplied 'old' value does not match current 'ControlledBy' link");
                        throw new SemanticConflict();
                    }
                }
                else if( old.resource() == null )
                {
                    store.controlled_by( (ControlledBy) _new );
                    post( newReplaceEvent( old, _new ));
                    modify(store);
                    return;
                }
                else
                {
                    if( getLogger().isDebugEnabled() ) getLogger().debug(
			     "old argument cannot replace a non assigned link");
                    throw new SemanticConflict();
                }
            }
            else if (( old instanceof Controls) && ( _new instanceof Controls))
            {
                // client is requesting the replacement of an subsidary Processor with
                // another
                
                try
                {
                    replaceLink( getProcessorReference(), store.controls(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'Controls' references";
                    if( getLogger().isErrorEnabled() ) getLogger().error(s,e);
                    throw new org.omg.CORBA.INTERNAL(s);
                }
            }
            else
            {
                super.replace( old, _new );
            }
            modify( store );
        }
    }
    
   /**
    * Releases an existing dependecy.
    * @param link Link to retract
    */
    public synchronized void release(Link link)
    {
        synchronized( store )
        {
            
            getLogger().info("[PRO] release");
            touch( store );
            
            if( link instanceof CoordinatedBy )
            {
                
                // a Task is notifying this resource of the retraction of an CoordinatedBy
                // association - the implication of this is the removal of the processor
                // as the processor serves not further purpose 
                
                if( store.coordinated_by().resource() != null )
                {
                    try
                    {
                        BaseBusinessObjectKey thisKey = store.coordinated_by().resource().get_key();
                        if( thisKey.equal( link.resource().get_key() ) )
                        {
                            store.coordinated_by( new CoordinatedBy() );
                		    if( getLogger().isDebugEnabled() ) getLogger().debug("[PRO] release "
                			+ "\n\tSOURCE: " + store.name()
                			+ "\n\tLINK: " + link.getClass().getName()
                			+ "\n\tTARGET: " + link.resource().name() );
                            post( newReleaseEvent( link ));
                            modify( store );
                        }
                    }
                    catch( Exception e)
                    {
                        String s = "failed to release CoordinatedBy association on Processor";
                        if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                        throw new org.omg.CORBA.INTERNAL( s );
                    }
                }
                try
		    {
		        remove();
		    }
		    catch( Throwable removeException )
		    {
			  String warning = "failed to self destruct processor";
			  if( getLogger().isWarnEnabled() ) getLogger().warn( warning, removeException );
	          }
            }
            else if( link instanceof ControlledBy )
            {
                
                // a client is notifying this task of the retraction of an ControlledBy
                // association - i.e. this processor is not longer controlled by a parent
                // which implicates the processor life-cycle (processor should be
                // remove - this is a parent responsibility)
                
                if( store.controlled_by().resource() != null )
                {
                    try
                    {
                        BaseBusinessObjectKey thisKey = store.controlled_by().resource().get_key();
                        if( thisKey.equal( link.resource().get_key() ) )
                        {
                            store.controlled_by( new ControlledBy() );
                		    if( getLogger().isDebugEnabled() ) getLogger().debug("[PRO] release "
                			+ "\n\tSOURCE: " + store.name()
                			+ "\n\tLINK: " + link.getClass().getName()
                			+ "\n\tTARGET: " + link.resource().name() );
                            post( newReleaseEvent( link ));
                            modify( store );
                        }
                    }
                    catch( Exception e)
                    {
                        String s = "failed to release Coordinates association on Task";
                        if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                        throw new org.omg.CORBA.INTERNAL( s );
                    }
                }
            }
            else if( link instanceof Controls )
            {
                
                // Notification of the retract of a Controls dependency.
                
                try
                {
                    LinkedList list = store.controls();
                    releaseLink( getProcessorReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release Controls association";
                    if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                    throw new org.omg.CORBA.INTERNAL( s );
                }
            }
            else
            {
                super.release( link );
            }
        }
    }
    
   /**
    * Returns a set of resources linked to this Processor by a specific relationship.
    * This operation may be used by desktop managers to present object relationship 
    * graphs. The Processor expand implmentation suppliments AbstractResource 
    * with the addition of support for CoordinatedBy, Controls, and ControlledBy 
    * link types.
    * Support for abstract link expansion is not available at this time.
    *
    * @return  LinkIterator an iterator of Link instances
    * @param  max_number maximum number of Link instance to include in the
    * seq value.
    * @param  seq Links a sequence of Links matching the type filter
    *
    * @param link Link to retract
    */
    public LinkIterator expand(org.omg.CORBA.TypeCode type, int max_number, LinksHolder links)
    {
        getLogger().info("[PRO] expand");
             
	  if( type.equivalent( CoordinatedByHelper.type() ))
	  {
		links.value = new Link[]{ store.coordinated_by() };
		touch( store );
		return null;
	  }
	  else if( type.equivalent( ControlledByHelper.type() ))
	  {
		links.value = new Link[]{ store.controlled_by() };
		touch( store );
		return null;
	  }
        else if( type.equivalent( ControlsHelper.type() ))
        {
            LinkedList controls = store.controls();
            synchronized( controls )
            {
                // prepare resource sequence
                links.value = create_link_sequence( controls, max_number ); 
		    touch( store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( orb, controls, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( orb );
            }
        }
        else
        {
            return super.expand( type, max_number, links );
        }
    }

    // ==========================================================================
    // BaseBusinessObject operation override
    // ==========================================================================
   /**
    * ProcessorDelegate suppliments the BaseBusienssObject remove implementation
    * through retraction of the coordination links between it and its coordinating 
    * Task, and the removal of any subsidary processors.
    */
    
    public synchronized void remove()
    throws NotRemovable
    {
        synchronized( store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[PRO] remove");
		try
		{
                terminate();
		}
		catch(CannotTerminate e)
		{
		    final String error = "cannot remove processor";
		    if( getLogger().isErrorEnabled() ) getLogger().error( 
			error, e );
		    throw new NotRemovable( error ); // should not happen
		}
		dispose();
        }
    }

    // ==========================================================================
    // Internal methods
    // ==========================================================================

   /**
    * Set the object reference to be returned for this delegate.
    * @param processor the object reference for the processor
    */
    protected void setProcessorReference( Processor processor )
    {
        m_processor = processor;
        setReference( processor );
    }

   /**
    * Returns the object reference for this delegate.
    * @return Processor the object referenced for the delegate
    */
    public Processor getProcessorReference( )
    {
        return m_processor;
    }

   /**
    * Internal method to handle verification of the association of the required
    * consumption and production releationships.
    *
    * @return  org.omg.CommunityFramework::Problem[]
    */
    protected Problem[] validate_usage_descriptors()
    {
        // verify that the processor is associated with a Task

        Task task = null;
        try
        {
		task = coordinator();
        }
        catch( ResourceUnavailable e )
	  {
            // non-association of a Task
            Problem p = new Problem( "NO_COORDINATOR" );
            return new Problem[]{ p };
	  }
        catch( Exception e )
        {
            String error = "failed to verify the existance of a coordinating Task";
            Problem p = new Problem( "PROCESSOR_COORDINATION_ERROR" );
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            return new Problem[]{ p };
        }
        
        // verify that the usage preconditions from ProcessorModel have been
        // satisfied.
        
        ProcessorModel model = store.model();
        UsageDescriptor[] usages = model.usage;
        java.util.Vector vector = new java.util.Vector();
        if( usages.length > 0 )
        {
            try
            {
                LinksHolder links = new LinksHolder();
                task.expand( ConsumesHelper.type(), 0 , links );
                for( int i = 0; i < usages.length; i++ )
                {
                    if( usages[i] instanceof InputDescriptor )
                    {
                        InputDescriptor input = (InputDescriptor) usages[i];
                        Problem inputProblem = validate_input_precondition( input, links.value );
                        if( inputProblem != null ) vector.add( inputProblem );
                    }
                }
            }
            catch( Exception e)
            {
		    e.printStackTrace();
                if( getLogger().isErrorEnabled() ) getLogger().error("failed to expand coordinator",e);
                return new Problem[]{new Problem("INPUT_PRECONDITION_ASSESSMENT_ERROR")};
            }
        }
        return (Problem[]) vector.toArray( new Problem[0] );
    }
    
   /**
    * Internal implemetation of the method to validate the input preconditions
    * declared under the processor model.
    */
    protected Problem validate_input_precondition( InputDescriptor input, Link[] links )
    {
        if( input.required ) try
        {
		ValueFactory factory = null;
		try
		{
		    factory = ((org.omg.CORBA_2_3.ORB)orb).lookup_value_factory( input.getID() );
		}
		catch( Exception e )
		{
		}

            if( factory != null )
            {

		    // the input type argument is a known valuetype, 
                // check that the value argument is an instance of that 
		    // type

		    Consumes link = (Consumes) locateTaggedLink( input.getTag(), links );

		    // make sure the resource is not null
		    if( link.resource() == null ) return new Problem( 
		      "net.osm.nullArgument",
			"Supplied argument is null.",
			"The supplied criteria value for '" + input.getTag()  +
		 	" is null.");

		    // the link's resource must be a GenericResource with 
		    // a value whos instance is a valuetype matching the 
		    // precondition

		    if( !link.resource()._is_a( GenericResourceHelper.id() ))
		    {
			  // it not a generic resource 
		        return new Problem( "net.osm.invalidArgumentContainer",
			"Supplied argument value is not contained within a generic resource.",
			"The criteria value for '" + input.getTag() + 
		 	" must be contained as a value within a GenericResource.");
		    }

		    GenericResource generic = GenericResourceHelper.narrow( link.resource() );
		    boolean ok = factory.getClass().isInstance( generic.value() );
		    if( ok ) return null;

		    if( generic.value() == null ) return new Problem( 
		      "net.osm.nullArgument",
			"GenericResource contained value is null.",
			"The supplied criteria value for '" + input.getTag() +
		 	" contained with a generic resource is null.");

		    // otherwise, the supplied instance is not the correct type
		    // so we need to construct an appropriate problem statement

		    return new Problem( "net.osm.invalidArgumentValue",
			"Supplied argument value does not match required type.",
			"The criteria value for '" + input.getTag() + "' of '" + generic.value().getClass().getName() +
		 	" is not an instance of '" + input.getID() + "'.");

            }
	      else
	      {
                // make sure that the Task coordinating this processor has
                // a input resource corresponding input tag, and that the
                // resource is of the required type
                
                for( int i=0; i<links.length; i++ )
                {
                    // if this link complies with the precondition then return
                    // otherwise post a problem
                    
                    Consumes c = (Consumes) links[i];

                    if( c.tag.equals( input.getTag() ))
                    {
				try
				{
				    // verify the type
				    String ID = input.getID();
				    if( c.resource()._is_a( ID ) )
				    {
					  // ok - type match 
					  return null;
				    }
				    else
				    {
                                java.util.Properties props = new java.util.Properties();
                                props.setProperty("required", ID);
                                props.setProperty("tag", input.getTag() );
                                props.setProperty("supplied", c.resource().resourceKind().id() );
					  if( getLogger().isDebugEnabled() ) getLogger().debug("REQUIRED_INPUT_TYPE_MISMATCH/1");
                                return new Problem("REQUIRED_INPUT_TYPE_MISMATCH", props );
				    }
				}
				catch( Exception e )
				{
                            java.util.Properties props = new java.util.Properties();
                            props.setProperty("required", input.getID());
                            props.setProperty("tag", input.getTag() );
                            props.setProperty("supplied", "UNKNOWN" );
				    if( getLogger().isDebugEnabled() ) getLogger().debug("REQUIRED_INPUT_TYPE_MISMATCH/2");
                            return new Problem("REQUIRED_INPUT_TYPE_MISMATCH", props );
				}
                    }
                }
                // not match found
                java.util.Properties props = new java.util.Properties();
                props.setProperty("tag", input.getTag());
                props.setProperty("required", input.getID());
                return new Problem("REQUIRED_INPUT_IS_MISSING", props);
            }
        }
        catch( Exception e)
        {
            String error = "Unexpected exception while validating an input precondition";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            Problem p = new Problem( "INPUT_PRECONDITION_VALIDATION_ERROR" );
        }
        return null;
    }

    private Link locateTaggedLink( String tag, Link[] links ) throws Exception
    {
        for( int i=0; i<links.length; i++ )
        {
		Link link = links[i];
		if( link instanceof Consumes )
		{
		   if( ((Consumes)link).tag().equals( tag ) ) return link;
		}
        }
	  throw new Exception("no matching tag");
    }
}
