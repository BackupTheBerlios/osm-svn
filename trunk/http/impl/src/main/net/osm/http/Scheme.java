/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.osm.http;

import net.dpml.lang.Enum;

/**
 * HTTP scheme enumeration.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class Scheme extends Enum
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Weak collection policy.
    */
    public static final Scheme HTTP = new Scheme( "http" );

   /**
    * Soft collection policy.
    */
    public static final Scheme HTTPS = new Scheme( "https" );

   /**
    * Array of static http schemes.
    */
    private static final Scheme[] ENUM_VALUES = 
      new Scheme[]{HTTP, HTTPS};

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static Scheme[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    */
    private Scheme( String label )
    {
        super( label );
    }
   
   /**
    * Parse the supplied name.
    * @param value the value to parse
    * @return the collection policy
    */
    public static Scheme parse( String value )
    {
        if( value.equalsIgnoreCase( "HTTP" ) )
        {
            return HTTP;
        }
        else if( value.equalsIgnoreCase( "HTTPS" ) )
        {
            return HTTPS;
        }
        else
        {
            final String error =
              "Unrecognized scheme [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
}
