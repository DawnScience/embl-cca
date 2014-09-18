package org.embl.cca.utils.datahandling.file;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;
import org.embl.cca.utils.datahandling.text.StringUtils;


//import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;
import uk.ac.diamond.scisoft.analysis.utils.FileUtils;

public class SmarterJavaImageSaver extends JavaImageSaver {

	//Lame, but have to store values here as well, because super has it private
	protected final String fileName;
	protected final String fileType;
	protected final int numBits;

	public SmarterJavaImageSaver(final String FileName, final String FileType,
			final int NumBits, final boolean asUnsigned) {
		super(FileName, FileType, NumBits, asUnsigned);
		this.fileName = FileName;
		this.fileType = FileType;
		this.numBits = NumBits;
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
			handleException(e, dh, fileName, maxVal, unsigned, numBits);
		}
	}

	public static void handleException(final ScanFileHolderException e,
		final IDataHolder dh, final String fileName, final double maxVal,
		final boolean unsigned, final int numBits)
		throws ScanFileHolderException, UnsupportedOperationException, IllegalArgumentException {
		/*
		 * Need massive error handling here, since we get same type of
		 * exception with different messages.
		 * The exception throwing might change as time passes, the
		 * current handling is determined on 2014-03-04.
		 * "Unable" means we can not recover, the image could not be
		 * created. Currently this can not happen, instead we get an
		 * exception with "Error" in its message. See below.
		 * "ScaledSaver" means not allowed parameters, that is use either 32 bits or float,
		 * or <=16 bits with big enough max and not unsigned if there is any negative value.
		 * "No writer" means the writer could not write the image (for
		 * example bmp writer can save only RGBDataset), in this case
		 * converting the dataset might help.
		 * "Error" means other kind of problem, probably not recoverable.
		 */
		do {
			if( StringUtils.matchStringWithPattern(e.getMessage(), ".*Unable.*", true) )
				break;
			final StringBuffer sb = new StringBuffer();
			if( StringUtils.matchStringWithPattern(e.getMessage(), ".*No writer.*", true) ) {
				sb.append(e.getLocalizedMessage());
				sb.append("\n\nSuggestion: ");
				sb.append("To save the image as " + FileUtils.getFileExtension(fileName) + " type, you can try to ");
				if( dh.getDataset(0) instanceof RGBDataset )
					sb.append("convert the RGB dataset to integer dataset.");
				else
					sb.append("convert the dataset to RGB dataset.");
				throw new IllegalArgumentException(sb.toString(), e.getCause());
			}
			if( StringUtils.matchStringWithPattern(e.getMessage(), ".*ScaledSaver.*", true) ) {
				if ( numBits > 16 ) //Currently impossible case
					break;
				sb.append(e.getLocalizedMessage());
				sb.append("\n\nSuggestion: ");
				sb.append("To save the image as " + FileUtils.getFileExtension(fileName) + " type, you can try to any of these possibilites:\n");
				sb.append("- Choose 32 bits or float type.\n");
				double maxTotalValue = maxVal;
				boolean addedSignedHint = !unsigned;
				for (int i = 0; i < dh.size() && !addedSignedHint && maxVal > 0; i++) {
					final IDataset data = dh.getDataset(i);
					if( maxVal > 0 )
						maxTotalValue = Math.max(data.max().doubleValue(), maxTotalValue);
					if( !addedSignedHint && (unsigned && data.min().doubleValue() < 0 )) {
						sb.append("- Choose signed values.\n");
						addedSignedHint = true;
					}
				}
				if( maxTotalValue > maxVal)
					sb.append("- Choose max value >= max value of image (" + maxTotalValue + ").\n");
				sb.append("- Choose autoscale option.");
				throw new IllegalArgumentException(sb.toString(), e.getCause());
			}
			if( StringUtils.matchStringWithPattern(e.getMessage(), ".*Error.*", true) )
				break;
			throw new UnsupportedOperationException("Unexpected error: " + e.getLocalizedMessage(), e.getCause());
		} while( false );
		throw e;
	}
}
