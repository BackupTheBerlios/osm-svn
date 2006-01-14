/*
 * Copyright 2005 Stephen J. McConnell, OSM
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

package org.acme.tutorial;

/**
 * An example of a service interface.
 *
 * @author <a href="http://www.osm.net">Open Service Management</a>
 */
public interface Widget
{
   /**
    * Log a color related message.
    *
    * @param color the color to use
    */
    void process( String color );
}