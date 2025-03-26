import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.foreign.MemoryLayout.*;
import java.lang.foreign.GroupLayout.*;
import java.text.MessageFormat;
import java.util.Arrays;


public class QlDllC {
	public static final String _C_DLL_PATH = "../libc/winCDynamic.dll";
	
    private static Arena _cArena;
    private static Linker _cLink;
    private static SymbolLookup _cDll;

    private static String[] _mmbNames;
    private static MemoryLayout _structLO;

    public static void main(String[] args) throws Throwable {
        init(_C_DLL_PATH);

        System.out.println("C qlStrArg: ");
        qlStrArg("string literal from <Java>\n");

        int a =3; int b =4;
        int _sum = qlIntRetArgs(a,b);
        System.out.println(MessageFormat.format("C qlIntRetArgs: {0} + {1} = {2}", a,b, _sum));

        double x =2.5; double y = 6.1;
        double _sum_d = qlDblRetArgs(x,y);
        System.out.println(MessageFormat.format("C qlDblRetArgs: {0} + {1} = {2}", x,y,_sum_d ));
 
        //int arr[] = IntStream.range(0, 8).toArray();
        double _da8[] = new double[8];
        Arrays.setAll(_da8, i -> i*i);
        double _std = qlArrayArg(_da8);
        System.out.println(MessageFormat.format("C qlArrayArg(std): {0}\n", _std));       

        double[] _da3 = new double[3];
        qlArrayFetch(_da3);
        System.out.println(MessageFormat.format("C qlArrayFetch: {0}", Arrays.toString(_da3)));
       
        _da3 = qlArrayRet(3);
        System.out.println(MessageFormat.format("C qlArrayRet: {0}\n", Arrays.toString(_da3)));

        _da3 = qlStructFetch(_structLO);
        System.out.println(MessageFormat.format("C qlStructFetch: {0}", Arrays.toString(_da3)));

        _da3 = qlStructPtrRet();
        System.out.println(MessageFormat.format("C qlStructPtrRet: {0}", Arrays.toString(_da3)));
    }

    private static void init(String c_dll) {
        _cArena = Arena.ofConfined();
        _cLink = Linker.nativeLinker();
        _cDll = SymbolLookup.libraryLookup(c_dll, _cArena); 

        _mmbNames= new String[] {"x","y","z"};
        _structLO = MemoryLayout.structLayout(
            ValueLayout.JAVA_DOUBLE.withName(_mmbNames[0]),
            ValueLayout.JAVA_DOUBLE.withName(_mmbNames[1]),
            ValueLayout.JAVA_DOUBLE.withName(_mmbNames[2])
        );
    }

    private static MethodHandle cFuncHandle(String cfunc_name, FunctionDescriptor cfunc_sig) throws Throwable {
        // Locate address of C function
        MemorySegment _cfunc_addr = _cDll.find(cfunc_name).get();
 
        // make downcall handle for C function
        MethodHandle _cfunc_hdl = _cLink.downcallHandle(_cfunc_addr, cfunc_sig);

        return _cfunc_hdl;    
    }

    public static void qlStrArg(String str_i) throws Throwable {
        // Allocate off-heap memory and copy string
        MemorySegment _str_MS = _cArena.allocateFrom(str_i);

        MemoryLayout _seq_LO = MemoryLayout.sequenceLayout(Long.MAX_VALUE, ValueLayout.JAVA_BYTE);
        // description of C function: argtypes...
        FunctionDescriptor _cfunc_sig = FunctionDescriptor.ofVoid( 
            ValueLayout.ADDRESS.withTargetLayout(_seq_LO) );

        // Call (foreign) C function directly from Java
        cFuncHandle("qlStrArg",_cfunc_sig).invokeExact(_str_MS);
    }

    public static int qlIntRetArgs(int a, int b) throws Throwable {
        // description of C function: rettype, argtypes...
        FunctionDescriptor _cfunc_sig = FunctionDescriptor.of(ValueLayout.JAVA_INT,
            ValueLayout.JAVA_INT, ValueLayout.JAVA_INT); 
     
        int _sum = (int)cFuncHandle("qlIntRetArgs",_cfunc_sig).invokeExact(a,b);
        return _sum;
    }

    public static double qlDblRetArgs(double a, double b) throws Throwable {
        FunctionDescriptor _cfunc_sig = FunctionDescriptor.of( ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE); 

        double _sum = (double)cFuncHandle("qlDblRetArgs",_cfunc_sig).invokeExact(a,b);
        return _sum;
    }


    public static double qlArrayArg(double[] arr) throws Throwable {
        FunctionDescriptor _cfunc_sig = FunctionDescriptor.of( ValueLayout.JAVA_DOUBLE,
            ValueLayout.ADDRESS, ValueLayout.JAVA_INT); 
        MethodHandle _cfunc_hdl = cFuncHandle("qlArrayArg",_cfunc_sig);

        // Allocate memory and store array
        MemorySegment _arr_MS = _cArena.allocateFrom(ValueLayout.JAVA_DOUBLE,arr);

        double _sum = (double)_cfunc_hdl.invokeExact(_arr_MS, (int)arr.length);
        return _sum;
    }
    
    public static void qlArrayFetch(double[] arr) throws Throwable {
        int _arr_sz = arr.length;

        MemoryLayout _arr_LO = MemoryLayout.sequenceLayout(_arr_sz, ValueLayout.JAVA_DOUBLE);
        FunctionDescriptor _cfunc_sig = FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS.withTargetLayout(_arr_LO), ValueLayout.JAVA_INT ); 
        MethodHandle _cfunc_hdl = cFuncHandle("qlArrayFetch", _cfunc_sig);
        MemorySegment _arr_MS = _cArena.allocateFrom(ValueLayout.JAVA_DOUBLE,arr);
        _cfunc_hdl.invokeExact(_arr_MS, _arr_sz);       
        
        for (int k=0; k<_arr_sz; ++k) {
            arr[k] = _arr_MS.getAtIndex(ValueLayout.JAVA_DOUBLE, k);  
        }
    }

    public static double[] qlArrayRet(int sz) throws Throwable {
        MemoryLayout _arr_LO = MemoryLayout.sequenceLayout(sz, ValueLayout.JAVA_DOUBLE);
        FunctionDescriptor _cfunc_sig = FunctionDescriptor.of(
            ValueLayout.ADDRESS.withTargetLayout(_arr_LO) , ValueLayout.JAVA_INT); 
        MethodHandle _cfunc_hdl = cFuncHandle("qlArrayRet", _cfunc_sig);
        MemorySegment _arr_MS = (MemorySegment)_cfunc_hdl.invokeExact(sz);       
        
        //double _da1 = _struct_ms.get(ValueLayout.JAVA_DOUBLE,0);  // works only with 0
        double[] _da = new double[sz];
        for (int k=0; k<sz; ++k) {
            _da[k] = _arr_MS.getAtIndex(ValueLayout.JAVA_DOUBLE, k);  
        }
        return _da;
    }

    public static double[] qlStructFetch(MemoryLayout struct_LO) throws Throwable {      
        FunctionDescriptor _cfunc_sig = FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS.withTargetLayout(struct_LO) ); 
        MemorySegment _struct_MS = _cArena.allocate(struct_LO);
        cFuncHandle("qlStructFetch", _cfunc_sig).invokeExact(_struct_MS);   
        
        return struct2Array(_struct_MS);
    }

    // C-struct returned as value does NOT work
    public static double[] qlStructPtrRet() throws Throwable {
        FunctionDescriptor _cfunc_sig = FunctionDescriptor.of(
            ValueLayout.ADDRESS.withTargetLayout(_structLO) ); 
        MethodHandle _cfunc_hdl = cFuncHandle("qlStructPtrRet", _cfunc_sig);
        MemorySegment _struct_MS = (MemorySegment)_cfunc_hdl.invokeExact();   
        
        return struct2Array(_struct_MS);
    }

    // helpers
    private static double[] struct2Array(MemorySegment ms) {
        int _sz = _mmbNames.length;
        double[] _da = new double[_sz]; int k=0;
        for (String nm: _mmbNames) {
            VarHandle _e_hdl = _structLO.varHandle(PathElement.groupElement(nm));
            _da[k++] = (double) _e_hdl.get(ms, 0);
        }
        return _da;
    }

}
