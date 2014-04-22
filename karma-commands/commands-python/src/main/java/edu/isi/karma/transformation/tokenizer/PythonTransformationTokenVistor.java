package edu.isi.karma.transformation.tokenizer;

import java.util.List;

public abstract class PythonTransformationTokenVistor {

	protected void visit(List<PythonTransformationToken> tokens)
	{
		for(PythonTransformationToken token : tokens)
		{
			token.accept(this);
		}
	}
	protected abstract void visit(PythonTransformationToken pythonTransformationToken);
	protected abstract void visit(PythonTransformationStringToken pythonTransformationToken);
	protected abstract void visit(PythonTransformationInvalidToken pythonTransformationToken);
	protected abstract void visit(PythonTransformationColumnToken pythonTransformationToken);

}
