/*
 * CallTrace.h
 *
 *  Created on: Apr 20, 2012
 *      Author: naray
 */

#ifndef CALLTRACE_H_
#define CALLTRACE_H_

#include <map>
#include <list>
#include "String.h"

namespace ccalltrace {

typedef std::list<cstring::CString> TCallList;

class CCallTrace {
	typedef std::map<unsigned int, TCallList> TCallMap;
	static TCallMap callMap;
public:
	CCallTrace( const char functionName[] );
	CCallTrace( const cstring::CString &functionName );
	CCallTrace(const CCallTrace &src);
	virtual ~CCallTrace();
	CCallTrace& operator= (const CCallTrace &src);

	static TCallList getCallList();
	cstring::CString toString() const;
	static cstring::CString toString( const TCallList &callList, unsigned int lineNumber = 0 );
};

} // namespace

#endif /* CALLTRACE_H_ */
