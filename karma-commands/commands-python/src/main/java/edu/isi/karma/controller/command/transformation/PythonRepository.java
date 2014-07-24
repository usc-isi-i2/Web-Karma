package edu.isi.karma.controller.command.transformation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.python.core.PyCode;
import org.python.util.PythonInterpreter;

import edu.isi.karma.transformation.PythonTransformationHelper;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PythonRepository {

	private ConcurrentHashMap<String, PyCode> scripts;
	private ConcurrentHashMap<String, PyCode> libraryScripts;
	private ConcurrentHashMap<String, Long> fileNameTolastTimeRead;
	private static PythonRepository instance = new PythonRepository();
	
	private PythonRepository()
	{
		scripts = new ConcurrentHashMap<String, PyCode>();
		libraryScripts = new ConcurrentHashMap<String, PyCode>();
		fileNameTolastTimeRead = new ConcurrentHashMap<String,Long>();
		initialize();
	}
	public static PythonRepository getInstance()
	{
		return instance;
	}
	
	private void initialize()
	{
		PythonInterpreter interpreter = new PythonInterpreter();
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getImportStatements());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getGetValueDefStatement());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getVDefStatement());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getTransformStatement());
	}
	
	public PyCode compileAndAddToRepositoryAndExec(PythonInterpreter interpreter, String statement)
	{
		PyCode py = compileAndAddToRepository(interpreter,statement);
		interpreter.exec(py);
		return py;
	}
	public PyCode compileAndAddToRepository(PythonInterpreter interpreter,
			String statement) {
		PyCode py  = null;
		if(!scripts.containsKey(statement))
		{
			py = interpreter.compile(statement);
			scripts.putIfAbsent(statement, py);
		}
		return scripts.get(statement);
	}
	public void initializeInterperter(PythonInterpreter interpreter)
	{
		interpreter.exec(scripts.get(PythonTransformationHelper.getImportStatements()));
		interpreter.exec(scripts.get(PythonTransformationHelper.getGetValueDefStatement()));
		interpreter.exec(scripts.get(PythonTransformationHelper.getVDefStatement()));
		
	}
	
	public PyCode getTransformCode()
	{
		return scripts.get(PythonTransformationHelper.getTransformStatement());
	}

	public void importUserScripts(PythonInterpreter interpreter) throws IOException {
		String dirpathString = ServletContextParameterMap
				.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY);

		
		if (dirpathString != null && dirpathString.compareTo("") != 0) {
			File f = new File(dirpathString);
			String[] scripts = f.list(new FilenameFilter(){

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".py");
				}});
			for(String script : scripts)
			{
				String fileName = dirpathString  + script;
				Long lastTimeRead = fileNameTolastTimeRead.get(fileName);
				File s = new File(fileName);
				if(lastTimeRead == null || s.lastModified() > lastTimeRead)
				{
					String statement = FileUtils.readFileToString(s);
					PyCode py = compileAndAddToRepository(interpreter, statement);
					libraryScripts.put(fileName, py);
					fileNameTolastTimeRead.put(fileName, System.currentTimeMillis());
				}
				else
				{
					interpreter.exec(libraryScripts.get(fileName));
				}
				
				//TODO prune scripts no longer present
			}
		}
		
	}
}
