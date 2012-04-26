/*
 * JNIException.cpp
 *
 *  Created on: Apr 20, 2012
 *      Author: naray
 */

#include "JNIException.h"
using namespace cstring;
using namespace ccalltrace;

JNIException::JNIException() {
}

JNIException::JNIException( const char *name, const char *msg, unsigned int lineNumber ) : exceptionName( name ), message( msg ), lineNumber( lineNumber ) {
	callList = CCallTrace::getCallList();
}

JNIException::JNIException( const char *name, const CString &msg, unsigned int lineNumber ) : exceptionName( name ), message( msg ), lineNumber( lineNumber ) {
	callList = CCallTrace::getCallList();
}

JNIException::JNIException( const CString &name, const char *msg, unsigned int lineNumber ) : exceptionName( name ), message( msg ), lineNumber( lineNumber ) {
	callList = CCallTrace::getCallList();
}

JNIException::JNIException( const CString &name, const CString &msg, unsigned int lineNumber ) : exceptionName( name ), message( msg ), lineNumber( lineNumber ) {
	callList = CCallTrace::getCallList();
}

JNIException::JNIException(const JNIException &src) {
	*this = src;
}

JNIException::~JNIException() throw() {
}

JNIException& JNIException::operator= (const JNIException &src) {
	if( this != &src ) {
		this->exceptionName = src.exceptionName;
		this->message = src.message;
		this->callList = src.callList;
		this->lineNumber = src.lineNumber;
	}
	return *this;
}

const char* JNIException::what() const throw() {
	return message.c_str();
}

CString JNIException::getExceptionName() const {
	return exceptionName;
}

CString JNIException::getMessage() const {
	return message;
}

CString JNIException::getCallTrace() const {
	return CCallTrace::toString( callList, lineNumber );
}
