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
package org.corticerasf.dice.servletimpl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

public class ApplicationServletContext implements ServletContext {

	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public ServletContext getContext(String uripath) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getEffectiveMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getEffectiveMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getMimeType(String file) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getResourcePaths(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public URL getResource(String path) throws MalformedURLException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getResourceAsStream(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public RequestDispatcher getNamedDispatcher(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Servlet getServlet(String name) throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration<Servlet> getServlets() {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration<String> getServletNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public void log(String msg) {
		// TODO Auto-generated method stub
		
	}

	public void log(Exception exception, String msg) {
		// TODO Auto-generated method stub
		
	}

	public void log(String message, Throwable throwable) {
		// TODO Auto-generated method stub
		
	}

	public String getRealPath(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getInitParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration<String> getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean setInitParameter(String name, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAttribute(String name, Object object) {
		// TODO Auto-generated method stub
		
	}

	public void removeAttribute(String name) {
		// TODO Auto-generated method stub
		
	}

	public String getServletContextName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Dynamic addServlet(String servletName, String className) {
		// TODO Auto-generated method stub
		return null;
	}

	public Dynamic addServlet(String servletName, Servlet servlet) {
		// TODO Auto-generated method stub
		return null;
	}

	public Dynamic addServlet(String servletName,
			Class<? extends Servlet> servletClass) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Servlet> T createServlet(Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	public ServletRegistration getServletRegistration(String servletName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}

	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, String className) {
		// TODO Auto-generated method stub
		return null;
	}

	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Class<? extends Filter> filterClass) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Filter> T createFilter(Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	public FilterRegistration getFilterRegistration(String filterName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}

	public SessionCookieConfig getSessionCookieConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSessionTrackingModes(
			Set<SessionTrackingMode> sessionTrackingModes) {
		// TODO Auto-generated method stub
		
	}

	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addListener(String className) {
		// TODO Auto-generated method stub
		
	}

	public <T extends EventListener> void addListener(T t) {
		// TODO Auto-generated method stub
		
	}

	public void addListener(Class<? extends EventListener> listenerClass) {
		// TODO Auto-generated method stub
		
	}

	public <T extends EventListener> T createListener(Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	public JspConfigDescriptor getJspConfigDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	public void declareRoles(String... roleNames) {
		// TODO Auto-generated method stub
		
	}

}
