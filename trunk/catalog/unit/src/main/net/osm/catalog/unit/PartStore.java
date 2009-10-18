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
import java.util.Collection;
import java.util.ArrayList;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;

/**
 * Part storage unit.
 */
@Entity
@Table( name="PARTS" )
public class PartStore implements Serializable
{
    private String m_id;
    private ProductStore m_product;
    private String m_description;
    private String m_comments;
    
   /**
    * Return the unique itentifier for this part.
    * @return the unique identifier
    */
    @Id
    public String getID()
    {
        return m_id;
    }

    public ProductStore getProduct()
    {
        return m_product;
    }
     
    public String getDescription()
    {
        return m_description;
    }
    
    public String getComments()
    {
        return m_comments;
    }
    
    public void setID( String id )
    {
        m_id = id;
    }
    
    public void setDescription( String description )
    {
        m_description = description;
    }
    
    public void setComments( String comments )
    {
        m_comments = comments;
    }
    
    public void setProduct( ProductStore product )
    {
        m_product = product;
    }
    
    public String toString()
    {
        return m_description;
    }
}
