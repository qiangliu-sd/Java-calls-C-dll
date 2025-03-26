# Call Windows C-dll (foreign) functions in Java: A Complete Template
Figuring out all the details of Java calling (foreign) C functions [(1)](#notes) defined in Windows dll could be daunting. This package uses simple examples to demonstrate the necessary know-how.

### Build Windows Dll
To create a DLL project in Visual Studio 2022:
1.	On VS2022 start-up, choose Create a new project.
2.	Choose C++, and choose Dynamic-link Library (DLL).
3.	Enter a name, in my case winCDynamic, for your project.
4.	Delete dllmain.cpp, framework.h, pch.h, pch.cpp from Project.
5.	Move qlcfuncs.cpp to the project folder (i.e., winCDynamic) and add to the project. Note: If you provide a header file qlcfuncs.h, it will NOT work!
6.	Set the VS2022 Compiler by opening the Property Pages dialog and Configuration Properties in the left pane:

- C/C++ -> Precompiled Headers: 
set [Precompiled Headers] to [Not Using Precompiled Headers]

Note: the default calling convention should be [__cdecl] in the VS2022 Compiler. If not, set as follows:
- C/C++ -> Advanced: 
		set [Calling Convention] to [__cdecl (/Gd)]

Now, you can build your Windows dll and copy winCDynamic.dll to your desired folder (in my case libc, see [2](#notes)).

### Java calls C-dll functions
To call dll functions in Java, you must pass the correct arguments (i.e., String, double, struct, or array) and return types. For String, array, and struct, MemoryLayout and MemorySegment are provided for data transfer between Java and C. Read the documentation directly in **QlDllC.java** for what most of you need to know.

Move QlDllC.java to the c4JvTest folder. Compile it by entering the command:
> javac QlDllC.java

and QlDllC.class will be created. Run by typing the command:
> java QlDllC

you will see three successful calls, such as qlDblRetArgs(). But you will get the following error as well:
- Exception in thread "main" java.util.NoSuchElementException: No value present
        at java.base/java.util.Optional.get(Optional.java:143)
        at QlDllC.cFuncHandle(QlDllC.java:67)
        at QlDllC.qlArrayArg(QlDllC.java:109)
        at QlDllC.main(QlDllC.java:36)

The error is caused by missing the qlArrayArg() function defined in a static library. Let’s build the static library then.

### Build Windows (Static) Lib
To create a static library project in Visual Studio 2022:
1.	On VS2022 start-up, choose Create a new project.
2.	Choose C++, and choose Static Library.
3.	Enter a name, in my case winCppStatic, for your project.
4.	Delete framework.h, pch.h, pch.cpp from Project.
5.	Move qlcpptools.h and qlcpptools.cpp to the project folder (i.e., winCppStatic).
6.	Set up the VS2022 Compiler:	set [Precompiled Headers] to [Not Using Precompiled Headers].

Note: The default calling convention should be [__cdecl] in the VS2022 Compiler. If not, set it as in the section Build Windows Dll.

Build your Windows static library and move qlcpptools.h and winCppStatic.lib to libc (see [2](#notes)).

### Re-build Windows Dll
To rebuild the dll, first set up the VS2022 Compiler:
- Linker -> Input: 
add [../libc/winCppStatic.lib] to [Additional Dependencies]

- C/C++ -> Preprocessor: 
Add [\_QL__INCLUDE_STATIC_LIB_] to [Preprocessor Definitions]

Rebuild dll, then, re-compile Java and run again, and you will see that qlArrayArg() is successful.

### Notes:
[1] [The Foreign Function and Memory API](https://dev.java/learn/ffm/)

[2] To automatically copy files after the build, select Configuration Properties > Build Events > Post-Build Event, and in the Command Line field, enter this command (for the dll project):
> copy $(TargetPath)  ..\\libc

or for the static lib project:
> copy $(ProjectDir) qlcpptools.h ..\\libc

> copy $(TargetPath) ..\\libc
