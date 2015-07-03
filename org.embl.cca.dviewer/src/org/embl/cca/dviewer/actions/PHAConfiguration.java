package org.embl.cca.dviewer.actions;

import org.dawnsci.common.widgets.decorator.BoundsDecorator;
import org.dawnsci.common.widgets.decorator.FloatDecorator;
import org.dawnsci.common.widgets.decorator.IntegerDecorator;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.filter.FilterConfiguration;
import org.eclipse.dawnsci.plotting.api.filter.IPlottingFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.embl.cca.dviewer.DViewerActivator;
import org.embl.cca.dviewer.ui.editors.preference.DViewerEditorConstants;

public class PHAConfiguration implements FilterConfiguration {

	public PHAConfiguration() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IPlottingSystem system, IPlottingFilter filter) {
		// TODO Auto-generated method stub

	}

	@Override
	public Composite createControl(Composite parent) {
		
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		
		Label label = new Label(content, SWT.NONE);
		label.setText("Radius   ");
		
		final Text radius = new Text(content, SWT.BORDER);
		radius.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		radius.setText(String.valueOf( DViewerActivator.getLocalPreferenceStore().getInt(DViewerEditorConstants.PREFERENCE_PHA_RADIUS)));
		BoundsDecorator deco = new IntegerDecorator(radius);
		deco.setMaximum(100);
		deco.setMinimum(0);
		deco.setAllowInvalidValues(false);
		radius.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				DViewerActivator.getLocalPreferenceStore().setValue(DViewerEditorConstants.PREFERENCE_PHA_RADIUS, Integer.parseInt(radius.getText()));
			}
		});
		
		label = new Label(content, SWT.NONE);
		label.setText("Lower   ");
		
		final Text lower = new Text(content, SWT.BORDER);
		lower.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		lower.setText(String.valueOf( DViewerActivator.getLocalPreferenceStore().getDouble(DViewerEditorConstants.PREFERENCE_VALID_VALUE_MIN)));
		deco = new FloatDecorator(lower);
		deco.setMaximum(100);
		deco.setMinimum(-100);
		deco.setAllowInvalidValues(false);
		lower.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				DViewerActivator.getLocalPreferenceStore().setValue(DViewerEditorConstants.PREFERENCE_VALID_VALUE_MIN, Double.parseDouble(lower.getText()));
			}
		});
		
		return content;
	}

}
