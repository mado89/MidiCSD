/**
 * 
 */
package at.univie.MidiCSD;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
public class Conversion
{
	/**
	 * 
	 * @return
	 */
	public static TwoByte TwoByteMidiRescale(double x, int inmin, int inmax)
	{
		long medval;
		TwoByte ret= new TwoByte();
		
		medval= Rescale(x, inmin, inmax, 0, 16383);
		ret.msb= (byte) (medval / (int) 128);
		ret.lsb= (byte) (medval % 128);
		
		return ret;
	}
	
	/**
	 * 
	 * @return
	 */
	public static long Rescale(double x, int inmin, int inmax, int outmin, int outmax)
	{
		double result;
		
		result = ((x - inmin) / (inmax - inmin)) * (outmax - outmin) + outmin;
		
		if( result > outmax )
			result = outmax;
		if( result < outmin )
			result = outmin;
		
		return (long)result;
	}
	
	/**
	 * 
	 * @param InValues
	 * @return
	 */
	public static String MakeString(Object ...InValues)
	{
		int i;
		String ret= "";
		
		ret= InValues[0].toString();
		for(i= 1; i < InValues.length; i++)
		{
			ret+= "\t" + InValues[i].toString();
		}
		
		return ret;
		/* Original Code
		Dim i As Integer
		On Error GoTo CatchError
		i = LBound(InValues)
		MakeString = InValues(i)
		Do While i < UBound(InValues)
		    i = i + 1
		    MakeString = MakeString & vbTab & CStr(InValues(i))
		    i = i + 1
		    If i > UBound(InValues) Then Exit Do
		    MakeString = MakeString & CStr(InValues(i))
		Loop
		Exit Function
		CatchError:
		MakeString = ""
		 */
	}
	
	/**
	 * 
	 * @param inString
	 * @return
	 */
	public static String ZeroPad(String inString)
	{
		if( inString.length() > 1 )
			return inString;
		else
			return "0" + inString;
	}
	
	/**
	 * RGB used a lot of times
	 */
	public static long RGB(int r, int g, int b)
	{
		return (long)( r << 16 | g << 8 | b);
	}
	
	public static byte[] splitLong(long l)
	{
		byte bytes[]= new byte[4];
		//int bytes[]= new int[4];
		
//		Debug.showMessage( Long.toHexString(l) );
		
		bytes[0]= (byte) (( l & 0xFF000000) >> 32);
		bytes[1]= (byte) (( l & 0xFF0000) >> 16);
		bytes[2]= (byte) ((l & 0xFF00) >> 8);
		bytes[3]= (byte) (l & 0xFF);
		
		return bytes;
	}
	
	public static String GetChannelPitchTimeVolume(TimedSymbolicEvent ev)
	{
		return String.format("00", ev.channel) +
		String.format("00", Long.parseLong(ev.EventParams[0])) +
		String.format("000000000", ev.AbsTime) +
		String.format("000)", 1000 * Long.parseLong( ev.EventParams[1]) );
	}
}

