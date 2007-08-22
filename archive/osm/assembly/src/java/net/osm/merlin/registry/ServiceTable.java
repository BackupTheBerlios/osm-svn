/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package net.osm.merlin.registry;

import java.util.List;
import java.util.LinkedList;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.excalibur.containerkit.metainfo.ServiceDesignator;

/**
 * Internal table that holds references to the available component types 
 * that represent candidate providers for a single service type. 
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:29 $
 */
final class ServiceTable extends AbstractLogEnabled
{

    //=======================================================================
    // state
    //=======================================================================

   /**
    * Component type lists keyed by service designator.
    */
    private List m_providers = new LinkedList();

   /**
    * Identification of the service type that this table is supporting.
    */
    private ServiceDesignator m_designator;

    public ServiceTable( ServiceDesignator designator, Logger logger )
    {
        m_designator = designator;
        super.enableLogging( logger );
    }

    //=======================================================================
    // ServiceTable
    //=======================================================================

   /**
    * Add a service provider to the set of provider managed by this table.
    *
    * @param classname the component class name
    * @return the component type
    */
    public void add( ComponentType type )
    {
        //getLogger().debug("addition\n\ttype:" + type + "\n\ttable: " + this );
        m_providers.add( type );
    }

   /**
    * Returns the set of providers currently registered in the table.
    * @return the set of component types capable of acting as a provider for the 
    *     service managed by the table
    */
    public ComponentType[] getTypes()
    {
        return (ComponentType[]) m_providers.toArray( new ComponentType[0] );
    }

   /**
    * Return the service type for the table.
    * @return the service designator
    */
    public ServiceDesignator getService()
    {
        return m_designator;
    }

   /**
    * Return the number of entries in the table.
    * @return the number of providers
    */
    public int getSize()
    {
        return m_providers.size();
    }

   /**
    * Returns true if the table service designator matches the supplied designator.
    * @param service a service type designator
    * @return TRUE if the supplied service type matches the the service type for 
    *    this table.
    */
    public boolean matches( ServiceDesignator service )
    {
        return m_designator.matches( service );
    }

    public String toString()
    {
       return "ServiceTable:" 
         + System.identityHashCode( this ) 
         + ", " 
         + m_designator;
    }
}

