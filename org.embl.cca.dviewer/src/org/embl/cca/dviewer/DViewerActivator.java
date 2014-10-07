package org.embl.cca.dviewer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.embl.cca.dviewer.server.MxCuBeConnectionManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class DViewerActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.embl.cca.dviewer"; //$NON-NLS-1$

	protected final static Logger logger = LoggerFactory.getLogger(DViewerActivator.class);

	// The shared instance
	private static DViewerActivator plugin;

	protected MxCuBeConnectionManager mxCuBeConnectionManager = null;
	protected final static int DefaultPort = -1;

	/**
	 * The constructor
	 */
	public DViewerActivator() {
	}

	protected static String getPackageName() {
		return DViewerActivator.class.getName().split("\\." + DViewerActivator.class.getSimpleName())[0];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		logger.debug("Starting " + getPackageName());
		super.start(context);
		plugin = this;
		logger.debug("Started " + getPackageName());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		logger.debug("Stopping " + getPackageName());
		plugin = null;
		super.stop(context);
		logger.debug("Stopped " + getPackageName());
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DViewerActivator getDefault() {
		return plugin;
	}

	/**
	 * Creates the image, this should be disposed later.
	 * @param path
	 * @return Image
	 */
	public static Image getImage(final String path) {
		return getImageDescriptor(path).createImage();
	}

	public static ImageDescriptor getImageDescriptor(final String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static IPreferenceStore getLocalPreferenceStore() {
		return getDefault().getPreferenceStore();
	}

	public MxCuBeConnectionManager getConnectionManager() {
		return mxCuBeConnectionManager;
	}

	protected void setConnectionManager(final MxCuBeConnectionManager cm) {
		mxCuBeConnectionManager = cm;
	}

	public static int getDefaultPort() {
		return DefaultPort;
	}

}
