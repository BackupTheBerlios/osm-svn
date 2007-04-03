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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.GeneratedValue;

@Entity
@Table(name="ORDERS")
public class OrderStorageUnit
{
    private int m_id;
    private String m_address;
    private CustomerStorageUnit m_customer;

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

    public String getAddress() 
    {
        return m_address;
    }

    public void setAddress( String address ) 
    {
        m_address = address;
    }
    
    @ManyToOne()
    @JoinColumn
    public CustomerStorageUnit getCustomer() 
    {
        return m_customer;
    }

    public void setCustomer( CustomerStorageUnit customer ) 
    {
        m_customer = customer;
    }

    public String toString()
    {
        return "ORDER: [" + m_id + "], " + m_address;
    }
}
