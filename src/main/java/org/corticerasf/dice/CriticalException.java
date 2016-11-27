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
package org.corticerasf.dice;

public class CriticalException extends Exception {

    private static final long serialVersionUID = 1L;

    //------------------------------------------------------------ Constructors


    /**
     * Construct a new CriticalException with no other information.
     */
    public CriticalException() {
        super();
    }


    /**
     * Construct a new CriticalException for the specified message.
     *
     * @param message Message describing this exception
     */
    public CriticalException(String message) {
        super(message);
    }


    /**
     * Construct a new CriticalException for the specified throwable.
     *
     * @param throwable Throwable that caused this exception
     */
    public CriticalException(Throwable throwable) {
        super(throwable);
    }


    /**
     * Construct a new CriticalException for the specified message
     * and throwable.
     *
     * @param message Message describing this exception
     * @param throwable Throwable that caused this exception
     */
    public CriticalException(String message, Throwable throwable) {
        super(message, throwable);
    }
}