package com.siemens.ct.osgi.vaadin.pm.main;

import com.vaadin.Application;

import de.nierbeck.ct.osgi.vaadin.tracker.service.VaadinAppHolder;

public class VaadinAppHolderImpl implements VaadinAppHolder {

	private Application application;

	public Application getApplication() {
		return this.application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}
	
}
