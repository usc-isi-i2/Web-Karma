package edu.isi.karma.er.compare.impl;

import java.util.Arrays;

import edu.isi.karma.er.compare.StringComparator;

public class StringQGramComparatorImpl implements StringComparator {

	@Override
	public float getSimilarity(String str1, String str2) {
		
		//QGramsDistance method = new QGramsDistance();
		return getSimilarityLocal(str1, str2);

	}

	private float getSimilarityLocal(String str1, String str2) {
		
		float degree = 0f;
		int q = 2;
		str1 = trim(str1);
		str2 = trim(str2);

		int s1 = str1.length(), s2 = str2.length();
		if (s1 < 2 || s2 < 2) 
			return 0;

		char[][] a = new char[s1 - q + 1][q];
		char[][] b = new char[s2 - q + 1][q];
		
		for (int i = 0; i < s1 -q + 1; i++) {
			for (int j = 0; j < q; j ++) {
				a[i][j] = str1.charAt(i + j);
			}
		}
		
		for (int i = 0; i < s2 -q + 1; i++) {
			for (int j = 0; j < q; j ++) {
				b[i][j] = str2.charAt(i + j);
			}
		}

		int k, count = 0;
		for (int i = 0; i < s1 - q + 1; i++) 
			for (int j = 0; j < s2 -q + 1; j++) {
				for (k = 0; k < q; k++) {
					if (a[i][k] != b[j][k])
						break;
				}
				if (k >= q) {
					count ++;
					break;
				}
			}
		degree = count * 2f / (s1 + s2 -2*q + 2);

		degree = degree + (1 - degree) * calcWeight(str1, str2);

		return degree;
	}
	
	private String trim(String str) {
		char last = ' ';
		int count = 0;
		char[] strch = new char[str.length()];
		for (int i = 0; i < strch.length; i++) {
			char ch = str.charAt(i);
			if (ch == ',' || ch == '.') ch = ' ';
			if (ch == ' ' && last == ' ') {
				
			} else {
				strch[count] = ch;
				count ++;
			}
			last = ch;
			
		}
		
		return new String(Arrays.copyOf(strch, count));
	}
	
	
	//int overlap = 0, total = 0;
			/*
			String[] ss1 = str1.split(" ");
			String[] ss2 = str2.split(" ");
			for (String t1 : ss1) {
				for (String t2 : ss2) {
					if (t1.equals(t2)) {
						overlap += 2;
						break;
					} else if (t1.startsWith(t2) || t2.startsWith(t1)) {
						overlap += 1;
						break;
					}
				}
			}
			*/
	private float calcWeight(String str1, String str2) {
		int weight = 0;
		int i, j, k, len1, len2, count;
		char[] ch1 = str1.toCharArray(), ch2 = str2.toCharArray();
		i = 0; 
		len1 = ch1.length; len2 = ch2.length;
		while (i < len1) {
			j = 0;
			while (j < len2) {
				for (k = 0; (i+k) < len1 && (j+k) < len2 && ch1[i+k] != ' ' && ch2[j+k] != ' ' && ch1[i+k] == ch2[j+k]; k++) {
					
				}
				if ((i+k) >= len1 || (j+k) >= len2) {
					if ((i+k) >= len1 && (j+k) >= len2) {
						weight += 2;
					} else if (((i+k) >= len1 && ch2[j+k] == ' ') || ((j+k) >= len2 && ch1[i+k] == ' ')) {
						weight += 2;
					} else {
						weight += 1;
					}
				} else if (ch1[i+k] == ' ' || ch2[j+k] == ' ') {
					if (((i+k) >= len1 || ch1[i+k] == ' ') && ((j+k) >= len2 || ch2[j+k] == ' ')) {
						weight += 2;
						break;
					} else {
						weight += 1;
						break;
					}
				}
				while (ch2[j++] != ' ' && j < len2) ;

				
			}
			while (ch1[i++] != ' ' && i < len1); 

		}
		
		count = 0;
		for (i = 0; i < len1; i++)
			if (ch1[i] == ' ')
				count ++;
		for (j = 0; j < len2; j++) 
			if (ch2[j] == ' ')
				count ++;
		return weight * 1f / (count + 2);
	}

}
