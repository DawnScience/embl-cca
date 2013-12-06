package org.embl.cca.dviewer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.embl.cca.dviewer.server.MxCuBeConnectionManager;
import org.embl.cca.utils.server.MxCuBeMessageAndEventTranslator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.embl.cca.dviewer"; //$NON-NLS-1$

	Logger logger = LoggerFactory.getLogger(Activator.class);

	// The shared instance
	private static Activator plugin;

	protected MxCuBeConnectionManager mxCuBeConnectionManager = null;
	protected final static int DefaultPort = -1;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	protected String getPackageName() {
		return getClass().getName().split("\\." + getClass().getSimpleName())[0];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		logger.debug("Starting " + getPackageName());
		super.start(context);
		plugin = this;
		startConnectionManager();
		logger.debug("Started " + getPackageName());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		logger.debug("Stopping " + getPackageName());
		stopConnectionManager();
		plugin = null;
		super.stop(context);
		logger.debug("Stopped " + getPackageName());
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Creates the image, this should be disposed later.
	 * @param path
	 * @return Image
	 */
	public static Image getImage(String path) {
		ImageDescriptor des = imageDescriptorFromPlugin(PLUGIN_ID, path);
		return des.createImage();
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public MxCuBeConnectionManager getConnectionManager() {
		return mxCuBeConnectionManager;
	}

	public void startConnectionManager() {
		final String portOption = "-port=";
		if( mxCuBeConnectionManager == null ) {
			int port = DefaultPort;
			for( String arg : Platform.getCommandLineArgs() ) {
				if( arg.startsWith(portOption)) {
					try {
						port = Integer.parseInt(arg.substring(portOption.length()));
					} catch( final NumberFormatException e ) {
						// Important - this is the activator to an OSGI plugin you do not know 
						// at what point it will be loaded. If this line was entered in the old way it caused a crash.
						getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, "Using default port " + DefaultPort + " for socket listener because the specified port is not valid.", e));
					}
				}
			}
			final int finalPort = port;
			try {
				if( finalPort > 0 )
					mxCuBeConnectionManager = new MxCuBeConnectionManager(port, new MxCuBeMessageAndEventTranslator());
			} catch (final Exception e) {
				// Important - this is the activator to an OSGI plugin you do not know 
				// at what point it will be loaded. If this line was entered in the old way it caused a crash.
				getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Can not start socket listener on port " + finalPort + ".\n" + e.getMessage(), e));
			}
		}
	}

	public void stopConnectionManager() {
		if( mxCuBeConnectionManager != null ) {
			mxCuBeConnectionManager.dispose();
			mxCuBeConnectionManager = null;
		}
	}

}
