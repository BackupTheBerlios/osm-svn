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
 * The DirectoryProductDirective class describes a working directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DirectoryDirective extends ProductDirective
{
    private final String m_path;
    
   /**
    * Creation of a new directory directive.
    * @param name the directory name
    * @param info infomation about the directory
    * @param path the directory path
    */
    public DirectoryDirective( final String name, final InfoDirective info, String path )
    {
        super( name, info );
        
        if( null == path )
        {
            throw new NullPointerException( "path" );
        }
        
        m_path = path;
    }
    
   /**
    * Get the directory path.
    * @return the path
    */
    public String getPath()
    {
        return m_path;
    }

   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof DirectoryDirective ) )
        {
            DirectoryDirective object = (DirectoryDirective) other;
            return m_path.equals( object.m_path );
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
        hash ^= hashValue( m_path );
        return hash;
    }
}
