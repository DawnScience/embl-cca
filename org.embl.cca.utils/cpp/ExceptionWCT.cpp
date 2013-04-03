/*
 * ExceptionWCT.cpp
 *
 *  Created on: Apr 20, 2012
 *      Author: naray
 */

#include "ExceptionWCT.h"
using namespace cstring;
using namespace ccalltrace;

ExceptionWCT::ExceptionWCT() {
}

ExceptionWCT::ExceptionWCT( const char *name, const char *msg, unsigned int lineNumber ) : exceptionName( name ), message( msg ), lineNumber( lineNumber ) {
	callList = CCallTrace::getCallList();
}

ExceptionWCT::ExceptionWCT( const char *name, const CString &msg, unsigned int lineNumber ) : exceptionName( name ), message( msg ), lineNumber( lineNumber ) {
	callList = CCallTrace::getCallList();
}

ExceptionWCT::ExceptionWCT( const CString &name, const char *msg, unsigned int lineNumber ) : exceptionName( name ), message( msg ), lineNumber( lineNumber ) {
	callList = CCallTrace::getCallList();
}

ExceptionWCT::ExceptionWCT( const CString &name, const CString &msg, unsigned int lineNumber ) : exceptionName( name ), message( msg ), lineNumber( lineNumber ) {
	callList = CCallTrace::getCallList();
}

ExceptionWCT::ExceptionWCT(const ExceptionWCT &src) {
	*this = src;
}

ExceptionWCT::~ExceptionWCT() throw() {
}

ExceptionWCT& ExceptionWCT::operator= (const ExceptionWCT &src) {
	if( this != &src ) {
		this->exceptionName = src.exceptionName;
		this->message = src.message;
		this->callList = src.callList;
		this->lineNumber = src.lineNumber;
	}
	return *this;
}

const char* ExceptionWCT::what() const throw() {
	return message.c_str();
}

CString ExceptionWCT::getExceptionName() const {
	return exceptionName;
}

CString ExceptionWCT::getMessage() const {
	return message;
}

CString ExceptionWCT::getCallTrace() const {
	return CCallTrace::toString( callList, lineNumber );
}
