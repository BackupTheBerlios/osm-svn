/*
 * Copyright 2006-2007 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.osm.http.spi;

import net.dpml.annotation.Context;

/**
* NCSA logging context.
*/
@Context
public interface NCSAContext
{
   /**
    * Get the array of ignore paths.
    * @param value the default value
    * @return the ignore path array
    */
    String[] getIgnorePaths( String[] value );
    
   /**
    * Return the append policy.
    * @param value the default policy value
    * @return the resolved value
    */
    boolean getAppend( boolean value );
    
   /**
    * Return the extended policy.
    * @param value the default policy value
    * @return the resolved value
    */
    boolean getExtended( boolean value );
    
   /**
    * Return the prefer-proxy-for-address policy.
    * @param value the default policy value
    * @return the resolved value
    */
    boolean getPreferProxiedForAddress( boolean value );
    
   /**
    * Return the log filename.
    * @param value the default filename value (may include symbolic
    *   references to system properties)
    * @return the resolved filename
    */
    String getFilename( String value );
    
   /**
    * Return the log date format.
    * @param value the default value
    * @return the resolved value
    */
    String getLogDateFormat( String value );
    
   /**
    * Return the log time zone.
    * @param value the default value
    * @return the resolved value
    */
    String getLogTimeZone( String value );
    
   /**
    * Return the retain days value.
    * @param value the default value
    * @return the resolved value
    */
    int getRetainDays( int value );
    
   /**
    * Get the log latency policy. Ig true the request processing latency will
    * included in the reqwuest log messages.
    * @param flag the log latency default value
    * @return the resulted log latency policy
    */
    boolean getLogLatency( boolean flag );
    
   /**
    * Get the log cookies policy.
    * @param flag the default policy
    * @return the resolved policy
    */
    boolean getLogCookies( boolean flag );
}
