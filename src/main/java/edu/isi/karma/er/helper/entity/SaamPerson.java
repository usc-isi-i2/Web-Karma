package edu.isi.karma.er.helper.entity;


public class SaamPerson {

	private String subject;
	
	private PersonProperty fullName;
	
	private PersonProperty birthYear;
	
	private PersonProperty deathYear;
	

	
	public SaamPerson() {
		
	}


	public PersonProperty getProperty(String propertyName) {
		PersonProperty p = null;
		if (propertyName != null) {
			if (propertyName.indexOf("fullName") > -1) {
				return this.fullName;
			} else if (propertyName.indexOf("birthYear") > -1) {
				return this.birthYear;
			} else if (propertyName.indexOf("deathYear") > -1) {
				return this.deathYear;
			}
		}
		
		return p;
	}

	public String getSubject() {
		return subject;
	}



	public void setSubject(String subject) {
		this.subject = subject;
	}



	public PersonProperty getFullName() {
		return fullName;
	}



	public void setFullName(PersonProperty fullName) {
		this.fullName = fullName;
	}



	public PersonProperty getBirthYear() {
		return birthYear;
	}



	public void setBirthYear(PersonProperty birthYear) {
		this.birthYear = birthYear;
	}



	public PersonProperty getDeathYear() {
		return deathYear;
	}



	public void setDeathYear(PersonProperty deathYear) {
		this.deathYear = deathYear;
	}
	
	
}
