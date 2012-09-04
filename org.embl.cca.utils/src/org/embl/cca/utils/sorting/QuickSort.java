/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.embl.cca.utils.sorting;


/**
 * The <code>QuickSort</code> class is for sorting an array.
 * Its special feature is being able to sort only an upper part of an array.
 * <p>
 *
 * @author  Gábor Náray
 * @version 1.10 18/07/2012
 * @since   20120718
 */
public class QuickSort {

	/* This section contains the methods for byte array. */

	/**
	 * Sort array of bytes using the QuickSort algorithm.
	 * 
	 * @param arr A byte array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 */
	public static void quickSort(byte arr[], int left, int right) {
	      int i = left, j = right;
	      byte tmp;
	      byte pivot = arr[(left + right) / 2];
	 
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
	 * Sort array of bytes using the QuickSort algorithm.
	 * 
	 * @param arr A byte array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void quickSortTop(byte arr[], int left, int right, int from) {
	      int i = left, j = right;
	      byte tmp;
	      byte pivot = arr[(left + right) / 2];
	 
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
	 * Sort array of bytes using the QuickSort algorithm.
	 * 
	 * @param arr A byte array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int quickSortFromValue(byte arr[], int left, int right, int pivot) {
	      int i = left, j = right;
	      byte tmp;
	 
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
	      if ( i < right )
	            quickSort( arr, i, right );
	      return i;
	}

	/**
	 * Sorts an array of bytes into itself using the QuickSort algorithm.
	 * 
	 * @param data A byte array.
	 */
	public static void sortInItself(byte[] data) {
		QuickSort.quickSort(data, 0, data.length - 1);
	}

	/**
	 * Sorts an array of bytes into a new array using the QuickSort algorithm.
	 * 
	 * @param data A byte array.
	 */
	public static byte[] sort(byte[] data) {
		byte[] newData = data.clone();
		QuickSort.quickSort(newData, 0, newData.length - 1);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of bytes into itself using the QuickSort algorithm.
	 * 
	 * @param data A byte array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void sortTopInItself(byte[] data, int from) {
		QuickSort.quickSortTop(data, 0, data.length - 1, from);
	}

	/**
	 * Sorts the upper part of an array of bytes into a new array using the QuickSort algorithm.
	 * 
	 * @param data A byte array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static byte[] sortTop(byte[] data, int from) {
		byte[] newData = data.clone();
		QuickSort.quickSortTop(newData, 0, newData.length - 1, from);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of bytes having values equal or above pivot into itself using the QuickSort algorithm.
	 * 
	 * @param data A byte array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int sortFromValueInItself(byte[] data, int pivot) {
		return QuickSort.quickSortFromValue(data, 0, data.length - 1, pivot);
	}

	/**
	 * Sorts the upper part of an array of bytes having values equal or above pivot into a new array using the QuickSort algorithm.
	 * 
	 * @param data A byte array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return ArrayAndIndexBI containing the sorted array and the index of first element equal or greater than pivot.
	 */
	public static ArrayAndIndexBI sortFromValue(byte[] data, int pivot) {
		ArrayAndIndexBI aai = new ArrayAndIndexBI();
		aai.setArray( data.clone() );
		aai.setIndex( QuickSort.quickSortFromValue(aai.getArray(), 0, aai.getArray().length - 1, pivot) );
		return aai;
	}

	/* This section contains the methods for short array. */

	/**
	 * Sort array of shorts using the QuickSort algorithm.
	 * 
	 * @param arr A short array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 */
	public static void quickSort(short arr[], int left, int right) {
	      int i = left, j = right;
	      short tmp;
	      short pivot = arr[(left + right) / 2];
	 
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
	 * Sort array of shorts using the QuickSort algorithm.
	 * 
	 * @param arr A short array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void quickSortTop(short arr[], int left, int right, int from) {
	      int i = left, j = right;
	      short tmp;
	      short pivot = arr[(left + right) / 2];
	 
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
	 * Sort array of shorts using the QuickSort algorithm.
	 * 
	 * @param arr A short array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int quickSortFromValue(short arr[], int left, int right, int pivot) {
	      int i = left, j = right;
	      short tmp;
	 
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
	      if ( i < right )
	            quickSort( arr, i, right );
	      return i;
	}

	/**
	 * Sorts an array of shorts into itself using the QuickSort algorithm.
	 * 
	 * @param data A short array.
	 */
	public static void sortInItself(short[] data) {
		QuickSort.quickSort(data, 0, data.length - 1);
	}

	/**
	 * Sorts an array of shorts into a new array using the QuickSort algorithm.
	 * 
	 * @param data A short array.
	 */
	public static short[] sort(short[] data) {
		short[] newData = data.clone();
		QuickSort.quickSort(newData, 0, newData.length - 1);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of shorts into itself using the QuickSort algorithm.
	 * 
	 * @param data A short array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void sortTopInItself(short[] data, int from) {
		QuickSort.quickSortTop(data, 0, data.length - 1, from);
	}

	/**
	 * Sorts the upper part of an array of shorts into a new array using the QuickSort algorithm.
	 * 
	 * @param data A short array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static short[] sortTop(short[] data, int from) {
		short[] newData = data.clone();
		QuickSort.quickSortTop(newData, 0, newData.length - 1, from);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of shorts having values equal or above pivot into itself using the QuickSort algorithm.
	 * 
	 * @param data A short array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int sortFromValueInItself(short[] data, int pivot) {
		return QuickSort.quickSortFromValue(data, 0, data.length - 1, pivot);
	}

	/**
	 * Sorts the upper part of an array of shorts having values equal or above pivot into a new array using the QuickSort algorithm.
	 * 
	 * @param data A short array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return ArrayAndIndexBI containing the sorted array and the index of first element equal or greater than pivot.
	 */
	public static ArrayAndIndexSI sortFromValue(short[] data, int pivot) {
		ArrayAndIndexSI aai = new ArrayAndIndexSI();
		aai.setArray( data.clone() );
		aai.setIndex( QuickSort.quickSortFromValue(aai.getArray(), 0, aai.getArray().length - 1, pivot) );
		return aai;
	}

	/* This section contains the methods for int array. */

	/**
	 * Sort array of ints using the QuickSort algorithm.
	 * 
	 * @param arr A int array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 */
	public static void quickSort(int arr[], int left, int right) {
	      int i = left, j = right;
	      int tmp;
	      int pivot = arr[(left + right) / 2];
	 
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
	 * Sort array of ints using the QuickSort algorithm.
	 * 
	 * @param arr A int array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void quickSortTop(int arr[], int left, int right, int from) {
	      int i = left, j = right;
	      int tmp;
	      int pivot = arr[(left + right) / 2];
	 
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
	 * Sort array of ints using the QuickSort algorithm.
	 * 
	 * @param arr A int array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int quickSortFromValue(int arr[], int left, int right, int pivot) {
	      int i = left, j = right;
	      int tmp;
	 
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
	      if ( i < right )
	            quickSort( arr, i, right );
	      return i;
	}

	/**
	 * Sorts an array of ints into itself using the QuickSort algorithm.
	 * 
	 * @param data A int array.
	 */
	public static void sortInItself(int[] data) {
		QuickSort.quickSort(data, 0, data.length - 1);
	}

	/**
	 * Sorts an array of ints into a new array using the QuickSort algorithm.
	 * 
	 * @param data A int array.
	 */
	public static int[] sort(int[] data) {
		int[] newData = data.clone();
		QuickSort.quickSort(newData, 0, newData.length - 1);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of ints into itself using the QuickSort algorithm.
	 * 
	 * @param data A int array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void sortTopInItself(int[] data, int from) {
		QuickSort.quickSortTop(data, 0, data.length - 1, from);
	}

	/**
	 * Sorts the upper part of an array of ints into a new array using the QuickSort algorithm.
	 * 
	 * @param data A int array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static int[] sortTop(int[] data, int from) {
		int[] newData = data.clone();
		QuickSort.quickSortTop(newData, 0, newData.length - 1, from);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of ints having values equal or above pivot into itself using the QuickSort algorithm.
	 * 
	 * @param data A int array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int sortFromValueInItself(int[] data, int pivot) {
		return QuickSort.quickSortFromValue(data, 0, data.length - 1, pivot);
	}

	/**
	 * Sorts the upper part of an array of ints having values equal or above pivot into a new array using the QuickSort algorithm.
	 * 
	 * @param data A int array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return ArrayAndIndexBI containing the sorted array and the index of first element equal or greater than pivot.
	 */
	public static ArrayAndIndexII sortFromValue(int[] data, int pivot) {
		ArrayAndIndexII aai = new ArrayAndIndexII();
		aai.setArray( data.clone() );
		aai.setIndex( QuickSort.quickSortFromValue(aai.getArray(), 0, aai.getArray().length - 1, pivot) );
		return aai;
	}

	/* This section contains the methods for long array. */

	/**
	 * Sort array of longs using the QuickSort algorithm.
	 * 
	 * @param arr A long array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 */
	public static void quickSort(long arr[], int left, int right) {
	      int i = left, j = right;
	      long tmp;
	      long pivot = arr[(left + right) / 2];
	 
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
	 * Sort array of longs using the QuickSort algorithm.
	 * 
	 * @param arr A long array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void quickSortTop(long arr[], int left, int right, int from) {
	      int i = left, j = right;
	      long tmp;
	      long pivot = arr[(left + right) / 2];
	 
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
	 * Sort array of longs using the QuickSort algorithm.
	 * 
	 * @param arr A long array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int quickSortFromValue(long arr[], int left, int right, int pivot) {
	      int i = left, j = right;
	      long tmp;
	 
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
	      if ( i < right )
	            quickSort( arr, i, right );
	      return i;
	}

	/**
	 * Sorts an array of longs into itself using the QuickSort algorithm.
	 * 
	 * @param data A long array.
	 */
	public static void sortInItself(long[] data) {
		QuickSort.quickSort(data, 0, data.length - 1);
	}

	/**
	 * Sorts an array of longs into a new array using the QuickSort algorithm.
	 * 
	 * @param data A long array.
	 */
	public static long[] sort(long[] data) {
		long[] newData = data.clone();
		QuickSort.quickSort(newData, 0, newData.length - 1);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of longs into itself using the QuickSort algorithm.
	 * 
	 * @param data A long array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void sortTopInItself(long[] data, int from) {
		QuickSort.quickSortTop(data, 0, data.length - 1, from);
	}

	/**
	 * Sorts the upper part of an array of longs into a new array using the QuickSort algorithm.
	 * 
	 * @param data A long array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static long[] sortTop(long[] data, int from) {
		long[] newData = data.clone();
		QuickSort.quickSortTop(newData, 0, newData.length - 1, from);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of longs having values equal or above pivot into itself using the QuickSort algorithm.
	 * 
	 * @param data A long array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int sortFromValueInItself(long[] data, int pivot) {
		return QuickSort.quickSortFromValue(data, 0, data.length - 1, pivot);
	}

	/**
	 * Sorts the upper part of an array of longs having values equal or above pivot into a new array using the QuickSort algorithm.
	 * 
	 * @param data A long array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return ArrayAndIndexBI containing the sorted array and the index of first element equal or greater than pivot.
	 */
	public static ArrayAndIndexLI sortFromValue(long[] data, int pivot) {
		ArrayAndIndexLI aai = new ArrayAndIndexLI();
		aai.setArray( data.clone() );
		aai.setIndex( QuickSort.quickSortFromValue(aai.getArray(), 0, aai.getArray().length - 1, pivot) );
		return aai;
	}

	/* This section contains the methods for float array. */

	/**
	 * Sort array of floats using the QuickSort algorithm.
	 * 
	 * @param arr A float array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
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

	/**
	 * Sort array of floats using the QuickSort algorithm.
	 * 
	 * @param arr A float array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param from The minimum bound of array part to sort.
	 */
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
	 * Sort array of floats using the QuickSort algorithm.
	 * 
	 * @param arr A float array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int quickSortFromValue(float arr[], int left, int right, int pivot) {
	      int i = left, j = right;
	      float tmp;
	 
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
	      if ( i < right )
	            quickSort( arr, i, right );
	      return i;
	}

	/**
	 * Sorts an array of floats into itself using the QuickSort algorithm.
	 * 
	 * @param data A float array.
	 */
	public static void sortInItself(float[] data) {
		QuickSort.quickSort(data, 0, data.length - 1);
	}

	/**
	 * Sorts an array of floats into a new array using the QuickSort algorithm.
	 * 
	 * @param data A float array.
	 */
	public static float[] sort(float[] data) {
		float[] newData = data.clone();
		QuickSort.quickSort(newData, 0, newData.length - 1);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of floats into itself using the QuickSort algorithm.
	 * 
	 * @param data A float array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void sortTopInItself(float[] data, int from) {
		QuickSort.quickSortTop(data, 0, data.length - 1, from);
	}

	/**
	 * Sorts the upper part of an array of floats into a new array using the QuickSort algorithm.
	 * 
	 * @param data A float array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static float[] sortTop(float[] data, int from) {
		float[] newData = data.clone();
		QuickSort.quickSortTop(newData, 0, newData.length - 1, from);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of floats having values equal or above pivot into itself using the QuickSort algorithm.
	 * 
	 * @param data A float array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int sortFromValueInItself(float[] data, int pivot) {
		return QuickSort.quickSortFromValue(data, 0, data.length - 1, pivot);
	}

	/**
	 * Sorts the upper part of an array of floats having values equal or above pivot into a new array using the QuickSort algorithm.
	 * 
	 * @param data A float array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return ArrayAndIndexBI containing the sorted array and the index of first element equal or greater than pivot.
	 */
	public static ArrayAndIndexFI sortFromValue(float[] data, int pivot) {
		ArrayAndIndexFI aai = new ArrayAndIndexFI();
		aai.setArray( data.clone() );
		aai.setIndex( QuickSort.quickSortFromValue(aai.getArray(), 0, aai.getArray().length - 1, pivot) );
		return aai;
	}

	/* This section contains the methods for double array. */

	/**
	 * Sort array of doubles using the QuickSort algorithm.
	 * 
	 * @param arr A double array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 */
	public static void quickSort(double arr[], int left, int right) {
	      int i = left, j = right;
	      double tmp;
	      double pivot = arr[(left + right) / 2];
	 
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
	 * Sort array of doubles using the QuickSort algorithm.
	 * 
	 * @param arr A double array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void quickSortTop(double arr[], int left, int right, int from) {
	      int i = left, j = right;
	      double tmp;
	      double pivot = arr[(left + right) / 2];
	 
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
	 * Sort array of doubles using the QuickSort algorithm.
	 * 
	 * @param arr A double array.
	 * @param left The current lower bound.
	 * @param right The current upper bound.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int quickSortFromValue(double arr[], int left, int right, int pivot) {
	      int i = left, j = right;
	      double tmp;
	 
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
	      if ( i < right )
	            quickSort( arr, i, right );
	      return i;
	}

	/**
	 * Sorts an array of doubles into itself using the QuickSort algorithm.
	 * 
	 * @param data A double array.
	 */
	public static void sortInItself(double[] data) {
		QuickSort.quickSort(data, 0, data.length - 1);
	}

	/**
	 * Sorts an array of doubles into a new array using the QuickSort algorithm.
	 * 
	 * @param data A double array.
	 */
	public static double[] sort(double[] data) {
		double[] newData = data.clone();
		QuickSort.quickSort(newData, 0, newData.length - 1);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of doubles into itself using the QuickSort algorithm.
	 * 
	 * @param data A double array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static void sortTopInItself(double[] data, int from) {
		QuickSort.quickSortTop(data, 0, data.length - 1, from);
	}

	/**
	 * Sorts the upper part of an array of doubles into a new array using the QuickSort algorithm.
	 * 
	 * @param data A double array.
	 * @param from The minimum bound of array part to sort.
	 */
	public static double[] sortTop(double[] data, int from) {
		double[] newData = data.clone();
		QuickSort.quickSortTop(newData, 0, newData.length - 1, from);
		return newData;
	}

	/**
	 * Sorts the upper part of an array of doubles having values equal or above pivot into itself using the QuickSort algorithm.
	 * 
	 * @param data A double array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return The index of first element equal or greater than pivot.
	 */
	public static int sortFromValueInItself(double[] data, int pivot) {
		return QuickSort.quickSortFromValue(data, 0, data.length - 1, pivot);
	}

	/**
	 * Sorts the upper part of an array of doubles having values equal or above pivot into a new array using the QuickSort algorithm.
	 * 
	 * @param data A double array.
	 * @param pivot The minimum value in the array to sort equal or above it.
	 * @return ArrayAndIndexBI containing the sorted array and the index of first element equal or greater than pivot.
	 */
	public static ArrayAndIndexDI sortFromValue(double[] data, int pivot) {
		ArrayAndIndexDI aai = new ArrayAndIndexDI();
		aai.setArray( data.clone() );
		aai.setIndex( QuickSort.quickSortFromValue(aai.getArray(), 0, aai.getArray().length - 1, pivot) );
		return aai;
	}

}
