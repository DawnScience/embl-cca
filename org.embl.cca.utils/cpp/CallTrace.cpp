/*
 * CallTrace.cpp
 *
 *  Created on: Apr 20, 2012
 *      Author: naray
 */

#include "CallTrace.h"
using namespace ccalltrace;
#include "ExceptionWCT.h"
using namespace cstring;

#include <pthread.h>

//for cout
#include <iostream>
#include <sstream>
using namespace std;

CCallTrace::TCallMap CCallTrace::callMap;

CCallTrace::CCallTrace( const char functionName[] ) {
	unsigned int currentThreadId = (unsigned int)pthread_self(); //or getpid() for process id (bah) if pthread does not exist
	TCallMap::iterator it = callMap.find( currentThreadId );
	if( it == callMap.end() )
		it = callMap.insert( std::make_pair( currentThreadId, list<CString>() ) ).first;
	it->second.push_back( CString( functionName ) );
}

CCallTrace::CCallTrace( const CString &functionName ) {
	unsigned int currentThreadId = (unsigned int)pthread_self();
	TCallMap::iterator it = callMap.find( currentThreadId );
	if( it == callMap.end() )
		it = callMap.insert( std::make_pair( currentThreadId, list<CString>() ) ).first;
	it->second.push_back( functionName );
}

CCallTrace::~CCallTrace() {
	unsigned int currentThreadId = (unsigned int)pthread_self();
	TCallMap::iterator it = callMap.find( currentThreadId );
	if( it == callMap.end() )
		throw ExceptionWCT( RUNTIME_EXCEPTION, CString( CSPARTS, "CCallTrace error: Call list of current thread not found, ", __PRETTY_FUNCTION__ ) );
	it->second.pop_back();
}

CCallTrace::CCallTrace(const CCallTrace &src) {
	*this = src;
}

CCallTrace& CCallTrace::operator= (const CCallTrace &src) {
	if( this != &src ) {
		throw ExceptionWCT( RUNTIME_EXCEPTION, "CCallTrace object does not allow copying" );
	}
	return *this;
}

TCallList CCallTrace::getCallList() {
	unsigned int currentThreadId = (unsigned int)pthread_self();
	TCallMap::iterator it = callMap.find( currentThreadId );
	if( it == callMap.end() )
		throw ExceptionWCT( RUNTIME_EXCEPTION, CString( CSPARTS, "CCallTrace error: Call list of current thread not found, ", __PRETTY_FUNCTION__ ) );
	return it->second;
}

CString CCallTrace::toString() const {
	return CCallTrace::toString( getCallList() );
}

CString CCallTrace::toString( const TCallList &callList, unsigned int lineNumber ) {
	CString result( "Call trace:\n" );
	TCallList::const_iterator itListBegin = callList.begin();
	TCallList::const_iterator itListEnd = callList.end();
	bool firstLine = true;
	while( itListBegin != itListEnd ) {
		itListEnd--;
		result.append( *itListEnd );
		if( firstLine ) {
			firstLine = false;
			if( lineNumber > 0 )
				result.append( CString( CSPARTS, ", line=", lineNumber ) );
		}
		result.append( "\n" );
	}
	return result;
}

