/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *  
 *    This code was developed by the Information Integration Group as part 
 *    of the Karma project at the Information Sciences Institute of the 
 *    University of Southern California.  For more information, publications, 
 *    and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

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
