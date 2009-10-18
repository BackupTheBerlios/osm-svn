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

public interface Manufacturer
{
   /**
    * Return the identity for the manufacturer record.
    */
    String getID();
    
   /**
    * Return the short name for the manufacturer.
    * @return the short name
    */
    String getLabel();
    
   /**
    * Return the full name of the manufacturer.
    * @return the full name
    */
    String getName( String name );
    
   /**
    * Set the manufacturer identifier key.
    * @param key the key
    */
    void setID( String key );
    
   /**
    * Set the full name of the manufacturer.
    * @param name the full name
    */
    void setName( String name );
    
   /**
    * Set the label for the manufacturer.
    * @param label the  label
    */
    void setLabel( String label );
}
