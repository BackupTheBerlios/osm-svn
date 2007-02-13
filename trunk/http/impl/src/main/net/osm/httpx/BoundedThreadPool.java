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

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import org.mortbay.thread.ThreadPool;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

/**
 * Thread pool implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="pool", lifestyle=SINGLETON )
@Services( ThreadPool.class )
public class BoundedThreadPool extends org.mortbay.thread.BoundedThreadPool
{
   /**
    * Creation of a new blocking thread pool.
    * @param context the component context
    * @exception Exception if an instantiation error occurs
    */
    public BoundedThreadPool( PoolConfiguration context ) throws Exception
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
