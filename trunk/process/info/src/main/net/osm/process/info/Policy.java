/*
 * Copyright 2005 Stephen J. McConnell.
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
 * Product consumption policy enumeration.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Policy extends Enum
{
    static final long serialVersionUID = 1L;

   /**
    * Optional policy.
    */
    public static final Policy OPTIONAL = new Policy( "optional" );

   /**
    * Optional policy.
    */
    public static final Policy CONDITIONAL = new Policy( "conditional" );

   /**
    * Array of scope enumeration values.
    */
    private static final Policy[] ENUM_VALUES = 
      new Policy[]
      {
        OPTIONAL, 
        CONDITIONAL 
      };

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static Policy[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    */
    private Policy( String label )
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
    public static Policy parse( String value ) throws IllegalArgumentException
    {
        if( null == value )
        {
            return OPTIONAL;
        }
        else
        {
            if( value.equalsIgnoreCase( "optional" ) )
            {
                return OPTIONAL;
            }
            else if( value.equalsIgnoreCase( "conditional" ) )
            {
                return CONDITIONAL;
            }
            else
            {
                final String error =
                  "Unrecognized policy argument [" + value + "]";
                throw new IllegalArgumentException( error );
            }
        }
    }
}

