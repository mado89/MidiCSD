package at.univie.MidiCSD;

/**
 * @author Martin Dobiasch
 *
 */
public class MidiMsgMidiCsvFile implements IMidiMsg
{
	public Object ChannelPressure(int channel, double rval)
	{
		//TODO: ChannelPressure = Channel_aftertouch_c, Channel, Value
		return "Channel_aftertouch_c, " + channel + ", " +
			Conversion.Rescale(rval, 0, 1, 0, 127);
		//return Conversion.MakeString("ChanPr", "ch=", channel, "c=", 
		//		Conversion.Rescale(rval, 0, 1, 0, 127));
	}

	public Object Controller(int channel, int contr, double ival)
	{
		return "Control_c, " + channel + ", " + contr + ", " + ival;
		//return Conversion.MakeString("Param", "ch=", channel, "c=", contr, "v=", ival);
	}

	public Object KeyPressure(int channel, int note, double rval)
	{
		//TODO: KeyPressure = Poly_aftertouch_c, Channel, Note, Value
		return "Poly_aftertouch_c, " + channel + ", " + note + ", " +
			Conversion.Rescale(rval, 0, 1, 0, 127);
		//return Conversion.MakeString("PolyPr", "ch=", channel, "n=", note, "v=", 
		//		Conversion.Rescale(rval, 0, 1, 0, 127));
	}

	public Object NoteOff(int channel, int note, int i)
	{
		return "Note_off_c, " + channel + ", " + note + ", 0";
		//return Conversion.MakeString("Off", "ch=", channel, "n=", note, "v=", 0);
	}

	public Object NoteOn(int channel, int note, double vol)
	{
		return "Note_on_c, " + channel + ", " + note + ", " + 
			Conversion.Rescale(vol, 0, 1, 0, 127);
		//Note_on_c, Channel, Note, Velocity
		//return Conversion.MakeString("On", "ch=", channel, "n=", note, "v=", 
		//		Conversion.Rescale(vol, 0, 1, 0, 127));
	}
	
	public Object Instrument(int channel, int Instrument)
	{
		return "Program_c, " + channel + ", " + (Instrument - 1);
		//return Conversion.MakeString("ProgCh", "ch=", channel, "p=", Instrument - 1);
	}

	public Object PitchBend(int channel, double rval)
	{
		//TODO: is that correct?
		TwoByte MyTwobyte = new TwoByte();
    MyTwobyte = Conversion.TwoByteMidiRescale(rval, -1, 1);
    return "Pitch_bend_c, " + channel + ", " + 
    	( 128 * MyTwobyte.msb + MyTwobyte.lsb );
		/*
		TwoByte MyTwobyte = new TwoByte();
    MyTwobyte = Conversion.TwoByteMidiRescale(rval, -1, 1);
    return Conversion.MakeString("Pb", "ch=", channel, "v=", 
    		256 * MyTwobyte.msb + MyTwobyte.lsb);
    */
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
	}
}
