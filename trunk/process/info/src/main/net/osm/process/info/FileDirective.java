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

/**
 * The FileDirective class describes a deliverable produced by a process.
 * A file is normally associated with a colocated MD5 and ASC resource.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class FileDirective extends ProductDirective
{
    private final String m_type;
    private final String m_base;
    
   /**
    * Creation of a new file directive. 
    * @param name the product identifier
    * @param info supplimentary product info
    * @param type the artifact type
    * @param base id of the partent directory
    */
    public FileDirective( final String name, final InfoDirective info, String type, String base )
    {
        super( name, info );
        
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        
        m_type = type;
        m_base = base;
    }
    
   /**
    * Get the product type name.
    * @return the product type.
    */
    public String getType()
    {
        return m_type;
    }
    
   /**
    * Get the product base directory id.
    * @return the product base directory identifier.
    */
    public String getBaseID()
    {
        return m_base;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof FileDirective ) )
        {
            FileDirective object = (FileDirective) other;
            return m_type.equals( object.m_type );
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
        hash ^= hashValue( m_type );
        return hash;
    }
}
