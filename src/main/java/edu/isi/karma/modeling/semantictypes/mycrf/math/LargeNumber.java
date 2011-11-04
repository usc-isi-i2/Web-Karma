package edu.isi.karma.modeling.semantictypes.mycrf.math ;

import edu.isi.karma.modeling.semantictypes.myutils.Prnt;

public class LargeNumber {

	double mantissa ;
	int exp ;

	public LargeNumber(double d) {
		if(d == 0.0) {
			mantissa = 0.0 ;
			exp = 0 ;
		}
		else {	
			exp = (int) Math.log10(d) ;
			mantissa = d / Math.pow(10, exp) ;
		}
	}

	public LargeNumber(double mantissa, int exp) {
		this.mantissa = mantissa ;
		this.exp = exp ;
	}
	
	public LargeNumber(LargeNumber ln) {
		this.mantissa = ln.mantissa ;
		this.exp = ln.exp ;
	}

	public void setValue(double d) {
		if(d == 0.0) {
			mantissa = 0.0 ;
			exp = 0 ;
		}
		else {	
			exp = (int) Math.log10(d) ;
			mantissa = d / Math.pow(10, exp) ;
		}
	}

	public void setValue(double mantissa, int exp) {
		this.mantissa = mantissa ;
		this.exp = exp ;
	}

	public void setValue(LargeNumber largeNumber) {
		mantissa = largeNumber.mantissa ;
		exp = largeNumber.exp ;
	}

	static public LargeNumber add(LargeNumber n1, LargeNumber n2) {
		double m ;
		int e ;

		if(n1.mantissa == 0.0)
			return n2 ;
		else if(n2.mantissa == 0.0)
			return n1 ;

		if(Math.abs(n1.exp - n2.exp) > 300 && n1.mantissa != 0.0 && n2.mantissa != 0.0) {
			//Prn.prn("The difference between the exponentials is more than 300. n1 = " + n1 + " and n2 = " + n2) ;
			if(n1.exp > n2.exp)
				return n1 ;
			else
				return n2 ;
		}

		if(n1.exp > n2.exp) {
			m = n1.mantissa + n2.mantissa / Math.pow(10, n1.exp - n2.exp) ;
			e = n1.exp ;
		}
		else if (n1.exp < n2.exp){
			m = n1.mantissa / Math.pow(10, n2.exp - n1.exp) + n2.mantissa ;
			e = n2.exp ;
		}
		else {
			m = n1.mantissa + n2.mantissa ;
			e = n1.exp ;
		}

		if(m > 10.0) {
			m/=10 ;
			e++ ;
		}

		LargeNumber ln = new LargeNumber(m, e) ;
		ln.normalize() ;
		return ln ;
	}

	static public LargeNumber multiply(LargeNumber n1, LargeNumber n2) {
		double m ;
		int e ;

		m = n1.mantissa * n2.mantissa ;
		e = n1.exp + n2.exp ;

		if(m > 10.0) {
			m/=10.0 ;
			e++ ;
		}

		LargeNumber ln = new LargeNumber(m, e) ;
		ln.normalize() ;
		return ln ;

	}

	public double val() {
		return mantissa * Math.pow(10, exp) ;
	}

	/*
	static public LargeNumber makeLargeNumberUsingExponent(double exponent) {
		LargeNumber total = new LargeNumber(1.0, 0) ;
		while(exponent > 300) {
			exponent-=300 ;
			double d = Math.pow(Math.E, 300) ;
			LargeNumber ln = new LargeNumber(d) ;
			total = LargeNumber.multiply(total, ln) ;
		}
		LargeNumber ln = new LargeNumber(Math.pow(Math.E, exponent)) ;
		total = LargeNumber.multiply(total, ln) ;
		total.normalize();
		return total ;
	}
	*/
	
	static public LargeNumber makeLargeNumberUsingExponent(double naturalExp) {
		double floatExp = naturalExp * Math.log10(Math.E) ;
		int intExp = (int) Math.floor(floatExp) ;
		double mantissa = Math.pow(10.0, floatExp - intExp) ;
		LargeNumber ln = new LargeNumber(mantissa, intExp) ;
		ln.normalize() ;
		return ln ;
	}
	
	public void setValueUsingExponent(double exponent) {
		LargeNumber tmpLargeNumber = new LargeNumber(0.0, 0) ;
		setValue(1.0, 0) ;
		while(exponent > 300) {
			exponent-=300 ;
			double d = Math.pow(Math.E, 300) ;
			tmpLargeNumber.setValue(d) ;
			multiplyEquals(tmpLargeNumber) ;
		}
		tmpLargeNumber.setValue(Math.pow(Math.E, exponent)) ;
		multiplyEquals(tmpLargeNumber) ;
	}

	static public double divide(LargeNumber ln1, LargeNumber ln2) {
		double m = ln1.mantissa / ln2.mantissa ;
		int e = ln1.exp - ln2.exp ;
		if(e <= -300)
			return 0.0 ;
		else {
			double d = m * Math.pow(10, e) ;
			return d ;
		}
	}

	public String toString() {
		String s = "" ;

		s = mantissa + " x E " + exp ;
		return s ;
	}

	static public double log(LargeNumber ln) {
		if(ln.mantissa == 0.0)
			Prnt.endIt("Zero passed to LargeNumber.log") ;

		double log = Math.log(ln.mantissa) + ln.exp * Math.log(10) ;
		return log ;
	}

	static public double log10(LargeNumber ln) {
		double value = 0.0 ;
		
		if(ln.mantissa < 0)
			Prnt.endIt("Attemping to take log10 of a negative number. Quiting.") ;
		
		value = Math.log10(ln.mantissa) + ln.exp ;
		return value ;
	}
	
	private void normalize() {
		if(mantissa >= 1.0 && mantissa < 10.0)
			return ;

		double exp_of_mantissa = Math.log10(mantissa) ;
		double floor_of_exp_of_mantissa = Math.floor(exp_of_mantissa) ;

		mantissa = Math.pow(10.0, exp_of_mantissa - floor_of_exp_of_mantissa) ;
		exp+=((int) floor_of_exp_of_mantissa) ;	
	}

	public void plusEquals(LargeNumber largeNumber2) {
		double m ;
		int e ;

		if(this.mantissa == 0.0) {
			setValue(largeNumber2) ;
			return ;
		}
		else if(largeNumber2.mantissa == 0.0)
			return ;

		if(Math.abs(this.exp - largeNumber2.exp) > 300 && this.mantissa != 0.0 && largeNumber2.mantissa != 0.0) {
			//Prn.prn("The difference between the exponentials is more than 300. n1 = " + this + " and n2 = " + largeNumber2) ;
			if(this.exp > largeNumber2.exp)
				return ;
			else {
				setValue(largeNumber2) ;
				return ;
			}
		}

		if(this.exp > largeNumber2.exp) {
			m = this.mantissa + largeNumber2.mantissa / Math.pow(10, this.exp - largeNumber2.exp) ;
			e = this.exp ;
		}
		else if (this.exp < largeNumber2.exp){
			m = this.mantissa / Math.pow(10, largeNumber2.exp - this.exp) + largeNumber2.mantissa ;
			e = largeNumber2.exp ;
		}
		else {
			m = this.mantissa + largeNumber2.mantissa ;
			e = this.exp ;
		}

		if(m > 10.0) {
			m/=10 ;
			e++ ;
		}

		setValue(m,e) ;
		normalize() ;
	}

	public void multiplyEquals(LargeNumber largeNumber2) {
		mantissa*= largeNumber2.mantissa ;
		exp+= largeNumber2.exp ;

		if(mantissa > 10.0) {
			mantissa/=10.0 ;
			exp++ ;
		}

		normalize() ;
	}

}

