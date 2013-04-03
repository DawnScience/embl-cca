/*
 * String.h
 *
 *  Created on: Mar 20, 2012
 *      Author: naray
 */

#ifndef CSTRING_H
#define CSTRING_H

#include <stdarg.h>
#include <string>
#include <sstream>

namespace cstring {

typedef enum { CSFORMAT } TCSFORMAT;
typedef enum { CSPARTS } TCSPARTS;

class CString : public std::string {
public:

	CString( );
    CString( const std::string& str );
    CString( const std::string& str, size_t pos, size_t n = npos );
    CString( const char * s, size_t n );
//    CString( const char * s );
    CString( size_t n, char c );
//    template<class InputIterator> CString( InputIterator begin, InputIterator end ) : std::string( begin, end ) {
//	} //Problem with this constructor is conflicting with CString(const char *format, ...) in case when ... is const char *
    CString( const CString& str );
    CString( TCSFORMAT dummy, const char *format, ... );
    CString& operator= ( const CString& str );
    CString& operator= ( const char* s );
    CString& operator= ( char c );
    void formatThis(const char* szFormat, ...);
	static void format(CString &result, const char* szFormat, va_list ap);
    static CString format( const char* szFormat, va_list ap );
/*
        static CString format(const char* szFormat, va_list ap);
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
	static CString format( const char* szFormat, ... );

	template <class t1>
    CString( TCSPARTS dummy, t1 p1 ) {
		std::ostringstream o;
		o << p1;
		*this = o.str();
	}
	template <class t1, class t2>
    CString( TCSPARTS dummy, t1 p1, t2 p2 ) {
		std::ostringstream o;
		o << p1 << p2;
		*this = o.str();
	}
	template <class t1, class t2, class t3>
    CString( TCSPARTS dummy, t1 p1, t2 p2, t3 p3 ) {
		std::ostringstream o;
		o << p1 << p2 << p3;
		*this = o.str();
	}
	template <class t1, class t2, class t3, class t4>
    CString( TCSPARTS dummy, t1 p1, t2 p2, t3 p3, t4 p4 ) {
		std::ostringstream o;
		o << p1 << p2 << p3 << p4;
		*this = o.str();
	}
	template <class t1, class t2, class t3, class t4, class t5>
    CString( TCSPARTS dummy, t1 p1, t2 p2, t3 p3, t4 p4, t5 p5 ) {
		std::ostringstream o;
		o << p1 << p2 << p3 << p4 << p5;
		*this = o.str();
	}
	template <class t1, class t2, class t3, class t4, class t5, class t6>
    CString( TCSPARTS dummy, t1 p1, t2 p2, t3 p3, t4 p4, t5 p5, t6 p6 ) {
		std::ostringstream o;
		o << p1 << p2 << p3 << p4 << p5 << p6;
		*this = o.str();
	}
	template <class t1, class t2, class t3, class t4, class t5, class t6, class t7>
    CString( TCSPARTS dummy, t1 p1, t2 p2, t3 p3, t4 p4, t5 p5, t6 p6, t7 p7 ) {
		std::ostringstream o;
		o << p1 << p2 << p3 << p4 << p5 << p6 << p7;
		*this = o.str();
	}
	template <class t1, class t2, class t3, class t4, class t5, class t6, class t7, class t8>
    CString( TCSPARTS dummy, t1 p1, t2 p2, t3 p3, t4 p4, t5 p5, t6 p6, t7 p7, t8 p8 ) {
		std::ostringstream o;
		o << p1 << p2 << p3 << p4 << p5 << p6 << p7 << p8;
		*this = o.str();
	}
};

} // namespace

#endif // CSTRING_H
