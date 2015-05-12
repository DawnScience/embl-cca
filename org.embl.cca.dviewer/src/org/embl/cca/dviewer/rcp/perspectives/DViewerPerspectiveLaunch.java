package org.embl.cca.dviewer.rcp.perspectives;

import org.dawb.common.ui.perspective.AbstractPerspectiveLaunch;

/**
 * Data Browsing Perspective launcher
 *
 * @author Gábor Náray
 *
 **/
public class DViewerPerspectiveLaunch extends AbstractPerspectiveLaunch {

	@Override
	public String getID() {
		return DViewerPerspective.PERSPECTIVE_ID;
	}
}
