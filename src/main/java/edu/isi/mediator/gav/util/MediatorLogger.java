// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.util;

import java.io.IOException;

import org.apache.log4j.Logger;

public class MediatorLogger{

	private Logger logger;

    /**
     * Private constructor. To create a new logger use the static
     * factory method.
     * 
     * @param theClass 
     *     The class being logged.
     * @throws IOException 
     * @throws SecurityException 
     */
    private MediatorLogger(String theClass)
    {
    	//System.out.println("CREATE logger...");
    	logger = Logger.getLogger(theClass);   
    }

    public static MediatorLogger getLogger(String theClass){
    	return new MediatorLogger(theClass);
    }

    public void info(String msg){
    	logger.info(msg);
    }
    public void fatal(String msg){
    	logger.fatal(msg);
    }
    public void debug(String msg){
    	logger.debug(msg);
    }
    }
