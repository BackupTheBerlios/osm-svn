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

import net.dpml.lang.AbstractDirective;

/**
 * The ProductDirective class describes a product instance such as a file or directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class ProductDirective extends AbstractDirective
{
    private final String m_name;
    private final InfoDirective m_info;
    
   /**
    * Creation of a new product directive.
    * @param name the process name
    * @param info product info
    */
    public ProductDirective( final String name, final InfoDirective info )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == info )
        {
            throw new NullPointerException( "info" );
        }
        m_name = name;
        m_info = info;
    }
    
   /**
    * Get the product name.
    * @return the product name.
    */
    public String getName()
    {
        return m_name;
    }

    
   /**
    * Get the product title.
    * @return the product title.
    */
    public String getTitle()
    {
        return m_info.getTitle();
    }
   /**
    * Get the product description.
    * @return the product description.
    */
    public String getDescription()
    {
        return m_info.getDescription();
    }

   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ProductDirective ) )
        {
            ProductDirective object = (ProductDirective) other;
            if( !m_name.equals( object.m_name ) )
            {
                return false;
            }
            else
            {
                return equals( m_info, object.m_info );
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
        hash ^= hashValue( m_name );
        hash ^= hashValue( m_info );
        return hash;
    }
}
