package edu.isi.karma.rep.cleaning;

import java.util.Collection;
import java.util.HashMap;

public interface TransformationOutput {
	public abstract HashMap<String,Transformation> getTransformations();
	public abstract ValueCollection getTransformedValues(String TransformatinId);
	public abstract Collection<String> getRecommandedNextExample();
}
