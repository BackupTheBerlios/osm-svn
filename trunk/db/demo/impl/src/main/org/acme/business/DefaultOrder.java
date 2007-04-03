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

import org.acme.api.Order;
import org.acme.api.Customer;
import org.acme.unit.CustomerStorageUnit;
import org.acme.unit.OrderStorageUnit;

public class DefaultOrder implements Order
{
    private final OrderStorageUnit m_unit;
    
    DefaultOrder( final OrderStorageUnit unit )
    {
        m_unit = unit;
    }

    public int getID() 
    {
        return m_unit.getID();
    }

    public String getAddress() 
    {
        return m_unit.getAddress();
    }

    public Customer getCustomer() 
    {
        CustomerStorageUnit store = m_unit.getCustomer();
        return new DefaultCustomer( store );
    }

    public boolean equals( Object other )
    {
        if( other instanceof Order )
        {
            Order order = (Order) other;
            return getID() == order.getID();
        }
        else
        {
            return false;
        }
    }
}
