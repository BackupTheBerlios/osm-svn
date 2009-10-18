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
package net.osm.http.spi;

import net.dpml.annotation.Context;

/**
 * HTTP server context.
  <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Context
public interface ServerContext
{
    ThreadContext getThreads( ThreadContext context );
    
    HttpConnectionContext getHttp( HttpConnectionContext context );
    
    HttpsConnectionContext getHttps( HttpsConnectionContext context );
    
    NCSAContext getLog( NCSAContext context );
    
   /**
    * Returns the close connection on stop policy.
    * @param wait the application supplied default shoutdown wait period
    * @return the resolved gracefull shutdown wait period
    */ 
    int getGracefulShutdown( int wait );
}
