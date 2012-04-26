/*
 * JNIException.h
 *
 *  Created on: Apr 20, 2012
 *      Author: naray
 */

#ifndef JNIEXCEPTION_H_
#define JNIEXCEPTION_H_

#include "String.h"
#include "CallTrace.h"

static cstring::CString NO_CLASS_DEF_FOUND_ERROR( "java/lang/NoClassDefFoundError" );
static cstring::CString NO_SUCH_FIELD_ERROR( "java/lang/NoSuchFieldError" );
static cstring::CString NO_SUCH_METHOD_ERROR( "java/lang/NoSuchMethodError" );
static cstring::CString INSTANTIATION_EXCEPTION( "java/lang/InstantiationException" );
static cstring::CString RUNTIME_EXCEPTION( "java/lang/RuntimeException" );
static cstring::CString OUT_OF_MEMORY_ERROR( "java/lang/OutOfMemoryError" );

/**
*  @brief Exception thrown by myJNIEnv.
*
*  @c JNIException (or classes derived from it) is used to report JNI errors
* */
class JNIException : public std::exception {
protected:
	cstring::CString exceptionName;
	cstring::CString message;
	ccalltrace::TCallList callList;
	unsigned int lineNumber;
public:
	JNIException();
	JNIException( const char *name, const char *msg, unsigned int lineNumber = 0 );
	JNIException( const char *name, const cstring::CString &msg, unsigned int lineNumber = 0 );
	JNIException( const cstring::CString &name, const char *msg, unsigned int lineNumber = 0 );
	JNIException( const cstring::CString &name, const cstring::CString &msg, unsigned int lineNumber = 0 );
	JNIException(const JNIException &src);
	// This declaration is not useless:
	// http://gcc.gnu.org/onlinedocs/gcc-3.0.2/gcc_6.html#SEC118
	virtual ~JNIException() throw(); //throw() inherited, else i would junk it
	JNIException& operator= (const JNIException &src);
	virtual const char* what() const throw(); //throw() inherited, else i would junk it
	cstring::CString getExceptionName() const;
	cstring::CString getMessage() const;
	cstring::CString getCallTrace() const;
};

#endif /* JNIEXCEPTION_H_ */
