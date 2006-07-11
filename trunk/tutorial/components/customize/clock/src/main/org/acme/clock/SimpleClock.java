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

package org.acme.clock;

import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * A minimal implementation of a clock.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SimpleClock implements Clock
{
    //------------------------------------------------------------------
    // static
    //------------------------------------------------------------------
    
    private static final String DEFAULT_PATTERN = "K:mm a, z";
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    
    //------------------------------------------------------------------
    // criteria
    //------------------------------------------------------------------
    
   /**
    * Declaration of the context information required by the implementation.
    */
    public interface Context
    {
       /**
        * Return the optional date format as a string.  
        * @param format the default value
        * @return the date format value
        */
        public String getFormat( final String format );
        
       /**
        * Return the operational locale.  
        * @param locale the default value
        * @return the locale
        */
        public Locale getLocale( final Locale locale );
    }
    
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------
    
    private final Context m_context; 
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------
    
   /**
    * Creation of a new instance.
    * @param context the initial configuration
    */
    public SimpleClock( final Context context )
    {
        m_context = context;
    }
    
    //------------------------------------------------------------------
    // Clock
    //------------------------------------------------------------------
    
   /**
    * Return the current time as a formatted string.
    * @return the current time as a string
    */
    public String getTimestamp()
    {
        Date date = new Date();
        DateFormat formatter = getDateFormatter();
        return formatter.format( date );
    }
    
    private DateFormat getDateFormatter()
    {
        String format = m_context.getFormat( DEFAULT_PATTERN );
        Locale locale = m_context.getLocale( DEFAULT_LOCALE );
        return new SimpleDateFormat( format, locale );
    }
}
