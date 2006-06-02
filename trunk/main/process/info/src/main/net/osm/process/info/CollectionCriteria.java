/*
 * Copyright 2006 Stephen J. McConnell.
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

import net.dpml.lang.Enum;

/**
 * Product selection criteria enumeration.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class CollectionCriteria extends Enum
{
    static final long serialVersionUID = 1L;

   /**
    * Any product.
    */
    public static final CollectionCriteria ANY = new CollectionCriteria( "any" );

   /**
    * Any deliverable file product.
    */
    public static final CollectionCriteria FILE = new CollectionCriteria( "file" );

   /**
    * Any interim directory product.
    */
    public static final CollectionCriteria DIR = new CollectionCriteria( "dir" );

   /**
    * Array of scope enumeration values.
    */
    private static final CollectionCriteria[] ENUM_VALUES = 
      new CollectionCriteria[]
      {
        ANY, 
        FILE,
        DIR 
      };

   /**
    * Returns an array of enum values.
    * @return the enumeration array
    */
    public static CollectionCriteria[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    */
    private CollectionCriteria( String label )
    {
        super( label );
    }
    
   /**
    * Return a string representation of the category.
    * @return the category name in uppercase
    */
    public String toString()
    {
        return getName().toUpperCase();
    }
    
   /**
    * Create a category by parsing the supplied name.
    * @param value the category name
    * @return the corresponding category
    * @exception IllegalArgumentException if the value is not recognized
    */
    public static CollectionCriteria parse( String value ) throws IllegalArgumentException
    {
        if( null == value )
        {
            return ANY;
        }
        else
        {
            if( value.equalsIgnoreCase( "file" ) )
            {
                return FILE;
            }
            else if( value.equalsIgnoreCase( "dir" ) )
            {
                return DIR;
            }
            else
            {
                final String error =
                  "Unrecognized selection criteria argument [" + value + "]";
                throw new IllegalArgumentException( error );
            }
        }
    }
}

