/*
 * Copyright 2014 Diamond Light Source Ltd. and EMBL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embl.cca.utils.ui.view.filenavigator;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dawb.common.services.ServiceManager;
import org.eclipse.dawnsci.plotting.api.image.IFileIconService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.file.VirtualCollectionFile;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.general.Util;
import org.embl.cca.utils.ui.view.filenavigator.preference.FileNavigatorPreferenceConstants;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeLabelProvider;

import uk.ac.diamond.sda.navigator.util.NavigatorUtils;

public class FileSystemLabelProvider extends TreeLabelProvider
	implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {

	protected final boolean preferWritables;
	protected final String[] visibilityPreferences;
	protected final IPreferenceStore store;
	protected final IFileIconService service;
	protected final SimpleDateFormat dateFormat;

	protected final FontRegistry registry = new FontRegistry();

	/**
	 * Constructs this object.
	 * @param preferWritables if true, then writable files and folders are
	 * preferred, else readable ones. Not preferring means that a file will be
	 * displayed "grayed" with a certain style.
	 */
	public FileSystemLabelProvider(final TreeViewer treeViewer, final boolean preferWritables) {
		super(treeViewer, 6);
		this.preferWritables = preferWritables;
		visibilityPreferences = new String[] { null,
				FileNavigatorPreferenceConstants.SHOW_DATE_COLUMN,
				FileNavigatorPreferenceConstants.SHOW_INDEXRANGE_COLUMN,
				FileNavigatorPreferenceConstants.SHOW_SIZE_COLUMN,
				FileNavigatorPreferenceConstants.SHOW_COMMENT_COLUMN,
				FileNavigatorPreferenceConstants.SHOW_SCANCMD_COLUMN
		};
//		{ "Name", "Date", "Type", "Size", "Comment", "Scan Command" }; //DLS style
		System.arraycopy(new String[] { "Name", "Date", "Index range", "Size", "Comment", "Scan Command" }, 0, titles, 0, titles.length);
//		{ 250, 120, 80, 150, 250, 300 }; //DLS style
		System.arraycopy(new int[] { 250, 110, 70, 100, 150, 200 }, 0, widths, 0, widths.length);
		dateFormat  = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		try {
			service = (IFileIconService)ServiceManager.getService(IFileIconService.class);
		} catch (final Exception e) { //Exceptioning code should not force Exception
			if( e instanceof RuntimeException )
				throw (RuntimeException)e;
			throw new RuntimeException(e);
		}
		store = Activator.getDefault().getPreferenceStore();
		for( int i = 0; i < getColumnAmount(); i++ ) {
			if( visibilityPreferences[i] != null ) {
				columnIndexByVisibilityName.put(visibilityPreferences[i], i);
				visibles[i] = store.getBoolean(visibilityPreferences[i]);
			} else
				visibles[i] = true;
		}
		System.arraycopy(visibles, 0, visibles, 0, visibles.length);
	}

	public IFileIconService getService() {
		return service;
	}

	protected String getRootLabel(final EFile file) {
		return Util.getRootPathForDisplay(file);
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		final FileSystemEntryNode fn = (FileSystemEntryNode) element;
		switch(columnIndex) {
		case 0:
			return fn.getName().isEmpty()
					? getRootLabel(fn.getFile()) : fn.getName();
		case 1:
			if( fn.lastModified != null )
				return dateFormat.format(new Date(fn.lastModified.toMillis()));
			break;
		case 2:
			if( !(fn.getFile() instanceof VirtualCollectionFile) )
				return StringUtils.EMPTY_STRING;
			final VirtualCollectionFile file = (VirtualCollectionFile)fn.getFile();
			final StringBuilder result = new StringBuilder();
			if( file.getFirstIndex() != null ) {
				result.append(file.getFirstIndex());
				if( !file.getLastIndex().equals(file.getFirstIndex()) )
					result.append("-").append(file.getLastIndex());
			}
			return result.toString();
//			return node.isDirectory() ? "Directory" : FileUtils.getFileExtension(node); //DLS style
		case 3:
			return fn.getSizeAsString(NotAvailable);
		case 4:
			final boolean showComment = store.getBoolean(FileNavigatorPreferenceConstants.SHOW_COMMENT_COLUMN);
			if(!fn.getFile().isDirectory() && showComment){
				try { //Converting to File, so the service is not confused by protocol
					return NavigatorUtils.getHDF5Title(fn.getFile().toFile().getAbsolutePath());
				} catch (final Exception e) {
					e.printStackTrace();
					break;
				}
			}
			return StringUtils.EMPTY_STRING;
		case 5:
			final boolean showScanCmd = store.getBoolean(FileNavigatorPreferenceConstants.SHOW_SCANCMD_COLUMN);
			if(!fn.getFile().isDirectory() && showScanCmd){
				try { //Converting to File, so the service is not confused by protocol
					return NavigatorUtils.getScanCommand(fn.getFile().toFile());
				} catch (final Exception e) {
					e.printStackTrace();
					break;
				}
			}
			return StringUtils.EMPTY_STRING;
		}
		return NotAvailable;
	}

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		switch(columnIndex) {
		case 0:
			return ((FileSystemEntryNode) element).getImage(this);
		}
		return null;
	}

	@Override
	public Color getForeground(final Object element, final int columnIndex) {
		final FileSystemEntryNode fn = (FileSystemEntryNode) element;
		if( fn.size == null || (preferWritables && !fn.isWritable()) || (!preferWritables && !fn.isReadable()) )
			return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		if( columnIndex != 0 )
			return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
		return null;
	}

	@Override
	public Color getBackground(final Object element, final int columnIndex) {
		return null;
	}

	@Override
	public Font getFont(final Object element, final int columnIndex) {
//		return registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());
		return null;
	}
}
