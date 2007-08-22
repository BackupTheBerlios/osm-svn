

package net.osm.session.processor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Iterator;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.activity.Initializable;

import net.osm.session.message.SystemMessage;
import net.osm.session.message.DefaultSystemMessage;
import net.osm.session.message.MessageClassification;
import net.osm.session.message.MessageHeader;
import net.osm.session.message.DefaultMessageHeader;
import net.osm.session.message.MessagePriority;
import net.osm.session.message.MessageBody;
import net.osm.session.message.DefaultMessageBody;

/**
 * AbstractAppliance is an abstract runnable object that handles the 
 * processors main background execution thread.
 */

public abstract class AbstractAppliance extends AbstractLogEnabled 
implements Appliance, Contextualizable, Disposable
{

    //=================================================================
    // static 
    //=================================================================

    protected static final int CONTINUE = 0;
    protected static final int START = 1;
    protected static final int SUSPEND = 2;
    protected static final int RESUME = 3;
    protected static final int STOP = 4;

    //=================================================================
    // state
    //=================================================================

   /**
    * The request action to apply.
    */
    protected int m_action = CONTINUE;

   /**
    * Internal thread state.
    */
    protected int m_state = Appliance.IDLE;

   /**
    * The appliance context.
    */
    private Context m_context;

   /**
    * The processor delegate hosting the appliance.
    */
    private ProcessorDelegate m_processor;

   /**
    * The list of listeners listening to the appliance state.
    */
    private LinkedList m_listeners = new LinkedList();

    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Establish the appliance context.
    * @param context the servant context
    */
    public void contextualize( Context context ) throws ContextException
    {
	  m_context = context;
        m_processor = (ProcessorDelegate) context.get( ProcessorDelegate.PROCESSOR_KEY );
        int m_state = ((Integer)context.get( ProcessorDelegate.PROCESSOR_STATE_KEY )).intValue();
        
    }

    //=================================================================
    // Runnable
    //=================================================================

   /**
    * The run method must be overriden by an application.  Implementation
    * should check frequently for modification to the internal reference to 
    * m_action and take appropriate actions to the bring the appliance into
    * the desired state.
    */
    public abstract void run();

    //=================================================================
    // Appliance
    //=================================================================

   /**
    * Return the current appliance state.
    */
    public synchronized int getApplianceState()
    {
        return m_state;
    }

    //=================================================================
    // Startable
    //=================================================================

   /**
    * Method invoked by a processor to request appliance termination.
    * The run implementation should frequently check the current state using the 
    * <code>getState</code> method for posible SUSPENDED or TERMINATED values
    * in which case the run implementation should return.
    */
    public void start() throws Exception
    {
        m_action = START;
    }

   /**
    * Method invoked by a processor to request appliance termination.
    * The run implementation should frequently check the current state using the 
    * <code>getState</code> method for posible SUSPENDED or TERMINATED values
    * in which case the run implementation should return.
    */
    public void stop() throws Exception
    {
        m_action = STOP;
    }

    //=================================================================
    // Suspendable
    //=================================================================

   /**
    * Method invoked by a processor to request appliance suspension.
    * The run implementation should frequently check the current state using the 
    * <code>getState</code> method for posible SUSPENDED or TERMINATED values
    * in which case the run implementation should return.
    */
    public void suspend()
    {
        m_action = SUSPEND;
    }

   /**
    * Method invoked by a processor to request appliance resumption.
    */
    public void resume()
    {
        m_action = RESUME;
    }

    //=================================================================
    // ApplianceHandler
    //=================================================================

   /**
    * Adds an <code>ApplianceListener</code> to an <code>Appliance</code>.
    */
    public void addApplianceListener( ApplianceListener listener )
    {
        synchronized( m_listeners )
        {
            m_listeners.add( listener );
        }
    }

   /**
    * Removes an <code>ApplianceListener</code> from an <code>Appliance</code>.
    */
    public synchronized void removeApplianceListener( ApplianceListener listener )
    {
        synchronized( m_listeners )
        {
            m_listeners.remove( listener );
        }
    }

    //=================================================================
    // AbstractAppliance
    //=================================================================

   /**
    * Set the current appliace state.
    */
    protected void setApplianceState( int state )
    {
        m_state = state;
        m_action = CONTINUE;
        fireApplianceEvent( state );
    }

   /**
    * Fire an appliance state change event.
    */
    protected void fireApplianceEvent( int state )
    {
        ApplianceEvent event = new ApplianceEvent( this, state );
        synchronized( m_listeners )
        {
            Iterator iterator = m_listeners.iterator();
            while( iterator.hasNext() )
            {
                ((ApplianceListener)iterator.next()).stateChanged( event );
            }
        }
    }

    //=================================================================
    // Disposable 
    //=================================================================

   /**
    * Clean up state members.
    */ 
    public synchronized void dispose()
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug("dispose");
	  m_context = null;
	  m_processor = null;
        m_listeners = null;
    }

}
