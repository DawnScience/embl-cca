package org.embl.cca.utils.ui.widget;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TypedListener;
import org.embl.cca.utils.extension.CommonExtension;
import org.embl.cca.utils.ui.widget.support.IndexAndValueConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The SpinnerSlider is a compound widget, containing a label, a spinner, and a slider.
 * The user can modify the value of this widget either by editing, spinning or sliding.
 * The slider is a boosted one, it has the usual integer range (linear),
 * but an user specified range (custom) can be attached, using the converter.
 * </p>
 * <p>
 * For example we want a widget to modify a number, where the number can be 1,2,4,8,16,32.
 * SpinnerSlider can be used, by specifying the linear range as [0,5], and attaching a
 * converter which converts the linear range [0,5] to {1,2,4,8,16,32} and back.
 * When we select index 4 of linear range with the slider, it will output 16 for the spinner
 * and for the widget user. The conversion is done transparently, only the converter must
 * be specified. If the converter function is exponential, then user can specify
 * only the logical range, and this widget creates the corresponding converter.
 * </p>
 * <p>
 * Example:
 * SpinnerSlider spinnerSlider = new SpinnerSlider( parent, SWT.None );
 * spinnerSlider.setValues( "Byte values with exponential slider", 1, 1, 8, 0, 1, 10, 1, 2, new AbstractMap.SimpleEntry<Number,Number>(1,256));
 * </p>
 * @see #setValues
 * 
 * @author Gábor Náray
 * 
*/
public class SpinnerSlider extends Composite {

	public final static IndexAndValueConverter NOT_NEGATIVE_INTEGER_CONVERTER = new IndexAndValueConverter() {
		@Override
		public Integer index2Value(final int i) {
			return i;
		}
		@Override
		public int value2index(final Number real) {
			return real.intValue();
		}
	};

	public final static class IntegerConverter extends IndexAndValueConverter {
		protected final int min;
		public IntegerConverter(final int min) {
			this.min = min;
		}
		@Override
		public Integer index2Value(final int i) {
			return i + min;
		}
		@Override
		public int value2index(final Number real) {
			return real.intValue() - min;
		}
	};

	public final static IndexAndValueConverter DEFAULT_CONVERTER = NOT_NEGATIVE_INTEGER_CONVERTER;

	private static Logger logger = LoggerFactory.getLogger(SpinnerSlider.class);

	//Variables to store initial (constructor) values, which help to setMinMax later
//	String title;
//	Number valueSelection;
//	Number valueIncrement;
//	Number valuePageIncrement;
//	int sliderIndexMinimum;
	int sliderIndexMaximum; //We must store it, since slider stores indexMaximum + thumb, and we do not want to react to thumb changing
//	int preferredDigits;
//	int indexIncrement;
//	int indexPageIncrement;
//	AbstractMap.SimpleEntry<Number, Number> valueMinMax;

	Label label;
	Spinner spinner;
	Slider slider;
	GridLayout layout;
	IndexAndValueConverter converter;
	boolean ignoreEvents = false; //Sounds like a hack, and it is a bit from architecture view. We should be able to set a value without firing events. 
	String prevSpinnerValue;
	int digitAdjuster;

	public SpinnerSlider(Composite parent, int style) {
		super (parent, checkStyle(style));
		label = new Label(this, SWT.HORIZONTAL);
		//Setting monospace font to avoid different lengths
		label.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		spinner = new Spinner(this, SWT.HORIZONTAL);
		//Setting monospace font to avoid different lengths
		spinner.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		slider = new Slider(this, SWT.HORIZONTAL);
		//Setting monospace font to avoid different lengths
		slider.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event e) {
				if( CommonExtension.debugMode )
					logger.debug("DEBUG: spinnerslider disposed!");
				widgetDisposed(e);
			}
		});
		slider.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if( CommonExtension.debugMode )
					logger.debug("DEBUG: slider selected!");
				widgetSelected(e);
			}
		});
		spinner.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if( CommonExtension.debugMode )
					logger.debug("DEBUG: spinner selected!");
				widgetSelected(e);
			}
		});
		spinner.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				if( CommonExtension.debugMode )
					logger.debug("DEBUG: spinner modified!");
				widgetModified(e);
			}
		});
		spinner.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				if( CommonExtension.debugMode )
					logger.debug("DEBUG: spinner verify! " + e.text);
				widgetVerify(e);
			}
		});
		layout = new GridLayout(getChildren().length, false);
		layout.marginHeight = 1;
		setLayout(layout);
	}

	static int checkStyle (int style) {
		/*
		* Even though it is legal to create this widget
		* with scroll bars, they serve no useful purpose
		* because they do not automatically scroll the
		* widget's client area.  The fix is to clear
		* the SWT style.
		*/
		return style & ~(SWT.H_SCROLL | SWT.V_SCROLL);
	}

	protected void widgetDisposed(Event e) {
		//Here should dispose created instances of descendants of org.eclipse.swt.graphics.Resource (has dispose), org.eclipse.swt.graphics.Device (has dispose)
		//With other words, here should dispose the created objects that are not widgets, but have dispose method, that is objects without parent.
	}

	protected void widgetVerify(Event e) {
		if( CommonExtension.debugMode )
			logger.debug("GRRR: widgetVerify: " + e.widget.toString() + ", event=" + e.toString() + ", event.text=" + e.text);
		if( ignoreEvents )
			return;
		try {
			ignoreEvents = true;
			if( CommonExtension.debugMode )
				logger.debug("GRRR: widgetVerify: " + e.widget.toString() + ", notifying listeners");
			notifyListeners(SWT.Verify, new Event());
		} finally {
			ignoreEvents = false;
		}
	}

	protected void widgetSelected(Event e) {
		if( CommonExtension.debugMode )
			logger.debug("GRRR: widgetSelected: " + e.widget.toString() + ", event=" + e.toString() + ", event.text=" + e.text);
		if( ignoreEvents )
			return;
		try {
			ignoreEvents = true;
			//Translating event (thank you, swt) and data
			if( e.widget instanceof Slider ) {
				Slider slider = (Slider)e.widget;
				int v = convertSliderValue2SpinnerValue(slider.getSelection());
				if( CommonExtension.debugMode )
					logger.debug("GRRR: widgetSelected=slider, spinner.setSelection(" + v + ")");
				spinner.setSelection(v);
			} else if( e.widget instanceof Spinner ) {
//				getParent().changed(new Control[] {this});
//				getParent().layout(true, true);
//				getParent().pack();
//				getParent().layout();
//				Spinner spinner = (Spinner)e.widget;
//				int v = convertSpinnerValue2SliderValue(spinner.getSelection());
//				if( CommonExtension.debugMode )
//					logger.debug("GRRR: widgetSelected=spinner, slider.setSelection(" + v + ")");
//				slider.setSelection(v);
			}
			if( CommonExtension.debugMode )
				logger.debug("GRRR: widgetSelected: " + e.widget.toString() + ", notifying listeners");
			notifyListeners(SWT.Selection, new Event());
		} finally {
			ignoreEvents = false;
		}
	}

	protected void widgetModified(Event e) {
		if( CommonExtension.debugMode )
			logger.debug("GRRR: widgetModified: " + e.widget.toString() + ", event=" + e.toString() + ", event.text=" + e.text);
		boolean prevIgnoreEvents = ignoreEvents;
		try {
			ignoreEvents = true;
			//Translating event (thank you, swt) and data
			if( e.widget instanceof Slider ) {
//				Slider slider = (Slider)e.widget;
//				int v = convertSliderValue2SpinnerValue(slider.getSelection());
//				if( CommonExtension.debugMode )
//					logger.debug("GRRR: widgetSelected=slider, spinner.setSelection(" + v + ")");
//				spinner.setSelection(v);
			} else if( e.widget instanceof Spinner ) {
				String currentValue = spinner.getText();
				if( currentValue.equals(prevSpinnerValue) )
					return;
//				getParent().changed(new Control[] {this});
//				getParent().layout(true, true);
//				getParent().pack();
//				getParent().layout();
				if( CommonExtension.debugMode )
					logger.debug("GRRR: widgetModified=spinner, prevValue=" + prevSpinnerValue + ", currentValue=" + currentValue + ", ignoring=" + prevIgnoreEvents);
				prevSpinnerValue = currentValue;
				if( prevIgnoreEvents )
					return;
				Spinner spinner = (Spinner)e.widget;
				int v = convertSpinnerValue2SliderValue(spinner.getSelection());
				if( CommonExtension.debugMode )
					logger.debug("GRRR: widgetModified=spinner, slider.setSelection(" + v + ")");
				slider.setSelection(v);
			}
			if( CommonExtension.debugMode )
				logger.debug("GRRR: widgetModified: " + e.widget.toString() + ", notifying listeners");
			notifyListeners(SWT.Modify, new Event());
		} finally {
			ignoreEvents = prevIgnoreEvents;
		}
	}

	@Override
	public Rectangle computeTrim (int x, int y, int width, int height) {
		int leftBorder = layout.marginLeft + layout.marginWidth;
		int rightBorder = layout.marginRight + layout.marginWidth;
		int topBorder = layout.marginTop + layout.marginHeight;
		int bottomBorder = layout.marginBottom + layout.marginHeight;
		x -= leftBorder;
		width += leftBorder + rightBorder;
		y -= topBorder;
		height += topBorder + bottomBorder;
		return new Rectangle (x, y, width, height);
	}
	
	/* Here the full width of this SpinnerSlider is distributed to parts. 
	 * TODO This distribution should be used when setting size of this SpinnerSlider.
	 */
	protected int resolveWidthByHint( Object obj, int wHint ) {
		int result = wHint;
		if( wHint != SWT.DEFAULT ) {
			int leftBorder = layout.marginLeft + layout.marginWidth;
			int rightBorder = layout.marginRight + layout.marginWidth;
			double dResult = result - leftBorder - rightBorder - layout.horizontalSpacing * (layout.numColumns - 1);
			if( obj == label )
				dResult *= 0.3;
			else if( obj == spinner )
				dResult *= 0.3;
			else if( obj == slider )
				dResult *= 0.4;
			result = (int)dResult;
		}
		return result;
	}

	protected Point computeSizeByParts(Point labelSize, Point spinnerSize, Point sliderSize) {
		int width = labelSize.x + spinnerSize.x + sliderSize.x + layout.horizontalSpacing * (layout.numColumns - 1);
		int height = Math.max(Math.max(labelSize.y, spinnerSize.y), sliderSize.y);
//		if( CommonExtension.debugMode )
//			System.out.println("SpinnerSlider.computeSize: labelSize=" + labelSize + ", spinnerSize=" + spinnerSize + ", sliderSize=" + sliderSize + ", width=" + width + ", height=" + height);
		//Here calculating trim
		Rectangle trim = computeTrim(0, 0, width, height);
		return new Point(trim.width, trim.height);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		if( wHint != SWT.DEFAULT && hHint != SWT.DEFAULT )
			return new Point( wHint, hHint );
		Point labelSize = label.computeSize(resolveWidthByHint(label, wHint), hHint, changed);
		Point spinnerSize = spinner.computeSize(resolveWidthByHint(spinner, wHint), hHint, changed);
		Point sliderSize = slider.computeSize(resolveWidthByHint(slider, wHint), hHint, changed);
		Point result = computeSizeByParts(labelSize, spinnerSize, sliderSize);
		if( wHint != SWT.DEFAULT )
			result.x = wHint;
		if( hHint != SWT.DEFAULT )
			result.y = hHint;
		return result;
	}

	public Point computeSizeByParts() {
		Point labelSize = label.getSize();
		Point spinnerSize = spinner.getSize();
		Point sliderSize = slider.getSize();
		return computeSizeByParts(labelSize, spinnerSize, sliderSize);
	}

	protected Number convertSpinnerValue2Value( int spinnerValue ) {
		Number result;
		if( spinner.getDigits() == 0 )
			result = spinnerValue * Math.pow(10, digitAdjuster);
		else
			result = spinnerValue / Math.pow(10, spinner.getDigits());
		return result;
	}

	protected int convertValue2SpinnerValue( Number value ) {
		int spinnerValue;
		if( spinner.getDigits() == 0 )
			spinnerValue = (int) (value.doubleValue() / Math.pow(10, digitAdjuster));
		else
			spinnerValue = (int) (value.doubleValue() * Math.pow(10, spinner.getDigits()));
		return spinnerValue;
	}

	protected Number convertSliderValue2Value( int sliderValue ) {
		return converter.index2Value( sliderValue );
	}

	protected int convertValue2SliderValue( Number value ) {
		return converter.value2index( value );
	}

	protected int convertSpinnerValue2SliderValue( int spinnerValue ) {
		return convertValue2SliderValue( convertSpinnerValue2Value( spinnerValue ) );
	}

	protected int convertSliderValue2SpinnerValue( int sliderValue ) {
		return convertValue2SpinnerValue(convertSliderValue2Value(sliderValue));
	}

	public Number getMinValue() {
		return convertSliderValue2Value(slider.getMinimum());
	}

	public Number getMaxValue() {
		return convertSliderValue2Value(slider.getMaximum());
	}

//	static int checkAndAdjustDigits(int preferredDigits, final int sliderIndexMaximum, final IndexAndValueConverter converter) {
//		double log = Math.log10(converter.index2Value( sliderIndexMaximum ).doubleValue() * Math.pow(10, preferredDigits) / (((double)Integer.MAX_VALUE) + 1)) + 1;
	static int checkAndAdjustDigits(int preferredDigits, final Number valueMax, boolean ignoreOverrange) {
		double log = Math.log10(valueMax.doubleValue() * Math.pow(10, preferredDigits) / (((double)Integer.MAX_VALUE) + 1)) + 1;
		if( log >= 1 ) {
			preferredDigits -= (int)log;
			if( preferredDigits < 0 ) {
				if( !ignoreOverrange )
//					throw new RuntimeException("Calculated maximum value by sliderIndexMaximum is invalid: " + converter.index2Value( sliderIndexMaximum ).doubleValue() + " by " + sliderIndexMaximum);
					throw new RuntimeException("Maximum value is too large: " + valueMax.doubleValue());
			}
		}
		return preferredDigits; 
	}

	public void setValues(final String title, final Number valueSelection, final int sliderIndexMinimum, final int sliderIndexMaximum,
		int preferredDigits, final Number valueIncrement, final Number valuePageIncrement, final int indexIncrement, final int indexPageIncrement, final IndexAndValueConverter converter) {
		if( CommonExtension.debugMode )
			System.out.println("GRRR: setValues: title=" + title + ", valueSelection=" + valueSelection + ", sliderIndexMinimum=" + sliderIndexMinimum + ", sliderIndexMaximum=" + sliderIndexMaximum);
		if (sliderIndexMaximum < sliderIndexMinimum)
			throw new RuntimeException("Maximum is less than minimum: " + sliderIndexMaximum + " < " + sliderIndexMinimum);
		if (preferredDigits < 0)
			throw new RuntimeException("PreferredDigits can not be negative: " + preferredDigits);
		if (indexIncrement < 1)
			throw new RuntimeException("IndexIncrement can not be less than 1: " + indexIncrement);
		if (indexPageIncrement < 1)
			throw new RuntimeException("IndexPageIncrement can not be less than 1: " + indexPageIncrement);
		long spinnerPageIncrement = Math.round(valuePageIncrement.doubleValue() * Math.pow(10, preferredDigits)); 
		if (spinnerPageIncrement < 1)
			throw new RuntimeException("PageIncrement is too small: " + valuePageIncrement.doubleValue() + ", using preferredDigits: " + preferredDigits);
		if ( spinnerPageIncrement > Integer.MAX_VALUE )
			throw new RuntimeException("PageIncrement is too large: " + valuePageIncrement.doubleValue() + ", using preferredDigits: " + preferredDigits);
		long spinnerIndexIncrement = Math.round(valueIncrement.doubleValue() * Math.pow(10, preferredDigits)); 
		if ( spinnerIndexIncrement < 1)
			throw new RuntimeException("IndexIncrement is too small: " + valueIncrement.doubleValue() + ", using preferredDigits: " + preferredDigits);
		if ( spinnerIndexIncrement > Integer.MAX_VALUE )
			throw new RuntimeException("IndexIncrement is too large: " + valueIncrement.doubleValue() + ", using preferredDigits: " + preferredDigits);
		IndexAndValueConverter converterNew = converter == null ? DEFAULT_CONVERTER : converter;
		//Spinner is an odd widget. It stores value in integer, but acts as it can manage floating point value, thus we have to convert with digits...
		//Here the digits is adjusted, if the max value would overflow the Spinner.
//		preferredDigits = SpinnerSlider.checkAndAdjustDigits(preferredDigits, sliderIndexMaximum, converterNew);
		preferredDigits = SpinnerSlider.checkAndAdjustDigits(preferredDigits, converterNew.index2Value( sliderIndexMaximum ), true);
		if( preferredDigits < 0 ) {
			digitAdjuster = -preferredDigits;
			preferredDigits = 0;
		} else
			digitAdjuster = 0;
		this.converter = converterNew;
		label.setToolTipText(title);
		label.setText(title + ":");
//		int pageIncrement = thumb;
		spinner.setDigits( preferredDigits ); //Must do this before using convertValue2SpinnerValue() 
		if( CommonExtension.debugMode )
			System.out.println("GRRR: spinner.setValues: valueSelection=" + valueSelection + ", sliderIndexMinimum=" + sliderIndexMinimum + ", sliderIndexMaximum=" + sliderIndexMaximum);
		spinner.setValues( convertValue2SpinnerValue(valueSelection), convertSliderValue2SpinnerValue( sliderIndexMinimum ), convertSliderValue2SpinnerValue( sliderIndexMaximum ),
			preferredDigits, (int)Math.round(valueIncrement.doubleValue() * Math.pow(10, preferredDigits)), (int)Math.round(valuePageIncrement.doubleValue() * Math.pow(10, preferredDigits)) );
		spinner.setToolTipText(title);
		if( CommonExtension.debugMode )
			System.out.println("GRRR: setValues: spinner[.getSelection=" + spinner.getSelection() + ", .getMinimum=" + spinner.getMinimum() + ", .getMaximum=" + spinner.getMaximum() + ", .getText=" + spinner.getText());
		final int thumb = indexPageIncrement;
		if( CommonExtension.debugMode )
			System.out.println("GRRR: slider.setValues: valueSelection=" + valueSelection + ", sliderIndexMinimum=" + sliderIndexMinimum + ", sliderIndexMaximum=" + sliderIndexMaximum + ", thumb=" + thumb);
		slider.setValues( convertValue2SliderValue(valueSelection), sliderIndexMinimum, sliderIndexMaximum + thumb, thumb, indexIncrement, indexPageIncrement );
		if( CommonExtension.debugMode )
			System.out.println("DEBUG: slider.maximum=" + slider.getMaximum());
		slider.setToolTipText(title);
//		this.title = title;
//		this.valueSelection = valueSelection;
//		this.sliderIndexMinimum = sliderIndexMinimum;
		this.sliderIndexMaximum = sliderIndexMaximum;
//		this.preferredDigits = preferredDigits;
//		this.valueIncrement = valueIncrement;
//		this.valuePageIncrement = valuePageIncrement;
//		this.indexIncrement = indexIncrement;
//		this.indexPageIncrement =  indexPageIncrement;
//		CommonThreading.execAsynced(new Runnable() {
//			public void run() {
////				spinner.setSelection( converterFinal.index2Real( selection ) );
//				spinner.setSelection( selection );
//			}
//		});
		spinner.pack(true);
		pack(true);
		CommonExtension.layoutIn(spinner, this);
		CommonExtension.layoutIn(this, getParent());
//		getParent().layout(true, true);
	}

	public void setValues(final String title, final int valueSelection, final int sliderIndexMinimum, final int sliderIndexMaximum,
			int preferredDigits, final Number valueIncrement, final Number valuePageIncrement, final int indexIncrement, final int indexPageIncrement) {
		setValues(title, valueSelection, sliderIndexMinimum, sliderIndexMaximum, preferredDigits, valueIncrement, valuePageIncrement, indexIncrement, indexPageIncrement, DEFAULT_CONVERTER);
	}

	public void setValues(final String title, final Number valueSelection, final int sliderIndexMinimum, final int sliderIndexMaximum,
		int preferredDigits, final Number valueIncrement, final Number valuePageIncrement, final int indexIncrement, final int indexPageIncrement, final Number valueMin, final Number valueMax, final boolean linear) {
		IndexAndValueConverter converterNew = new IndexAndValueConverter() {
			double valueMinus1 = valueMin.doubleValue() - 1; //-1 so even 0 long interval works (though does no much)
			double linearRate = (valueMax.doubleValue() - valueMin.doubleValue()) / (sliderIndexMaximum - sliderIndexMinimum);
			double logbase = Math.pow(valueMax.doubleValue() - valueMinus1, 1.0 / (sliderIndexMaximum - sliderIndexMinimum));
			@Override
			public Double index2Value(int i) { //i=sliderIndexMinimum,...,sliderIndexMaximum
				if( CommonExtension.debugMode )
					System.out.println("DEBUG: index2Value.ori: logbase=" + logbase + ", linearRate=" + linearRate + ", valueMinus1=" + valueMinus1 + ", i=" + i);
				if( linear )
					return (i - sliderIndexMinimum) * linearRate + valueMin.doubleValue();
				else
					return Math.pow(logbase, i - sliderIndexMinimum) + valueMinus1;
			}

			@Override
			public int value2index(Number real) {
				double dbl = Math.log(real.doubleValue() - valueMinus1) / Math.log(logbase) + sliderIndexMinimum;
				if( CommonExtension.debugMode )
					System.out.println("DEBUG: value2index.ori: logbase=" + logbase + ", linearRate=" + linearRate + ", valueMinus1=" + valueMinus1 + ", real=" + real + ", sliderIndex=" + dbl);
				if( linear )
					return (int) ((real.doubleValue() - valueMin.doubleValue()) / linearRate + sliderIndexMinimum);
				else
					return (int) (Math.log(real.doubleValue() - valueMinus1) / Math.log(logbase) + sliderIndexMinimum);
			}
		};
		setValues(title, valueSelection, sliderIndexMinimum, sliderIndexMaximum, preferredDigits, valueIncrement, valuePageIncrement, indexIncrement, indexPageIncrement, converterNew );
	}

//	public void setMinMax(final AbstractMap.SimpleEntry<Number, Number> valueMinMax, boolean keepCurrentSelection) {
//		setValues(title, keepCurrentSelection ? spinner.getSelection() : valueSelection, sliderIndexMinimum, sliderIndexMaximum,
//				preferredDigits, valueIncrement, valuePageIncrement, indexIncrement, indexPageIncrement, valueMinMax);
	public void setMinMax(final Number valueMin, final Number valueMax) {
		final int sliderIndexMinimumCurrent = slider.getMinimum();
		final int sliderIndexMaximumCurrent = sliderIndexMaximum; //slider.getMaximum(); //See comment at declaration of sliderIndexMaximum
		int preferredDigits = SpinnerSlider.checkAndAdjustDigits(spinner.getDigits(), valueMax, true);
		if( preferredDigits < 0 ) {
			digitAdjuster = -preferredDigits;
			preferredDigits = 0;
		} else
			digitAdjuster = 0;
		spinner.setDigits( preferredDigits );
		final Number valueMaxForConverter = convertSpinnerValue2Value( convertValue2SpinnerValue( valueMax ) + 1 );
		IndexAndValueConverter converterNew = new IndexAndValueConverter() {
			double valueMinus1 = valueMin.doubleValue() - 1; //-1 so even 0 long interval works (though does no much)
			double logbase4i2v = Math.pow(valueMaxForConverter.doubleValue() - valueMinus1, 1.0 / (sliderIndexMaximumCurrent - sliderIndexMinimumCurrent));
			double logbase4v2i = Math.pow(valueMax.doubleValue() - valueMinus1, 1.0 / (sliderIndexMaximumCurrent - sliderIndexMinimumCurrent));
			@Override
			public Double index2Value(int i) { //i=sliderIndexMinimumCurrent,...,sliderIndexMaximumCurrent
				if( CommonExtension.debugMode )
					System.out.println("DEBUG: index2Value: logbase4i2v=" + logbase4i2v + ", valueMinus1=" + valueMinus1 + ", i=" + i);
				return Math.pow(logbase4i2v, i - sliderIndexMinimumCurrent) + valueMinus1;
			}

			@Override
			public int value2index(Number real) {
				double dbl = Math.log(real.doubleValue() - valueMinus1) / Math.log(logbase4v2i) + sliderIndexMinimumCurrent;
				if( CommonExtension.debugMode )
					System.out.println("DEBUG: value2index: logbase4v2i=" + logbase4v2i + ", valueMinus1=" + valueMinus1 + ", real=" + real + ", sliderIndex=" + dbl);
				return (int) (Math.log(real.doubleValue() - valueMinus1) / Math.log(logbase4v2i) + sliderIndexMinimumCurrent);
			}
		};
//		int preferredDigits = SpinnerSlider.checkAndAdjustDigits(spinner.getDigits(), sliderIndexMaximum, converterNew);
		converter = converterNew;
		int prevSel = spinner.getSelection();
		if( CommonExtension.debugMode )
			System.out.println("GRRR: setMinMax: prevSel=" + prevSel + ", slider.getSelection=" + slider.getSelection() ); 
		//Setting minimum or maximum fires Modify event (and thus setting slider's selection value) only if the current value (selection) must be adjusted, i.e. it changes.
		if( CommonExtension.debugMode )
			System.out.println("GRRR: setMinMax/setMinimum: sliderIndexMinimumCurrent=" + sliderIndexMinimumCurrent + "->" + convertSliderValue2SpinnerValue( sliderIndexMinimumCurrent ));
		spinner.setMinimum(convertSliderValue2SpinnerValue( sliderIndexMinimumCurrent ));
		if( CommonExtension.debugMode )
			System.out.println("GRRR: setMinMax/setMaximum: sliderIndexMaximumCurrent=" + sliderIndexMaximumCurrent + "->" + convertSliderValue2SpinnerValue( sliderIndexMaximumCurrent ));
		spinner.setMaximum(convertSliderValue2SpinnerValue( sliderIndexMaximumCurrent ));
		//We have to manually adjust the value (selection) if it did not change (which also means it remained in the minimum maximum range).
		if( prevSel == spinner.getSelection() ) {
			int slvl = convertSpinnerValue2SliderValue(prevSel);
			if( CommonExtension.debugMode )
				System.out.println("GRRR: setMinMax: slvl=" + slvl ); 
			slider.setSelection(slvl);
//			slider.setSelection(convertSpinnerValue2SliderValue(prevSel));
			//And since slider does not know about Modify event, we have to emulate slider selection and notify the spinner
//			Event event = new Event();
//			event.widget = slider;
//			event.display = getDisplay();
//			event.type = SWT.Selection;
//			ignoreEvents = true;
//			widgetSelected(event);
//			ignoreEvents = false;
		}
		getParent().layout(true, true);
	}

	public Number getSelection() {
		return convertSpinnerValue2Value( spinner.getSelection() );
	}

	public byte getSelectionAsByte() {
		return getSelection().byteValue();
	}

	public short getSelectionAsShort() {
		return getSelection().shortValue();
	}

	public int getSelectionAsInteger() {
		return getSelection().intValue();
	}

	public long getSelectionAsLong() {
		return getSelection().longValue();
	}

	public float getSelectionAsFloat() {
		return getSelection().floatValue();
	}

	public double getSelectionAsDouble() {
		return getSelection().doubleValue();
	}

	protected void setSpinnerSelection( int value ) {
		if( CommonExtension.debugMode )
			System.out.println("GRRR: setSpinnerSelection: spinner.setSelection=" + value);
		spinner.setSelection(value);
		if( CommonExtension.debugMode )
			System.out.println("GRRR: setSpinnerSelection: spinner[.getSelection=" + spinner.getSelection() + ", .getMinimum=" + spinner.getMinimum() + ", .getMaximum=" + spinner.getMaximum() + ", .getText=" + spinner.getText());
//		Event event = new Event();
//		event.widget = spinner;
//		event.display = getDisplay();
//		event.type = SWT.Selection;
//		widgetSelected(event);
	}

	public void setSelection(Number value) {
		setSpinnerSelection(convertValue2SpinnerValue(value));
	}

	public void setSelectionAsByte(byte value) {
		setSelection(value);
	}

	public void setSelectionAsShort(short value) {
		setSelection(value);
	}

	public void setSelectionAsInteger(int value) {
		setSelection(value);
	}

	public void setSelectionAsLong(long value) {
		setSelection(value);
	}

	public void setSelectionAsFloat(float value) {
		setSelection(value);
	}

	public void setSelectionAsDouble(double value) {
		setSelection(value);
	}

//	public void selectCurrentValue() {
//		setSpinnerSelection(spinner.getSelection()); //Looks funny, but that is the way
//	}

	public void addModifyListener (ModifyListener listener) {
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		addListener(SWT.Modify, new TypedListener (listener));
	}

	public void removeModifyListener (ModifyListener listener) {
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Modify, listener);
	}

	public void addSelectionListener(SelectionListener listener) {
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection,typedListener);
		addListener(SWT.DefaultSelection,typedListener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}

	public void addVerifyListener (VerifyListener listener) {
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		addListener(SWT.Verify, new TypedListener (listener));
	}

	public void removeVerifyListener (VerifyListener listener) {
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Verify, listener);
	}

}
