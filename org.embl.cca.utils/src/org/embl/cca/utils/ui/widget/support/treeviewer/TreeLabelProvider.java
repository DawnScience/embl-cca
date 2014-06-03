package org.embl.cca.utils.ui.widget.support.treeviewer;

import java.util.HashMap;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeColumn;
import org.embl.cca.utils.Activator;
import org.slf4j.Logger;

public class TreeLabelProvider extends LabelProvider implements ITableLabelProvider {

	public final String NotAvailable = "<n/a>";

	protected final TreeViewer treeViewer; //AbstractTreeViewer
	protected final String[] titles;
	protected final int[] widths;
	protected final boolean[] visibles;
	protected final HashMap<String, Integer> columnIndexByVisibilityName;

	/**
	 * Constructs a basic TreeLabelProvider, which provides a "Name" column.
	 * @param treeViewer for which this class is providing labels
	 */
	public TreeLabelProvider(final TreeViewer treeViewer) {
		this(treeViewer, 1);
		System.arraycopy(new String[] { "Name" }, 0, titles, 0, titles.length);
		System.arraycopy(new int[] { 250 }, 0, widths, 0, widths.length);
		System.arraycopy(new boolean[] { true }, 0, visibles, 0, visibles.length);
		columnIndexByVisibilityName.putAll(new HashMap<String, Integer>());
	}

	/**
	 * Constructs a TreeLabelProvider, which provides columns amount of column.
	 * The caller must set the titles, widths, visibles, and optionally the
	 * columnIndexByVisibilityName.
	 * 
	 * @param treeViewer
	 * @param columns
	 */
	protected TreeLabelProvider(final TreeViewer treeViewer, final int columns) {
		this.treeViewer = treeViewer;
		titles = new String[columns];
		widths = new int[columns];
		visibles = new boolean[columns];
		columnIndexByVisibilityName = new HashMap<String, Integer>(columns);
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				setColumnVisible(event.getProperty(), (Boolean)event.getNewValue());
			}
		});

	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public int getColumnAmount() {
		return titles.length;
	}

	public void setColumn(final int columnIndex, final TreeColumn treeColumn) {
		treeColumn.setText(titles[columnIndex]);
		treeColumn.setWidth(visibles[columnIndex] ? widths[columnIndex] : 0);
		treeColumn.setResizable(true);
		treeColumn.setMoveable(true);
	}

	/**
	 * Creates columns in treeViewer, and logs the raised exceptions if
	 * logger is specified.
	 * @param treeViewer where the columns will be created
	 * @param logger where the exceptions are logged, maybe be null
	 */
	public void createColumns(final Logger logger) {
		final int iSup = getColumnAmount();
		for (int i = 0; i < iSup; i++) {
			final TreeColumn treeColumn = new TreeColumn(treeViewer.getTree(), SWT.NONE);
			try {
				setColumn(i, treeColumn);
			} catch (final SWTException e) {
				if( logger != null )
					logger.error("Error occured when setting {}. column in TreeViewer: {}", i, e);
			}
		}
	}

	public void setColumnVisible(final int columnIndex, final boolean isVisible) {
		visibles[columnIndex] = isVisible;
		treeViewer.getTree().getColumn(columnIndex).setWidth(visibles[columnIndex] ? widths[columnIndex] : 0);
	}

	public void setColumnVisible(final String columnVisibilityName, final boolean isVisible) {
		final Integer columnIndex = columnIndexByVisibilityName.get(columnVisibilityName);
		if( columnIndex != null )
			setColumnVisible(columnIndex, isVisible);
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if( columnIndex == 0 )
			return ((TreeNode) element).getName();
		return NotAvailable;
	}

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		if( columnIndex == 0 )
			return ((TreeNode) element).getImage();
		return null;
	}
}
