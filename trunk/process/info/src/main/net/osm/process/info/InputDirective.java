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
 * The InputDirective describes the consumption of a product by a process.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class InputDirective extends AbstractDirective
{
    private final Policy m_policy;
    private final String m_id;
    private final String[] m_includes;
    private final String[] m_excludes;
    private final boolean m_filtering;
    
   /**
    * Creation of a new input directive.
    * @param id the input id
    * @param policy the input policy
    */
    public InputDirective( 
      final String id, final Policy policy )
    {
        this( id, policy, false, new String[0], new String[0] );
    }
    
   /**
    * Creation of a new input directive.
    * @param id the input id
    * @param policy the input policy
    * @param filtering the filterining flag 
    * @param includes the set of include names
    * @param excludes the set of exclude names
    */
    public InputDirective( 
      final String id, final Policy policy, boolean filtering, 
      final String[] includes, final String[] excludes )
    {
        if( null == id )
        {
            throw new NullPointerException( "id" );
        }
        if( null == includes )
        {
            throw new NullPointerException( "includes" );
        }
        if( null == excludes )
        {
            throw new NullPointerException( "excludes" );
        }
        
        m_id = id;
        if( null == policy )
        {
            m_policy = Policy.OPTIONAL;
        }
        else
        {
            m_policy = policy;
        }
        m_filtering = filtering;
        m_includes = includes;
        m_excludes = excludes;
    }
    
   /**
    * Get the input id.
    * @return the id
    */
    public String getID()
    {
        return m_id;
    }
    
   /**
    * Get the policy associated within the input.
    * @return the input policy
    */
    public Policy getPolicy()
    {
        return m_policy;
    }
    
   /**
    * Get the filtering policy.
    * @return the filtering policy
    */
    public boolean isFiltering()
    {
        return m_filtering;
    }
    
   /**
    * Get the array of includes.
    * @return the include array
    */
    public String[] getIncludes()
    {
        return m_includes;
    }

   /**
    * Get the array of excludes.
    * @return the exclude array
    */
    public String[] getExcludes()
    {
        return m_excludes;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof InputDirective ) )
        {
            InputDirective object = (InputDirective) other;
            if( !m_id.equals( object.m_id ) )
            {
                return false;
            }
            else if( !m_policy.equals( object.m_policy ) )
            {
                return false;
            }
            else if( m_filtering != object.m_filtering )
            {
                return false;
            }
            else if( !equals( m_includes, object.m_includes ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_excludes, object.m_excludes );
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
        hash ^= hashValue( m_id );
        if( m_filtering )
        {
            hash ^= 27;
        }
        hash ^= hashValue( m_policy );
        hash ^= hashArray( m_includes );
        hash ^= hashArray( m_excludes );
        return hash;
    }
}
