package edu.isi.karma.er.helper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.python.core.PyCode;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonRepository {

	private static Logger logger = LoggerFactory.getLogger(PythonRepository.class);
	private ConcurrentHashMap<String, PyCode> scripts = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, PyCode> libraryScripts = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Long> fileNameTolastTimeRead = new ConcurrentHashMap<>();
	private List<String> localKeys = Arrays.asList("workspaceid", "selectionName", "command", "worksheetId", "selection", "nodeid");
	private boolean libraryHasBeenLoaded = false;
	private boolean reloadLibrary = true;
	private PyStringMap initialLocals;
	private PythonInterpreter interpreter;
	private String repositoryPath;
	
	public PythonRepository(boolean reloadLibrary, String repositoryPath)
	{
		this.repositoryPath = repositoryPath;
		this.reloadLibrary = reloadLibrary;
		initialize();
		resetLibrary();
	}

	private void initialize()
	{
		scripts = new ConcurrentHashMap<>();
		initialLocals = new PyStringMap();
		interpreter = PythonInterpreter.threadLocalStateInterpreter(initialLocals);
		
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getImportStatements());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getRowIndexDefStatement());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getGetValueDefStatement());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getGetValueFromNestedColumnByIndexDefStatement());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getIsEmptyDefStatement());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getHasSelectedRowsStatement());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getVDefStatement());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getTransformStatement());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getSelectionStatement());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getModelName());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getModelPrefix());
		compileAndAddToRepository(interpreter, PythonTransformationHelper.getModelBaseUri());
		initializeInterpreter(interpreter);
	}

	public PyCode compileAndAddToRepositoryAndExec(PythonInterpreter interpreter, String statement)
	{
		PyCode py;
		if(!scripts.containsKey(statement))
		{
			py = compileAndAddToRepository(interpreter,statement);
			
		}
		else
			py = scripts.get(statement);
		interpreter.exec(py);
		return py;
	}
	public PyCode compileAndAddToRepository(PythonInterpreter interpreter,
			String statement) {
		PyCode py;
		if(!scripts.containsKey(statement))
		{
			py = compile(interpreter, statement);
			scripts.putIfAbsent(statement, py);
		}
		return scripts.get(statement);
	}

	private PyCode compile(PythonInterpreter interpreter, String statement) {
		return interpreter.compile(statement);
	}

	public void initializeInterpreter(PythonInterpreter interpreter)
	{
		boolean localsUninitialized = interpreter.getLocals() == initialLocals;
		if(localsUninitialized)
		{
			PyStringMap locals = new PyStringMap();
			interpreter.setLocals(locals);
			interpreter.exec(scripts.get(PythonTransformationHelper.getImportStatements()));
			interpreter.exec(scripts.get(PythonTransformationHelper.getGetValueDefStatement()));
			interpreter.exec(scripts.get(PythonTransformationHelper.getIsEmptyDefStatement()));
			interpreter.exec(scripts.get(PythonTransformationHelper.getHasSelectedRowsStatement()));
			interpreter.exec(scripts.get(PythonTransformationHelper.getGetValueFromNestedColumnByIndexDefStatement()));
			interpreter.exec(scripts.get(PythonTransformationHelper.getRowIndexDefStatement()));
			interpreter.exec(scripts.get(PythonTransformationHelper.getVDefStatement()));
			interpreter.exec(scripts.get(PythonTransformationHelper.getModelName()));
			interpreter.exec(scripts.get(PythonTransformationHelper.getModelPrefix()));
			interpreter.exec(scripts.get(PythonTransformationHelper.getModelBaseUri()));
			
		}
		if(localsUninitialized ||(!libraryHasBeenLoaded || reloadLibrary))
		{
			importUserScripts(interpreter);
		}

	}

	public PyCode getTransformCode()
	{
		return scripts.get(PythonTransformationHelper.getTransformStatement());
	}
	
	public PyCode getSelectionCode()
	{
		return scripts.get(PythonTransformationHelper.getSelectionStatement());
	}

	public synchronized void importUserScripts(PythonInterpreter interpreter) {
		

		if (repositoryPath != null && repositoryPath.compareTo("") != 0) {
			
			if(!libraryHasBeenLoaded || reloadLibrary)
			{
				File f = new File(repositoryPath);
				String[] scripts = f.list(new FilenameFilter(){

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".py");
					}});
				for(String script : scripts)
				{
					String fileName = repositoryPath  + script;
					Long lastTimeRead = fileNameTolastTimeRead.get(fileName);
					File s = new File(fileName);
					if(lastTimeRead == null || s.lastModified() > lastTimeRead)
					{
						String statement;
						try {
							statement = FileUtils.readFileToString(s);
							PyCode py = compile(interpreter, statement);
							libraryScripts.put(fileName, py);
							fileNameTolastTimeRead.put(fileName, System.currentTimeMillis());
						} catch (IOException e) {
							logger.error("Unable to process python script in {}: {}", fileName,e.toString());
						}
						
					}
					interpreter.exec(libraryScripts.get(fileName));
					//TODO prune scripts no longer present
				}
				libraryHasBeenLoaded = true;

			}
			else
			{
				
				for(PyCode code : libraryScripts.values())
				{
					interpreter.exec(code);
				}
			}

		}

	}

	public synchronized void resetLibrary()
	{
		libraryScripts = new ConcurrentHashMap<>();
		fileNameTolastTimeRead = new ConcurrentHashMap<>();		
		libraryHasBeenLoaded = false;

	}

	protected String getRepositoryPath() {
		return repositoryPath;
	}

	private void resetRuntimeLocal() {
		for (String key : localKeys) {
			try {
				this.interpreter.getLocals().__delitem__(key);
			} catch (Exception e) {
				//Key is missing, do nothing
			}
		}
	}

	public PythonInterpreter getInterpreter() {
		resetRuntimeLocal();
		//interpreter.cleanup();
		return this.interpreter;
	}
}
