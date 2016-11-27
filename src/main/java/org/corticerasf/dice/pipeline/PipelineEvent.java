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
package org.corticerasf.dice.pipeline;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PipelineEvent {

	public enum EventType {
		BEGIN, READ, END, ERROR
	}

	public enum EventSubType {
		TIMEOUT, CLIENT_DISCONNECT, IOEXCEPTION, WEBAPP_RELOAD, SERVER_SHUTDOWN, SESSION_END
	}

	public HttpServletRequest getHttpServletRequest();

	public HttpServletResponse getHttpServletResponse();

	public EventType getEventType();

	public EventSubType getEventSubType();

	public void close() throws IOException;

	public void setTimeout(int timeout) throws IOException, ServletException,
			UnsupportedOperationException;

}
