package edu.isi.karma.cleaning;

public interface InterpreterType {
	public void func(String name, Object x);

	public String execute(String value);

	public String execute_debug(String value);
}
