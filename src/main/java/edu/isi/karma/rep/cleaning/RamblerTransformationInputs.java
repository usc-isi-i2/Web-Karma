package edu.isi.karma.rep.cleaning;

import java.util.Collection;


public class RamblerTransformationInputs implements TransformationInputs {

	private Collection<TransformationExample> examples;
	private ValueCollection inputValues;
	private Transformation preferedTransformation;
	public RamblerTransformationInputs(Collection<TransformationExample> examples,ValueCollection inputValues)
	{
		this.examples = examples;
		this.inputValues = inputValues;
	}
	@Override
	public Collection<TransformationExample> getExamples() {
		// TODO Auto-generated method stub
		return this.examples;
	}
	
	@Override
	public ValueCollection getInputValues() {
		// TODO Auto-generated method stub
		return this.inputValues;
	}

	@Override
	public void setPreferredRule(Transformation t) {
		// TODO Auto-generated method stub
		this.preferedTransformation = t;
	}
	
}
