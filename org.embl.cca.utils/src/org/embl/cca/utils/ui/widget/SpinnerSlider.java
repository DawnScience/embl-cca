package org.embl.cca.utils.ui.widget;

import java.util.AbstractMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
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
import org.embl.cca.utils.ui.widget.support.IndexAndValueConverter;

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

	final static IndexAndValueConverter DEFAULT_CONVERTER = new IndexAndValueConverter() {
		public Integer index2Value(int i) {
			return i;
		}
		@Override
		public int value2index(Number real) {
			return real.intValue();
		}
	};

	Label label;
	Spinner spinner;
	Slider slider;
	GridLayout layout;
	IndexAndValueConverter converter;

	public SpinnerSlider(Composite parent, int style) {
		super (parent, checkStyle(style));
		label = new Label(this, SWT.NONE);
		spinner = new Spinner(this, SWT.HORIZONTAL);
		slider = new Slider(this, SWT.HORIZONTAL);
		addListener(SWT.Dispose, new Listener() {
		     public void handleEvent(Event e) {
		    	 System.out.println("spinnerslider disposed!");
		    	 SpinnerSlider.this.widgetDisposed(e);
		     }
		});
		spinner.addListener(SWT.Selection, new Listener() {
		     public void handleEvent(Event e) {
		    	 System.out.println("spinner selected!");
		    	 SpinnerSlider.this.widgetSelected(e);
		     }
		});
		slider.addListener(SWT.Selection, new Listener() {
		     public void handleEvent(Event e) {
		    	 System.out.println("slider selected!");
		    	 SpinnerSlider.this.widgetSelected(e);
		     }
		});
		spinner.addListener(SWT.Modify, new Listener() {
		     public void handleEvent(Event e) {
		    	 System.out.println("spinner modified!");
		    	 SpinnerSlider.this.widgetModified(e);
		     }
		});
		slider.addListener(SWT.Modify, new Listener() {
		     public void handleEvent(Event e) {
		    	 System.out.println("slider modified!");
		    	 SpinnerSlider.this.widgetModified(e);
		     }
		});
		layout = new GridLayout(getChildren().length, false);
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

	protected void widgetSelected(Event e) {
		do {
			if( e.widget instanceof Slider ) {
				Slider slider = (Slider)e.widget;
				int v = convertIndex2SpinnerValue(slider.getSelection());
				spinner.setSelection(v);
			} else if( e.widget instanceof Spinner ) {
				Spinner spinner = (Spinner)e.widget;
				int v = convertSpinnerValue2Index(spinner.getSelection());
				slider.setSelection(v);
			}
			notifyListeners(SWT.Selection, new Event());
		} while( false );
	}

	protected void widgetModified(Event e) {
		notifyListeners(SWT.Modify, new Event());
	}

	protected int convertSpinnerValue2Index( int value ) {
		Number real;
		if( spinner.getDigits() == 0 )
			real = value;
		else
			real = value / Math.pow(10, spinner.getDigits());
		int index = converter.value2index( real );
		return index;
	}

	protected int convertIndex2SpinnerValue( int index ) {
		Number real = converter.index2Value( index );
		int spinnerValue;
		if( spinner.getDigits() == 0 )
			spinnerValue = real.intValue();
		else
			spinnerValue = (int) (real.doubleValue() * Math.pow(10, spinner.getDigits()));
		return spinnerValue;
	}

	public void setValues(final String title, final int indexSelection, final int indexMinimum, final int indexMaximum,
		int preferredDigits, final Number valueIncrement, final Number valuePageIncrement, final int indexIncrement, final int indexPageIncrement, final IndexAndValueConverter converter) {
		if (indexMaximum < indexMinimum)
			throw new RuntimeException("Maximum is less than minimum: " + indexMaximum + " < " + indexMinimum);
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
		this.converter = converter == null ? DEFAULT_CONVERTER : converter;
		//Spinner is an odd widget. It stores value in integer, but acts as it can manage floating point value, thus we have to convert with digits...
		//Here the digits is adjusted, if the max value would overflow the Spinner.
		double log = Math.log10((converter.index2Value( indexMaximum ).doubleValue() * Math.pow(10, preferredDigits) - 1) / Integer.MAX_VALUE) + 1;
		if( log >= 1 ) {
			preferredDigits -= (int)log;
			if( preferredDigits < 0 )
				throw new RuntimeException("Calculated maximum value by indexMaximum is invalid: " + converter.index2Value( indexMaximum ).doubleValue() + " by " + indexMaximum);
		}
		label.setToolTipText(title);
		label.setText(title + ":");
//		int pageIncrement = thumb;
		spinner.setDigits( preferredDigits ); //Must do this before using convertIndex2Value() 
		spinner.setValues( convertIndex2SpinnerValue( indexSelection ), convertIndex2SpinnerValue( indexMinimum ), convertIndex2SpinnerValue( indexMaximum ),
			preferredDigits, (int)Math.round(valueIncrement.doubleValue() * Math.pow(10, preferredDigits)), (int)Math.round(valuePageIncrement.doubleValue() * Math.pow(10, preferredDigits)) );
		spinner.setToolTipText(title);
		final int thumb = indexPageIncrement;
		slider.setValues( indexSelection, indexMinimum, indexMaximum + thumb, thumb, indexIncrement, indexPageIncrement );
		slider.setToolTipText(title);
//		CommonThreading.execAsynced(new Runnable() {
//			public void run() {
////				spinner.setSelection( converterFinal.index2Real( selection ) );
//				spinner.setSelection( selection );
//			}
//		});
	}

	public void setValues(final String title, final int indexSelection, final int indexMinimum, final int indexMaximum,
			int preferredDigits, final Number valueIncrement, final Number valuePageIncrement, final int indexIncrement, final int indexPageIncrement) {
		setValues(title, indexSelection, indexMinimum, indexMaximum, preferredDigits, valueIncrement, valuePageIncrement, indexIncrement, indexPageIncrement, DEFAULT_CONVERTER);
	}

	public void setValues(final String title, final int indexSelection, final int indexMinimum, final int indexMaximum,
		final int preferredDigits, final Number valueIncrement, final Number valuePageIncrement, final int indexIncrement, final int indexPageIncrement, final AbstractMap.SimpleEntry<Number, Number> valueMinMax) {
		setValues(title, indexSelection, indexMinimum, indexMaximum, preferredDigits, valueIncrement, valuePageIncrement, indexIncrement, indexPageIncrement, new IndexAndValueConverter() {
			double logbase = Math.pow(valueMinMax.getValue().doubleValue() - (valueMinMax.getKey().doubleValue() - 1), 1.0 / (indexMaximum - indexMinimum));
			double valueMinus1 = valueMinMax.getKey().doubleValue() - 1;
			@Override
			public Double index2Value(int i) { //Key is minimum, Value is maximum in SimpleEntry
				return Math.pow(logbase, i - 1) + valueMinus1;
			}

			@Override
			public int value2index(Number real) {
				return (int) (Math.log(real.doubleValue() - valueMinus1) / Math.log(logbase) + 1);
			}
		});
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
	
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point labelSize = label.computeSize(wHint, hHint, changed);
		Point spinnerSize = spinner.computeSize(wHint, hHint, changed);
		Point sliderSize = slider.computeSize(wHint, hHint, changed);
		int width = labelSize.x + spinnerSize.x + sliderSize.x + layout.horizontalSpacing * (layout.numColumns - 1);
		int height = Math.max(Math.max(labelSize.y, spinnerSize.y), sliderSize.y);
		width = wHint == SWT.DEFAULT ? width : wHint;
		height = hHint == SWT.DEFAULT ? height : hHint;
		//Here calculating trim
		Rectangle trim = computeTrim (0, 0, width, height);
		return new Point (trim.width, trim.height);
	}

	public int getSelection() {
		return spinner.getDigits() == 0 ? spinner.getSelection() : (int) (spinner.getSelection() / Math.pow(10, spinner.getDigits()));
	}

	public void setSelection( int value ) {
		spinner.setSelection(value);
		Event event = new Event();
		event.widget = spinner;
		event.display = getDisplay();
		event.type = SWT.Selection;
		widgetSelected(event);
	}

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
	}

}
