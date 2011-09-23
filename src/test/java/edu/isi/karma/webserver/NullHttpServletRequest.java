package edu.isi.karma.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Copied from http://www.jguru.com/faq/view.jsp?EID=110660
 * 
 * @author szekely
 *
 */
public class NullHttpServletRequest implements HttpServletRequest {
	@SuppressWarnings("rawtypes")
	public Hashtable parameters = new Hashtable();

	@SuppressWarnings("unchecked")
	public void setParameter(String key, String value) {
		parameters.put(key, value);
	}

	public String getParameter(String key) {
		return (String) this.parameters.get(key);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getParameterNames() {
		return this.parameters.elements();
	}

	public Cookie[] getCookies() {
		return null;
	}

	public String getMethod() {
		return null;
	}

	public String getRequestURI() {
		return null;
	}

	public String getServletPath() {
		return null;
	}

	public String getPathInfo() {
		return null;
	}

	public String getPathTranslated() {
		return null;
	}

	public String getQueryString() {
		return null;
	}

	public String getRemoteUser() {
		return null;
	}

	public String getAuthType() {
		return null;
	}

	public String getHeader(String name) {
		return null;
	}

	public int getIntHeader(String name) {
		return 0;
	}

	public long getDateHeader(String name) {
		return 0;
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getHeaderNames() {
		return null;
	}

	public HttpSession getSession(boolean create) {
		return null;
	}

	public String getRequestedSessionId() {
		return null;
	}

	public boolean isRequestedSessionIdValid() {
		return false;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	public int getContentLength() {
		return 0;
	}

	public String getContentType() {
		return null;
	}

	public String getProtocol() {
		return null;
	}

	public String getScheme() {
		return null;
	}

	public String getServerName() {
		return null;
	}

	public int getServerPort() {
		return 0;
	}

	public String getRemoteAddr() {
		return null;
	}

	public String getRemoteHost() {
		return null;
	}

	public String getRealPath(String path) {
		return null;
	}

	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	public String[] getParameterValues(String name) {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		return null;
	}

	public Object getAttribute(String name) {
		return null;
	}

	public HttpSession getSession() {
		return null;
	}

	public BufferedReader getReader() throws IOException {
		return null;
	}

	public String getCharacterEncoding() {
		return null;
	}

	public void setAttribute(String name, Object o) {
	}

	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getParameterMap() {
		return null;
	}

	@Override
	public void removeAttribute(String name) {
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getLocales() {
		return null;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getHeaders(String name) {
		return null;
	}

	@Override
	public String getContextPath() {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		return null;
	}
}