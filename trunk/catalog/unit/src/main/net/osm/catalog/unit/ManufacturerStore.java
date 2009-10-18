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
import javax.persistence.GeneratedValue;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table( name="MANUFACTURERS" )
public class ManufacturerStore implements Serializable
{
    private String m_id;
    private String m_label;
    private String m_name;
    private Collection<ProductStore> m_products = new ArrayList<ProductStore>();
    
   /**
    * Return the identity for the manufacturer record.
    */
    @Id
    @GeneratedValue
    public String getID()
    {
        return m_id;
    }
    
   /**
    * Return the short name for the manufacturer.
    * @return the short name
    */
    public String getLabel()
    {
        return m_label;
    }
    
   /**
    * Return the full name of the manufacturer.
    * @return the full name
    */
    public String getName()
    {
        return m_name;
    }
    
    @OneToMany( cascade=ALL, mappedBy="manufacturer" )
    public Collection<ProductStore> getProducts()
    {
        return m_products;
    }

   /**
    * Set the manufacturer identifier key.
    * @param id the identifier key
    */
    public void setID( String id )
    {
        m_id = id;
    }
    
   /**
    * Set the full name of the manufacturer.
    * @param name the full name
    */
    public void setName( String name )
    {
        m_name = name;
    }
    
   /**
    * Set the label for the manufacturer.
    * @param label the  label
    */
    void setLabel( String label )
    {
        m_label = label;
    }
    
    public void setProducts( Collection<ProductStore> products ) 
    {
        m_products = products;
    }
    
    public ProductStore newProduct( CategoryStore category, String name )
    {
        ProductStore product = new ProductStore();
        product.setManufacturer( this );
        product.setCategory( category );
        product.setName( name );
        m_products.add( product );
        return product;
    }
    
    public String toString()
    {
        if( null != m_name )
        {
            return m_name;
        }
        else
        {
            return m_label;
        }
    }
}
