/*
 * Copyright 2006 Stephen J. McConnell
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

package net.osm.process.info;

import java.util.Arrays;

import net.dpml.lang.AbstractDirective;

/**
 * The ConsumptionDirective describes the consumption of a set of products.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ConsumptionDirective extends AbstractDirective
{
    private final InputDirective[] m_input;
    private final Policy m_policy;
    
   /**
    * Creation of a new comsumption directive.
    * @param input the set of input process names
    * @param policy the consumption policy
    */
    public ConsumptionDirective( InputDirective[] input, Policy policy )
    {
        if( null == input )
        {
            throw new NullPointerException( "input" );
        }
        if( null == policy )
        {
            throw new NullPointerException( "policy" );
        }
        m_input = input;
        m_policy = policy;
    }
    
   /**
    * Get the array of input directives.
    * @return the input directive array
    */
    public InputDirective[] getInputDirectives()
    {
        return m_input;
    }

    
   /**
    * Get the input hanlding policy.
    * @return the policy
    */
    public Policy getPolicy()
    {
        return m_policy;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ConsumptionDirective ) )
        {
            ConsumptionDirective object = (ConsumptionDirective) other;
            if( !m_policy.equals( object.m_policy ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_input, object.m_input );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashValue( m_policy );
        hash ^= hashArray( m_input );
        return hash;
    }
}
