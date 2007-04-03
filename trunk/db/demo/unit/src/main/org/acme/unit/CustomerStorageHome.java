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
 
package org.acme.unit;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.Persistence;

import static javax.persistence.CascadeType.ALL;

public class CustomerStorageHome
{
    private static final String PERSISTENCE_UNIT_NAME = "@PERSISTENCE-UNIT-NAME@";
    
    private final EntityManagerFactory m_factory;
    
    public CustomerStorageHome( final Map parameters )
    {
        m_factory = 
          Persistence.createEntityManagerFactory( 
            PERSISTENCE_UNIT_NAME, 
            parameters );
    }
    
    public EntityManagerFactory getEntityManagerFactory()
    {
        return m_factory;
    }
    
    public CustomerStorageUnit create( String name )
    {
        EntityManager manager = m_factory.createEntityManager();
        try
        {
            manager.getTransaction().begin();
            CustomerStorageUnit unit = new CustomerStorageUnit();
            unit.setName( name );
            manager.persist( unit );
            manager.getTransaction().commit();
            return unit;
        }
        finally
        {
            manager.close();
        }
    }
    
    public CustomerStorageUnit find( int id )
    {
        EntityManager manager = m_factory.createEntityManager();
        try
        {
            Query query = manager.createQuery( "SELECT c FROM CustomerStorageUnit c WHERE c.ID = :id" );
            query.setParameter( "id", id );
            CustomerStorageUnit unit = (CustomerStorageUnit) query.getSingleResult();
            if( null != unit )
            {
                return unit;
            }
            else
            {
                final String error = 
                  "Customer with id [" + id + "] not found.";
                throw new IllegalArgumentException( error );
            }
        }
        finally
        {
            manager.close();
        }
    }
    
    public List getCustomers()
    {
        EntityManager manager = m_factory.createEntityManager();
        try
        {
            Query query = manager.createQuery( "SELECT c FROM CustomerStorageUnit c" );
            return query.getResultList();
        }
        finally
        {
            manager.close();
        }
    }
}
