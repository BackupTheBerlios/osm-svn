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

import org.acme.api.Customer;
import org.acme.api.Order;
import org.acme.unit.CustomerStorageHome;
import org.acme.unit.CustomerStorageUnit;
import org.acme.unit.OrderStorageUnit;


public class DefaultCustomer implements Customer
{
    private final CustomerStorageUnit m_unit;
    
    public DefaultCustomer( CustomerStorageHome home, int id )
    {
        this( home.find( id ) );
    }
    
    public DefaultCustomer( CustomerStorageUnit unit )
    {
        m_unit = unit;
    }
    
    public int getID()
    {
        return m_unit.getID();
    }
    
    public String getName()
    {
        return m_unit.getName();
    }
    
    public void setName( String value )
    {
        m_unit.setName( value );
    }
    
    public Iterable<Order> getOrders()
    {
        return new OrderWrapper( m_unit.getOrders() );
    }
    
    public boolean equals( Object other )
    {
        if( other instanceof Customer )
        {
            Customer customer = (Customer) other;
            return getID() == customer.getID();
        }
        else
        {
            return false;
        }
    }
    
    private class OrderWrapper implements Iterable<Order>
    {
        private Iterable<OrderStorageUnit> m_units;
        
        OrderWrapper( Iterable<OrderStorageUnit> units )
        {
            m_units = units;
        }
        
        public Iterator<Order> iterator()
        {
            return new LazyIterator( m_units );
        }
    }
    
    private class LazyIterator implements Iterator<Order>
    {
        private Iterator<OrderStorageUnit> m_iterator;
        
        LazyIterator( Iterable<OrderStorageUnit> units )
        {
            m_iterator = units.iterator();
        }
        
        public boolean hasNext()
        {
            return m_iterator.hasNext();
        }
        
        public Order next()
        {
            OrderStorageUnit unit = m_iterator.next();
            return new DefaultOrder( unit );
        }
        
        public void remove()
        {
            m_iterator.remove();
        }
    }
}
