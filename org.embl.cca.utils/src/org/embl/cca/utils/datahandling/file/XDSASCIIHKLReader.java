package org.embl.cca.utils.datahandling.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class XDSASCIIHKLReader extends BufferedReader {

	protected double oscillationRange = Double.NaN;
	protected double reflectingRange = Double.NaN;

	public XDSASCIIHKLReader(final InputStream in) {
		super(new InputStreamReader(in));
	}

	public void readHeader() throws IOException {
		do {
			mark(256);
			final String line = readLine();
			if( line == null )
				return;
			if( line.startsWith("!")) {
				if( line.substring(1).equals("OSCILLATION_RANGE") ) {
					final String[] values = line.split("=");
					oscillationRange = Double.parseDouble(values[1]);
				} else if( line.substring(1).equals("REFLECTING_RANGE_E.S.D.") ) {
					final String[] values = line.split("=");
					reflectingRange = Double.parseDouble(values[1]);
				}
				continue;
			}
			reset();
			break;
		} while( true );
		return;
	}

	/**
	 * Reads a HKL record. If there is not HKL record at the current position,
	 * then it tries to get to the next HKL record, and then reads the
	 * HKL record.
	 * @return HKL record, or null if the end of the stream has been reached
	 * @throws IOException If an I/O error occurs
	 */
	public XDSASCIIHKLRecord readNextHKLRecord() throws IOException {
//		double d = in.read();
//		new HKL(Amount.valueOf(d, NonSI.ANGSTROM));
		final XDSASCIIHKLRecord result;
		do {
			final String line = readLine();
			if( line == null )
				return null;
			if( line.startsWith("!"))
				continue;
			final String[] values = line.split(" +");
			Assert.isTrue(values.length == 13);
			//Normally a line starts with ' ', thus values[0] is empty
			result = new XDSASCIIHKLRecord(
				Integer.parseInt(values[1]), Integer.parseInt(values[2]), //H, K
				Integer.parseInt(values[3]), //L
				Double.parseDouble(values[4]), Double.parseDouble(values[5]), //IOBS, SIGMA
				Double.parseDouble(values[6]), Double.parseDouble(values[7]), //XD, YD
				Double.parseDouble(values[8]), Double.parseDouble(values[9]), //ZD, RLP
				Integer.parseInt(values[10]), Integer.parseInt(values[11]), //PEAK, CORR
				Double.parseDouble(values[12]), oscillationRange, reflectingRange //PSI
			);
			break;
		} while( true );
		return result;
	}

	public List<XDSASCIIHKLRecord> readAllHKLRecords() throws IOException {
		ArrayList<XDSASCIIHKLRecord> result = new ArrayList<XDSASCIIHKLRecord>();
		do {
			final XDSASCIIHKLRecord record = readNextHKLRecord();
			if( record == null )
				break;
			result.add(record);
		} while( true );
		return result;
	}
}
