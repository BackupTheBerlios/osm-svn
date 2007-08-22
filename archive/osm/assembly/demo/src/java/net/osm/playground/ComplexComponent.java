/*
 * ComplexComponent.java
 */

package net.osm.playground;

import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

/**
 * This is a minimal demonstration component that declares no interface but
 * has dependecies on two services.  These include SimpleService and 
 * BasicService. 
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class ComplexComponent extends AbstractLogEnabled
implements Serviceable, Initializable, Executable, Disposable
{

    private ServiceManager m_manager;
    private SimpleService m_simple;
    private BasicService m_basic;

    //=================================================================
    // Serviceable
    //=================================================================
    
    /**
     * Pass the <code>ServiceManager</code> to the <code>Serviceable</code>.
     * The <code>Serviceable</code> implementation uses the specified
     * <code>ServiceManager</code> to acquire the services it needs for
     * execution.
     *
     * @param manager The <code>ServiceManager</code> which this
     *                <code>Serviceable</code> uses.
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
        if( getLogger().isDebugEnabled() )
          getLogger().debug("service");

	  m_manager = manager;
    }


    //=======================================================================
    // Initializable
    //=======================================================================

    public void initialize()
    throws Exception
    {       
        if( getLogger().isDebugEnabled() )
          getLogger().debug("initialize");

        //
        // verify current state
        //

        if( getLogger() == null ) throw new IllegalStateException(
          "Logging channel has not been assigned.");

        if( m_manager == null ) throw new IllegalStateException(
          "Manager has not been declared.");

        //
        // lookup the primary service
        //

        m_simple = (SimpleService) m_manager.lookup( "simple" );
        m_basic = (BasicService) m_manager.lookup( "basic" );

    }

    //=======================================================================
    // Executable
    //=======================================================================

    public void execute()
    {
        getLogger().info("hello from ComplexComponent");
        m_simple.doObjective();
        m_basic.doPrimeObjective();
    }

    //=======================================================================
    // Disposable
    //=======================================================================
    
    public void dispose()
    {
        if( getLogger().isDebugEnabled() )
          getLogger().debug("dispose");

        m_simple = null;
        m_manager = null;
    }

}
