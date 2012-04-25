package de.nierbeck.ct.osgi.vaadin.tracker.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.ops4j.pax.web.extender.whiteboard.ResourceMapping;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultResourceMapping;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.vaadin.Application;

import de.nierbeck.ct.osgi.vaadin.tracker.service.VaadinAppHolder;

public class VaadinApplicationTracker {
	
	private static final String _VAADIN = "/VAADIN";

	private BundleContext bundleContext;
	
	private final Map<Application, ServiceRegistration> registeredServlets = Collections
	.synchronizedMap(new HashMap<Application, ServiceRegistration>());

	private Bundle vaadin;
	
	public void init() {
		for (Bundle bundle : bundleContext.getBundles()) {
			if ("com.vaadin".equals(bundle.getSymbolicName())) {
				vaadin = bundle;
				break;
			}
		}
		
		Dictionary<String, String> props;

        props = new Hashtable<String, String>();
        props.put("alias", _VAADIN);
		
        HttpServlet vaadinResourceServlet = new HttpServlet() {
        	
        	@Override
        	protected void doGet(HttpServletRequest req,
        			HttpServletResponse resp) throws ServletException,
        			IOException {
        		String path = req.getPathInfo();
        		String resourcePath = _VAADIN + path;

        		URL u = vaadin.getResource(resourcePath);
        		if (null == u) {
        			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        			return;
        		}

        		InputStream in = u.openStream();
        		OutputStream out = resp.getOutputStream();

        		byte[] buffer = new byte[1024];
        		int read = 0;
        		while (-1 != (read = in.read(buffer))) {
        			out.write(buffer, 0, read);
        		}
        	}
        	
        };
        
		bundleContext.registerService( Servlet.class.getName(), vaadinResourceServlet, props );
	}
	
	public void onBind(VaadinAppHolder holder, Map properties) {
		Servlet servlet = new VaadinApplicationServlet(holder.getApplication());
		
		Dictionary dict = new Hashtable(properties);
		
		ServiceRegistration serviceRegistration = bundleContext.registerService( Servlet.class.getName(), servlet, dict); 
		
		registeredServlets.put(holder.getApplication(), serviceRegistration);
	}
	
	public void unregisterApp(VaadinAppHolder holder) {
		ServiceRegistration serviceRegistration = registeredServlets.get(holder.getApplication());
		serviceRegistration.unregister();
		registeredServlets.remove(holder.getApplication());
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

}
