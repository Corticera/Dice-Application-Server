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

import org.corticerasf.dice.connector.CorticeraRequest;
import org.corticerasf.dice.connector.CorticeraResponse;

public interface Valve {

	public String getInfo();

	public Valve getNext();

	public void setNext(Valve valve);
	
	public void backgroundProcess();

	public void invoke(CorticeraRequest request, CorticeraResponse response)
			throws IOException, ServletException;

	public void event(CorticeraRequest request, CorticeraResponse response,
			PipelineEvent event) throws IOException, ServletException;

}
