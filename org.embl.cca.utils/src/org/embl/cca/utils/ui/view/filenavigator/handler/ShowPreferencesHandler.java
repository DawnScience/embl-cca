package org.embl.cca.utils.ui.view.filenavigator.handler;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.embl.cca.utils.ui.view.filenavigator.IFileView;

public class ShowPreferencesHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IFileView fileView = (IFileView)EclipseUtils.getActivePage().getActivePart();
		fileView.showPreferences();
		return Boolean.TRUE;
	}

}
