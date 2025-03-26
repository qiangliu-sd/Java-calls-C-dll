// cppStaticLib.cpp : Defines the functions for the static library.
//

/* ---Compiling LIB---
REMOVE framework.h, pch.h, pch.cpp from Project

VS Compiler setting:
	C/C++ -> Advanced: 
		set [Calling Convention] to [__cdecl (/Gd)]
	C/C++ -> Precompiled Headers: 
		set [Precompiled Headers] to [Not Using Precompiled Headers]
*/

#include "qlcpptools.h"


// an example of a library function
double stdev_Cpp(const std::vector<double> & vd)
{
	double _sum(0.0), _sq_sum(0.0);
	for (auto & x: vd) {
		_sum += x;
		_sq_sum += x * x;
	}
	auto _1over_sz = 1.0 / vd.size();
	auto _mean = _sum * _1over_sz;
	return sqrt(_sq_sum * _1over_sz - _mean * _mean);
}
