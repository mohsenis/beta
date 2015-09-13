package com.webapp.listeners;

import javax.servlet.ServletContextEvent;

import com.webapp.uploads.FileUpload;

public class Listener implements javax.servlet.ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		FileUpload.schedulePlayground();
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}
}
