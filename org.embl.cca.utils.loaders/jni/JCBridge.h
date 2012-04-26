/*
 * JCBridge.h
 *
 *  Created on: Apr 16, 2012
 *      Author: naray
 */

#ifndef JCBRIDGE_H_
#define JCBRIDGE_H_

#include "String.h"
#include "JNIException.h"

#include <jni.h>

//using namespace std;

class myJNIEnv {
protected:
	JNIEnv *env;
//	mutable char tempBuffer[ 256 ];
//	const int tempBufferSize;

public:
	myJNIEnv();
	myJNIEnv( JNIEnv *env );

	void setEnv( JNIEnv *env );
	bool isUsable() { return env != NULL; }

	void throwToJNI(const cstring::CString &name, const cstring::CString &msg) const;
	void throwToJNI(const char *name, const char *msg) const;
	void throwToJNI(const JNIException &e) const;

/*
	#define getClassWTB(env, className, result) { if( (result = env->getClassWT(className)) == NULL ) break; }
	#define getObjectClassWTB(env, jobjectObject, result) { if( (result = env.getObjectClassWT(jobjectObject)) == NULL ) break; }
	#define getMethodIdWTB(env, jclassClass, methodName, args, result) { if( (result = env.getMethodIdWT(jclassClass, methodName, args)) == NULL ) break; }
	#define getStaticFieldIdWTB(env, jclassClass, fieldName, className, result) { if( (result = env.getStaticFieldIdWT(jclassClass, fieldName, className)) == NULL ) break; }
	#define getStaticObjectFieldWTB(env, jclassClass, jfieldField, result) { if( (result = env.getStaticObjectFieldWT(jclassClass, jfieldField)) == NULL ) break; }
*/
	jclass getClass( const char* className ) const;
	jclass getClass( const cstring::CString &className ) const;
	jclass getObjectClass( jobject jobjectObject ) const;
	jmethodID getMethodId( jclass jclassClass, const char* methodName, const char *args ) const;
	jmethodID getMethodId( jclass jclassClass, const cstring::CString &methodName, const cstring::CString &args ) const;
	jfieldID getStaticFieldId( jclass jclassClass, const char* fieldName, const char *className ) const;
	jfieldID getStaticFieldId( jclass jclassClass, const cstring::CString &fieldName, const cstring::CString &className ) const;
	jobject getStaticObjectField( jclass jclassClass, jfieldID jfieldField ) const;
	jstring newStringUTF( const char *bytes ) const;
	jstring newStringUTF( const cstring::CString &bytes ) const;
	const char* getStringUTFChars( jstring string, jboolean *isCopy ) const;
	void releaseStringUTFChars( jstring string, const char *utf ) const;
	void releaseFloatArrayElements( jfloatArray array, jfloat *elems, jint mode ) const;

	//http://gcc.gnu.org/java/jni-comp.txt
	void SystemOutPrintln( const char *string ) const;
	void SystemOutPrintln( const cstring::CString &string ) const;

};

#endif /* JCBRIDGE_H_ */
