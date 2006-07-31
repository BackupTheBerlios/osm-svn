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
 * Thread pool implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class BoundedThreadPool extends org.mortbay.thread.BoundedThreadPool
{
   /**
    * HTTP Context handler context defintion.
    */
    public interface Context
    {
       /**
        * Get the minimum thread level.
        *
        * @param min the default minimum value
        * @return the minimum thread level
        */
        int getMin( int min );
        
       /**
        * Return maximum thread level.
        *
        * @param max the default maximum value
        * @return the maximum thread level
        */
        int getMax( int max );
        
       /**
        * Return the deamon flag.
        *
        * @param flag true if a damon thread 
        * @return the deamon thread policy
        */
        boolean getDaemon( boolean flag );
        
       /**
        * Get the thread pool name.
        *
        * @param name the pool name
        * @return the name
        */
        String getName( String name );
        
       /**
        * Get the thread pool priority.
        *
        * @param priority the thread pool priority
        * @return the priority
        */
        int getPriority( int priority );
        
       /**
        * Get the maximum idle time.
        *
        * @param idle the default maximum idle time
        * @return the maximum idle time in milliseconds
        */
        int getIdle( int idle );
        
    }

   /**
    * Creation of a new blocking thread pool.
    * @param context the component context
    * @exception Exception if an instantiation error occurs
    */
    public BoundedThreadPool( Context context ) throws Exception
    {
        super();
        
        int min = context.getMin( 1 );
        int max = context.getMax( 255 );
        boolean daemon = context.getDaemon( true );
        String name = context.getName( "pool" );
        int priority = context.getPriority( Thread.NORM_PRIORITY );
        int idle = context.getIdle( 10000 );
        
        setMinThreads( min );
        setMaxThreads( max );
        setDaemon( daemon );
        setThreadsPriority( priority );
        setName( name );
        setMaxIdleTimeMs( idle );
    }
}
