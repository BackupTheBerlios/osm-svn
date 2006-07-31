/*
 * Copyright 2006 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.osm.http;

/**
 * A ServletEntry maintains a mapping between a servlet name and a relative path.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ServletEntry
{
    private final String m_name;
    private final String m_path;
    
   /**
    * Creation of a new servlet name to path entry.
    * @param name the servlet name
    * @param path the context relative path
    */
    public ServletEntry( String name, String path )
    {
        m_name = name;
        m_path = path;
    }
    
   /**
    * Return the servlet name.
    * @return the servlet name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Return the path.
    * @return the servlet path
    */
    public String getPath()
    {
        return m_path;
    }
    
   /**
    * Test is this object is equal to the supplied object.
    * @param other the object to evaluate this object against
    * @return tryue if the objects are equal else false
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof ServletEntry )
        {
            ServletEntry entry = (ServletEntry) other;
            if( !m_name.equals( entry.m_name ) )
            {
                return false;
            }
            else
            {
                return m_path.equals( entry.m_path ); 
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Return the instance hashcode.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = m_name.hashCode();
        hash ^= m_path.hashCode();
        return hash;
    }
}
