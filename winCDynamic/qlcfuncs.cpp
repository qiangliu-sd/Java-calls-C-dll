#pragma once

/* 	do NOT work if header file qlcfuncs.h is present!

 ---Windows cdll---
	USE the default [__cdecl] calling convention for C/C++

	int __cdecl func (int a):
		The C calling convention [__cdecl] decorates the func-name as _func

	[__declspec(dllexport)] does the following:
		If function is exported with [__cdecl],
		it strips the leading underscore [ _ ] when the func-name is exported
*/


/* ---Compiling DLL: VS2022---
REMOVE dllmain.cpp, framework.h, pch.h, pch.cpp from Project

VS2022 Compiler setting:
	C/C++ -> Advanced: 
		set [Calling Convention] to [__cdecl (/Gd)]
	C/C++ -> Precompiled Headers: 
		set [Precompiled Headers] to [Not Using Precompiled Headers]
*/

// works for <<<Python, Java, C#>>>, if not noted otherwise

#include <iostream>
#include <string>

using namespace std;

extern "C" __declspec(dllexport) void qlStrArg(char *s);
// [ create_string_buffer(b-string) ] Python; [MemorySegment(String)] Java; [string] C#
void qlStrArg(char *s) {
	string _str(s);
	cout << "print INSIDE Windows C-dll: "<< _str << endl;
	/*while (*s != '\0') std::cout << *s; ++s;	// works!
	std::cout << std::endl;*/
}


// default: C functions return [c_int] to Python
extern "C" __declspec(dllexport) int qlIntRetArgs(int x, int y);
// [c_int] Python; [int] Java; [int] C#
int qlIntRetArgs(int x, int y) {
	return x + y;
}

extern "C" __declspec(dllexport) double qlDblRetArgs(double x, double y);
// [c_double] Python; [double] Java; [double] C#
double qlDblRetArgs(double x, double y) {
	return x + y;
}


// wrapper for static-lib in C++
#ifdef _QL__INCLUDE_STATIC_LIB_
#include "../libc/qlcpptools.h"
//using DBL_VEC = std::vector<double>;
extern "C" __declspec(dllexport) double qlArrayArg(double arr[], int size);
// [ctypes-array: c_double * 5] Python; [double[] ] Java; [double[] ] C#
double qlArrayArg(double arr[], int size) {
	vector<double> _vd(arr, arr + size);
	return stdev_Cpp(_vd);   // call func defined in qlcpptools.h
}
#endif // _QL__INCLUDE_STATIC_LIB_


// use array to pack and fetch multiple values
extern "C" __declspec(dllexport) void qlArrayFetch(double arr[], int sz);
// [ctypes-array: c_double * 5] Python; [double[] ] Java; [double[] ] C#
void qlArrayFetch(double arr[], int sz) {
	for (int k = 0; k < sz; ++k)
		arr[k] = 80.1 - 20 * k;
}

// use array to pack values and return [void*] (all types)
// 'new'ed memory is not 'free'd: NOT recommended!
extern "C" __declspec(dllexport) void* qlArrayRet(int sz);
// [POINTER(c_double)] Python; [double[] ] Java; [ IntPtr ] C#
void* qlArrayRet(int sz) {
	double* _da = new double[sz];
	for (int k = 0; k < sz; ++k)
		_da[k] = 80.2 - 20 * k;	
	return _da;
}


extern "C" struct C_struct {
	double x;
	double y;
	double z;
	// Ctor: C++ (NOT C)
	C_struct(double p, double d, double g) : x(p), y(d), z(g) {};
};

// use struct to pack and fetch multiple values
extern "C" __declspec(dllexport) void qlStructFetch(C_struct* ptr);
// [POINTER(PyC_struct)] Python; [ MemorySegment(StructLayout) ] Java; [ ref Cs_struct ] C#
void qlStructFetch(C_struct* ptr) {
	ptr->x = 10.1; ptr->y = 30.1; ptr->z = 90.1;
}

// Warning C4190: returns UDT 'C_struct' which is incompatible with C
extern "C" __declspec(dllexport) C_struct qlStructRet(void);
// [PyC_struct] Python; [Cs_struct] C#
C_struct qlStructRet(void) { return C_struct(10.2, 30.2, 90.2); }

// 'new'ed memory is not 'free'd: NOT recommended!
extern "C" __declspec(dllexport) C_struct* qlStructPtrRet(void);
// [MemorySegment(StructLayout)] Java
C_struct* qlStructPtrRet(void) {
	C_struct* _c_struct = new C_struct(10.3, 30.3, 90.3);
	return _c_struct;
}
