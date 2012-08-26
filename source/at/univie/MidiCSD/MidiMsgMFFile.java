/**
 * 
 */
package at.univie.MidiCSD;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
@Deprecated
public class MidiMsgMFFile implements IMidiMsg
{
	public Object ChannelPressure(int channel, double rval)
	{
		return Conversion.MakeString("ChanPr", "ch=", channel, "c=", 
				Conversion.Rescale(rval, 0, 1, 0, 127));
	}

	public Object Controller(int channel, int contr, double ival)
	{
		return Conversion.MakeString("Param", "ch=", channel, "c=", contr, "v=", ival);
	}

	public Object KeyPressure(int channel, int note, double rval)
	{
		return Conversion.MakeString("PolyPr", "ch=", channel, "n=", note, "v=", 
				Conversion.Rescale(rval, 0, 1, 0, 127));
	}

	public Object NoteOff(int channel, int note, int i)
	{
		return Conversion.MakeString("Off", "ch=", channel, "n=", note, "v=", 0);
	}

	public Object NoteOn(int channel, int note, double vol)
	{
		return Conversion.MakeString("On", "ch=", channel, "n=", note, "v=", 
				Conversion.Rescale(vol, 0, 1, 0, 127));
	}
	
	public Object Instrument(int channel, int Instrument)
	{
		return Conversion.MakeString("ProgCh", "ch=", channel, "p=", Instrument - 1);
	}

	public Object PitchBend(int channel, double rval)
	{
		TwoByte MyTwobyte = new TwoByte();
    MyTwobyte = Conversion.TwoByteMidiRescale(rval, -1, 1);
    return Conversion.MakeString("Pb", "ch=", channel, "v=", 
    		256 * MyTwobyte.msb + MyTwobyte.lsb);
	}

	public String SysEx(Object[] ByteVals)
	{
		int i,j;
		Object[] tmp;
		String ret = "SysEx";
		
		for(i= 0; i < ByteVals.length; i++ )
		{
			if( !ByteVals.getClass().isArray() )
				ret+= " " + Character.forDigit((Integer)ByteVals[i], 10);
			else
			{
				tmp= (Object[]) ByteVals[i];
				for(j= 0; j < tmp.length; j++)
				{
					ret+= " " + Conversion.ZeroPad(Integer.toHexString((Integer) tmp[j]));
							
				}
			}
		}
		
		return ret;
		/*
		Public Function SysEx(ParamArray ByteVals() As Variant) As String
		    Dim i As Integer
		    Dim j As Integer
		    Dim Temp As Variant
		    SysEx = "SysEx"
		    For i = LBound(ByteVals) To UBound(ByteVals)
		        If Not IsArray(ByteVals(i)) Then
		            SysEx = SysEx & " " & ZeroPad(Hex(ByteVals(i)))
		        Else
		            Temp = ByteVals(i)
		            For j = LBound(Temp) To UBound(Temp)
		                SysEx = SysEx & " " & ZeroPad(Hex(Temp(j)))
		            Next j
		        End If
		    Next i
		End Function
		*/
	}
}
