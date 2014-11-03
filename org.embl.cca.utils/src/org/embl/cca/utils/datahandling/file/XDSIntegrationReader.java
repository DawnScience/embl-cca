package org.embl.cca.utils.datahandling.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class XDSIntegrationReader extends BufferedReader {

	public XDSIntegrationReader(final InputStream in) {
		super(new InputStreamReader(in));
	}

	/**
	 * Reads a HKL record. If there is not HKL record at the current position,
	 * then it tries to get to the next HKL record, and then reads the
	 * HKL record.
	 * @return HKL record, or null if the end of the stream has been reached
	 * @throws IOException If an I/O error occurs
	 */
	public XDSHKLRecord readNextHKLRecord() throws IOException {
//		double d = in.read();
//		new HKL(Amount.valueOf(d, NonSI.ANGSTROM));
		final XDSHKLRecord result;
		do {
			final String line = readLine();
			if( line == null )
				return null;
			if( line.startsWith("!"))
				continue;
			final String[] values = line.split(" ");
			Assert.isTrue(values.length == 21);
			//Normally a line starts with ' ', thus values[0] is empty
			result = new XDSHKLRecord(
				Integer.parseInt(values[1]), Integer.parseInt(values[2]), //H, K
				Integer.parseInt(values[3]), //L
				Double.parseDouble(values[4]), Double.parseDouble(values[5]), //IOBS, SIGMA
				Double.parseDouble(values[6]), Double.parseDouble(values[7]), //XCAL, YCAL
				Double.parseDouble(values[8]), Double.parseDouble(values[9]), //ZCAL, RLP
				Integer.parseInt(values[10]), Integer.parseInt(values[11]), //PEAK, CORR
				Integer.parseInt(values[12]), //MAXC
				Double.parseDouble(values[13]), Double.parseDouble(values[14]), //XOBS, YOBS
				Double.parseDouble(values[15]), Double.parseDouble(values[16]), //ZOBS, ALF0
				Double.parseDouble(values[17]), Double.parseDouble(values[18]), //BET0, ALF1
				Double.parseDouble(values[19]), Double.parseDouble(values[20]) //BET1, PSI
			);
			break;
		} while( true );
		return result;
	}

	public List<XDSHKLRecord> readAllHKLRecords() throws IOException {
		ArrayList<XDSHKLRecord> result = new ArrayList<XDSHKLRecord>();
		do {
			final XDSHKLRecord record = readNextHKLRecord();
			if( record == null )
				break;
			result.add(record);
		} while( true );
		return result;
	}
}
