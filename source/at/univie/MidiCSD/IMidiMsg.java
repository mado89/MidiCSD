/**
 * 
 */
package at.univie.MidiCSD;

/**
 * @author Martin Dobiasch
 *
 */
public interface IMidiMsg
{

	Object NoteOff(int channel, int note, int i);

	Object NoteOn(int channel, int note, double vol);

	Object KeyPressure(int channel, int note, double rval);

	Object Controller(int channel, int contr, double ival);

	Object ChannelPressure(int channel, double rval);
	
	Object Instrument(int channel, int Instrument);

	Object PitchBend(int channel, double rval);
	
	//TODO: passt das wirklich mit Object[]?
	String SysEx(Object[] ByteVals);	
}
