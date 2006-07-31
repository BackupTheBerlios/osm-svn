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

/**
 * The CollectionDirective class describes a collection of include and exclude
 * names.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CollectionDirective extends ProductDirective
{
    private final CollectionCriteria m_criteria;
    private final String[] m_includes;
    private final String[] m_excludes;
    
   /**
    * Creation of a new collection directive.
    * @param name the collection name
    * @param info information about the collection
    * @param criteria collection criteria
    * @param includes the set of include names
    * @param excludes the set of exclude names
    */
    public CollectionDirective( 
      final String name, final InfoDirective info, CollectionCriteria criteria, 
      String[] includes, String[] excludes )
    {
        super( name, info );
        
        if( null == criteria )
        {
            throw new NullPointerException( "criteria" );
        }
        if( null == includes )
        {
            throw new NullPointerException( "includes" );
        }
        if( null == excludes )
        {
            throw new NullPointerException( "excludes" );
        }
        
        m_criteria = criteria;
        m_includes = includes;
        m_excludes = excludes;
    }
    
   /**
    * Get the collection criteria.
    * @return the criteria value
    */
    public CollectionCriteria getCollectionCriteria()
    {
        return m_criteria;
    }

   /**
    * Get the selection includes.
    * @return the include selectors
    */
    public String[] getIncludes()
    {
        return m_includes;
    }

   /**
    * Get the selection excludes.
    * @return the exclude selectors
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
        if( super.equals( other ) && ( other instanceof CollectionDirective ) )
        {
            CollectionDirective object = (CollectionDirective) other;
            if( !Arrays.equals( m_includes, object.m_includes ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_excludes, object.m_excludes ) )
            {
                return false;
            }
            else
            {
                return m_criteria.equals( object.m_criteria );
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
        hash ^= hashValue( m_criteria );
        return hash;
    }
}
