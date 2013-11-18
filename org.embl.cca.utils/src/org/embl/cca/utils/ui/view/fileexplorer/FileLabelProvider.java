package org.embl.cca.utils.ui.view.fileexplorer;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dawb.common.services.IFileIconService;
import org.dawb.common.services.ServiceManager;
//import org.dawb.common.util.io.FileUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.datahandling.file.VirtualCollectionFile;
import org.embl.cca.utils.ui.view.fileexplorer.preference.FileExplorerPreferenceConstants;

import uk.ac.diamond.sda.navigator.util.NavigatorUtils;

public class FileLabelProvider extends ColumnLabelProvider {

	private int columnIndex;
	private SimpleDateFormat dateFormat;
	private IFileIconService service;
	private IPreferenceStore store;

	public FileLabelProvider(final int column) throws Exception {
		this.columnIndex = column;
		this.dateFormat  = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		this.service = (IFileIconService)ServiceManager.getService(IFileIconService.class);
		this.store = Activator.getDefault().getPreferenceStore();
	}

	@Override
	public Color getForeground(Object element) {
		if (columnIndex==0) return null;
		return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
	}
	
	@Override
	public Image getImage(Object element) {
		
		if (element instanceof String) return null;
		final File node   = (File)element;
	
		switch(columnIndex) {
		case 0:
			try {
  			    return service.getIconForFile(node);
			} catch (Throwable ne) {
				return null;
			}

        default:
        	return null;
		}
	}

	/**
	 * { "Name", "Date", "Type", "Size", "Comment", "Scan Command" }; DLS settings
	 * { "Name", "Date", "Index range", "Size", "Comment", "Scan Command" };
	 * @see org.embl.cca.utils.ui.view.fileexplorer.FileColumnsLabelProvider
	 */
	@Override
	public String getText(Object element) {
		
		boolean showComment = store.getBoolean(FileExplorerPreferenceConstants.SHOW_COMMENT_COLUMN);
		boolean showScanCmd = store.getBoolean(FileExplorerPreferenceConstants.SHOW_SCANCMD_COLUMN);

		if (element instanceof String) return (String)element;
		final File node   = (File)element;
	
		switch(columnIndex) {
		case 0:
			return "".equals(node.getName())
				   ? getRootLabel(node)
				   : node.getName();
		case 1:
			return dateFormat.format(new Date(node.lastModified()));
		case 2:
			if (element instanceof String)
				return (String)element;
			if( !(element instanceof VirtualCollectionFile) )
				return "";
			final VirtualCollectionFile file   = (VirtualCollectionFile)element;
			String result = "";
			if( file.getFirstIndex() != null ) {
				result += file.getFirstIndex();
				if( !file.getLastIndex().equals(file.getFirstIndex()) )
					result += "-" + file.getLastIndex();
			}
			return result;
//			return node.isDirectory() ? "Directory" : FileUtils.getFileExtension(node); //DLS settings
		case 3:
			return formatSize(node.length());
		case 4:
			String comment;
			if(!node.isDirectory() && showComment){
				try {
					comment = NavigatorUtils.getComment(node);
				} catch (Exception e) {
					e.printStackTrace();
					comment = "N/A";
				}
			} else {
				comment = "";
			}
			return comment;
		case 5:
			String scanCmd;
			if(!node.isDirectory() && showScanCmd){
				try {
					scanCmd = NavigatorUtils.getScanCommand(node);
				} catch (Exception e) {
					e.printStackTrace();
					scanCmd = "N/A";
				}
			} else {
				scanCmd = "";
			}
			return scanCmd;
		default:
			return null;
		}
	}

	private String getRootLabel(File node) {
    	if (isWindowsOS()) {
    		return	"("+node.getAbsolutePath().substring(0, node.getAbsolutePath().length()-1)+")";
    	}
		return node.getPath();
    }
	/**
	 * @return true if windows
	 */
	static public boolean isWindowsOS() {
		return (System.getProperty("os.name").indexOf("Windows") == 0);
	}

	protected double getColumnIndex() {
		return columnIndex;
	}

	private static final double BASE = 1024, KB = BASE, MB = KB*BASE, GB = MB*BASE;
    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static String formatSize(double size) {
        if(size >= GB) {
            return df.format(size/GB) + " GB";
        }
        if(size >= MB) {
            return df.format(size/MB) + " MB";
        }
        if(size >= KB) {
            return df.format(size/KB) + " KB";
        }
        return "" + (int)size + " bytes";
    }
}
