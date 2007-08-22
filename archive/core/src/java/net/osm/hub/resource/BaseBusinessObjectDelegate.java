

package net.osm.hub.resource;

import java.util.Hashtable;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.logger.LogEnabled;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.CosLifeCycle.LifeCycleObject;
import org.omg.CosLifeCycle.FactoryFinder;
import org.omg.CosLifeCycle.NVP;
import org.omg.CosLifeCycle.NoFactory;
import org.omg.CosLifeCycle.NotCopyable;
import org.omg.CosLifeCycle.InvalidCriteria;
import org.omg.CosLifeCycle.CannotMeetCriteria;
import org.omg.CosLifeCycle.NotMovable;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CosLifeCycle.GenericFactory;
import org.omg.CosLifeCycle.GenericFactoryHelper;
import org.omg.CosTime.TimeService;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNotifyComm.StructuredPushSupplierHelper;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.TimeBase.UtcT;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.NamingAuthority.RegistrationAuthority;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.Session;

import org.omg.Session.BaseBusinessObjectKey;
import org.omg.Session.IdentifiableDomainConsumer;
import org.omg.Session.BaseBusinessObjectOperations;
import org.omg.Session.BaseBusinessObjectKey;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.AbstractResource;
import org.omg.Session.Workspace;

import net.osm.list.LinkedList;
import net.osm.list.EntryStorage;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;
import net.osm.hub.pss.ConsumerStorage;
import net.osm.hub.pss.ConsumerStorageHome;
import net.osm.hub.pss.DomainStorage;
import net.osm.hub.pss.DomainStorageHome;
import net.osm.hub.pss.SubscriberStorage;
import net.osm.hub.pss.SubscriberStorageHome;
import net.osm.hub.pss.AbstractResourceStorage;
import net.osm.hub.pss.LinkStorage;
import net.osm.hub.pss.LinkStorageHome;
import net.osm.hub.resource.StructuredPushSupplierService;
import net.osm.hub.resource.StructuredPushSupplierDelegate;

import net.osm.time.TimeUtils;
import net.osm.util.Incrementor;
import net.osm.util.IOR;
import net.osm.util.ExceptionHelper;
import net.osm.hub.gateway.CannotTerminate;
import net.osm.hub.gateway.FactoryException;
import net.osm.session.SubscriberProxy;

/**
* BaseBusinessObject is the abstract base class for all  
* principal Task and Session objects. It has identity, is  
* transactional, has a lifecycle, and is a notification  
* supplier. The CosNotification service defines a StructuredEvent 
* that provide a framework for the naming of an event and the  
* association of specific properties to that event.  All events  
* specified within this facility conform to the StructuredEvent  
* interface.  This specification requires specific event types  
* to provide the following properties as a part of the  
* filterable_data of the structured event header. Under the  
* CosNotification specification all events are associated with  
* a unique domain name space. This specification establishes  
* the domain namespace "org.omg.session" for structured events  
* associated with AbstractResource and its sub-types.  
* IdentifiableDomainConsumer defines a StructuredPushConsumer  
* callback object that can be passed to an implementation of  
* BaseBusienssObject under the add_consumer operation.  An  
* implementation of this operation is required to establish  
* the association of the consumer with an instance of  
* StructuredPushSupplier before returning the supplier to the  
* invoking client. The operations, creation, modification,  
* and access return a Timestamp value.  
*/

public class BaseBusinessObjectDelegate extends IdentifiableDomainObjectDelegate implements BaseBusinessObjectOperations
{

    //=========================================================================
    // static
    //=========================================================================
    
    private static final Incrementor entryIncrementor = Incrementor.create("ENTRY");
    private static final Incrementor subscriptionIncrementor = Incrementor.create("SUBSCRIPTION");
    //private static final Incrementor eventIncrementor = Incrementor.create("EVENT-STORE");

    //=========================================================================
    // state
    //=========================================================================

    protected boolean trace = false;

   /**
    * Storage object representing this base business object.
    */
    private AbstractResourceStorage store;
    
   /**
    * Collection of the identifying state members for this instance.
    */
    private BaseBusinessObjectKey key;

    private StructuredPushSupplierService spsService;

    //=======================================================================
    // Composable
    //=======================================================================

    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation uses the supplied
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     * @param controller the <code>ComponentManager</code> for this delegate
     * @exception ComponentException
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("BBO composition");
	  super.compose( manager );
	  try
	  {
	      this.spsService = 
              (StructuredPushSupplierService)manager.lookup("STRUCTURED-PUSH-SUPPLIER");
	  }
	  catch( Exception e )
	  {
		final String error = "manager failed to provide structured push supplier service";
		throw new ComponentException( error, e);
	  }
        if( getLogger().isDebugEnabled() ) getLogger().debug("BBO user composition");

    }

    protected StructuredPushSupplierService getStructuredPushSupplierService()
    {
        return spsService;
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Signal completion of contextualization phase and readiness to serve requests.
    * @exception Exception if the servant cannot complete normal initialization
    */
    public void initialize()
    throws Exception
    {
	  final String pre = "BBO initialization";
	  final String post = "BBO initialization complete";
	  if( getLogger().isDebugEnabled() ) getLogger().debug( pre );
        super.initialize();
        this.store = (AbstractResourceStorage) super.getContext().getStorageObject();
	  if( getLogger().isDebugEnabled() ) getLogger().debug( post );
    }

    //=======================================================================
    // Vulnerable implementation
    //=======================================================================

   /**
    * Destroys the persistent identity of this object. The 
    * implementation is responsible for disposing of references 
    * to external resources on completion of termiation actions. 
    */
    public void terminate( ) throws CannotTerminate
    {
	  synchronized( store )
        {
            if( !expendable() ) throw new CannotTerminate("unknown");
            if( getLogger().isDebugEnabled() ) getLogger().debug("[BBO] terminate ");
		try
		{
                //
                // drop the subscriptions associated with this resource
                //
                                
		    StructuredPushSupplierService spss = getStructuredPushSupplierService();
                if( getLogger().isDebugEnabled() ) getLogger().debug(
			"destroying subscriptions");
		    
                Iterator iterator = store.subscriptions().iterator();
                while( iterator.has_next() )
                {
			  try
			  {
			      SubscriberStorage s = (SubscriberStorage) iterator.next();
				s.terminal( true );
			  }
		        catch( Throwable e )
			  {
				if( getLogger().isWarnEnabled() ) getLogger().warn(
				  "Exception (ignored) while terminating subscriber.", e );
			  }
		    }
		}
		catch( Exception e )
		{
		    if( getLogger().isErrorEnabled() ) getLogger().error(
			"unexpected exception during termination", e);
		}
		super.terminate();
        }
    }

    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Clean up state members.
    */ 
    public synchronized void dispose()
    {
 	  if( getLogger().isDebugEnabled() ) getLogger().debug("[BBO] dispose");
        this.store = null;
        this.key = null;
	  super.dispose();
    }
    
    //=======================================================================
    // BaseBusinessObjectOperations
    //=======================================================================
    
   /**
    * The creation operation returns the date and time of the creation of
    * this BaseBusinessObject.
    * @return UtcT creation date
    */
    public UtcT creation()
    {
        return TimeUtils.convertToUtcT( store.creation() );
    }
    
    
   /**
    * The modification operation returns the last modification date and time
    * (where modification refers to a modification of the state of a concrete
    * derived type).
    * @return UtcT modification date
    */
    public UtcT modification()
    {
        return TimeUtils.convertToUtcT( store.modification());
    }
    
   /**
    * The access operation returns the date and time a derived type was accessed.
    * This value will not be changed through a server request to creation,
    * modification or access requestors.
    * @return UtcT last access date
    */
    public UtcT access()
    {
        return TimeUtils.convertToUtcT( this.store.access() );
    }
    
   /**
    * The <code>add_consumer</code> method takes a client supplied
    * <code>IdentifiableDomainConsumer</code> and returns a
    * <code>StructuredPushSupplier</code> event channel interface.
    * This operation allows clients to establish event subscriptions
    * to structured events produced by this BaseBusinessObject.
    * @return StructuredPushSupplier event supplier
    */
    public StructuredPushSupplier add_consumer( IdentifiableDomainConsumer idc )
    {
	  if( idc == null )
	  {
		if( getLogger().isWarnEnabled() ) getLogger().warn("attempt to add a null consumer");
		throw new org.omg.CORBA.INTERNAL("null consumer argument");
	  }

        StructuredPushSupplierService spss = null;
        Iterator iterator = null;

        synchronized( store )
        {
            try
            {
		    spss = getStructuredPushSupplierService();
                iterator = store.subscriptions().iterator();
            }
            catch( Throwable e )
            {
                final String error = "Unable to establish the supplier service ot subscription iterator.";
                throw new org.omg.CORBA.INTERNAL( error );
            }

		//
		// make sure that the supplied IDC is not already subscribed
		//

            while( iterator.has_next() )
            {
                try
                {
			  SubscriberStorage subscriber = (SubscriberStorage) iterator.next();
			  if( subscriber.consumer()._is_equivalent( idc ) ) 
                      return spss.createStructuredPushSupplier( subscriber );
                }
 		    catch( NoEntry e )
	          {
			   // ignorable - just means that entry was removed from the 
			   // list between the call to hasNext and next 
		    }
                catch( Throwable e )
		    {
			   // an internal problem occured while resolving the supplier object ref
			  final String error = "An unexpected problem occured while attempting to "
                      + "return the reference to an existing subscription.";
			  if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
                    e.printStackTrace();
			  throw new org.omg.CORBA.INTERNAL( error );
	          }
            }

            if( getLogger().isDebugEnabled() ) getLogger().debug("adding IDC consumer");
		SubscriberStorage subscriber = null;
		try
		{
		    subscriber = spss.createSubscriberStorage( store, idc );
                store.subscriptions().add( subscriber );
                if( getLogger().isDebugEnabled() ) getLogger().debug("created a new subscription ");
                modify( store );
		    return spss.createStructuredPushSupplier( subscriber );
		}
		catch( Throwable e )
		{
		    final String error = "subscriber creation failure";
		    if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		    throw new org.omg.CORBA.INTERNAL( error );
            }
        }
    }
        
    // LifeCycle object implementation
    
   /**
    * The copy operation makes a copy of the object. The copy is located in the scope of
    * the factory finder passed as the first parameter. The copy operation returns an object
    * reference to the new object. The new object is initialized from the existing object.
    * The first parameter, there, may be a nil object reference. If passed a nil object
    * reference, the target object can determine the location or fail with the NoFactory
    * exception.
    * The second parameter, the_criteria, allows for a number of optional parameters
    * to be passed. Typically, the target simply passes this parameter to the factory used in
    * creating the new object. The criteria parameter is explained in detail in section 6.2.4
    * If the target cannot find an appropriate factory to create a copy "over there", the
    * NoFactory exception is raised. An implementation that refuses to copy itself should
    * raise the NotCopyable exception. If the target does not understand the criteria, the
    * InvalidCriteria exception is raised. If the target understands the criteria but
    * cannot satisfy the criteria, the CannotMeetCriteria exception is raised.
    * In addition to these exceptions, implementations may raise standard CORBA
    * exceptions. For example, if resources cannot be acquired for the copied object,
    * NO_RESOURCES will be raised. Similarly, if a target does not implement the copy
    * operation, the NO_IMPLEMENT exception will be raised.
    * It is implementation dependent whether this operation is atomic.
    */
    public LifeCycleObject copy(FactoryFinder there, NVP[] the_criteria)
    throws NoFactory, NotCopyable, InvalidCriteria, CannotMeetCriteria
    {
        throw new NotCopyable();
    }
    
   /**
    * The move operation on the target moves the object to the scope of the factory finder
    * passed as the first parameter. The object reference for the target object remains valid
    * after move has successfully executed.
    * The first parameter, there, may be a nil object reference. If passed a nil object
    * reference, the target object can determine the location or fail with the NoFactory
    * exception.
    * The second parameter, the_criteria, allows for a number of optional parameters
    * to be passed. Typically, the target simply passes this parameter to the factory used in
    * migrating the new object. The criteria parameter is explained in detail in section 6.2.4
    * If the target cannot find an appropriate factory to support migration of the object "over
    * there", the NoFactory exception is raised. An implementation that refuses to move
    * itself should raise the NotMovable exception. If the target does not understand the
    * criteria, the InvalidCriteria exception is raised. If the target understands the
    * criteria but cannot satisfy the criteria, the CannotMeetCriteria exception is
    * raised.
    * In addition to these exceptions, implementations may raise standard CORBA
    * exceptions. For example, if resources cannot be acquired for migrating the object,
    * NO_RESOURCES will be raised. Similarly, if a target does not implement the move
    * operation, the NO_IMPLEMENT exception will be raised.
    * It is implementation dependent whether this operation is atomic.
    */
    public synchronized void move(FactoryFinder there, NVP[] the_criteria)
    throws NoFactory, NotMovable, InvalidCriteria, CannotMeetCriteria
    {
        throw new NotMovable();
    }
    
    
   /**
    * Remove checks all standing subscriptions for a subscription to the remove
    * event, and if found, posts a new remove event to each subscribed consumer.
    * The implementation proceeds with destruction of the resource storage instance.
    */
    public synchronized void remove()
    throws NotRemovable
    {
        synchronized( store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[BBO] remove");
		try
		{
                terminate();
		}
		catch(CannotTerminate e)
		{
		    if( getLogger().isErrorEnabled() ) getLogger().error( e.getMessage(), e );
		    throw new NotRemovable( e.getMessage() ); // should not happen
		}
		try
		{
 		    dispose();
		}
		catch(Exception e)
		{
		    System.err.println("unexpected error while removing resource");
		    e.printStackTrace();
		}
        }
    }
    
    // ===========================================================
    // Extensions.
    // ===========================================================
    
    public BaseBusinessObjectKey get_key()
    {
        if( key == null ) key = new BaseBusinessObjectKey( 
          domain(), store.random(), store.creation() );
        return key;
    }
    
    // ===========================================================
    // utilities
    // ===========================================================
    
   /**
    * If there is a subscription associated with this resource that has subscribed
    * to the event type of the suplied event, then event will be placed into a persistent
    * event storage object and passed to each respective consumer queue.
    */
    protected void post( final StructuredEvent event )
    {
        if( event == null ) throw new NullPointerException( 
		"null structured event supplied to post" );
                
        StructuredPushSupplierService spss = getStructuredPushSupplierService();
        net.osm.list.LinkedList subscriptions = store.subscriptions();
        synchronized( subscriptions )
        {
            Iterator iterator = store.subscriptions().iterator();
            while( iterator.has_next() )
            {
                try
                {
			  final SubscriberStorage subscriber = (SubscriberStorage) iterator.next();
                    SubscriberProxy proxy = spss.createSubscriber( subscriber );
                    proxy.post( event );
                }
                catch(NoEntry e)
                {
			  // ignore this case - just means that a subscriber has removed itself
                    // while we have been processing the iterator
	          }
                catch(Throwable e)
                {
			  final String error = "Unexpected exception while posting event.";
			  ExceptionHelper.printException( error, e, this, true );
                    if( getLogger().isErrorEnabled() ) getLogger().error( error, e);
	          }
            }
        }
    }
}
