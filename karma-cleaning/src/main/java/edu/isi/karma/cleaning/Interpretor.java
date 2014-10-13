package edu.isi.karma.cleaning;

import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class Interpretor {
	private PyObject interpreterClass;

	private static Logger logger = LoggerFactory.getLogger(Interpretor.class);
	public Interpretor() {
		PythonInterpreter interpreter = new PythonInterpreter();
		
		String dirpathString = ServletContextParameterMap.getParameterValue(ContextParameter.WEBAPP_PATH) + 
									"/" + ServletContextParameterMap
									.getParameterValue(ContextParameter.PYTHON_SCRIPTS_DIRECTORY);
		
									;
		if(dirpathString == null || dirpathString.toString().length() <= 1) {
			dirpathString = "../karma-web/src/main/webapp/resources/pythonCleaningscripts";
		} 
		logger.info("Setting Python Scripts Directory for karma-cleaning: " + dirpathString);
		
		interpreter.exec("import sys");
		// /Users/bowu/projects/IDCT/src/edu/isi/karma/cleaning
		interpreter.exec("sys.path.append('" + dirpathString + "')");
		interpreter.exec("from FunctionList import *");
		interpreter.exec("from Interpreter import *");
		// interpreter.exec("print sys.path");
		interpreterClass = interpreter.get("Interpreter");
	}

	/**
	 * The create method is responsible for performing the actual coercion of
	 * the referenced python module into Java bytecode
	 */

	public InterpreterType create(String scripts) {

		PyObject buildingObject = interpreterClass.__call__(new PyString(
				scripts));
		InterpreterType ele = (InterpreterType) buildingObject
				.__tojava__(InterpreterType.class);
		return ele;
	}
}