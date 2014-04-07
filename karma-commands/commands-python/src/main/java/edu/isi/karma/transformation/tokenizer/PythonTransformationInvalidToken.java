package edu.isi.karma.transformation.tokenizer;

public class PythonTransformationInvalidToken extends PythonTransformationToken {

	public PythonTransformationInvalidToken(String token) {
		super(token);
	}
	@Override
	public void accept(PythonTransformationTokenVistor visitor) {
		visitor.visit(this);
	}
}
