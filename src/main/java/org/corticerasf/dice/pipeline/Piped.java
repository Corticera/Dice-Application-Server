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

/**
 * Decoupling interface which specifies that an implementing class is associated
 * with at most one Pipeline instance.
 * 
 * @author J. Godara
 */
public interface Piped {

	/**
	 * Set the Pipeline to which this instance is associated.
	 * 
	 * @param pipeline
	 *            The Pipeline which this instance is to be associated, or null
	 *            to disassociate this instance from any Pipeline.
	 */
	public void setPipleline(Pipeline pipeline);

	/**
	 * @return The Pipeline with which this instance is associated (if any);
	 *         otherwise return null.
	 */
	public Pipeline getPipeline();

}
