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
 * The InfoDirective describes a title and general information about an entiry.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class InfoDirective extends AbstractDirective
{
    private final String m_title;
    private final String m_description;
    
   /**
    * Creation of a new info directive.
    * @param title the title
    * @param description the description
    */
    public InfoDirective( final String title, final String description )
    {
        m_title = title;
        m_description = description;
    }
    
   /**
    * Get the title.
    * @return the name.
    */
    public String getTitle()
    {
        return m_title;
    }

   /**
    * Get the description.
    * @return the description.
    */
    public String getDescription()
    {
        return m_description;
    }

   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof InfoDirective ) )
        {
            InfoDirective object = (InfoDirective) other;
            if( !equals( m_title, object.m_title ) )
            {
                return false;
            }
            else
            {
                return equals( m_description, object.m_description );
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
        hash ^= hashValue( m_title );
        hash ^= hashValue( m_description );
        return hash;
    }
}
