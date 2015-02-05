package org.embl.cca.dviewer.ui.editors.utils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.ByteDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.LongDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.ShortDataset;
import org.eclipse.swt.graphics.Rectangle;

/**
 * The <code>PHA</code> class is for applying a PHA effect to an image.
 * PHA effect is achieved by replacing value(x,y) with
 * max(f(xi,yi)*g(xi,yi)) for each (xi,yi) within kernel around (x,y).
 * <p>
 *
 * @author  Gábor Náray
 * @version 1.0 10/04/2014
 * @since   20140410
 */
public class PHA {

	public static final String featureShortName = PHA.class.getSimpleName();
	public static final String featureFullName = "Point Highlighting Alghorithm";
	public static final String featureIdentifierName = featureShortName.toLowerCase();
	public static final int radiusDefault = 2;

	protected int radius;
	protected DoubleDataset kernelSet;
	protected double validValueMin;
	protected final Boolean kernelLock = new Boolean(true); //This is a lock, has no value

	public PHA() {
		this(0, 0);
	}

	public PHA(final int radius) {
		this(radius, 0);
	}

	public PHA(final int radius, final double validValueMin) {
		setRadius(radius);
		setValidValueMin(validValueMin);
	}

	public void setRadius(final int radius ) {
		Assert.isLegal(radius>=0);
		synchronized (kernelLock) {
			if( this.radius != radius ) {
				kernelSet = radius == 0 ? null : calculateGaussKernel(radius);
				this.radius = radius;
			}
		}
	}

	public int getRadius() {
		return radius;
	}

	public double getValidValueMin() {
		return validValueMin;
	}

	public void setValidValueMin( double validValueMin ) {
		synchronized (kernelLock) {
			if( this.validValueMin != validValueMin ) {
				this.validValueMin = validValueMin;
			}
		}
	}

	public int getKernelLength() {
		return getKernelLength(radius);
	}

	public static int getKernelLength(final int radius) {
		Assert.isLegal(radius>0);
		return radius * 2 + 1;
	}

	public static DoubleDataset calculateGaussKernel(final int radius) {
		Assert.isLegal(radius>0);
		final int kernelLength = getKernelLength(radius);
		final DoubleDataset kernelSet = new DoubleDataset(kernelLength, kernelLength);
		final double kernel[] = kernelSet.getData();
		final double amax = 4; //By experience. In equation: 1 / ( c * Math.sqrt( 2*Math.PI ) )
		final double amin = 1;
		final double b = 0; // mean (mu)
//		final double c = 2.2; // standard deviation (sigma), should depend on radius
		final double c = Math.abs( radius - b ) / Math.sqrt( 2 * Math.log( amax/amin )) ; // standard deviation (sigma), should depend on radius
		final double c2m2 = 2 * Math.pow(c, 2);
		int k = 0;
		for (int j = 0; j < kernelLength; j++) {
			final double j2 = Math.pow(j - radius, 2);
			for (int i = 0; i < kernelLength; i++) {
				kernel[k++] = amax * Math.pow(Math.E,
					-Math.pow(Math.sqrt(Math.pow(i - radius, 2) + j2)
						- b, 2) / c2m2);
			}
		}
		final double kernelMaxValue = kernelSet.get(radius, radius); // center is max
		k = 0;
		for (int j = 0; j < kernelLength; j++) {
			for (int i = 0; i < kernelLength; i++) {
				kernel[k++] /= kernelMaxValue;
			}
		}
		return kernelSet;
	}

	public IDataset applyPHA(final IDataset ds, final Rectangle rectangle, final IProgressMonitor monitor) {
		final long t0 = System.nanoTime();
		final IDataset result;
		final int threadAmount = Runtime.getRuntime().availableProcessors();
		if( ds instanceof ByteDataset)
			result = applyPHA((ByteDataset)ds, rectangle, threadAmount, monitor);
		else if( ds instanceof ShortDataset)
			result = applyPHA((ShortDataset)ds, rectangle, threadAmount, monitor);
		else if( ds instanceof IntegerDataset) {
			result = applyPHA((IntegerDataset)ds, rectangle, threadAmount, monitor);
		} else if( ds instanceof LongDataset)
			result = applyPHA((LongDataset)ds, rectangle, threadAmount, monitor);
		else if( ds instanceof FloatDataset)
			result = applyPHA((FloatDataset)ds, rectangle, threadAmount, monitor);
		else if( ds instanceof DoubleDataset)
			result = applyPHA((DoubleDataset)ds, rectangle, threadAmount, monitor);
		else
			throw new IllegalArgumentException(new StringBuilder(PSF.featureName)
				.append(" is not implemented for this image containing values of class ")
				.append(ds.elementClass().toString()).toString() );
		final long t1 = System.nanoTime();
		System.out.println( "DEBUG: applyPHA took [msec]= " + ( t1 - t0 ) / 1000000 );
		return result;
	}

	public static Rectangle getDataSetRectangle(final IDataset dataSet) {
		return new Rectangle(0, 0, dataSet.getShape()[1], dataSet.getShape()[0]);
	}

	/**
	 * Administrates the given worked in the specified monitor,
	 * adding it to workedPercentPrev (it has % unit),
	 * and returns the new workedPercentPrev value.
	 * @param monitor the monitor where the worked is administrated
	 * @param workedPercentPrev the previous value of total worked (it has % unit)
	 * @param worked the worked part (if it is 1, then 100 is administrated)
	 * @return the new workedPercentPrev value
	 */
	protected static double administrateWork(final IProgressMonitor monitor, double workedPercentPrev, final double worked) {
		workedPercentPrev += 100 * worked;
		if( workedPercentPrev >= 1 ) {
			final int reportWorked = (int)workedPercentPrev;
			monitor.worked(reportWorked);
			workedPercentPrev -= reportWorked;
		}
		return workedPercentPrev;
	}

	public ByteDataset applyPHA(final ByteDataset dataSet, final Rectangle targetRectangle, final int threadAmount, final IProgressMonitor monitor) {
		final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		thisMonitor.beginTask("Applying PHA", 100); //Counting in percent
		try {
			synchronized (kernelLock) {
				if(radius == 0)
					return (ByteDataset)dataSet.clone();
				final ByteDataset newDataSet = new ByteDataset(dataSet.getShape());
				final byte[] dataSetValues = dataSet.getData();
				final byte[] newDataSetValues = newDataSet.getData();
				final byte valueMin = (byte) validValueMin;
				newDataSet.setName(dataSet.getName());
				newDataSet.setMetadata(dataSet.getMetadata());
				final int kernelLength = getKernelLength(radius);
				final double kernel[] = kernelSet.getData();
				final Rectangle dataSetRectangle = getDataSetRectangle(dataSet);
				targetRectangle.intersect(dataSetRectangle);
				final int dataSetWidth = dataSet.getShape()[1];
				final double[] workedTotal = {0};
				final class PHAThread extends Thread {
					final int threadIndex;
					public PHAThread(final int threadIndex) {
						this.threadIndex = threadIndex;
					}
					@Override
					public void run() {
						int xySup = targetRectangle.width * targetRectangle.height;
						for( int xy = threadIndex; xy < xySup; xy+=threadAmount ) {
							//(x,y) dataSet coordinates, to calculate value of
							final int x = targetRectangle.x + (xy % targetRectangle.width);
							final int y = targetRectangle.y + (xy / targetRectangle.width);
							final int valueXY = y * dataSetWidth + x;
							if( dataSetValues[ valueXY ] < validValueMin ) //Not touching special values
								newDataSetValues[ valueXY ] = dataSetValues[ valueXY ];
							else {
								final Rectangle kernelRectangle = new Rectangle(x - radius, y - radius, kernelLength, kernelLength).intersection(dataSetRectangle);
								int valuesXY = kernelRectangle.y * dataSetWidth + kernelRectangle.x; //topLeftOffset
								final int dValuesXY = dataSetWidth - kernelRectangle.width;
								final int iMin = kernelRectangle.x - (x - radius);
								final int jMin = kernelRectangle.y - (y - radius); //(i,j) kernel coordinates
								final int iSup = iMin + kernelRectangle.width;
								final int jSup = jMin + kernelRectangle.height;
								byte valueMax = valueMin;
								int kernelI = jMin * kernelLength + iMin;
								final int dKernelI = kernelLength - kernelRectangle.width;
								for( int j = jMin; j < jSup; valuesXY += dValuesXY, kernelI+=dKernelI, j++ ) {
									for( int i = iMin; i < iSup; valuesXY++, i++ ) {
										//Protecting the gaps (besides this algorithm does no good for negative values)
										if( dataSetValues[ valuesXY ] < validValueMin )
											continue;
										byte valueIJ = (byte)(kernel[kernelI++] * dataSetValues[ valuesXY ]);
										if( valueIJ > valueMax )
											valueMax = valueIJ;
									}
								}
								newDataSetValues[ valueXY ] = valueMax;
							}
							if( thisMonitor.isCanceled() )
								return;
							synchronized (thisMonitor) {
								workedTotal[0] = administrateWork(thisMonitor, workedTotal[0], 1./(double)xySup);
							}
						}
					}
				};
				final PHAThread threads[] = new PHAThread[threadAmount];
				for( int i = 0; i < threads.length; i++ ) {
					threads[i] = new PHAThread(i);
					threads[i].start();
				}
				for( int i = 0; i < threads.length; i++ )
					try {
						threads[i].join();
					} catch (final InterruptedException e) {
						System.out.println("DEBUG: applyPHA thread (" + threads[i].threadIndex + ") interrupted");
						thisMonitor.setCanceled(true);
						break;
					}
				System.out.println("DEBUG: applyPHA monitor.isCanceled? " + thisMonitor.isCanceled());
				return thisMonitor.isCanceled() ? null : newDataSet;
			}
		} finally {
			thisMonitor.done();
		}
	}

	public ShortDataset applyPHA(final ShortDataset dataSet, final Rectangle targetRectangle, final int threadAmount, final IProgressMonitor monitor) {
		final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		thisMonitor.beginTask("Applying PHA", 100); //Counting in percent
		try {
			synchronized (kernelLock) {
				if(radius == 0)
					return (ShortDataset)dataSet.clone();
				final ShortDataset newDataSet = new ShortDataset(dataSet.getShape());
				final short[] dataSetValues = dataSet.getData();
				final short[] newDataSetValues = newDataSet.getData();
				final short valueMin = (short) validValueMin;
				newDataSet.setName(dataSet.getName());
				newDataSet.setMetadata(dataSet.getMetadata());
				final int kernelLength = getKernelLength(radius);
				final double kernel[] = kernelSet.getData();
				final Rectangle dataSetRectangle = getDataSetRectangle(dataSet);
				targetRectangle.intersect(dataSetRectangle);
				final int dataSetWidth = dataSet.getShape()[1];
				final double[] workedTotal = {0};
				final class PHAThread extends Thread {
					final int threadIndex;
					public PHAThread(final int threadIndex) {
						this.threadIndex = threadIndex;
					}
					@Override
					public void run() {
						int xySup = targetRectangle.width * targetRectangle.height;
						for( int xy = threadIndex; xy < xySup; xy+=threadAmount ) {
							//(x,y) dataSet coordinates, to calculate value of
							final int x = targetRectangle.x + (xy % targetRectangle.width);
							final int y = targetRectangle.y + (xy / targetRectangle.width);
							final int valueXY = y * dataSetWidth + x;
							if( dataSetValues[ valueXY ] < validValueMin ) //Not touching special values
								newDataSetValues[ valueXY ] = dataSetValues[ valueXY ];
							else {
								final Rectangle kernelRectangle = new Rectangle(x - radius, y - radius, kernelLength, kernelLength).intersection(dataSetRectangle);
								int valuesXY = kernelRectangle.y * dataSetWidth + kernelRectangle.x; //topLeftOffset
								final int dValuesXY = dataSetWidth - kernelRectangle.width;
								final int iMin = kernelRectangle.x - (x - radius);
								final int jMin = kernelRectangle.y - (y - radius); //(i,j) kernel coordinates
								final int iSup = iMin + kernelRectangle.width;
								final int jSup = jMin + kernelRectangle.height;
								short valueMax = valueMin;
								int kernelI = jMin * kernelLength + iMin;
								final int dKernelI = kernelLength - kernelRectangle.width;
								for( int j = jMin; j < jSup; valuesXY += dValuesXY, kernelI+=dKernelI, j++ ) {
									for( int i = iMin; i < iSup; valuesXY++, i++ ) {
										//Protecting the gaps (besides this algorithm does no good for negative values)
										if( dataSetValues[ valuesXY ] < validValueMin )
											continue;
										short valueIJ = (short)(kernel[kernelI++] * dataSetValues[ valuesXY ]);
										if( valueIJ > valueMax )
											valueMax = valueIJ;
									}
								}
								newDataSetValues[ valueXY ] = valueMax;
							}
							if( thisMonitor.isCanceled() )
								return;
							synchronized (thisMonitor) {
								workedTotal[0] = administrateWork(thisMonitor, workedTotal[0], 1./(double)xySup);
							}
						}
					}
				};
				final PHAThread threads[] = new PHAThread[threadAmount];
				for( int i = 0; i < threads.length; i++ ) {
					threads[i] = new PHAThread(i);
					threads[i].start();
				}
				for( int i = 0; i < threads.length; i++ )
					try {
						threads[i].join();
					} catch (final InterruptedException e) {
						System.out.println("DEBUG: applyPHA thread (" + threads[i].threadIndex + ") interrupted");
						thisMonitor.setCanceled(true);
						break;
					}
				System.out.println("DEBUG: applyPHA monitor.isCanceled? " + thisMonitor.isCanceled());
				return thisMonitor.isCanceled() ? null : newDataSet;
			}
		} finally {
			thisMonitor.done();
		}
	}

	public IntegerDataset applyPHA(final IntegerDataset dataSet, final Rectangle targetRectangle, final int threadAmount, final IProgressMonitor monitor) {
		final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		thisMonitor.beginTask("Applying PHA", 100); //Counting in percent
		try {
			synchronized (kernelLock) {
				if(radius == 0)
					return (IntegerDataset)dataSet.clone();
				final IntegerDataset newDataSet = new IntegerDataset(dataSet.getShape());
				final int[] dataSetValues = dataSet.getData();
				final int[] newDataSetValues = newDataSet.getData();
				final int valueMin = (int) validValueMin;
				newDataSet.setName(dataSet.getName());
				newDataSet.setMetadata(dataSet.getMetadata());
				final int kernelLength = getKernelLength(radius);
				final double kernel[] = kernelSet.getData();
				final Rectangle dataSetRectangle = getDataSetRectangle(dataSet);
				targetRectangle.intersect(dataSetRectangle);
				final int dataSetWidth = dataSet.getShape()[1];
				final double[] workedTotal = {0};
				final class PHAThread extends Thread {
					final int threadIndex;
					public PHAThread(final int threadIndex) {
						this.threadIndex = threadIndex;
					}
					@Override
					public void run() {
						final int xySup = targetRectangle.width * targetRectangle.height;
						for( int xy = threadIndex; xy < xySup; xy+=threadAmount ) {
							//(x,y) dataSet coordinates, to calculate value of
							final int x = targetRectangle.x + (xy % targetRectangle.width);
							final int y = targetRectangle.y + (xy / targetRectangle.width);
							final int valueXY = y * dataSetWidth + x;
							if( dataSetValues[ valueXY ] < validValueMin ) //Not touching special values
								newDataSetValues[ valueXY ] = dataSetValues[ valueXY ];
							else {
								final Rectangle kernelRectangle = new Rectangle(x - radius, y - radius, kernelLength, kernelLength).intersection(dataSetRectangle);
								int valuesXY = kernelRectangle.y * dataSetWidth + kernelRectangle.x; //topLeftOffset
								final int dValuesXY = dataSetWidth - kernelRectangle.width;
								final int iMin = kernelRectangle.x - (x - radius);
								final int jMin = kernelRectangle.y - (y - radius); //(i,j) kernel coordinates
								final int iSup = iMin + kernelRectangle.width;
								final int jSup = jMin + kernelRectangle.height;
								int kernelI = jMin * kernelLength + iMin;
								final int dKernelI = kernelLength - kernelRectangle.width;
								int valueMax = valueMin;
								for( int j = jMin; j < jSup; valuesXY += dValuesXY, kernelI+=dKernelI, j++ ) {
									for( int i = iMin; i < iSup; valuesXY++, i++ ) {
										//Protecting the gaps (besides this algorithm does no good for negative values)
										if( dataSetValues[ valuesXY ] < validValueMin )
											continue;
										int valueIJ = (int)(kernel[kernelI++] * dataSetValues[ valuesXY ]);
										if( valueIJ > valueMax )
											valueMax = valueIJ;
									}
								}
								newDataSetValues[ valueXY ] = valueMax;
							}
							if( thisMonitor.isCanceled() )
								return;
							synchronized (thisMonitor) {
								workedTotal[0] = administrateWork(thisMonitor, workedTotal[0], 1./(double)xySup);
							}
						}
						System.out.println("DEBUG: PHAThread (" + threadIndex + ") finished");
					}
				};
				final PHAThread threads[] = new PHAThread[threadAmount];
				for( int i = 0; i < threads.length; i++ ) {
					threads[i] = new PHAThread(i);
					threads[i].start();
				}
				for( int i = 0; i < threads.length; i++ )
					try {
						threads[i].join();
					} catch (final InterruptedException e) {
						System.out.println("DEBUG: applyPHA thread (" + threads[i].threadIndex + ") interrupted");
						thisMonitor.setCanceled(true);
						break;
					}
				System.out.println("DEBUG: applyPHA monitor.isCanceled? " + thisMonitor.isCanceled());
				return thisMonitor.isCanceled() ? null : newDataSet;
			}
		} finally {
			thisMonitor.done();
		}
	}

	public LongDataset applyPHA(final LongDataset dataSet, final Rectangle targetRectangle, final int threadAmount, final IProgressMonitor monitor) {
		final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		thisMonitor.beginTask("Applying PHA", 100); //Counting in percent
		try {
			synchronized (kernelLock) {
				if(radius == 0)
					return (LongDataset)dataSet.clone();
				final LongDataset newDataSet = new LongDataset(dataSet.getShape());
				final long[] dataSetValues = dataSet.getData();
				final long[] newDataSetValues = newDataSet.getData();
				final long valueMin = (long) validValueMin;
				newDataSet.setName(dataSet.getName());
				newDataSet.setMetadata(dataSet.getMetadata());
				final int kernelLength = getKernelLength(radius);
				final double kernel[] = kernelSet.getData();
				final Rectangle dataSetRectangle = getDataSetRectangle(dataSet);
				targetRectangle.intersect(dataSetRectangle);
				final int dataSetWidth = dataSet.getShape()[1];
				final double[] workedTotal = {0};
				final class PHAThread extends Thread {
					final int threadIndex;
					public PHAThread(final int threadIndex) {
						this.threadIndex = threadIndex;
					}
					@Override
					public void run() {
						int xySup = targetRectangle.width * targetRectangle.height;
						for( int xy = threadIndex; xy < xySup; xy+=threadAmount ) {
							//(x,y) dataSet coordinates, to calculate value of
							final int x = targetRectangle.x + (xy % targetRectangle.width);
							final int y = targetRectangle.y + (xy / targetRectangle.width);
							final int valueXY = y * dataSetWidth + x;
							if( dataSetValues[ valueXY ] < validValueMin ) //Not touching special values
								newDataSetValues[ valueXY ] = dataSetValues[ valueXY ];
							else {
								final Rectangle kernelRectangle = new Rectangle(x - radius, y - radius, kernelLength, kernelLength).intersection(dataSetRectangle);
								int valuesXY = kernelRectangle.y * dataSetWidth + kernelRectangle.x; //topLeftOffset
								final int dValuesXY = dataSetWidth - kernelRectangle.width;
								final int iMin = kernelRectangle.x - (x - radius);
								final int jMin = kernelRectangle.y - (y - radius); //(i,j) kernel coordinates
								final int iSup = iMin + kernelRectangle.width;
								final int jSup = jMin + kernelRectangle.height;
								long valueMax = valueMin;
								int kernelI = jMin * kernelLength + iMin;
								final int dKernelI = kernelLength - kernelRectangle.width;
								for( int j = jMin; j < jSup; valuesXY += dValuesXY, kernelI+=dKernelI, j++ ) {
									for( int i = iMin; i < iSup; valuesXY++, i++ ) {
										//Protecting the gaps (besides this algorithm does no good for negative values)
										if( dataSetValues[ valuesXY ] < validValueMin )
											continue;
										long valueIJ = (long)(kernel[kernelI++] * dataSetValues[ valuesXY ]);
										if( valueIJ > valueMax )
											valueMax = valueIJ;
									}
								}
								newDataSetValues[ valueXY ] = valueMax;
							}
							if( thisMonitor.isCanceled() )
								return;
							synchronized (thisMonitor) {
								workedTotal[0] = administrateWork(thisMonitor, workedTotal[0], 1./(double)xySup);
							}
						}
					}
				};
				final PHAThread threads[] = new PHAThread[threadAmount];
				for( int i = 0; i < threads.length; i++ ) {
					threads[i] = new PHAThread(i);
					threads[i].start();
				}
				for( int i = 0; i < threads.length; i++ )
					try {
						threads[i].join();
					} catch (final InterruptedException e) {
						System.out.println("DEBUG: applyPHA thread (" + threads[i].threadIndex + ") interrupted");
						thisMonitor.setCanceled(true);
						break;
					}
				System.out.println("DEBUG: applyPHA monitor.isCanceled? " + thisMonitor.isCanceled());
				return thisMonitor.isCanceled() ? null : newDataSet;
			}
		} finally {
			thisMonitor.done();
		}
	}

	public FloatDataset applyPHA(final FloatDataset dataSet, final Rectangle targetRectangle, final int threadAmount, final IProgressMonitor monitor) {
		final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		thisMonitor.beginTask("Applying PHA", 100); //Counting in percent
		try {
			synchronized (kernelLock) {
				if(radius == 0)
					return (FloatDataset)dataSet.clone();
				final FloatDataset newDataSet = new FloatDataset(dataSet.getShape());
				final float[] dataSetValues = dataSet.getData();
				final float[] newDataSetValues = newDataSet.getData();
				final float valueMin = (float) validValueMin;
				newDataSet.setName(dataSet.getName());
				newDataSet.setMetadata(dataSet.getMetadata());
				final int kernelLength = getKernelLength(radius);
				final double kernel[] = kernelSet.getData();
				final Rectangle dataSetRectangle = getDataSetRectangle(dataSet);
				targetRectangle.intersect(dataSetRectangle);
				final int dataSetWidth = dataSet.getShape()[1];
				final double[] workedTotal = {0};
				final class PHAThread extends Thread {
					final int threadIndex;
					public PHAThread(final int threadIndex) {
						this.threadIndex = threadIndex;
					}
					@Override
					public void run() {
						int xySup = targetRectangle.width * targetRectangle.height;
						for( int xy = threadIndex; xy < xySup; xy+=threadAmount ) {
							//(x,y) dataSet coordinates, to calculate value of
							final int x = targetRectangle.x + (xy % targetRectangle.width);
							final int y = targetRectangle.y + (xy / targetRectangle.width);
							final int valueXY = y * dataSetWidth + x;
							if( dataSetValues[ valueXY ] < validValueMin ) //Not touching special values
								newDataSetValues[ valueXY ] = dataSetValues[ valueXY ];
							else {
								final Rectangle kernelRectangle = new Rectangle(x - radius, y - radius, kernelLength, kernelLength).intersection(dataSetRectangle);
								int valuesXY = kernelRectangle.y * dataSetWidth + kernelRectangle.x; //topLeftOffset
								final int dValuesXY = dataSetWidth - kernelRectangle.width;
								final int iMin = kernelRectangle.x - (x - radius);
								final int jMin = kernelRectangle.y - (y - radius); //(i,j) kernel coordinates
								final int iSup = iMin + kernelRectangle.width;
								final int jSup = jMin + kernelRectangle.height;
								float valueMax = valueMin;
								int kernelI = jMin * kernelLength + iMin;
								final int dKernelI = kernelLength - kernelRectangle.width;
								for( int j = jMin; j < jSup; valuesXY += dValuesXY, kernelI+=dKernelI, j++ ) {
									for( int i = iMin; i < iSup; valuesXY++, i++ ) {
										//Protecting the gaps (besides this algorithm does no good for negative values)
										if( dataSetValues[ valuesXY ] < validValueMin )
											continue;
										float valueIJ = (float)(kernel[kernelI++] * dataSetValues[ valuesXY ]);
										if( valueIJ > valueMax )
											valueMax = valueIJ;
									}
								}
								newDataSetValues[ valueXY ] = valueMax;
							}
							if( thisMonitor.isCanceled() )
								return;
							synchronized (thisMonitor) {
								workedTotal[0] = administrateWork(thisMonitor, workedTotal[0], 1./(double)xySup);
							}
						}
					}
				};
				final PHAThread threads[] = new PHAThread[threadAmount];
				for( int i = 0; i < threads.length; i++ ) {
					threads[i] = new PHAThread(i);
					threads[i].start();
				}
				for( int i = 0; i < threads.length; i++ )
					try {
						threads[i].join();
					} catch (final InterruptedException e) {
						System.out.println("DEBUG: applyPHA thread (" + threads[i].threadIndex + ") interrupted");
						thisMonitor.setCanceled(true);
						break;
					}
				System.out.println("DEBUG: applyPHA monitor.isCanceled? " + thisMonitor.isCanceled());
				return thisMonitor.isCanceled() ? null : newDataSet;
			}
		} finally {
			thisMonitor.done();
		}
	}

	public DoubleDataset applyPHA(final DoubleDataset dataSet, final Rectangle targetRectangle, final int threadAmount, final IProgressMonitor monitor) {
		final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		thisMonitor.beginTask("Applying PHA", 100); //Counting in percent
		try {
			synchronized (kernelLock) {
				if(radius == 0)
					return (DoubleDataset)dataSet.clone();
				final DoubleDataset newDataSet = new DoubleDataset(dataSet.getShape());
				final double[] dataSetValues = dataSet.getData();
				final double[] newDataSetValues = newDataSet.getData();
				final double valueMin = (double) validValueMin;
				newDataSet.setName(dataSet.getName());
				newDataSet.setMetadata(dataSet.getMetadata());
				final int kernelLength = getKernelLength(radius);
				final double kernel[] = kernelSet.getData();
				final Rectangle dataSetRectangle = getDataSetRectangle(dataSet);
				targetRectangle.intersect(dataSetRectangle);
				final int dataSetWidth = dataSet.getShape()[1];
				final double[] workedTotal = {0};
				final class PHAThread extends Thread {
					final int threadIndex;
					public PHAThread(final int threadIndex) {
						this.threadIndex = threadIndex;
					}
					@Override
					public void run() {
						int xySup = targetRectangle.width * targetRectangle.height;
						for( int xy = threadIndex; xy < xySup; xy+=threadAmount ) {
							//(x,y) dataSet coordinates, to calculate value of
							final int x = targetRectangle.x + (xy % targetRectangle.width);
							final int y = targetRectangle.y + (xy / targetRectangle.width);
							final int valueXY = y * dataSetWidth + x;
							if( dataSetValues[ valueXY ] < validValueMin ) //Not touching special values
								newDataSetValues[ valueXY ] = dataSetValues[ valueXY ];
							else {
								final Rectangle kernelRectangle = new Rectangle(x - radius, y - radius, kernelLength, kernelLength).intersection(dataSetRectangle);
								int valuesXY = kernelRectangle.y * dataSetWidth + kernelRectangle.x; //topLeftOffset
								final int dValuesXY = dataSetWidth - kernelRectangle.width;
								final int iMin = kernelRectangle.x - (x - radius);
								final int jMin = kernelRectangle.y - (y - radius); //(i,j) kernel coordinates
								final int iSup = iMin + kernelRectangle.width;
								final int jSup = jMin + kernelRectangle.height;
								double valueMax = valueMin;
								int kernelI = jMin * kernelLength + iMin;
								final int dKernelI = kernelLength - kernelRectangle.width;
								for( int j = jMin; j < jSup; valuesXY += dValuesXY, kernelI+=dKernelI, j++ ) {
									for( int i = iMin; i < iSup; valuesXY++, i++ ) {
										//Protecting the gaps (besides this algorithm does no good for negative values)
										if( dataSetValues[ valuesXY ] < validValueMin )
											continue;
										double valueIJ = (double)(kernel[kernelI++] * dataSetValues[ valuesXY ]);
										if( valueIJ > valueMax )
											valueMax = valueIJ;
									}
								}
								newDataSetValues[ valueXY ] = valueMax;
							}
							if( thisMonitor.isCanceled() )
								return;
							synchronized (thisMonitor) {
								workedTotal[0] = administrateWork(thisMonitor, workedTotal[0], 1./(double)xySup);
							}
						}
					}
				};
				final PHAThread threads[] = new PHAThread[threadAmount];
				for( int i = 0; i < threads.length; i++ ) {
					threads[i] = new PHAThread(i);
					threads[i].start();
				}
				for( int i = 0; i < threads.length; i++ )
					try {
						threads[i].join();
					} catch (final InterruptedException e) {
						System.out.println("DEBUG: applyPHA thread (" + threads[i].threadIndex + ") interrupted");
						thisMonitor.setCanceled(true);
						break;
					}
				System.out.println("DEBUG: applyPHA monitor.isCanceled? " + thisMonitor.isCanceled());
				return thisMonitor.isCanceled() ? null : newDataSet;
			}
		} finally {
			thisMonitor.done();
		}
	}

}
