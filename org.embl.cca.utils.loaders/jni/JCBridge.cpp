/*
 * JCBridge.cpp
 *
 *  Created on: Apr 16, 2012
 *      Author: naray
 */

#include "JCBridge.h"
using namespace cstring;

static CString STR_CLASS_BY_NAME_NOT_FOUND( "class by name not found" );
static CString STR_CLASS_BY_OBJECT_NOT_FOUND( "class by object not found" );
static CString STR_NO_SUCH_FIELD_ERROR( "field not found" );
static CString STR_METHOD_NOT_FOUND( "method not found" );
static CString STR_OBJECT_NOT_FOUND( "object not found" );
static CString STR_OUT_OF_MEMORY( "out of memory" );

static CString COLONSPACE( ": " );
static CString COMMASPACE( ", " );

myJNIEnv::myJNIEnv() {
	this->env = NULL;
}

myJNIEnv::myJNIEnv( JNIEnv *env ) {
	this->env = env;
}

void myJNIEnv::setEnv( JNIEnv *env ) {
	this->env = env;
}

void myJNIEnv::throwToJNI(const CString &name, const CString &msg) const {
  throwToJNI(name.c_str(), msg.c_str());
}

void myJNIEnv::throwToJNI(const char *name, const char *msg) const {
	  jclass cls = env->FindClass(name);
	  if (cls != NULL)
	    env->ThrowNew(cls, msg);
	  else {
	    cls = env->FindClass(NO_CLASS_DEF_FOUND_ERROR.c_str());
	    if (cls != NULL)
	      env->ThrowNew(cls, CString( CSPARTS, name, ". Original exception: ", msg ).c_str());
	  }
	  /* free the local ref */
	  if( cls != NULL )
	    env->DeleteLocalRef(cls);
}

void myJNIEnv::throwToJNI(const JNIException &e) const {
	throwToJNI( e.getExceptionName(), e.getMessage() );
}

jclass myJNIEnv::getClass( const char* className ) const {
	  jclass jClassResult = env->FindClass( className );
	  if( jClassResult == NULL )
	    throw JNIException( NO_CLASS_DEF_FOUND_ERROR, CString( CSPARTS, STR_CLASS_BY_NAME_NOT_FOUND, COLONSPACE, className ) );
	  return jClassResult;
}

jclass myJNIEnv::getClass( const CString &className ) const {
	return getClass( className.c_str() );
}

jclass myJNIEnv::getObjectClass( jobject jobjectObject ) const {
  jclass jClassResult = env->GetObjectClass( jobjectObject );
  if( jClassResult == NULL )
    throw JNIException( NO_CLASS_DEF_FOUND_ERROR, CString( CSPARTS, STR_CLASS_BY_OBJECT_NOT_FOUND ) );
  return jClassResult;
}

jmethodID myJNIEnv::getMethodId( jclass jclassClass, const char* methodName, const char *args ) const {
  jmethodID jMethodResult = env->GetMethodID( jclassClass, methodName, args);
  if( jMethodResult == NULL )
    throw JNIException( NO_SUCH_METHOD_ERROR, CString( CSPARTS, STR_METHOD_NOT_FOUND, COLONSPACE, methodName, COMMASPACE, args ) );
  return jMethodResult;
}

jfieldID myJNIEnv::getStaticFieldId( jclass jclassClass, const char* fieldName, const char *className ) const {
  jfieldID jfieldIDResult = env->GetStaticFieldID( jclassClass, fieldName, className );
  if( jfieldIDResult == NULL )
    throw JNIException( NO_SUCH_METHOD_ERROR, CString( CSPARTS, STR_NO_SUCH_FIELD_ERROR, COLONSPACE, fieldName, COMMASPACE, className ) );
  return jfieldIDResult;
}

jobject myJNIEnv::getStaticObjectField( jclass jclassClass, jfieldID jfieldField ) const {
  jobject jobjectResult = env->GetStaticObjectField( jclassClass, jfieldField );
  if( jobjectResult == NULL )
    throw JNIException( NO_SUCH_METHOD_ERROR, CString( CSPARTS, STR_OBJECT_NOT_FOUND ) );
  return jobjectResult;
}
/*
jobject myJNIEnv::newObject( jclass jclassClass, jmethodID jmethodMethod, jobjectArray , jobjectArray ) const {
jPH = env->NewObject( jclassPH, methodId, jKeys, jValues );
if( jPH == NULL )
  throw JNIException( INSTANTIATION_EXCEPTION, "constructing error (loaders/pilatus/PilatusHeader)" );
*/

jstring myJNIEnv::newStringUTF( const char *bytes ) const {
	jstring jstringResult = env->NewStringUTF( bytes );
	if( jstringResult == NULL )
	    throw JNIException( OUT_OF_MEMORY_ERROR, CString( CSPARTS, STR_OUT_OF_MEMORY, COLONSPACE, bytes ) );
	return jstringResult;
}

jstring myJNIEnv::newStringUTF( const CString &bytes ) const {
	return newStringUTF( bytes.c_str() );
}

const char* myJNIEnv::getStringUTFChars( jstring string, jboolean *isCopy ) const {
	const char *charsResult = env->GetStringUTFChars( string, isCopy );
	if( charsResult == NULL )
	    throw JNIException( OUT_OF_MEMORY_ERROR, STR_OUT_OF_MEMORY );
	return charsResult;
}

void myJNIEnv::releaseStringUTFChars( jstring string, const char *utf ) const {
	env->ReleaseStringUTFChars( string, utf );
}

void myJNIEnv::releaseFloatArrayElements( jfloatArray array, jfloat *elems, jint mode ) const {
	env->ReleaseFloatArrayElements( array, elems, mode );
}

//http://gcc.gnu.org/java/jni-comp.txt
void myJNIEnv::SystemOutPrintln( const char *string ) const {
  class CFinally {
  protected:
	JNIEnv *env;
	jstring jText;
  public:
	CFinally( JNIEnv *env ) : env( env ), jText( NULL ) {
	}
	~CFinally() {
	  if( jText != NULL ) {
	    env->DeleteLocalRef( jText );
	    jText = NULL;
	  }
	}
	void setJText(jstring jText) {
	  this->jText = jText;
	}
  } finally( env );
  jclass jclassSystem = NULL;
  jfieldID fieldOut = NULL;
  jobject objectSystemOut = NULL;
  jclass jclassPrintStream = NULL;
  jmethodID methodPrintln = NULL;
  jclassSystem = getClass( "java/lang/System" );
  fieldOut = getStaticFieldId( jclassSystem, "out", "Ljava/io/PrintStream;" );
  objectSystemOut = getStaticObjectField( jclassSystem, fieldOut );
  jclassPrintStream = getObjectClass( objectSystemOut );
  methodPrintln = getMethodId( jclassPrintStream, "println", "(Ljava/lang/String;)V" );
  jstring jText = env->NewStringUTF( string );
  finally.setJText( jText );
  env->CallVoidMethod( objectSystemOut, methodPrintln, jText );
}

void myJNIEnv::SystemOutPrintln( const CString &string ) const {
	SystemOutPrintln( string.c_str() );
}
