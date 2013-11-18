package org.embl.cca.utils.ui.view.fileexplorer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import org.dawnsci.common.widgets.content.IFilterExtensionProvider;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.file.FileLoader;

/**
 * File proposals. You must set the proposal adapter to replace if using this: <code>
 * <p>
 		FileContentProposalProvider prov = new FileContentProposalProvider();<br>
 		ContentProposalAdapter ad = new ContentProposalAdapter(filePath, new TextContentAdapter(), prov, null, null);<br>
		ad.setProposalAcceptanceStyle(<b>ContentProposalAdapter.PROPOSAL_REPLACE</b>);<br>
   </p>
   </code>
 */
public class FileContentProposalProvider implements IContentProposalProvider, IFileContentProposalProvider {
	protected class FileContentProposal implements IContentProposal {
		private final File   file;
		FileContentProposal(final File file) {
			this.file = file;
		}

		@Override
		public String getContent() {
			return getLabel();
		}

		@Override
		public int getCursorPosition() {
			return getLabel().length();
		}

		@Override
		public String getDescription() {
			//return FileUtils.getSystemInfo(file);
			return null;
		}

		@Override
		public String getLabel() {
			if( file instanceof EFile )
				return ((EFile)file).getAbsolutePathWithoutProtocol();
			else
				return file.getAbsolutePath();
		}

	}

	private class FilenameChecker implements FileFilter {
		@Override
		public boolean accept(File file) {
			if (file == null)
				return true;
			if (file.isDirectory())
				return true;
			if (filterExtensionProv == null)
				return true;
			final String[] filters = filterExtensionProv.getFilterExtensions();
			if (filters == null)
				return true;
			for (int i = 0; i < filters.length; i++) {
				String filter = filters[i].replace(".", "\\.");
				filter = filters[i].replace("*", ".*");
				if (file.getName().matches(filter))
					return true;
			}
			return false;
		}
	}

	protected IFilterExtensionProvider filterExtensionProv;
	protected IContentProposal[] currentProposals;

	public int getSize() {
		return currentProposals != null ? currentProposals.length : -1;
	}

	public String getFirstPath() {
		return currentProposals != null ? currentProposals[0].getContent() : null;
	}

	/**
	 * Returns the String representation of file (e.g. absolute path).
	 * 
	 * @param file
	 *            the file of which the String representation is returned
	 */
	@Override
	public String fileToString(File file) {
		return file instanceof EFile
				? ((EFile)file).getAbsolutePathWithoutProtocol()
				: file.getAbsolutePath();
	}

	/**
	 * Returns the File representation of String.
	 * @param path the file path of which the File representation is returned
	 */
	@Override
	public File stringToFile(String path) {
		File result;
		FileLoader fl = new FileLoader();
		fl.setFilePath(path);
		if( fl.isCollection() ) {
			if( fl.getFile().exists() )
				result = new File(fl.getFile().getFilesFromAll().get(0).getAbsolutePathWithoutProtocol());
			else
				result = new File(fl.getFile().getAbsolutePathWithoutProtocol()).getParentFile();
		} else
			result = fl.getFile().getAbsoluteFile();//new File(path); -> wrong because contains "file:"
		return result;
	}

	/**
	 * Return an array of content proposals representing the valid proposals for a field. If the specified contents is
	 * null, then the
	 * 
	 * @param contents
	 *            the current contents of the text field
	 * @param position
	 *            the current position of the cursor in the contents
	 * @return the array of {@link IContentProposal} that represent valid proposals for the field.
	 */
	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		if (contents == null) // This is a convention, due to lame ContentProposalAdapter, it can not tell the proposals
								// (everything is private)
			return currentProposals;
		currentProposals = null;
		final File dir = getDirectory(contents, position);
		if (dir == null)
			return new IContentProposal[0];
		IContentProposal[] ret = getProposals(contents, dir);
		if (ret == null)
			return new IContentProposal[0];
		return ret;
	}

	protected FileContentProposal getFileContentProposal(File file) {
		return new FileContentProposal(file);
	}

	private IContentProposal[] getProposals(String existing, File dir) {
		File test = new File(existing);
		String fnameToFilter = "";
		if (existing.charAt(existing.length() - 1) != File.separatorChar)
			fnameToFilter = test.getName();
		final File[] fa = dir.listFiles(new FilenameChecker());
		if (fa == null)
			return null;
		List<IContentProposal> ret = new ArrayList<IContentProposal>(fa.length);
		// ret.add(new FileContentProposal(dir));
		for (int i = 0; i < fa.length; i++) {
			final File file = fa[i];
			// filter, but also allow symbolic links
			String fname = file.getName();
			if (!fnameToFilter.isEmpty() && fname.lastIndexOf(fnameToFilter) != 0)
				continue;
			ret.add(getFileContentProposal(file));
		}
		if (ret.isEmpty())
			return null;
		this.currentProposals = ret.toArray(new IContentProposal[ret.size()]);
		return currentProposals;
	}

	private File getDirectory(String contents, int position) {
		if (contents == null)
			return null;
		if (position > -1)
			contents = contents.substring(0, position);
		int index = contents.lastIndexOf('/');
		boolean linuxabspathroot = false;
		if (index == 0)
			linuxabspathroot = true;
		if (index < 0)
			index = contents.lastIndexOf('\\');
		if (index > -1)
			contents = contents.substring(0, index);
		// on linux select root directory as default if no subdirs were specified
		if (linuxabspathroot)
			contents = "/";
		return new File(contents);
	}

	/**
	 * @return Returns the filterExtensionProv.
	 */
	public IFilterExtensionProvider getFilterExtensionProv() {
		return filterExtensionProv;
	}

	/**
	 * @param filterExtensionProv
	 *            The filterExtensionProv to set.
	 */
	public void setFilterExtensionProv(IFilterExtensionProvider filterExtensionProv) {
		this.filterExtensionProv = filterExtensionProv;
	}

}
