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
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.GeneratedValue;

import static javax.persistence.CascadeType.ALL;

/**
 * Product storage unit.
 * As an example, a 7050 is a Product produced by the manufacturer 
 * ALLIS CHALMERS and serves as a reference to available parts.
 */
@Entity
@Table( name="PRODUCTS" )
public class ProductStore implements Serializable
{
    private int m_id;
    private String m_name;
    private String m_description;
    private ManufacturerStore m_manufacturer;
    private Collection<PartStore> m_parts = new ArrayList<PartStore>();
    private CategoryStore m_category;
    
   /**
    * Return the internal unique itentifier for the product.
    * @return the unique identifier
    */
    @Id
    @GeneratedValue
    public int getID()
    {
        return m_id;
    }

    public String getName()
    {
        return m_name;
    }
    
    public String getDescription()
    {
        return m_description;
    }
    
    @ManyToOne()
    @JoinColumn
    public ManufacturerStore getManufacturer() 
    {
        return m_manufacturer;
    }

    @OneToMany( cascade=ALL, mappedBy="product" )
    public Collection<PartStore> getParts()
    {
        return m_parts;
    }

    public CategoryStore getCategory()
    {
        return m_category;
    }

    public void setID( int id )
    {
        m_id = id;
    }
    
    void setName( String name )
    {
        m_name = name;
    }
    
    void setDescription( String description )
    {
        m_description = description;
    }
    
    public void setManufacturer( ManufacturerStore manufacturer ) 
    {
        m_manufacturer = manufacturer;
    }
    
    public void setParts( Collection<PartStore> parts ) 
    {
        m_parts = parts;
    }

    public void newPart( String id, String description ) 
    {
        newPart( id, description, null );
    }
    
    public void newPart( String id, String description, String comments ) 
    {
        PartStore part = new PartStore();
        part.setID( id );
        part.setProduct( this );
        part.setDescription( description );
        part.setComments( comments );
        m_parts.add( part );
    }

    public void setCategory( CategoryStore category ) 
    {
        m_category = category;
    }

    public String toString()
    {
        return m_name;
    }
}
