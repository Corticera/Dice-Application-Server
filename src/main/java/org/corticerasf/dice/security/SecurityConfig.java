/*
 * Copyright 2016 Corticera Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.corticerasf.dice.security;

import java.security.Security;

import org.apache.log4j.Logger;
import org.corticerasf.dice.Globals;


/**
 * Util class to protect DefaultServerManager against package access and insertion.
 */
public final class SecurityConfig{
    private static SecurityConfig singleton = null;

    private static final Logger log = Logger.getLogger( SecurityConfig.class );


    private static final String PACKAGE_ACCESS =  "sun.,"
                                                + "org.corticerasf.dice.";

    // FIX ME package "javax." was removed to prevent HotSpot
    // fatal internal errors
    private static final String PACKAGE_DEFINITION= "java.,sun."
                                                + ",org.corticerasf.dice.";
    /**
     * List of protected package from conf/corticera.properties
     */
    private String packageDefinition;


    /**
     * List of protected package from conf/corticera.properties
     */
    private String packageAccess;


    /**
     * Create a single instance of this class.
     */
    private SecurityConfig(){
        try{
            packageDefinition = Globals.getProperty("package.definition");
            packageAccess = Globals.getProperty("package.access");
        } catch (java.lang.Exception ex){
            if (log.isDebugEnabled()){
                log.debug("Unable to load properties using CorticeraProperties", ex);
            }
        }
    }


    /**
     * Returns the singleton instance of that class.
     * @return an instance of that class.
     */
    public static SecurityConfig newInstance(){
        if (singleton == null){
            singleton = new SecurityConfig();
        }
        return singleton;
    }


    /**
     * Set the security package.access value.
     */
    public void setPackageAccess(){
        // If corticera.properties is missing, protect all by default.
        if (packageAccess == null){
            setSecurityProperty("package.access", PACKAGE_ACCESS);
        } else {
            setSecurityProperty("package.access", packageAccess);
        }
    }


    /**
     * Set the security package.definition value.
     */
     public void setPackageDefinition(){
        // If corticera.properties is missing, protect all by default.
         if (packageDefinition == null){
            setSecurityProperty("package.definition", PACKAGE_DEFINITION);
         } else {
            setSecurityProperty("package.definition", packageDefinition);
         }
    }


    /**
     * Set the proper security property
     * @param properties the package.* property.
     */
    private final void setSecurityProperty(String properties, String packageList){
        if (System.getSecurityManager() != null){
            String definition = Security.getProperty(properties);
            if( definition != null && definition.length() > 0 ){
                if (packageList.length() > 0) {
                    definition = definition + ',' + packageList;
                }
            } else {
                definition = packageList;
            }

            Security.setProperty(properties, definition);
        }
    }


}


