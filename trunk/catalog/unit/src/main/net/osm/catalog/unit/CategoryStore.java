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

package net.osm.catalog.unit;

import java.io.Serializable;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * BProduct category storage unit.
 * As an example, a 7050 is a Product produced by ALLIS CHALMERS. It 
 * is classed under the category TRACTOR.
 */
@Entity
@Table( name="CATEGORIES" )
public class CategoryStore implements Serializable
{
    private String m_id;
    
    public CategoryStore()
    {
    }
    
    public CategoryStore( String id )
    {
        m_id = id;
    }
    
   /**
    * Return the internal category label.
    * @return the unique label
    */
    @Id
    public String getID()
    {
        return m_id;
    }
    
    public void setID( String id )
    {
        m_id = id;
    }
    
    public boolean equals( Object other )
    {
        if( other instanceof CategoryStore )
        {
            CategoryStore store = (CategoryStore) other;
            return getID().equals( store.getID() );
        }
        else
        {
            return false;
        }
    }
    
    public String toString()
    {
        return m_id;
    }
}
