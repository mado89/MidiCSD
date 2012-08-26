/**
 * 
 */
package at.univie.MidiCSD;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * @author Martin
 *
 */
@Deprecated
public class T2Mf
{
	private native int t2mf(byte[] in, byte[] out);
	//public native void test();
	
	private static boolean loaded = false;
	
	/*public class T2MfLibNotLoadedException extends Exception
	{
		
	};*/
	
	/**
	 * Before calling the library has to be loaded (via load Method)
	 * @param in
	 * @param out
	 * @return
	 * @see load
	 */
	public int t2mf(String in, String out)
	{
		byte[] _in= null;
		byte[] _out= null;
		
		try
		{
			//Debug.showMessage(in + "\n" + out);
			_in= toBytes(in);
			_out= toBytes(out);
			
		}
		catch (UnsupportedEncodingException e)
		{
			Debug.showException(e, "T2MF::t2mf");
			
		}
		return t2mf(_in,_out);
	}
	
	/**
	 * Quick and dirty approach to help sending data to native t2mf library
	 * @param s
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private byte[] toBytes(String s) throws UnsupportedEncodingException
	{
		byte[] bytes= s.getBytes("ASCII");
		byte[] bytes2= new byte[bytes.length + 1];
		int i;
		
		for(i= 0; i < bytes.length; i++ )
			bytes2[i]= bytes[i];
		bytes2[i]= 0;
		
		return bytes2;
	}
	
	/**
	 * This method has to be called before t2mf is called
	 * @param path Path where the t2mf-Library is stored
	 */
	public static void load(String path)
	{
		String libpath= "";
		String libname;
		File f;
		
		libname= System.mapLibraryName("t2mflib");
		//libname= "t2mflib";
		//System.out.println(path + libname);
		//Workaround to get the path correct
		
		try
		{
			f= new File(path + libname);
			libpath= f.getCanonicalPath();
			//need to do this on Windows
			libpath= libpath.replaceAll("%20", "\\ ");
			//Debug.showMessage(libpath);
		}
		/*catch (IOException e)
		{
			System.out.println("IOException in load: " + e.getMessage());
			e.printStackTrace();
		}*/
		catch(Exception e)
		{
			Debug.showException(e, "T2MF::load");
		}
		f= null;
		try
		{
			//System.loadLibrary(libpath);
			//System.load("E:\\Uni\\lvas\\BakkArbeit\\MidiCSDext\\bin\\t2mflib.dll");
			System.load(libpath);
			loaded= true;
		}
    catch( SecurityException e)
    {
    	Debug.showException(e, "T2MF::load");
    }
    catch( UnsatisfiedLinkError e)
    {
    	Debug.showMessage("UnsatisfiedLinkException in load: " + e.getMessage() + "\n");
    	//e.printStackTrace();
    }
 		catch( NullPointerException e)
 		{
 			Debug.showException(e, "T2MF::load");
 		}
	}
	
	public static boolean isLoaded()
	{
		return loaded;
	}
}
