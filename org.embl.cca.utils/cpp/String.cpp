/*
 * String.cpp
 *
 *  Created on: Mar 20, 2012
 *      Author: naray
 */

#include "String.h"
using namespace cstring;

#include <iostream>
#include <stdio.h>
#include <stdlib.h>
using namespace std;

CString::CString( ) : string() {
}

CString::CString( const string& str ) : string( str ) {
}

CString::CString( const string& str, size_t pos, size_t n ) : string( str, pos, n ) {
}

CString::CString( const char * s, size_t n ) : string( s, n ) {
}

//CString::CString( const char * s ) : string( s ) {};

CString::CString( size_t n, char c ) : string( n, c ) {
}

CString::CString(const CString& src) {
	*this = src;
}

CString::CString(TCSFORMAT dummy, const char *format, ...) : string() {
        va_list arguments;
        va_start( arguments, format ); /* Initializing arguments to use all values after format */
        CString::format( *this, format, arguments );
        va_end ( arguments );                  // Cleans up the list
}

CString& CString::operator= ( const CString& src ) {
	if( this != &src ) {
		this->assign( src );
	}
	return *this;
}

CString& CString::operator= ( const char* s ) {
	this->assign( s );
	return *this;
}

CString& CString::operator= ( char c ) {
	this->assign( 1, c );
	return *this;
}

void CString::formatThis(const char* szFormat, ...) {
        va_list ap;
        va_start(ap, szFormat);
        format( *this, szFormat, ap );
        va_end(ap);
}

void CString::format(CString &result, const char* szFormat, va_list ap) {
        char *str;
	cout << "CString::format(2), szFormat=" << szFormat << endl;
        if( vasprintf( &str, szFormat, ap ) < 0 )
	  cout << "CString::format(2) error" << endl;
          ;//throw ;
        result.assign( str );
	cout << "CString::format(2), result=" << result << endl;
        free( str );
}

CString CString::format(const char* szFormat, va_list ap) {
        CString strReturn;
	format( strReturn, szFormat, ap );
        return strReturn;
}
/*
CString CString::format(const char* szFormat, va_list ap) {
        int     n = 255;
        string  strProc;
        string  strReturn;
  cout << "CString::format, szFormat=" << szFormat << endl;
        try {
          while (true) {
 cout << "CString::format 2" << endl;
            strProc.reserve((n > -1)?(n + 1):(n << 1));
 cout << "CString::format 3" << endl;
            n = vsnprintf((char*)strProc.data(), strProc.capacity(), szFormat, ap); // see Win32Type.h
 cout << "CString::format 4, n=" << n << endl;
            if ((n > -1) && (n < (int)strProc.capacity())) {
cout << "CString::format 5" << endl;
              strReturn = strProc.data();
              return strReturn;
            }
          }
        } catch(...) {
        }
        return "";
}
*/
        /**
         * Format function for CString
         *
         * @note  While the return value of this function is an CString,
         *        due to the use of vnsprintf internally to format the string
         *        data it is not possible to pass basic_string's as parameters to
         *        be formatted, however the .c_str() function of basic_string can
         *        be used to pass the pointer to the start of the basic_string's
         *        internal buffer
         *
         * @param  char*  [in]  Format string
         * @param  ...    [in]  Format parameters
         */
CString CString::format(const char* szFormat, ...) {
        va_list ap;
        va_start(ap, szFormat);
        CString result = format( szFormat, ap );
        va_end(ap);
        return result;
}

