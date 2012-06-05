/* Copyright 2011 - iSencia Belgium NV

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.embl.cca.utils.imageviewer;

import java.util.Vector;

public class QuickSort {

	public static void quickSort(float arr[], int left, int right) {
	      int i = left, j = right;
	      float tmp;
	      float pivot = arr[(left + right) / 2];
	 
	      /* partition */
	      while (i <= j) {
	            while (arr[i] < pivot)
	                  i++;
	            while (arr[j] > pivot)
	                  j--;
	            if (i <= j) {
	    			tmp = arr[i];
	    			arr[i] = arr[j];
	    			arr[j] = tmp;
	    			i++;
	                j--;
	            }
	      };
	 
	      /* recursion */
	      if (left < j)
	            quickSort(arr, left, j);
	      if (i < right)
	            quickSort(arr, i, right);
	}

	/**
	 * QuickSort - adapted from Doug Lea's Public Domain collection library.
	 * 
	 * @author <a href="mailto:mbryson@mindspring.com">Dave Bryson</a>
	 */
/**
	 * Sort array of Objects using the QuickSort algorithm.
	 * 
	 * @param s
	 *            A float[].
	 * @param lo
	 *            The current lower bound.
	 * @param hi
	 *            The current upper bound.
	 */
	public static void quickSortBad(float s[], int lo, int hi) {
		if (lo >= hi)
			return;

		/*
		 * Use median-of-three(lo, mid, hi) to pick a partition. Also swap them
		 * into relative order while we are at it.
		 */
		int mid = (lo + hi) / 2;

		if (s[lo] > s[mid]) { // Swap.
			float tmp = s[lo];
			s[lo] = s[mid];
			s[mid] = tmp;
		}

		if (s[mid] > s[hi]) { // Swap.
			float tmp = s[mid];
			s[mid] = s[hi];
			s[hi] = tmp;

			if (s[lo] > s[mid]) { // Swap.
				float tmp2 = s[lo];
				s[lo] = s[mid];
				s[mid] = tmp2;
			}
		}

		// Start one past lo since already handled lo.
		int left = lo + 1;

		// Similarly, end one before hi since already handled hi.
		int right = hi - 1;

		// If there are three or fewer elements, we are done.
		if (left >= right)
			return;

		float partition = s[mid];

		for (;;) {
			while (s[right] > partition)
				--right;

			while (left < right && s[left] <= partition)
				++left;

			if (left < right) { // Swap.
				float tmp = s[left];
				s[left] = s[right];
				s[right] = tmp;

				--right;
			} else
				break;
		}
		System.out.println( "quickSort1(s, lo, left): " + lo + ", " + left );
		quickSort(s, lo, left);
		System.out.println( "quickSort2(s, left + 1, hi): " + (left + 1) + ", " + hi );
		quickSort(s, left + 1, hi);
	}

	/**
	 * Sorts and array of objects.
	 * 
	 * @param data
	 *            An Object[].
	 */
	public static void sortInItself(float[] data) {
		QuickSort.quickSort(data, 0, data.length - 1);
	}

	public static float[] sort(float[] data) {
		float[] newData = data.clone();
		QuickSort.quickSort(newData, 0, newData.length - 1);
		return newData;
	}

	public static void quickSort(Vector<Float> s, int lo, int hi) {
		if (lo >= hi)
			return;

		int mid = (lo + hi) / 2;
		if (s.get(lo) > s.get(mid)) { // Swap.
			Float tmp = s.get(lo);
			s.set(lo, s.get(mid));
			s.set(mid, tmp);
		}
		if (s.get(mid) > s.get(hi)) { // Swap.
			Float tmp = s.get(mid);
			s.set(mid, s.get(hi));
			s.set(hi, tmp);
			if (s.get(lo) > s.get(mid)) { // Swap.
				tmp = s.get(lo);
				s.set(lo, s.get(mid));
				s.set(mid, tmp);
			}
		}
		int left = lo + 1;
		int right = hi - 1;
		if (left >= right)
			return;
		float partition = s.get(mid);
		for (;;) {
			while (s.get(right) > partition)
				--right;
			while (left < right && s.get(left) <= partition)
				++left;
			if (left < right) { // Swap.
				Float tmp = s.get(left);
				s.set(left, s.get(right));
				s.set(right, tmp);
				--right;
			} else
				break;
		}
		quickSort(s, lo, left);
		quickSort(s, left + 1, hi);
	}

	public static void sortInItself(Vector<Float> data) {
		QuickSort.quickSort(data, 0, data.size() - 1);
	}
}
