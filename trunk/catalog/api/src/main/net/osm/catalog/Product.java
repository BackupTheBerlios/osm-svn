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

package net.osm.catalog;

/**
 * Business interface to a product. 
 * As an example, a 7050 is a Product produced by ALLIS CHALMERS. It 
 * is catagories as a TRACTOR, and serves as a reference to available 
 * parts.
 */
public interface Product
{
   /**
    * Return the internal unique itentifier for the product.
    * @return the unique identifier
    */
    int getID();

    String getName();
    
    void setName( String name );
    
    String getDescription();
    
    void setDescription( String description );
    
    Manufacturer getManufacturer();
    
    Iterable<Part> getParts();
    
    Category getCategory();
}
