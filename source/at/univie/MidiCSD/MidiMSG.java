/**
 * 
 */
package at.univie.MidiCSD;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
public class MidiMSG
{
	private boolean CommandsInFileFormat;
	private IMidiMsg OutFormat;
	
	public MidiMSG()
	{
		CommandsInFileFormat= false;
		
		DeviceOutput();
	}
	
	public void FileOutput()
	{
		CommandsInFileFormat= true;
		OutFormat= new MidiMsgMidiCsvFile();
	}
	
	public void DeviceOutput()
	{
		CommandsInFileFormat= false;
		OutFormat= new MidiMsgDevice();
	}
	
	public boolean getOutputToFile()
	{
		return CommandsInFileFormat;
	}
	
	public Object NoteOff(int channel, int note, double vol)
	{
		return OutFormat.NoteOff(channel, note, 0);
	}

	public Object NoteOn(int channel, int note, double vol)
	{
		return OutFormat.NoteOn(channel, note, vol);
	}

	public Object KeyPressure(int channel, int note, double rval)
	{
		return OutFormat.KeyPressure(channel, note, rval);
	}

	public Object Controller(int channel, int contr, double ival)
	{
		return OutFormat.Controller(channel, contr, ival);
	}

	public Object Instrument(int channel, int instrument)
	{
		return OutFormat.Instrument(channel, instrument);
	}

	public Object ChannelPressure(int channel, double rval)
	{
		return OutFormat.ChannelPressure(channel, rval);
	}

	public Object PitchBend(int channel, double rval)
	{
		return OutFormat.PitchBend(channel, rval);
	}

	//GM controllers
	public Object Modulation(int channel, double val)
	{
		return Controller(channel, 1, Conversion.Rescale(val, 0, 1, 0, 127));
	}
	
	public Object Volume(int channel, double vol)  
	{
		return Controller(channel, 7, Conversion.Rescale(vol, 0, 1, 0, 127));
	}
	
	public Object Expression(int channel, double vol)
	{
		return Controller(channel, 11, Conversion.Rescale(vol, 0, 1, 0, 127));
	}
	
	public Object Pan(int channel, double pos)
	{
		return Controller(channel, 10, Conversion.Rescale(pos, -1, 1, 0, 127));
	}
	
	public Object Sustain(int channel, double val)
	{
		return Controller(channel, 64, Conversion.Rescale(val, 0, 1, 0, 127));
	}
	
	public Object Reverb(int channel, double val)
	{
		return Controller(channel, 91, Conversion.Rescale(val, 0, 1, 0, 127));
	}
	
	public Object Chorus(int channel, double val)
	{
		return Controller(channel, 93, Conversion.Rescale(val, 0, 1, 0, 127));
	}
	
	public Object ResetControllers(int channel)
	{
		return Controller(channel, 121, 0);
	}
	
	public Object AllNotesOff(int channel)
	{
		return Controller(channel, 123, 0);
	}
	
	public Object BankSelectMSB(int channel, int Bank)
	{
		return Controller(channel, 32, Bank);
	}
	
	public Object BankSelectLSB(int channel, int Bank)
	{
		return Controller(channel, 0, Bank);
	}
	
	public Object Breath(int channel, double rval)
	{
		return Controller(channel, 2, Conversion.Rescale(rval, 0, 1, 0, 127));
	}
	
	public Object FootPedal(int channel, double rval)
	{
		return Controller(channel, 4, Conversion.Rescale(rval, 0, 1, 0, 127));
	}
	
	public Object PortamentoTime(int channel, int val)
	{
		return Controller(channel, 5, val);
	}
	
	public Object PitchBendRange(int channel, int semitones)
	{
		return RPN(channel, 0, 0, 0, semitones);
	}
	
	public Object MasterTuneCoarse(int channel, int semitones)
	{
		return RPN(channel, 2, 0, 0, semitones + 64);
	}
	
	public Object MasterTuneFine(int channel, int cents)
	{
		TwoByte MyTwoByte;
		byte lbyte, mbyte;

		MyTwoByte = Conversion.TwoByteMidiRescale(cents, -100, 100);
		lbyte = MyTwoByte.lsb;
		mbyte = MyTwoByte.msb;

		return RPN(channel, 1, 0, lbyte, mbyte);
	}
	
	public long[] NRPNRPN(int channel, int LoRPN, int HiRPN, int LoData, 
			int HiData, boolean IsRPN)
	{
		long[] MyRPN= new long[7];
		int HiByte, LoByte;
		
		if( IsRPN )
		{
			HiByte = 101;
			LoByte = 100;
		}
		else
		{
			HiByte = 99;
			LoByte = 98;
		}
		
		MyRPN[1] = (Long) Controller(channel, HiByte, HiRPN);
		MyRPN[2] = (Long) Controller(channel, LoByte, LoRPN);
		MyRPN[3] = (Long) Controller(channel, 6, HiData);
		MyRPN[4] = (Long) Controller(channel, 38, LoData);
		MyRPN[5] = (Long) Controller(channel, HiByte, 127);
		MyRPN[6] = (Long) Controller(channel, LoByte, 127);

		return MyRPN;
	}
	
	public long[] RPN(int channel, int LoRPN, int HiRPN, int LoData,
			int HiData)
	{
		return NRPNRPN(channel, LoRPN, HiRPN, LoData, HiData, true);
	}
	
	
	public long[] NRPN(int channel, int LoRPN, int HiRPN, int LoData,
			int HiData)
	{
		return NRPNRPN(channel, LoRPN, HiRPN, LoData, HiData, false);
	}
}
