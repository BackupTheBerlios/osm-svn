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

import java.io.Serializable;
import java.util.Collection;
import java.util.ArrayList;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.PostPersist;
import javax.persistence.GeneratedValue;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table( name="CUSTOMERS" )
public class CustomerStorageUnit implements Serializable
{
    private int m_id;
    private String m_name;
    private Collection<OrderStorageUnit> m_orders = new ArrayList<OrderStorageUnit>();

    @Id
    @GeneratedValue
    public int getID() 
    {
        return m_id;
    }

    public void setID( int id ) 
    {
        m_id = id;
    }

    public String getName() 
    {
        return m_name;
    }

    public void setName( String name ) 
    {
        m_name = name;
    }

    @OneToMany( cascade=ALL, mappedBy="customer" )
    public Collection<OrderStorageUnit> getOrders()
    {
        return m_orders;
    }

    public void setOrders( Collection<OrderStorageUnit> orders ) 
    {
        m_orders = orders;
    }
        
    @PostPersist
    public void notifyPersist()
    {
        //System.out.println( "CUSTOMER PERSISTED " + m_name );
    }
    
    public String toString()
    {
        return "CUSTOMER: [" + m_id + "], " + m_name + ", " + m_orders.size() + " orders";
    }
}
