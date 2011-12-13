package edu.isi.mediator.gav.graph.cost_estimation;


public class CostUtil{

    // takes two non-negative numbers, n and k, where k < n, 
    // and returns the comb(n , k) = n! / ((n - k)! * k!) Note: 
    // We assume n >= 0, 0 <= k <= n.
    //     All possible combination of selecting k from n 
    // combinari de n luate cate k
    static public long combinations (int n, int k) {
        int i, j;

        if (n <= 1)
	    return 1;
	
        int[] numerators = new int[ n - 1 ];     // only 2..n numbers
        int[] denumerators = new int[ (k - 1) + (n - k - 1) ]; // 2..k and 2..n-k
	
        for (i = 0 ; i < numerators.length ; i++)// fill all numerators
	    numerators[ i ] = i + 2;
        for (i = 0 ; i < (k - 1) ; i++)         // fill all denumerators 2..k
	    denumerators[ i ] = i + 2;
        for (i = 0 ; i < (n - k - 1) ; i++)     // fill all denumerators 2..n-k
	    denumerators[ i + (k - 1)] = i + 2;
	
        int num_denumerators = denumerators.length;// how many diff from 1
        while (num_denumerators > 0)               // still need to "cancel"?
	    for (i = 0 ; i < denumerators.length ; i++) {
		if (denumerators[ i ] == 1)
		    continue;               //skip it!
		while (denumerators[ i ] > 1)
		    for (j = 0 ; j < numerators.length ; j++) {
			if (numerators[ j ] == 1)
			    continue;       //skip it!
			int g = gcd(denumerators[ i ], numerators[ j ]);
			if (g == 1)
			    continue;       //skip it!
			// we have a new divisor! g!
			numerators[ j ] /= g;
			denumerators[ i ] /= g;
			if (denumerators[ i ] == 1)
			    num_denumerators--;
		    }
	    }
	
	long r = 1;
        for (j = 0 ; j < numerators.length ; j++) {
	    if (numerators[ j ] == 1)
		continue;       //skip it!
	    r *= numerators[ j ];
        }
        return r;
    }                                               // end comb4
    
    // greatest common denominator
    static int gcd(int x, int y) {
        if (x < y)
	    return gcd(y, x);
        // we have: x >= y
        if (y == 0)
	    return x;
        if (y == 1)
	    return 1;
        return gcd(y, x % y);
    }    
    //////////////////////////

    // DETERMINE THE NEW NUMBER OF DISTINCT VALUES - determined by phi()

    // Consider that we have a relation with R tuples, and an attribute A with 
    // V distinct values. If we select R1 tuples we want to estimate the NEW 
    // number of distinct values V1 for A.

    // probility of NOT selecting a certain class
    // C(R-k, R')/C(R,R')

    static double probability(int r, int rp, int v) {

	int k = r/v;

	if(r-k < rp) return 0;

	/* special case handling to minimize the intermediate result */
	if(rp >= k+1) {
	    double p = combinations(r-rp, k)/combinations(r,k);
	    return p;
	}

	double p = combinations(r-k, rp)/combinations(r,rp);
	return p;
    }

    // V'=V*(1-C(R-k, R')/C(R,R'))=phi(R, R', V)
    static public int phi(int r, int rp, int v) {

	int new_v =  (int)(v * (double)(1.0 - probability(r, rp, v)));

	if(new_v==0) new_v = 1;

	return new_v;
    }
    /////////////////////////////////////////////

    //Determine the new extreme values for numerical attributes

    //When selecting R' tuples from a number of R tuples, the values of the 
    //MIN and MAX values of a numeric attribute A will also change.

    //Consider "min" the old minimum, "max" the old maximum, and V' the new variety of attribute A. Then the cost function estimates the new minimum/maximum as follows: divide the [min, max] interval into V' intervals, and take the middle of the leftmost interval (for the min) or rightmost interval (for the max). 
    //newmin = min + ((max-min)/V')/2 
    static public double computeNewMin(double oldmin,double oldmax,int newv){
	
	double new_min=0;
	new_min = oldmin + ((oldmax - oldmin)/newv)/2;
	return new_min;
    }

    //newmax = max - ((max-min)/V')/2
    static public double computeNewMax(double oldmin,double oldmax,int newv){
	double new_max=0;
	new_max = oldmax - ((oldmax - oldmin)/newv)/2;
	return new_max;
    }
    ////////////////////////////////////////

}
