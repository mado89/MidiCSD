/**
 * 
 */
package at.univie.MidiCSD;

/**
 * Helper Call
 * @author Martin Dobiasch
 */
public class RGB
{
	public byte R;
	public byte G;
	public byte B;
	
	public RGB(byte R, byte G, byte B)
	{
		this.R= R;
		this.G= G;
		this.B= B;
	}
	
	public RGB(long R, long G, long B)
	{
		this.R= (byte) R;
		this.G= (byte) G;
		this.B= (byte) B;
	}
	
	public RGB(int R, int G, int B)
	{
		this.R= (byte) R;
		this.G= (byte) G;
		this.B= (byte) B;
	}
}
