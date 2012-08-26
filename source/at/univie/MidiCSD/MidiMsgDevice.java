/**
 * 
 */
package at.univie.MidiCSD;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
public class MidiMsgDevice implements IMidiMsg
{
	public class InstChg
	{
		int channel;
		int instnr;
	}
	
	public Object ChannelPressure(int channel, double rval)
	{
		//return Conversion.RGB(208 + channel - 1, (int) Conversion.Rescale(rval, 0, 1, 0, 127), 0);
		ShortMessage msg= new ShortMessage();
		try {
			msg.setMessage(ShortMessage.CHANNEL_PRESSURE, channel - 1, (int) Conversion.Rescale(rval, 0, 1, 0, 127), 0);
			
		} catch (InvalidMidiDataException e) {
			Debug.showException(e, "Exception MidiMsgDevice::ChannelPressure");
			e.printStackTrace();
		}
		
		return msg;
	}

	public Object Controller(int channel, int contr, double ival)
	{
		ShortMessage msg= new ShortMessage();
		try {
			msg.setMessage(ShortMessage.CONTROL_CHANGE, channel - 1, contr, (int) ival);
			
		} catch (InvalidMidiDataException e) {
			Debug.showException(e, "Exception MidiMsgDevice::Controller");
			e.printStackTrace();
		}
		
		return msg;
		
		//old code:
		//return Conversion.RGB(176 + channel - 1, contr, (int) ival);
	}

	public Object KeyPressure(int channel, int note, double rval)
	{
		ShortMessage msg= new ShortMessage();
		try {
			msg.setMessage(ShortMessage.CHANNEL_PRESSURE, channel - 1, note, (int) Conversion.Rescale(rval, 0, 1, 0, 127));
			
		} catch (InvalidMidiDataException e) {
			Debug.showException(e, "MidiMsgDevice::KeyPressure");
			e.printStackTrace();
		}
		
		return msg;
		
		//old code:
		//return Conversion.RGB(160 + channel - 1, note, (int) Conversion.Rescale(rval, 0, 1, 0, 127));
	}

	public Object NoteOff(int channel, int note, int i)
	{
		//return Conversion.RGB(128 + channel - 1, note, 0);
		ShortMessage msg= new ShortMessage();
		try {
			msg.setMessage(ShortMessage.NOTE_OFF, channel - 1, note, 0);
			
		} catch (InvalidMidiDataException e) {
			Debug.showException(e, "MidiMsgDevice::NoteOff");
			e.printStackTrace();
		}
		
		return msg;
	}

	public Object NoteOn(int channel, int note, double vol)
	{
		//return Conversion.RGB(144 + channel - 1, note, (int) Conversion.Rescale(vol, 0, 1, 0, 127));
		ShortMessage msg= new ShortMessage();
		try {
			//msg.setMessage(ShortMessage.NOTE_ON, channel - 1, note, (int) Conversion.Rescale(vol, 0, 1, 0, 127));
			msg.setMessage(ShortMessage.NOTE_ON, channel - 1, note, (int) Conversion.Rescale(vol, 0, 1, 0, 127));
			//Debug.showMessage("" + channel + " " + note + " " + ((int) vol) );
			
		} catch (InvalidMidiDataException e) {
			Debug.showException(e, "MidiMsgDevice::NoteOn");
			e.printStackTrace();
		}
		
		return msg;
	}

	public Object PitchBend(int channel, double rval)
	{
		TwoByte MyTwobyte = new TwoByte();
		
		MyTwobyte = Conversion.TwoByteMidiRescale(rval, -1, 1);
    
		//return Conversion.RGB(224 + channel - 1, MyTwobyte.lsb, MyTwobyte.msb);
		ShortMessage msg= new ShortMessage();
		try
		{
			msg.setMessage(ShortMessage.PITCH_BEND, channel - 1, MyTwobyte.lsb, MyTwobyte.msb);
		}
		catch (InvalidMidiDataException e)
		{
			Debug.showException(e, "Exception MidiMsgDevice::NoteOff");
			e.printStackTrace();
		}
		
		return msg;
	}

	public String SysEx(Object[] ByteVals)
	{
		int i;
		//long tmp;
		String ret = "";
		
		for(i= 0; i < ByteVals.length; i++ )
		{
			if( !ByteVals.getClass().isArray() )
				ret+= Character.forDigit((Integer)ByteVals[i], 10);
			else
			{
				/* TODO: ist das gleich?
				 * Temp = ByteVals(i)
		         *   For j = LBound(Temp) To UBound(Temp)
		         *      SysEx = SysEx & Chr(Temp(j))
		         *   Next j
				 */
				ret+= SysEx((Object[])ByteVals[i]);
			}
		}
		
		return ret;
	}

	public Object Instrument(int channel, int Instrument)
	{
		//Version 2
		ShortMessage msg= new ShortMessage();
		try
		{
			msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel - 1, Instrument - 1, 0);
			return msg;
		} catch (InvalidMidiDataException e) {
			Debug.showException(e, "MidiMsgDevice::Instrument");
			e.printStackTrace();
		}
		//Version 1
		InstChg ret= new InstChg();
		ret.channel= channel - 1;
		ret.instnr= Instrument - 1;
		return ret;
		//return Conversion.RGB(192 + channel - 1, Instrument - 1, 0);
	}
	
}
