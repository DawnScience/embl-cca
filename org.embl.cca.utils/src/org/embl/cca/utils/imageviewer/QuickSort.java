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

	/**
	 * Sort array of Objects using the QuickSort algorithm.
	 * 
	 * @param s A float[].
	 * @param lo The current lower bound.
	 * @param hi The current upper bound.
	 */
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

	public static void quickSortTop(float arr[], int left, int right, int from) {
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
	      if ( left < j && j >= from )
	            quickSortTop( arr, left, j, from );
	      if ( i < right && right >= from )
	            quickSortTop( arr, i, right, from );
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

	public static float[] sortTop(float[] data, int from) {
		float[] newData = data.clone();
		QuickSort.quickSortTop(newData, 0, newData.length - 1, from);
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
