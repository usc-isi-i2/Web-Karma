package edu.isi.karma.er.compare.impl;

import java.util.Set;

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
		String t;
		str1 = trim(str1);
		str2 = trim(str2);
		
		
		int s1 = str1.length(), s2 = str2.length();
		if (s1 < 2 || s2 < 2) 
			return 0;
		//str1 = '#' + str1 + '#';
		//str2 = '#' + str2 + '#';
		/*
		if (s1 > s2) {
			t = str1;
			str1 = str2;
			str2 = t;
		}
		*/
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
		float factor = s1 < s2 ? (s1 * 1f / s2) : (s2 * 1f / s1);
		degree = count * factor / ((s1 < s2 ? s1 : s2 ) -q + 1);
		
		int overlap = 0, total = 0;
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
		total = ss1.length + ss2.length;
		if (overlap > 0) {
			degree = degree + (1 - degree) * (overlap * 1f / total);
		}
		
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
		return new String(strch);
	}

}
