package org.embl.cca.utils.ui.nebula;

/*
 * Copyright (c) 2012 Diamond Light Source Ltd., EMBL.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
//Originated from package org.dawnsci.plotting.system;

import org.dawnsci.plotting.system.AnnotationWrapper;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.draw2d.IFigure;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation.CursorLineStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.embl.cca.utils.ui.nebula.AnnotationEmbl.CursorLineStyleEmbl;

public class AnnotationWrapperEmbl extends AnnotationWrapper implements IAnnotation {

	final protected AnnotationEmbl annotation;

	/**
	 * 
	 * @param system
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public static final IAnnotation replaceCreateAnnotation(final IPlottingSystem system, final String name) throws Exception {
		
		if (system.getAnnotation(name)!=null) {
			system.removeAnnotation(system.getAnnotation(name));
		}
		//return system.createAnnotation(name); //Instead of this, create our Annotation
		final Axis xAxis = (Axis) system.getSelectedXAxis();
		final Axis yAxis = (Axis) system.getSelectedYAxis();
		return new AnnotationWrapperEmbl(name, xAxis, yAxis);
	}

	public AnnotationWrapperEmbl(final String name, final Axis xAxis, final Axis yAxis) {
		this(new AnnotationEmbl(name, xAxis, yAxis));
	}

	public AnnotationWrapperEmbl(final AnnotationEmbl annotation) {
		super(null);
		this.annotation = annotation;
	}

	/**
	 * @see IFigure#containsPoint(int, int)
	 */
	public boolean containsPoint(final int x, final int y) {
		return annotation.containsPoint(x, y);
	}

	/** Set the position of the annotation based on plot values
	 *  @param x Position as value on the X axis
	 *  @param y Position as value on the Y axis
	 *  @see #setCurrentPosition(Point, boolean) for setting the position based on screen coordinates
	 */
	public void setValues(final double x, final double y) {
		annotation.setValues(x, y);
	}

	/**
	 * @param axis the xAxis to set
	 */
	public void setXAxis(final Axis axis) {
		annotation.setXAxis(axis);
	}

	/**
	 * @param axis the yAxis to set
	 */
	public void setYAxis(final Axis axis) {
		annotation.setYAxis(axis);
	}

	/**
	 * @param name the name to set
	 */
	@Override
	public void setName(final String name) {
		annotation.setName(name);
	}

	@Override
	public void setShowName(final boolean showName) {
		annotation.setShowName(showName);
	}

	@Override
	public void setShowInfo(final boolean showSampleInfo) { //originally setShowSampleInfo
		annotation.setShowSampleInfo(showSampleInfo);
	}

	@Override
	public void setShowPosition(final boolean showPosition) {
		annotation.setShowPosition(showPosition);
	}

	/**
	 * @param showArrow the showArrow to set
	 */
	public void setShowArrow(final boolean showArrow) {
		annotation.setShowArrow(showArrow);
	}

	@Override
	public void setAnnotationColor(final Color annotationColor) {
		annotation.setAnnotationColor(annotationColor);
	}

	@Override
	public void setAnnotationFont(final Font annotationFont) {
		annotation.setAnnotationFont(annotationFont);
	}

	@Override
	public Font getAnnotationFont() {
		return annotation.getAnnotationFont();
	}

	/**
	 * @param ls the LineStyle to set
	 */
	@Override
	public void setLineStyle(final LineStyle ls) {
		switch(ls) {
		case NONE:
			annotation.setCursorLineStyle(CursorLineStyle.NONE);
			return;
		case UP_DOWN:
			annotation.setCursorLineStyle(CursorLineStyle.UP_DOWN);
			return;
		case LEFT_RIGHT:
			annotation.setCursorLineStyle(CursorLineStyle.LEFT_RIGHT);
			return;
		case FOUR_DIRECTIONS:
			annotation.setCursorLineStyle(CursorLineStyle.FOUR_DIRECTIONS);
			return;
		default:
			annotation.setCursorLineStyle(CursorLineStyle.NONE);
			return;
		}

	}

	/**
	 * @param ls the LineStyle to set
	 */
	public void setCursorLineStyle(final CursorLineStyleEmbl ls) {
		annotation.setCursorLineStyle(ls);
	}

	public void setdxdy(final double dx, final double dy) {
		annotation.setdxdy(dx, dy);
	}

	@Override
	public void setLocation(final double x, final double y) {
		annotation.setLocation(x, y);
	}

	/**
	 * @return the xAxis
	 */
	public Axis getXAxis() {
		return annotation.getXAxis();
	}

	/**
	 * @return the yAxis
	 */
	public Axis getYAxis() {
		return annotation.getYAxis();
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return annotation.getName();
	}

	/** @return X value, i.e. value of this annotation on the X Axis */
	public double getXValue() {
		return annotation.getXValue();
	}

	/** @return Y value, i.e. value of this annotation on the Y Axis */
	public double getYValue() {
		return annotation.getYValue();
	}

	/**
	 * @return the LineStyle
	 */
	@Override
	public LineStyle getLineStyle() {
		final CursorLineStyle style = annotation.getCursorLineStyle();
		switch(style) {
		case NONE:
			return LineStyle.NONE;
		case UP_DOWN:
			return LineStyle.UP_DOWN;
		case LEFT_RIGHT:
			return LineStyle.LEFT_RIGHT;
		case FOUR_DIRECTIONS:
			return LineStyle.FOUR_DIRECTIONS;
		default:
			return LineStyle.NONE;
		}
	}

	/**
	 * @return the trace
	 */
	public Trace getTrace() {
		return annotation.getTrace();
	}

	/**
	 * @return the showName
	 */
	@Override
	public boolean isShowName() {
		return annotation.isShowName();
	}

	/**
	 * @return the showSampleInfo
	 */
	@Override
	public boolean isShowInfo() { //originally isShowSampleInfo
		return annotation.isShowSampleInfo();
	}

	/**
	 * @return the showPosition
	 */
	@Override
	public boolean isShowPosition() {
		return annotation.isShowPosition();
	}

	/**
	 * @return the showArrow
	 */
	public boolean isShowArrow() {
		return annotation.isShowArrow();
	}

	/**
	 * @return the annotationColor
	 */
	@Override
	public Color getAnnotationColor() {
		return annotation.getAnnotationColor();
	}
	
	protected Annotation getAnnotation() {
		return annotation;
	}

	@Override
	public boolean isVisible() {
		return annotation.isVisible();
	}

	@Override
	public void setVisible(final boolean isVis) {
		annotation.setVisible(isVis);
	}

}
