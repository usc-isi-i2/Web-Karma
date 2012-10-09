package edu.isi.karma.er.helper.entity;

public enum ScoreType {

	NORMAL // get result normally
	, IGNORE // ignore result, generally means neglect the score result
	, INVALID; // the parameter passed in may be invalid, check them first.

}
