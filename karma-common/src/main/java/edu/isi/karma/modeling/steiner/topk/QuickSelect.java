package edu.isi.karma.modeling.steiner.topk;

import java.util.Random;
 
public class QuickSelect {
 
	private static <E extends Comparable<? super E>> int partition(E[] arr, int left, int right, int pivot) {
		E pivotVal = arr[pivot];
		swap(arr, pivot, right);
		int storeIndex = left;
		for (int i = left; i < right; i++) {
			if (arr[i].compareTo(pivotVal) < 0) {
				swap(arr, i, storeIndex);
				storeIndex++;
			}
		}
		swap(arr, right, storeIndex);
		return storeIndex;
	}
 
	public static <E extends Comparable<? super E>> E select(E[] arr, int n) {
		int left = 0;
		int right = arr.length - 1;
		Random rand = new Random();
		while (right >= left) {
			int pivotIndex = partition(arr, left, right, rand.nextInt(right - left + 1) + left);
			if (pivotIndex == n) {
				return arr[pivotIndex];
			} else if (pivotIndex < n) {
				left = pivotIndex + 1;
			} else {
				right = pivotIndex - 1;
			}
		}
		return null;
	}
	
	private static void swap(Object[] arr, int i1, int i2) {
		if (i1 != i2) {
			Object temp = arr[i1];
			arr[i1] = arr[i2];
			arr[i2] = temp;
		}
	}
 
	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			Integer[] input = {9, 8, 7, 6, 5, 0, 1, 2, 3, 4};
			System.out.print(select(input, i));
			if (i < 9) System.out.print(", ");
		}
		System.out.println();
		
		ModelFrequencyPair[] m = new ModelFrequencyPair[5];
		
		
		ModelFrequencyPair m0 = new ModelFrequencyPair("m0", 60);
		ModelFrequencyPair m1 = new ModelFrequencyPair("m1", 30);
		ModelFrequencyPair m2 = new ModelFrequencyPair("m2", 70);
		ModelFrequencyPair m3 = new ModelFrequencyPair("m3", 20);
		ModelFrequencyPair m4 = new ModelFrequencyPair("m4", 10);
		
		m[0] = m0; 
		m[1] = m1;
		m[2] = m2;
		m[3] = m3;
		m[4] = m4;
		
		System.out.println(select(m,3).getId());
	}
 
}