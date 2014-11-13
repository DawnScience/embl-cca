package org.embl.cca.dviewer.ui.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.CheckBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.embl.cca.dviewer.ui.editors.DViewerController;
import org.embl.cca.dviewer.ui.editors.DViewerImageEditorPart;
import org.embl.cca.dviewer.ui.editors.IDViewerControllable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HKLSelectorPage extends Page implements IAdaptable {

	private final static Logger logger = LoggerFactory.getLogger(HKLSelectorPage.class);
	public final static String HKLSelectorPageAsString = "HKL Selector";

	public class SliderControl {
		final protected Slider slider;
		final protected Text text;
		final protected double d_minimum;
		final protected double d_maximum;
		final protected double d_resolution;
		final protected String name;
		protected int selValue;
		
		public double getValue() {
			return selValue/d_resolution+d_minimum;
		}
		
		public int getValueAsInteger() {
			return (int)Math.floor(selValue/d_resolution+d_minimum);
		}
		
		public SliderControl(final Composite parent, final String title,
				final double selection, final double minimum, final double maximum,
				final double resolution, final double increment,
				final double pageIncrement) {
			name=title;
			d_minimum=minimum;
			d_maximum=maximum;
			d_resolution=(resolution!=0.0)?resolution:1.0;

			final Label label= new Label(parent, SWT.NONE);
			label.setText(title);
			slider = new Slider(parent,SWT.HORIZONTAL);	
			selValue=(int)((selection-d_minimum)*d_resolution);
			slider.setValues(selValue, 0, (int)((d_maximum-d_minimum+increment)*d_resolution), (int)(increment*d_resolution), (int)(increment*d_resolution), (int)(pageIncrement*d_resolution));
			slider.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					selValue=slider.getSelection();
					text.setText(String.valueOf(getValueAsInteger()));
					text.pack(true);
					text.getParent().pack(true);
//////					updateImage(); //TODO
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			text=new Text(parent,SWT.NONE);
			text.setEditable(false);
			text.setText(String.valueOf(getValueAsInteger()));
			//da.setAttr(name,getValue());
		}

	}

	protected final IDViewerControllable controllable;
	protected Composite content;

	protected SliderControl hMinSlider, hMaxSlider, kMinSlider, kMaxSlider, lMinSlider, lMaxSlider;
	protected Button hklEnabledButton;

	/**
	 * Creates a HKLSelectorPage for ed editor part.
	 * @param ed
	 * @return
	 */
	public static HKLSelectorPage getPageFor(final IDViewerControllable controllable) {
		return new HKLSelectorPage(controllable);
	}

	public HKLSelectorPage(final IDViewerControllable controllable) {
		this.controllable = controllable;
	}

	@Override
	public void createControl(final Composite parent) {
		content = new Composite(parent, SWT.NONE);
		content.setLayout(new RowLayout(SWT.VERTICAL));
		
		final Composite sliders = new Composite(content, SWT.NONE);
		sliders.setLayout(new GridLayout(3, false));

		hMinSlider = new SliderControl(sliders, "Hmin", 0, -100, 100, 1, 5, 20);
		hMaxSlider = new SliderControl(sliders, "Hmax", 0, -100, 100, 1, 5, 20);
		kMinSlider = new SliderControl(sliders, "Kmin", 0, -100, 100, 1, 5, 20);
		kMinSlider = new SliderControl(sliders, "Kmax", 0, -100, 100, 1, 5, 20);
		lMinSlider = new SliderControl(sliders, "Lmin", 0, -100, 100, 1, 5, 20);
		lMaxSlider = new SliderControl(sliders, "Lmax", 0, -100, 100, 1, 5, 20);

		final Composite exec =new Composite(content, SWT.NONE);
		exec.setLayout(new GridLayout(1,true));
		hklEnabledButton = new Button(exec, SWT.CHECK);
		hklEnabledButton.setText("Display HKL values");
		hklEnabledButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				//TODO
//				controllable.setAutoSelectLatestNewImage(DViewerController.this, autoSelectLatestNewImageButton.getSelection());
			}
		});
}

	@Override
	public Control getControl() {
		return content;
	}

	@Override
	public void setFocus() {
		content.setFocus();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		if (adapter == String.class) {
			return HKLSelectorPageAsString;
		}
		return null;
	}

}
