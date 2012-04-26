#include "loaders_pilatus_PilatusLoader.h"

#include "cbf.h"
#include "cbf_simple.h"
#include "JCBridge.h"
#include "String.h"
using namespace cstring;
#include "CallTrace.h"
using namespace ccalltrace;

#include <boost/regex.hpp>
#include <boost/lexical_cast.hpp>
using namespace boost;
#include <stdlib.h>
#include <iostream>
#include <sstream>
using namespace std;

//http://java.sun.com/docs/books/jni/html/refs.html

myJNIEnv gEnv;

//char tempBuffer[ 256 ];
//const int tempBufferSize = sizeof( tempBuffer ) - 1;
/*
char *myprintf( const char *format, ... ) {
  va_list a_list;
  va_start( a_list, format );
  vsnprintf( tempBuffer, tempBufferSize, format, a_list );
  va_end( a_list );
  return tempBuffer;
}
*/

class CCBFDataParameters {
public:
  unsigned int compression;
  int binary_id;
  size_t elsize;
  int elsigned;
  int elunsigned;
  size_t elements;
  int minelement;
  int maxelement;
  const char *byteorder;
  size_t dimfast;
  size_t dimmid;
  size_t dimslow;
  size_t padding;
};

class CCBFExperimentParameters : public CCBFDataParameters {
public:
	CString detectorType;
	CString detectorDetails;
//# Detector: PILATUS 6M SN: 60-0001 [DETECTOR_SN(whole str)] {type="PILATUS 6M", details="60-0001"}
//# 2007/Jun/17 15:12:36.928 [DATE(str)] {yyyy[/-]MMM[/-]dd([ Tt:]hh(:mm(:ss)?)?)?}
//# Pixel_size 172e-6 m x 172e-6 m [PIXEL_SIZE(float)*=PIXEL_SIZE_UNITS[m=1,mm=1000], PIXEL_SIZE_UNITS(str) ...] {value1 (m|mm|um)( x value2 (m|mm|um))?, default=1}
//# Silicon sensor, thickness 0.000320 m
//# Exposure_time 0.995000 s
//# Exposure_period 1.000000 s
//# Tau = 194.0e-09 s
//# Count_cutoff 1048575 counts [CCD_IMAGE_SATURATION (int=65535)]
//# Threshold_setting 5000 eV
//# Wavelength 1.2398 A [WAVELENGTH(float)]
//# Energy_range (0, 0) eV {value[value]? eV, default=0}
//# Detector_distance 0.15500 m [DISTANCE(float)*=DISTANCE_UNITS[m=1,mm=1000], DISTANCE_UNITS(str)]
//# Detector_Voffset -0.01003 m [DETECTOR_SN]
//# Beam_xy (1231.00, 1277.00) pixels [BEAM_CENTER_X(float)*=PIXEL_SIZE, BEAM_CENTER_Y(float)*=PIXEL_SIZE] {\(value1(->mm|->pixels|->bins|.)?(, value2)?\), default=1 pixel, 2 units must be compatible} //swapped in pilatus_minicbf.py
//# Flux 22487563295 ph/s
//# Filter_transmission 0.0008
//# Start_angle 13.0000 deg. [OSC_START(float)]
//# Angle_increment 1.0000 deg. [OSC_RANGE(float)]
//# Detector_2theta 0.0000 deg. [TWOTHETA(float)]
//# Polarization 0.990
//# Alpha 0.0000 deg.
//# Omega 0.0000 deg. [OMEGA(float)] //missing from miniCBF example
//# Kappa 0.0000 deg.
//# Phi 0.0000 deg. [PHI(float)]
//# Chi 0.0000 deg.
//# Oscillation_axis  X, CW [AXIS(str)]
//# N_oscillations 1

//X-Binary-Size-Second-Dimension [size1]
//X-Binary-Size-Fastest-Dimension [size2]
//sizem=size1*size2/1024/1024; sizem<1 ? sizem*10 : sizem [vendortype="Pilatus-"round(sizem)"sizem<1 ? "K" : "M"]
};

void consoleMessage( const CString &msg ) {
	cout << msg << endl;
	if( gEnv.isUsable() )
		gEnv.SystemOutPrintln( msg );
}

void consoleMessage( const char *msg ) {
	cout << msg << endl;
	if( gEnv.isUsable() )
		gEnv.SystemOutPrintln( msg );
}

#define cbf_checkerror(result) \
	if( result != 0 ) { \
		throw JNIException( RUNTIME_EXCEPTION, CString( CSPARTS, "CBFlib fatal error ", result ), __LINE__ ); \
	}

#define cbf_checkerror_msg(result, msg) \
	if( result != 0 ) { \
		throw JNIException( RUNTIME_EXCEPTION, CString( CSPARTS, "CBFlib fatal error ", result, ": ", msg ), __LINE__ ) ); \
	}
/*
void cbf_checkerror( int result, int goodResult = 0 ) {
	CCallTrace ct( __PRETTY_FUNCTION__ );
	if( result != goodResult )
		throw JNIException( RUNTIME_EXCEPTION, CString( CSPARTS, "CBFlib fatal error ", result, "\n", ct.toString() ) );
}
*/
//-----------------------------------------------------------------------------
/*
//Normal CBF by http://www.mrc-lmb.cam.ac.uk/harry/herb.header
HEADER_BYTES=  512;
DIM=2;
BYTE_ORDER=little_endian;
TYPE=unsigned_short;
 PIXEL_SIZE=0.102592;
BIN=2x2;
ADC=fast;
 DETECTOR_SN=902;
 DATE=Thu Jan 11 12:19:57 2007;
TIME=1.50;
 DISTANCE=200.000;
 OSC_RANGE=0.500;
 PHI=203.000;
 OSC_START=203.000;
 TWOTHETA=0.000;
 AXIS=phi;
 WAVELENGTH=0.97940;
 BEAM_CENTER_X=157.500;
 BEAM_CENTER_Y=157.500;
CREV=1;
CCD=TH7899;
BIN_TYPE=HW;
TIME=1.500000;
ACC_TIME=3375;
UNIF_PED=1500;
IMAGE_PEDESTAL=40;
 SIZE1=3072;
 SIZE2=3072;

//miniCBF by http://www.medsbio.org/meetings/BSR_2007_imgCIF_Workshop/SLS_miniCBF_header.html
//miniCBF handled (written in []) by http://cci.lbl.gov/cctbx_sources/iotbx/detectors/pilatus_minicbf.py
//miniCBF handled (written in {}) by convert_minibcbf.c
{if convention || _array_data.header_contents}
# Detector: PILATUS 6M SN: 60-0001 [DETECTOR_SN(whole str)] {type="PILATUS 6M", details="60-0001"}
# 2007/Jun/17 15:12:36.928 [DATE(str)] {yyyy[/-]MMM[/-]dd([ Tt:]hh(:mm(:ss)?)?)?}
# Pixel_size 172e-6 m x 172e-6 m [PIXEL_SIZE(float)*=PIXEL_SIZE_UNITS[m=1,mm=1000], PIXEL_SIZE_UNITS(str) ...] {value1 (m|mm|um)( x value2 (m|mm|um))?, default=1}
# Silicon sensor, thickness 0.000320 m
# Exposure_time 0.995000 s
# Exposure_period 1.000000 s
# Tau = 194.0e-09 s
# Count_cutoff 1048575 counts [CCD_IMAGE_SATURATION (int=65535)]
# Threshold_setting 5000 eV
# Wavelength 1.2398 A [WAVELENGTH(float)]
# Energy_range (0, 0) eV {value[value]? eV, default=0}
# Detector_distance 0.15500 m [DISTANCE(float)*=DISTANCE_UNITS[m=1,mm=1000], DISTANCE_UNITS(str)]
# Detector_Voffset -0.01003 m [DETECTOR_SN]
# Beam_xy (1231.00, 1277.00) pixels [BEAM_CENTER_X(float)*=PIXEL_SIZE, BEAM_CENTER_Y(float)*=PIXEL_SIZE] {\(value1(->mm|->pixels|->bins|.)?(, value2)?\), default=1 pixel, 2 units must be compatible} //swapped in pilatus_minicbf.py
# Flux 22487563295 ph/s
# Filter_transmission 0.0008
# Start_angle 13.0000 deg. [OSC_START(float)]
# Angle_increment 1.0000 deg. [OSC_RANGE(float)]
# Detector_2theta 0.0000 deg. [TWOTHETA(float)]
# Polarization 0.990
# Alpha 0.0000 deg.
# Omega 0.0000 deg. [OMEGA(float)] //missing from miniCBF example
# Kappa 0.0000 deg.
# Phi 0.0000 deg. [PHI(float)]
# Chi 0.0000 deg.
# Oscillation_axis  X, CW [AXIS(str)]
# N_oscillations 1

X-Binary-Size-Second-Dimension [size1]
X-Binary-Size-Fastest-Dimension [size2]
sizem=size1*size2/1024/1024; sizem<1 ? sizem*10 : sizem [vendortype="Pilatus-"round(sizem)"sizem<1 ? "K" : "M"]
*/
//-----------------------------------------------------------------------------

//cbfhandle2header
void getHeader( cbf_handle cbf, CCBFDataParameters *pCBFDPs ) {
	CCallTrace ct( __PRETTY_FUNCTION__ );
  try {
    if( NULL == pCBFDPs )
        throw JNIException( RUNTIME_EXCEPTION, "getHeader: Argument pCBFDPs is NULL" );
    cbf_checkerror( cbf_rewind_datablock(cbf) );
	/* Find the binary data */
consoleMessage("getHeader.cbf_find_category");
    cbf_checkerror( cbf_find_category( cbf, "array_data" ) );
consoleMessage("getHeader.cbf_find_column");
    cbf_checkerror( cbf_find_column( cbf, "data" ) );
/*consoleMessage("getHeader.cbf_count_rows");
int nr = 0;
	cbf_checkerror( cbf_count_rows  ( cbf, &nr ) );
sprintf( testCounterBuf, "getHeader, cbf_count_rows, %d", nr );
consoleMessage( testCounterBuf );
consoleMessage("getHeader.cbf_get_typeofvalue");
  cbf_checkerror( cbf_get_typeofvalue ( cbf, &p) );
consoleMessage(p);
*/
consoleMessage("getHeader.cbf_get_integerarrayparameters_wdims_fs");
    cbf_checkerror( cbf_get_integerarrayparameters_wdims_fs(cbf,
      &pCBFDPs->compression, &pCBFDPs->binary_id, &pCBFDPs->elsize, &pCBFDPs->elsigned,
      &pCBFDPs->elunsigned, &pCBFDPs->elements, &pCBFDPs->minelement, &pCBFDPs->maxelement,
      &pCBFDPs->byteorder, &pCBFDPs->dimfast, &pCBFDPs->dimmid, &pCBFDPs->dimslow, &pCBFDPs->padding) );
consoleMessage( CString( CSFORMAT, "getHeader.cbf_get_integerarrayparameters_wdims_fs"
		  ", compression=%u, binary_id=%d, elsize=%lu, elsigned=%d, elunsigned=%d"
		  ", elements=%lu, minelement=%d, maxelement=%d, byteorder=%s"
		  ", dimfast=%lu, dimmid=%lu, dimslow=%lu, padding=%lu",
		  pCBFDPs->compression, pCBFDPs->binary_id, (unsigned long)pCBFDPs->elsize, pCBFDPs->elsigned,
		    pCBFDPs->elunsigned, (unsigned long)pCBFDPs->elements, pCBFDPs->minelement, pCBFDPs->maxelement,
		    pCBFDPs->byteorder, (unsigned long)pCBFDPs->dimfast, (unsigned long)pCBFDPs->dimmid,
		    (unsigned long)pCBFDPs->dimslow, (unsigned long)pCBFDPs->padding ) );

//consoleMessage("getHeader.cbf_find_column(header_contents)");
//    cbf_checkerror( cbf_find_column( cbf, "header_contents" ) );
consoleMessage("getHeader.cbf_find_tag(_array_data.header_contents)");
    cbf_checkerror( cbf_find_tag( cbf, "_array_data.header_contents" ) );
	char *header_info;
	cbf_checkerror( cbf_get_value( cbf,(const char * *)&header_info ) );
consoleMessage( CString( CSPARTS, "getHeader header_contents=", header_info ) );
	//...;
	cmatch what;
	if( !regex_search( header_info, what, regex( "^# Beam_xy \\((-?[0-9.]+), (-?[0-9.]+)\\) pixels$" ) ) )
      throw JNIException( RUNTIME_EXCEPTION, CString( CSFORMAT, "header_info: Beam_xy not recognized" ) );
	float BeamX, BeamY;
	try {
		BeamX = boost::lexical_cast<float>( what[1] );
	} catch( boost::bad_lexical_cast const &e ) {
      throw JNIException( RUNTIME_EXCEPTION, CString( CSFORMAT, "header_info: Beam_xy recognized, but X is not interpretable" ) );
	}
	try {
		BeamY = boost::lexical_cast<float>( what[2] );
	} catch( boost::bad_lexical_cast const &e ) {
      throw JNIException( RUNTIME_EXCEPTION, CString( CSFORMAT, "header_info: Beam_xy recognized, but Y is not interpretable" ) );
	}
	consoleMessage( CString( CSPARTS, "Beam_xy=", BeamX, ", ", BeamY ) );
//    cbf_parse_sls_header(minicbf, header_info, 0);
  } catch( JNIException &e ) {
	throw;
  }
}

void getWidthHeight( cbf_handle cbf, int *width, int *height ) {
	CCallTrace ct( __PRETTY_FUNCTION__ );
//http://www.bernstein-plus-sons.com/software/CBF/examples/cbf2adscimg_sub.c
//http://http://www.ccp4.ac.uk/dist/x-windows/Mosflm/mosflm/pilatus_c.c
consoleMessage("getWidthHeight");
//*width = 2463;
//*height = 2527;
//return 0;
  try {
    *width = 0;
    *height = 0;
consoleMessage("getWidthHeight.getHeader");
    CCBFDataParameters CBFDPs;
    getHeader( cbf, &CBFDPs ); //Read header...
//*width = 2463;
//*height = 2527;
//return 0;  
    *width = CBFDPs.dimfast;
    *height = CBFDPs.dimmid;
    if( *width == 0 || *height == 0 )
      throw JNIException( RUNTIME_EXCEPTION, CString( CSPARTS, "Wrong dimension value, ", *width, "x", *height ) );
  } catch( JNIException &e ) {
	throw;
  }
}

void loadImage( cbf_handle cbf, int width, int height, float *data ) {
	CCallTrace ct( __PRETTY_FUNCTION__ );
	int *int_data = NULL;
	class CFinally {
	protected:
		int **int_data;
	public:
		CFinally( int **int_data ) : int_data( int_data ) {
		}
		~CFinally() {
			if( *int_data != NULL ) {
				free( *int_data );
				*int_data = NULL;
			}
		}
	} finally( &int_data );
consoleMessage("cbfhandle2img_sub");
  try {
    if( NULL == data )
      throw JNIException( RUNTIME_EXCEPTION, "loadImage: Argument data is NULL" );
    cbf_checkerror( cbf_rewind_datablock(cbf) );
    unsigned long memsize = width * height * sizeof(int);
    if( NULL == ( int_data = (int *)malloc( memsize ) ) )
      throw JNIException( RUNTIME_EXCEPTION, CString( CSFORMAT, "loadImage: Cannot allocate %lu bytes of memory", memsize ) );
    /* Find the binary data */
    cbf_checkerror( cbf_find_category( cbf, "array_data" ) );
    cbf_checkerror( cbf_find_column  ( cbf, "data" ) );
    int id;
    size_t nelem_read;
    cbf_checkerror( cbf_get_integerarray( cbf, &id, int_data, sizeof(int), 1, width * height, &nelem_read) );
    for( int i = 0; i < height; i++ ) {
      int istart = i * width;
      /* Store the frame in the image array */
      for( int j = 0; j < width; j++ ) {
        int val = int_data[ istart + j ];
        if( val == 0x100000 - 1 )
          val = -2;
        data[ istart + j ] = val;
      }
    }
  } catch( JNIException &e ) {
	throw;
  }
}

/* CBFlib assigns a file to the cbf_handle when reading or writing, then removes from the cbf_handle. */
void loadFileToCBF( FILE *&in, cbf_handle cbf ) {
	CCallTrace ct( __PRETTY_FUNCTION__ );
  try {
    int result = cbf_read_widefile( cbf, in, MSG_DIGESTWARN );
    in = NULL; //cbf_read_widefile closes it, we have to sign it
    if( result )
      throw JNIException( RUNTIME_EXCEPTION, "Cannot read binary data from image file" );
  } catch( JNIException &e ) {
	throw;
  }
}

void loadFileToCBF( const char *filename, cbf_handle &cbf ) {
	CCallTrace ct( __PRETTY_FUNCTION__ );
	  FILE *in = NULL;
	class CFinally {
	protected:
		myJNIEnv *env;
		FILE **file;
	public:
		CFinally( myJNIEnv *env, FILE **file )
		: env( env ), file( file ) {
		}
		~CFinally() {
			if( *file != NULL ) {
				fclose( *file );
				*file = NULL;
			}
		}
	} finally( &gEnv, &in );
  static int testCounter = 0;
  try {
    /* http://en.wikipedia.org/wiki/Java_Native_Interface says that
       GetStringChars() should be used, then <somehow> convert from
       UTF-16 to UTF-8. Until <somehow> identified, using GetStringUTFChars().
     */
consoleMessage( CString( CSPARTS, "LOADING ", filename, " as ", testCounter++, ". file" ) );
/* Open the image */
    if( ( in = fopen( filename, "rb" ) ) == NULL )
    	cout << "in failed: " << in << endl;
//      throw JNIException( RUNTIME_EXCEPTION, CString( CSPARTS, "Cannot open image file: ", filename ) );
consoleMessage("loadFileToCBF");
	loadFileToCBF( in, cbf );
  } catch( JNIException &e ) {
	throw;
  }
}

void getHeaderKeysAndValues( cbf_handle cbf, CCBFDataParameters *pCBFDPs ) {
	CCallTrace ct( __PRETTY_FUNCTION__ );
consoleMessage("getHeaderKeysAndValues");
  try {
//break;
//"_array_data.header_convention SLS_1.0"
//"_array_data.header_contents"

consoleMessage("getHeaderKeysAndValues.cbf_rewind_datablock");
    cbf_checkerror( cbf_rewind_datablock(cbf) );
    getHeader( cbf, pCBFDPs ); //Read header...
  } catch( JNIException &e ) {
	throw;
  }
}

void loadFileToCBF( const CString &filename, cbf_handle &cbf ) {
	CCallTrace ct( __PRETTY_FUNCTION__ );
	loadFileToCBF( filename.c_str(), cbf );
}

/* java: PilatusData PilatusLoader.loadPilatus( String filePath ) */
JNIEXPORT jobject JNICALL Java_loaders_pilatus_PilatusLoader_loadPilatus
(JNIEnv *env, jclass jThis, jstring jFilename) {
  cbf_handle cbf = NULL;
  jfloatArray jData = NULL;
  const char *filename = NULL;
  float *data = NULL;
	class CFinally {
	protected:
		myJNIEnv *env;
		jstring jFilename;
		const char **filename;
		cbf_handle *cbf;
		jfloatArray *jData;
		float **data;
	public:
		CFinally( myJNIEnv *env, jstring jFilename, const char **filename, jfloatArray *jData, float **data, cbf_handle *cbf )
		: env( env ), jFilename( jFilename), filename( filename ), cbf( cbf ), jData( jData ), data( data ) {
		}
		~CFinally() {
			if( *filename != NULL ) {
			    env->releaseStringUTFChars( jFilename, *filename );
				*filename = NULL;
				if( *cbf != NULL ) {
					cbf_free_handle( *cbf );
					*cbf = NULL;
					if( *jData != NULL ) {
						if( *data != NULL ) {
							env->releaseFloatArrayElements( *jData, *data, JNI_ABORT );
							*data = NULL;
						}
						*jData = NULL; //Can not free this, probably Java will do it
					}
				}
			}
		}
	} finally( &gEnv, jFilename, &filename, &jData, &data, &cbf );
  jclass jclassPF = NULL;
  jmethodID methodId = NULL;
  jobject jPD = NULL;
  try {
    gEnv.setEnv( env );
    cbf_checkerror( cbf_make_handle( &cbf ) );
/* http://en.wikipedia.org/wiki/Java_Native_Interface says that
   GetStringChars() should be used, then <somehow> convert from
   UTF-16 to UTF-8. Until <somehow> identified, using GetStringUTFChars().
 */
	filename = gEnv.getStringUTFChars( jFilename, NULL );
    if( filename == NULL )
    	throw JNIException( OUT_OF_MEMORY_ERROR, "out of memory (GetStringUTFChars)" );
consoleMessage("Java_loaders_pilatus_PilatusLoader_loadPilatus.loadFileToCBF");
    loadFileToCBF( filename, cbf );


    int width;
    int height;
consoleMessage("getWidthHeight");
    getWidthHeight( cbf, &width, &height );
    int size = width * height;
    jData = env->NewFloatArray( size );
    if( jData == NULL )
        throw JNIException( OUT_OF_MEMORY_ERROR, "out of memory (NewFloatArray)" );
    data = env->GetFloatArrayElements( jData, NULL );
    if( data == NULL )
        throw JNIException( OUT_OF_MEMORY_ERROR, "out of memory (GetFloatArrayElements)" );
    loadImage( cbf, width, height, data );
    env->ReleaseFloatArrayElements( jData, data, 0 );
    data = NULL;
    //java: PilatusData jPD = new PilatusData( width, height, jData );
    jclassPF = env->FindClass( "loaders/pilatus/PilatusData" );
    if( jclassPF == NULL )
        throw JNIException( NO_CLASS_DEF_FOUND_ERROR, "class not found (loaders/pilatus/PilatusData)" );
    methodId = gEnv.getMethodId(jclassPF, "<init>", "(II[F)V");
    jPD = env->NewObject( jclassPF, methodId, width, height, jData );
    if( jPD == NULL )
        throw JNIException( INSTANTIATION_EXCEPTION, "constructing error (loaders/pilatus/PilatusData)" );
  } catch( JNIException &e ) {
	  gEnv.throwToJNI( e );
  }
  return jPD;
}

/* java: PilatusHeader PilatusLoader.loadHeader(String filePath) */
JNIEXPORT jobject JNICALL Java_loaders_pilatus_PilatusLoader_loadHeader
(JNIEnv *env, jclass jThis, jstring jFilename) {
  cbf_handle cbf = NULL;
  const char *filename = NULL;
	class CFinally {
	protected:
		myJNIEnv *env;
		jstring jFilename;
		const char **filename;
	    cbf_handle *cbf;
	public:
		CFinally( myJNIEnv *env, jstring jFilename, const char **filename, cbf_handle *cbf )
		: env( env ), jFilename( jFilename), filename( filename ), cbf( cbf ) {
		}
		~CFinally() {
			if( *filename != NULL ) {
			    env->releaseStringUTFChars( jFilename, *filename );
				*filename = NULL;
				if( *cbf != NULL ) {
					cbf_free_handle( *cbf );
					*cbf = NULL;
				}
			}
		}
	} finally( &gEnv, jFilename, &filename, &cbf );
//  char *value = NULL;
//  jstring jValue = NULL;
  jclass jclassPH = NULL;
  jmethodID methodId = NULL;
  jclass jclassString = NULL;
  jobject jobjectString = NULL;
  jobject jPH = NULL;
  jobjectArray jKeys = NULL;
  jobjectArray jValues = NULL;
  try {
    gEnv.setEnv( env );
consoleMessage("Java_loaders_pilatus_PilatusLoader_loadPilatus.cbf_make_handle");
    cbf_checkerror( cbf_make_handle( &cbf ) );
	filename = gEnv.getStringUTFChars( jFilename, NULL );
    if( filename == NULL )
    	throw JNIException( OUT_OF_MEMORY_ERROR, "out of memory (GetStringUTFChars)" );
consoleMessage("Java_loaders_pilatus_PilatusLoader_loadPilatus.loadFileToCBF");
    loadFileToCBF( filename, cbf );
    CCBFDataParameters CBFDPs;
consoleMessage("getHeaderKeysAndValues");
	getHeaderKeysAndValues( cbf, &CBFDPs );

    jclassPH = gEnv.getClass("loaders/pilatus/PilatusHeader");
    methodId = gEnv.getMethodId(jclassPH, "<init>", "([Ljava/lang/String;[Ljava/lang/String;)V");
    jclassString = gEnv.getClass("java/lang/String");
    jobjectString = gEnv.newStringUTF( "" );
    int size = 13; //elements in CBFDPs
    jKeys = env->NewObjectArray( size, jclassString, jobjectString );
    if( jKeys == NULL )
      throw JNIException( OUT_OF_MEMORY_ERROR, "out of memory (NewObjectArray)" );
    jValues = env->NewObjectArray( size, jclassString, jobjectString );
    if( jValues == NULL )
      throw JNIException( OUT_OF_MEMORY_ERROR, "out of memory (NewObjectArray)" );
    int i = 0;    
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Compression"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.compression))); //unsigned
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Binary-ID"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.binary_id))); //signed
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Element-Size"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.elsize))); //unsigned
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Signed"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.elsigned))); //signed
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Unsigned"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.elunsigned))); //signed
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Number-of-Elements"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.elements))); //unsigned
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Minelement"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.minelement))); //signed
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Maxelement"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.maxelement))); //signed
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Byte-Order"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.byteorder))); //string
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Fastest-Dimension"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.dimfast))); //unsigned
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Second-Dimension"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.dimmid))); //unsigned
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Slowest-Dimension"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.dimslow))); //unsigned
    env->SetObjectArrayElement(jKeys, i, env->NewStringUTF("Padding"));
    env->SetObjectArrayElement(jValues, i++, gEnv.newStringUTF( CString( CSPARTS, CBFDPs.padding))); //unsigned
    jPH = env->NewObject( jclassPH, methodId, jKeys, jValues );
    if( jPH == NULL )
      throw JNIException( INSTANTIATION_EXCEPTION, "constructing error (loaders/pilatus/PilatusHeader)" );
  } catch( JNIException &e ) {
consoleMessage("Java_loaders_pilatus_PilatusLoader_loadHeader exception caught!");
consoleMessage(e.what());
	  gEnv.throwToJNI( e );
  }
  return jPH;
}

void test() {
	CCallTrace ct( __PRETTY_FUNCTION__ );
	cbf_handle cbf = NULL;
	const char *filename = "/home/naray/workspace@eclipse/debug_dawb_workspace/data/data/MSG_xtal1/MSG_xtal1_w1_1_0004.cbf";
	try {
consoleMessage( "Java_loaders_pilatus_PilatusLoader_loadPilatus.cbf_make_handle" );
		cbf_checkerror( cbf_make_handle( &cbf ) );
consoleMessage( "Java_loaders_pilatus_PilatusLoader_loadPilatus.loadFileToCBF" );
		loadFileToCBF( filename, cbf );
		CCBFDataParameters CBFDPs;
consoleMessage( "getHeaderKeysAndValues" );
		getHeaderKeysAndValues( cbf, &CBFDPs );
		consoleMessage( CString( CSPARTS, "Byte-Order: ", CBFDPs.byteorder ) );
	} catch( JNIException &e ) {
		consoleMessage( CString( CSPARTS, e.getExceptionName(), " caught, ", e.getMessage(), ", ", e.getCallTrace() ) );
	}
}

int main(int argc, char *argv[]) {
	CCallTrace ct( __PRETTY_FUNCTION__ );
	string alma = "alma";
	int i = 3;
	CString testStr( CSPARTS, "No ez mi? ", alma, i, "\n" );
	cout << testStr.c_str();
	test();
	cout << "vege" << endl;
	return 0;
}
