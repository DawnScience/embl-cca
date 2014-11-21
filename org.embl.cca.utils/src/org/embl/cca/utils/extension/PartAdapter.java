package org.embl.cca.utils.extension;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

public class PartAdapter implements IPartListener, IPartListener2 {

	@Override
	public void partActivated(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partBroughtToTop(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partDeactivated(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partOpened(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partHidden(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partVisible(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(final IWorkbenchPartReference partRef) {
	}

	@Override
	public void partActivated(final IWorkbenchPart part) {
	}

	@Override
	public void partBroughtToTop(final IWorkbenchPart part) {
	}

	@Override
	public void partClosed(final IWorkbenchPart part) {
	}

	@Override
	public void partDeactivated(final IWorkbenchPart part) {
	}

	@Override
	public void partOpened(final IWorkbenchPart part) {
	}
}
