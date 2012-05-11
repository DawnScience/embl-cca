package loaders.pilatus;


/**
 * The <code>Calibration</code> class contains parameters by which
 * an image can be calibrated to display it optimally.
 * <p>
 *
 * @author  Gabor Naray
 * @version 1.00 07/12/2011
 * @since   20111207
 */
public class PilatusLoader {
	//----------------------------------------------------------------
	// static initializer
	//----------------------------------------------------------------
	static {
		System.loadLibrary("jPilatusLoader");
	}

	//----------------------------------------------------------------
	// constructors and finalizers
	//----------------------------------------------------------------
	/**
	 * Dummy constructor.
	 */
	private PilatusLoader() throws NoSuchMethodException {
		throw new NoSuchMethodException();
	}
	//----------------------------------------------------------------
	// private static methods
	//----------------------------------------------------------------

	/**
	 * Loads pilatus image.
	 * @param fileName the filename of pilatus image.
	 * @return the loaded pilatus image
	 */
	public static synchronized native PilatusData loadPilatus(String filePath);
//	public static synchronized PilatusData loadPilatus(String filePath) {
//	  return new PilatusData( 2463, 2527, new float[ 6224001 ] );
//	}
	public static synchronized native PilatusHeader loadHeader(String filePath); //Uses C bridge
//	public static synchronized PilatusHeader loadHeader(String filePath) {
//	  return new PilatusHeader( new String[0], new String[0] );
//	}
	
	public static boolean supports( String filePath ) {
		if( filePath.toLowerCase().endsWith(".cbf"))
			return true;
		return false;
	}
}
