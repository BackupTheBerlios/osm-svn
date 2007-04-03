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

import net.dpml.util.Logger;
import dpml.util.DefaultLogger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import junit.framework.TestCase;

/**
 * Demonstration of application managed persistence.
 */
public class EntityTestCase extends TestCase
{
    private static final String PERSISTENCE_UNIT_NAME = "@PERSISTENCE-UNIT-NAME@";
    
    private static final String CUSTOMER_NAME = "Allan Quatermain";
    private static final String ORDER_ADDRESS_1 = "10 Lexington Avenue, New York, NY";
    private static final String ORDER_ADDRESS_2 = "20 rue Thibaud, Paris 75014";
    
    public static final Logger LOGGER = new DefaultLogger( "test" );
    
    //
    // setup the storage unit and persistence sub-system configuration
    //
    
    public static final Map<String,String> MAP = new Hashtable<String,String>();
    static
    {
        MAP.put( "toplink.jdbc.url", "jdbc:derby:testDB;create=true" );
        MAP.put( "toplink.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver" );
        MAP.put( "toplink.logging.level", "SEVERE" );
        MAP.put( "toplink.ddl-generation", "drop-and-create-tables" );
        MAP.put( "toplink.application-location", "./target/test" );
    }
    
    // 
    // testcase state
    //
    
    private EntityManagerFactory m_factory;
    
   /**
    * Setup the testcase during which we establish the entity manager to 
    * be used throught the test process.
    */
    public void setUp() throws Exception
    {
        m_factory = Persistence.createEntityManagerFactory( PERSISTENCE_UNIT_NAME, MAP );
    }
    
    public void tearDown() throws Exception
    {
        m_factory.close();
    }
    
    public void testPersistence() throws Exception
    {
        EntityManager manager = m_factory.createEntityManager();
        manager.getTransaction().begin();
        CustomerStorageUnit c = executeCreation( manager, CUSTOMER_NAME );
        verifyCustomerCreation( manager, CUSTOMER_NAME );
        LOGGER.info( "validating state" );
        int n = getCustomerCount( manager );
        int m = getOrderCount( manager );
        LOGGER.info( "customer count: " + n );
        LOGGER.info( "order count: " + m );
        assertEquals( "customer count", 1, n );
        assertEquals( "order count", 2, m );
        LOGGER.info( "deleting customer" );
        manager.remove( c );
        n = getCustomerCount( manager );
        m = getOrderCount( manager );
        LOGGER.info( "customer count: " + n );
        LOGGER.info( "order count: " + m );
        assertEquals( "customer count", 0, n );
        assertEquals( "order count", 0, m );
        manager.getTransaction().commit();
    }
    
    private CustomerStorageUnit executeCreation( EntityManager manager, String name ) throws Exception
    {
        LOGGER.info( "creating customer" );
        CustomerStorageUnit customer = new CustomerStorageUnit();
        customer.setName( name );

        LOGGER.info( "creating orders" );
        OrderStorageUnit order1 = new OrderStorageUnit();
        order1.setAddress( ORDER_ADDRESS_1 );

        OrderStorageUnit order2 = new OrderStorageUnit();
        order2.setAddress( ORDER_ADDRESS_2 );

        LOGGER.info( "setting referential associations" );
        customer.getOrders().add( order1 );
        order1.setCustomer( customer );

        customer.getOrders().add( order2 );
        order2.setCustomer( customer );
        
        LOGGER.info( "storing customer" );
        manager.persist( customer );
        return customer;
    }
    
    private void verifyCustomerCreation( EntityManager manager, String name ) 
    {
        CustomerStorageUnit c = findCustomer( manager, name );
        Collection<OrderStorageUnit> orders = c.getOrders();
        if( orders == null || orders.size() != 2) 
        {
            fail(
              "Unexpected number of orders: " 
              + ( (orders == null) ? "null" : "" + orders.size() ) );
        }
        LOGGER.info( c.toString() );
        for( OrderStorageUnit order : orders )
        {
            LOGGER.info( order.toString() );
        }
    }
    
    private CustomerStorageUnit findCustomer( EntityManager manager, String name )
    {
        Query query = manager.createQuery( "SELECT c FROM CustomerStorageUnit c WHERE c.name = :name" );
        query.setParameter( "name", name );
        return (CustomerStorageUnit) query.getSingleResult();
    }
    
    private int getCustomerCount( EntityManager manager ) 
    {
        Query query = manager.createQuery( "SELECT c FROM CustomerStorageUnit c" );
        List list = query.getResultList();
        return list.size();
    }
    
    private int getOrderCount( EntityManager manager ) 
    {
        Query query = manager.createQuery( "SELECT o FROM OrderStorageUnit o" );
        List list = query.getResultList();
        return list.size();
    }
    
}
