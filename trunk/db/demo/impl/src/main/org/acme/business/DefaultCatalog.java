/*
 * Copyright 2007 Stephen J. McConnell
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.acme.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.dpml.util.Logger;

import org.acme.api.Catalog;
import org.acme.api.Customer;
import org.acme.unit.CustomerStorageHome;
import org.acme.unit.CustomerStorageUnit;
import org.acme.unit.OrderStorageUnit;

public class DefaultCatalog implements Catalog
{
    public interface Context
    {
        Map<String,String> getFactoryParameters();
    }
    
    private final Logger m_logger;
    private final CustomerStorageHome m_home;
    private final EntityManagerFactory m_factory;
    
    public DefaultCatalog( Logger logger, Context context )
    {
        m_logger = logger;
        Map<String,String> parameters = context.getFactoryParameters();
        m_home = new CustomerStorageHome( parameters );
        m_factory = m_home.getEntityManagerFactory();
    }
    
    public void dispose()
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "catalog disposal" );
        }
        m_factory.close();
    }
    
    public Iterable<Customer> getCustomers()
    {
        List result = m_home.getCustomers();
        return new CustomerWrapper( result );
    }
    
    public Customer create( String name )
    {
        CustomerStorageUnit unit = m_home.create( name );
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( 
              "created new customer [" 
              + unit.getID() 
              + "]" );
        }
        return new DefaultCustomer( unit );
    }
    
    private class CustomerWrapper implements Iterable<Customer>
    {
        private List m_units;
        
        CustomerWrapper( List units )
        {
            m_units = units;
        }
        
        public Iterator<Customer> iterator()
        {
            return new LazyIterator( m_units );
        }
    }
    
    private class LazyIterator implements Iterator<Customer>
    {
        private Iterator m_iterator;
        
        LazyIterator( List units )
        {
            m_iterator = units.iterator();
        }
        
        public boolean hasNext()
        {
            return m_iterator.hasNext();
        }
        
        public Customer next()
        {
            CustomerStorageUnit unit = (CustomerStorageUnit) m_iterator.next();
            return new DefaultCustomer( unit );
        }
        
        public void remove()
        {
            m_iterator.remove();
        }
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }    
}
