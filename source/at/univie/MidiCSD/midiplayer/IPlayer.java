/**
 * 
 */
package at.univie.MidiCSD.midiplayer;

import at.univie.MidiCSD.TimedMidiStream;

/**
 * @author Martin Dobiasch
 *
 */
public interface IPlayer
{
	public void Play(TimedMidiStream stream, int deviceNum );
	public void PlayFile(String filename);
	public void setWorkbookPath(String path);
}
