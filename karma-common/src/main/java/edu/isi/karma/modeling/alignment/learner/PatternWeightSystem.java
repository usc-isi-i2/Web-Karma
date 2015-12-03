package edu.isi.karma.modeling.alignment.learner;

public enum PatternWeightSystem {
	Default, // assign wl to all links
	OriginalWeights,
	JWSPaperFormula // w = wl - [x / (n+1)]
}
