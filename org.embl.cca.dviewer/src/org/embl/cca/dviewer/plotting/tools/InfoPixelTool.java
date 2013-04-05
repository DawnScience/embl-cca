/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embl.cca.dviewer.plotting.tools;

import java.util.Collection;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import org.dawnsci.plotting.tools.Vector3dutil;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;

public class InfoPixelTool /*extends AbstractToolPage*/ implements IROIListener, IRegionListener/*, MouseListener*/ {
	protected static final double qScaleDefault = 2*Math.PI; //2*PI is used at DLS

	protected final static Logger logger = LoggerFactory.getLogger(InfoPixelTool.class);
	protected double qScale;
	protected MouseMotionListener hairMouseMotionListener; 

	protected IPlottingSystem plotSystem; //From Abstract

	protected IRegion xHair, yHair;

	public double xValues [] = new double[1];
	public double yValues [] = new double[1];

	public InfoPixelTool(IPlottingSystem system, double qScale) {
		this.plotSystem = system;
		this.qScale = qScale;
		hairMouseMotionListener = new MouseMotionListener.Stub() {
			/**
			 * @see org.eclipse.draw2d.MouseMotionListener#mouseExited(MouseEvent)
			 */
			@Override
			public void mouseExited(MouseEvent me) {
//				Point loc = me.getLocation();
//				System.out.println("hair exited: " + loc.toString());
				setVisible(false);
			}
			/**
			 * @see org.eclipse.draw2d.MouseMotionListener#mouseEntered(MouseEvent)
			 */
			@Override
			public void mouseEntered(MouseEvent me) {
//				Point loc = me.getLocation();
//				System.out.println("hair entered: " + loc.toString());
				setVisible(true);
			}
		};
	}

	public InfoPixelTool(IPlottingSystem system) {
		this( system, qScaleDefault );
	}

	public void setPlottingSystem(IPlottingSystem system) {
		this.plotSystem = system;
	}

	public IPlottingSystem getPlottingSystem() {
		return plotSystem;
	}

	public void setQScale(double qScale) {
		this.qScale = qScale;
	}

	public double getQScale() {
		return qScale;
	}

	public IImageTrace getImageTrace() {
		final Collection<ITrace> traces = plotSystem.getTraces(IImageTrace.class);
		return traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
	}

	private void addRegion(String jobName, IRegion region) {
		region.setVisible(false);
		region.setTrackMouse(true);
		region.setRegionColor(ColorConstants.red);
		region.setUserRegion(false); // They cannot see preferences or change it!
		getPlottingSystem().addRegion(region);
	}

	private void createRegions() {
		
		if (getPlottingSystem()==null) return;
		try {
			if (xHair==null || getPlottingSystem().getRegion(xHair.getName())==null) {
				this.xHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("X Driver", getPlottingSystem()), IRegion.RegionType.XAXIS_LINE);
				addRegion("Updating x cross hair", xHair);

			}
			
			if (yHair==null || getPlottingSystem().getRegion(yHair.getName())==null) {
				this.yHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Y Driver", getPlottingSystem()), IRegion.RegionType.YAXIS_LINE);
				addRegion("Updating y cross hair", yHair);
			}

		} catch (Exception ne) {
			logger.error("Cannot create initial regions in pixel info tool!", ne);
		}
	}
	
	/**
	 * Visibility
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		if(xHair!=null && yHair!=null) {
			xHair.setVisible(visible);
			yHair.setVisible(visible);
		}
	}

	/**
	 * Visibility
	 * 
	 * @return visible
	 */
	public boolean isVisible() {
		return xHair!=null && yHair!=null && xHair.isVisible() && yHair.isVisible();
	}

	protected boolean isActive = false;
	public boolean isActive() {
		return isActive;
	}

	public void activate() {
		
		createRegions();
		if (xHair!=null && yHair!=null) {
			xHair.addMouseMotionListener(hairMouseMotionListener);
			yHair.addMouseMotionListener(hairMouseMotionListener);
			xHair.addROIListener(this);
			yHair.addROIListener(this);
			setVisible(true);
		}

		if (getPlottingSystem()!=null) {
			getPlottingSystem().addRegionListener(this);
		}
		
		isActive = true;
	}
	
	public void deactivate() {
		isActive = false;

		if (xHair!=null && yHair!=null) {
			setVisible(false);
			xHair.removeMouseMotionListener(hairMouseMotionListener);
			yHair.removeMouseMotionListener(hairMouseMotionListener);
			xHair.removeROIListener(this);
			yHair.removeROIListener(this);
		}

		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(this);
		}
	}

	public void dispose() {
		if( isActive() ) //For sure
			deactivate();
	}
/*
	@Override
	public void mousePressed(MouseEvent evt) {
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void mouseDoubleClicked(MouseEvent me) {
		// TODO Auto-generated method stub
	}
*/
	@Override
	public void regionAdded(RegionEvent evt) {
		if (!isActive()) return;
		if (evt.getRegion()!=null) {
			evt.getRegion().addROIListener(this);
		}
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		if (!isActive()) return;
		if (evt.getRegion()!=null) {
			evt.getRegion().removeROIListener(this);
		}
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		if (!isActive()) return;
		createRegions(); //Have to add our hairs again, because unexpectedly removed by "removeAllRegions"
	}
	
	@Override
	public void regionCreated(RegionEvent evt) {
	}

	@Override
	public void regionCancelled(RegionEvent evt) {
	}

	@Override
	public void roiChanged(ROIEvent evt) {
	}

	@Override
	public void roiDragged(ROIEvent evt) {
//		ROIBase rb = evt.getROI(); //Is this ROIBase the same as getText(...).region.getROI()? Because if yes, then enough to get it there.
	}

	@Override
	public void roiSelected(ROIEvent evt) {
	}

	public String getText(Object element, int column) {
		
		double xIndex = 0.0;
		double yIndex = 0.0;
		double xLabel = Double.NaN;
		double yLabel = Double.NaN;
		
		try {
			IImageTrace imageTrace = getImageTrace();
			if (element instanceof IRegion){
				
				final IRegion region = (IRegion)element;
				
				if (region.getRegionType()==RegionType.POINT) {
//					PointROI pr = (PointROI)getBounds(region); //Original
					PointROI pr = (PointROI)region.getROI(); //See question in roiDragged(...)
					xIndex = pr.getPointX();
					yIndex = pr.getPointY();
					
					// Sometimes the image can have axes set. In this case we need the point
					// ROI in the axes coordinates
					if (imageTrace!=null) {
						pr = (PointROI)imageTrace.getRegionInAxisCoordinates(pr);
						xLabel = pr.getPointX();
						yLabel = pr.getPointY();
					}
				} else {
					xIndex = xValues[0];
					yIndex = yValues[0];
					final double[] dp = new double[]{xValues[0], yValues[0]};
					if (imageTrace!=null) imageTrace.getPointInAxisCoordinates(dp);
					xLabel = dp[0];
					yLabel = dp[1];
				}
	
			} else {
				return null;
			}
			
			if (Double.isNaN(xLabel)) xLabel = xIndex;
			if (Double.isNaN(yLabel)) yLabel = yIndex;
	
			IDiffractionMetadata dmeta = null;
			AbstractDataset set = null;
			if (imageTrace!=null) {
				set = imageTrace.getData();
				final IMetaData      meta = set.getMetadata();
				if (meta instanceof IDiffractionMetadata) {
	
					dmeta = (IDiffractionMetadata)meta;
				}
			}
	
			QSpace qSpace  = null;
			Vector3dutil vectorUtil= null;
			if (dmeta != null) {
	
				try {
					DetectorProperties detector2dProperties = dmeta.getDetector2DProperties();
					DiffractionCrystalEnvironment diffractionCrystalEnvironment = dmeta.getDiffractionCrystalEnvironment();
					
					if (!(detector2dProperties == null)){
						qSpace = new QSpace(detector2dProperties,
								diffractionCrystalEnvironment,qScale);
										
						vectorUtil = new Vector3dutil(qSpace, xIndex, yIndex);
					}
				} catch (Exception e) {
					logger.error("Could not create a detector properties object from metadata", e);
				}
			}
	
			switch(column) {
			case 0: // "Point Id"
				return ( ( (IRegion)element).getRegionType() == RegionType.POINT) ? ((IRegion)element).getName(): "";
			case 1: // "X position"
				return String.format("% 4.4f", xLabel);
			case 2: // "Y position"
				return String.format("% 4.4f", yLabel);
			case 3: // "Data value"
				//if (set == null || vectorUtil==null || vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (set == null) return "-";
				return String.format("% 4.4f", set.getDouble((int)yIndex, (int) xIndex));
			case 4: // q X
				//if (vectorUtil==null || vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null ) return "-";
				return String.format("% 4.4f", vectorUtil.getQx());
			case 5: // q Y
				//if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null) return "-";
				return String.format("% 4.4f", vectorUtil.getQy());
			case 6: // q Z
				//if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null) return "-";
				return String.format("% 4.4f", vectorUtil.getQz());
			case 7: // 20
				if (vectorUtil==null || qSpace == null) return "-";
				return String.format("% 3.3f", Math.toDegrees(vectorUtil.getQScatteringAngle(qSpace)));
			case 8: // resolution
				//if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
				if (vectorUtil==null ) return "-";
				return String.format("% 4.4f", (2*Math.PI)/vectorUtil.getQlength());
			case 9: // Dataset name
				if (set == null) return "-";
				return set.getName();
	
			case 20: // Qvec
				if (vectorUtil==null) return "-";
				return String.format("(% 4.4f %4.4f %4.4f)", vectorUtil.getQx(),vectorUtil.getQy(),vectorUtil.getQz());
			default:
				return "Not found";
			}
		} catch (Throwable ne) { 
			// Must not throw anything from this method - user sees millions of messages!
			logger.error("Cannot get label!", ne);
			return "";
		}
		
	}

}
