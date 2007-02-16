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

import java.net.URI;

import net.dpml.annotation.Context;

/**
 * Realm context.
 */
@Context
public interface RealmContext
{
   /**
    * Get the user realm name.
    *
    * @return the realm name
    */
    String getName();
    
   /**
    * Return a uri of the real configuration properties file.
    *
    * @return the realm configuration uri
    */
    URI getURI();
}
