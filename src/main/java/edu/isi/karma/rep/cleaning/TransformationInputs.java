package edu.isi.karma.rep.cleaning;

import java.util.Collection;

public interface TransformationInputs {
	public abstract Collection<TransformationExample> getExamples();
	public abstract ValueCollection getInputValues();
	public abstract void setPreferredRule(Transformation t);
}
