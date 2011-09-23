package edu.isi.karma.webserver;

import junit.framework.TestCase;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.view.VWorkspace;

public class AbstractExecuteCommandTester extends TestCase {

	protected RepFactory f;
	protected VWorkspace vwsp;
	protected ExecutionController em;
	protected NullHttpServletRequest r;

	protected AbstractExecuteCommandTester(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.vwsp = new VWorkspace(f.createWorkspace());
		this.em = new ExecutionController(vwsp);
		this.r = new NullHttpServletRequest();
	}
}
