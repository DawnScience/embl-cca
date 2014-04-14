package org.embl.cca.dviewer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.dawnsci.plotting.api.preferences.PlottingConstants;
import org.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.embl.cca.dviewer.server.MxCuBeConnectionManager;
import org.embl.cca.dviewer.ui.editors.DViewerImageArrayEditorPart;
import org.embl.cca.dviewer.ui.editors.preference.DViewerEditorConstants;
import org.embl.cca.dviewer.ui.editors.preference.EditorPreferenceHelper;
import org.embl.cca.dviewer.ui.editors.utils.PHA;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.JavaSystem;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.extension.EclipseBug362561Workaround;
import org.embl.cca.utils.server.MxCuBeMessageAndEventTranslator;
import org.embl.cca.utils.threading.CommonThreading;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class DviewerStartup implements IStartup {

	protected final static Logger logger = LoggerFactory.getLogger(DviewerStartup.class);

	public final static String dViewerEnabledJavaProperty = "dViewer";
	public final static String dViewerImagePortProperty = "dViewerImagePort";
	public final static String dViewerLogSettingsProperty = "dViewerLogSettings";

	protected String getPackageName() {
		return getClass().getName().split("\\." + getClass().getSimpleName())[0];
	}

	public class StringAndObject { //TODO could be generic in utils
		public IPreferenceStore preferenceStore;
		public String string;
		public Object object;
		StringAndObject(final IPreferenceStore preferenceStore, final String string, final Object object) {
			this.preferenceStore = preferenceStore;
			this.string = string;
			this.object = object;
		}
	}

	protected void setToDefault(final StringAndObject propertyDefault) {
		setToDefault(propertyDefault.preferenceStore, propertyDefault.string, propertyDefault.object);
	}

	protected void setToDefault(final IPreferenceStore preferenceStore, final String name, final Object value) {
		//Since setDefault does not fire event, it must be set AFTER the value
		if( value instanceof String ) {
			preferenceStore.setValue(name, (String)value); //To fire event
			preferenceStore.setDefault(name, (String)value); //No event from this, odd concept
		} else if( value instanceof Boolean ) {
			preferenceStore.setValue(name, (Boolean)value); //To fire event
			preferenceStore.setDefault(name, (Boolean)value); //No event from this, odd concept
		} else if( value instanceof Integer ) {
			preferenceStore.setValue(name, (Integer)value); //To fire event
			preferenceStore.setDefault(name, (Integer)value); //No event from this, odd concept
		} else if( value instanceof Long ) {
			preferenceStore.setValue(name, (Long)value); //To fire event
			preferenceStore.setDefault(name, (Long)value); //No event from this, odd concept
		} else if( value instanceof Float ) {
			preferenceStore.setValue(name, (Float)value); //To fire event
			preferenceStore.setDefault(name, (Float)value); //No event from this, odd concept
		} else if( value instanceof Double ) {
			preferenceStore.setValue(name, (Double)value); //To fire event
			preferenceStore.setDefault(name, (Double)value); //No event from this, odd concept
		}
	}

	protected void setDefaultPreferences() {
		final String defaultColourScheme = "Film Negative";
		final String defaultPollServerDirectory = StringUtils.EMPTY_STRING;
		//getLocalPreferenceStore for Toolbar*
		final IPreferenceStore dviewerPS = DViewerActivator.getLocalPreferenceStore();
		final IPreferenceStore plottingPS = PlottingSystemActivator.getPlottingPreferenceStore();
		//getLocalPreferenceStore for Toolbar*
		final IPreferenceStore localPS = PlottingSystemActivator.getLocalPreferenceStore();
		 /* getAnalysisRCPPreferenceStore for PlottingConstants.PLOT_VIEW_PLOT2D_COLOURMAP,
		  * probably used outside of DAWN.
		  */
		final IPreferenceStore rpcPS = PlottingSystemActivator.getAnalysisRCPPreferenceStore();
		final StringAndObject[] propertyDefaults = {
			new StringAndObject(localPS, ToolbarConfigurationConstants.CONFIG.getId(), false),
			new StringAndObject(localPS, ToolbarConfigurationConstants.ANNOTATION.getId(), false),
			new StringAndObject(localPS, ToolbarConfigurationConstants.UNDO.getId(), false),
			new StringAndObject(localPS, ToolbarConfigurationConstants.ASPECT.getId(), false),
			new StringAndObject(localPS, ToolbarConfigurationConstants.EXPORT.getId(), false),
			new StringAndObject(plottingPS, PlottingConstants.COLOUR_SCHEME, defaultColourScheme),
			//No need poll server, the best we can do is setting directory to empty
			new StringAndObject(new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.sda.polling"), "pathPreference", defaultPollServerDirectory), //TODO
			new StringAndObject(dviewerPS, DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE, EditorPreferenceHelper.getDefaultValue(DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE)),
			new StringAndObject(dviewerPS, DViewerEditorConstants.PREFERENCE_APPLY_PHA, EditorPreferenceHelper.getDefaultValue(DViewerEditorConstants.PREFERENCE_APPLY_PHA)),
			new StringAndObject(dviewerPS, DViewerEditorConstants.PREFERENCE_PHA_RADIUS, EditorPreferenceHelper.getDefaultValue(DViewerEditorConstants.PREFERENCE_PHA_RADIUS)),
			};
		for( final StringAndObject propertyDefault : propertyDefaults ) {
			setToDefault(propertyDefault);
		}
	}

	/**
	 * Configures the logger as specified in the string.
	 * @param logSettingsRelPath the path relative to bundle containing this class
	 */
	protected void configureLogger(final String logSettingsRelPath) { //Here we touch logger config if we have any
		InputStream in = null;
		System.out.println("Starting to Configure Logger by " + getPackageName());
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		final Bundle bundle = Platform.getBundle(DViewerActivator.PLUGIN_ID);
		final Path path = new Path(logSettingsRelPath);
		final URL fileURL = FileLocator.find(bundle, path, null); // now find the configuration file
		final String logSettingsPath = EFile.getPathWithTrailingSeparator(DViewerActivator.PLUGIN_ID) + path.toString();
		try {
			if( fileURL == null ) {
				System.out.println("Logging Configuration File not found at '" + logSettingsPath + "'");
				throw new FileNotFoundException(logSettingsPath);
			}
			in = fileURL.openStream();
			System.out.println("Logging Configuration File found at '" + logSettingsPath + "'");
			loggerContext.reset();
			System.out.println("Logger Context Reset");
			final JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			configurator.doConfigure(in);
			System.out.println("Logging Configuration complete");
		} catch (final Throwable e) { //IOException, JoranException
			System.out.println("Could not set up logging properly, loggin to stdout for now, error follows:");
			e.printStackTrace();
		} finally {
			if( in != null ) {
				try {
					in.close();
				} catch (final IOException e1) {
				}
				in = null;
			}
		}
	}

	/**
	 * Starts the connection manager.
	 */
	protected void startConnectionManager() {
		final DViewerActivator dViewerActivator = DViewerActivator.getDefault();
		if( dViewerActivator.getConnectionManager() != null )
			return;
		final String portString = JavaSystem.getProperty(dViewerImagePortProperty);
		if( portString != null) { //port is specified
			final String msgEnd = "for listening to MxCuBE by " + getPackageName() + " package.";
			int port = DViewerActivator.getDefaultPort();
			try {
				port = JavaSystem.getPropertyAsInt(dViewerImagePortProperty);
			} catch( final NumberFormatException e ) {
				CommonThreading.execUISynced(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "dViewer startup warning", "The specified port " + portString + " is not valid\n" + msgEnd + "\n" + e.getMessage());
					}
				});
			}
			try {
				if( port > 0 ) {
					logger.info("Opening port " + port + " " + msgEnd);
					dViewerActivator.setConnectionManager(new MxCuBeConnectionManager(port, new MxCuBeMessageAndEventTranslator()));
				}
			} catch (final Exception e) {
				final int finalPort = port;
				CommonThreading.execUISynced(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "dViewer startup error", "Can not start socket listener on port " + finalPort + "\n" + msgEnd + "\n" + e.getMessage());
					}
				});
			}
		}
	}

	/**
	 * Stops the connection manager.
	 */
	protected void stopConnectionManager() {
		final DViewerActivator dViewerActivator = DViewerActivator.getDefault();
		if( dViewerActivator.getConnectionManager() != null ) {
			dViewerActivator.getConnectionManager().dispose();
			dViewerActivator.setConnectionManager(null);
		}
	}

	@Override
	public void earlyStartup() {
		if( JavaSystem.getPropertyAsBoolean(dViewerEnabledJavaProperty) ) {
			final String logSettingsRelPath = JavaSystem.getProperty(dViewerLogSettingsProperty);
			if( logSettingsRelPath != null )
				configureLogger(logSettingsRelPath);
			logger.debug("Starting " + getPackageName());
	
			PlatformUI.getWorkbench().addWorkbenchListener( new IWorkbenchListener() {
				public boolean preShutdown( final IWorkbench workbench, final boolean forced ) {
					stopConnectionManager();
					return true;
				}
				public void postShutdown( final IWorkbench workbench ) {
				}
			});

			final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
			final IFileEditorMapping[] allEditors = editorRegistry.getFileEditorMappings();
			for( final IFileEditorMapping editor : allEditors ) {
				final IEditorDescriptor thisEditors[] = editor.getEditors();
				for( final IEditorDescriptor thisEditor : thisEditors ) {
					if( thisEditor.getId().equals(DViewerImageArrayEditorPart.ID)) { //If dViewer Editor is registered for this extension
						if( !editor.getDefaultEditor().getId().equals(DViewerImageArrayEditorPart.ID)) { //and it is not default editor
							editorRegistry.setDefaultEditor(editor.getLabel(), DViewerImageArrayEditorPart.ID);
						}
						break;
					}
				}
			}
			setDefaultPreferences();
			startConnectionManager();
			new EclipseBug362561Workaround();
		}
		logger.debug("Started " + getPackageName());
	}

}
