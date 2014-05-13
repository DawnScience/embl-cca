package org.embl.cca.utils.datahandling.file;

import uk.ac.diamond.scisoft.analysis.io.ScanFileHolderException;


import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.JavaImageScaledSaver;

public class SmarterJavaImageScaledSaver extends JavaImageScaledSaver {

	//Lame, but have to store values here as well, because super has it private
	protected final String fileName;
	protected final String fileType;
	protected final int numBits;
	protected final double gmin;
	protected final double gmax;

	public SmarterJavaImageScaledSaver(final String FileName, final String FileType,
			final int NumBits, final boolean asUnsigned) {
		super(FileName, FileType, NumBits, asUnsigned);
		this.fileName = FileName;
		this.fileType = FileType;
		this.numBits = NumBits;
		gmin = Double.NaN;
		gmax = Double.NaN;
	}

	public SmarterJavaImageScaledSaver(final String FileName, final String FileType,
			final int NumBits, final boolean asUnsigned,
			final double min, final double max) {
		super(FileName, FileType, NumBits, asUnsigned, min, max);
		this.fileName = FileName;
		this.fileType = FileType;
		this.numBits = NumBits;
		gmin = min;
		gmax = max;
	}

	/**
	 * Saves the data contained in dh with filename contained in data.
	 * The thrown exception might contain a cause, especially if its
	 * message contains "Error".
	 * 
	 * @param dh the dataholder holding the data
	 * @exception ScanFileHolderException is thrown when serious error
	 * occurs, which is not recoverable
	 * @exception UnsupportedOperationException is thrown when unexpected
	 * error occurs, unexpected for the developer of this class
	 * @exception IllegalArgumentException is thrown when wrong argument
	 * caused an error, and probably recoverable with different argument
	 */
	@Override
	public void saveFile(final IDataHolder dh) throws ScanFileHolderException, UnsupportedOperationException, IllegalArgumentException {
		try {
			super.saveFile(dh);
		} catch (final ScanFileHolderException e) {
			SmarterJavaImageSaver.handleException(e, dh, fileName, maxVal, unsigned, numBits);
		}
	}

}
