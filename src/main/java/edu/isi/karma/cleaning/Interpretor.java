package edu.isi.karma.cleaning;

import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;
public class Interpretor {
	private PyObject interpreterClass;

    public Interpretor() {
        PythonInterpreter interpreter = new PythonInterpreter();
        //change the sys.path
        String dirpathString = ServletContextParameterMap.getParameterValue(ContextParameter.PYTHON_SCRIPTS_DIRECTORY);
        interpreter.exec("import sys");
        interpreter.exec("sys.path.append('"+dirpathString+"')");
        ///Users/bowu/projects/IDCT/src/edu/isi/karma/cleaning
        interpreter.exec("sys.path.append('"+dirpathString+"')");
        interpreter.exec("from FunctionList import *");
        interpreter.exec("from Interpreter import *");
        //interpreter.exec("print sys.path");
        interpreterClass = interpreter.get("Interpreter");
    }

    /**
     * The create method is responsible for performing the actual
     * coercion of the referenced python module into Java bytecode
     */

    public InterpreterType create(String scripts) {

        PyObject buildingObject = interpreterClass.__call__(new PyString(scripts));
        InterpreterType ele =  (InterpreterType)buildingObject.__tojava__(InterpreterType.class);
        return ele;
    }
    public static void main(String[] args)
    {
    		Interpretor it = new Interpretor();
    		String scripts="substr(value,indexOf(value,'START','WRD'),indexOf(value,'WRD','SYB'))+'*'+substr(value,indexOf(value,'SYB','NUM'),indexOf(value,'NUM','END'))";
    		String value = "Dositejeva&nbsp;22";
    		InterpreterType worker = it.create(scripts);
    	
    }
}
