package edu.isi.karma.cleaning.correctness;


public interface VerificationFunc {
	//label a record
	//label 0 correct, >=1 doubious
	public String verify(TransRecord record);
	
}
