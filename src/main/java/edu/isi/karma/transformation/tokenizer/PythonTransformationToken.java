package edu.isi.karma.transformation.tokenizer;

public abstract class PythonTransformationToken {

	private String token;

	public PythonTransformationToken(String token) {
		this.token = token;
	}
	public abstract void accept(PythonTransformationTokenVistor visitor);
	
	@Override
	public String toString()
	{
		return token;
	}
}
