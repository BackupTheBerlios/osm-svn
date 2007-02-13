/*
 * Copyright 2006 Stephen McConnell.
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
package net.osm.http;

/**
 * Common connector context contract.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ConnectorContext
{
   /**
    * Return the connector host name. 
    * @param host implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getHost( String host );
    
   /**
    * Return the connector port. 
    * @return the assigned connector port
    */
    int getPort( int port );
    
   /**
    * Return the connector header buffer size.
    * @param size implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    int getHeaderBufferSize( int size );
    
   /**
    * Return the maximum idle time in milliseconds.
    * @param time implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    int getMaxIdleTime( int time );
    
   /**
    * Return the request buffer size.
    * @param size implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    int getRequestBufferSize( int size );
    
   /**
    * Return the response buffer size.
    * @param size implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    int getResponseBufferSize( int size );
    
   /**
    * Return the accept queue size.
    * @param size implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    int getAcceptQueueSize( int size );
    
   /**
    * Return the number of initial acceptors.
    * @param size implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    int getAcceptors( int size );
    
   /**
    * Return the soLingerTime parameter value.
    * @param time implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    int getSoLingerTime( int time );
    
   /**
    * Return the confidential port.
    * @param port implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    int getConfidentialPort( int port );
    
   /**
    * Return the confidential scheme (http or https).
    * @param scheme implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getConfidentialScheme( String scheme );
    
   /**
    * Return the integral port.
    * @param port implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    int getIntegralPort( int port );
    
   /**
    * Return the integral scheme (http or https).
    * @param scheme implementation defined default value
    * @return the supplied value unless overriden in the deployment configuration
    */
    String getIntegralScheme( String scheme );
    
}
