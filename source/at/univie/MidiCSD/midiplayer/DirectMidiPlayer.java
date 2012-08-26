/**
 * 
 */
package at.univie.MidiCSD.midiplayer;

import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

import at.univie.MidiCSD.Conversion;
import at.univie.MidiCSD.Debug;
import at.univie.MidiCSD.TimedMidiStream;
import at.univie.MidiCSD.TimedOutputEvent;

/**
 * @author Martin Dobiasch
 *
 */
public class DirectMidiPlayer implements IPlayer
{

	public void Play(TimedMidiStream stream, int deviceNum)
	{
		ArrayList<TimedOutputEvent> events;
		
		events= stream.getOutputEvents();
		
		try
		{
			MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
			/*String s = "Vorhandene MIDI Devices: \n";
			for (int i = 0; i < infos.length; i++)
			{
				s = s + infos[i].getName() + "\n";
			}
			Debug.showMessage(s);*/
			MidiDevice dev = MidiSystem.getMidiDevice(infos[0]);
			if (!dev.isOpen())
			{
//				Debug.showMessage("Device oeffnen");
				dev.open();
			}
			
//			Debug.showMessage("start playing");
			Synthesizer synth;
			Receiver recv/* = dev.getReceiver() */;
			
			synth = MidiSystem.getSynthesizer();
			synth.open();
			recv = synth.getReceiver();
			
//			Debug.showMessage("alles offen, los gehts");
			
			// ShortMessage cp= new ShortMessage();
			// cp.setMessage(ShortMessage.CHANNEL_PRESSURE, 1, 1);
			// recv.send(cp, -1);
			
			for (int i = 0; i < events.size(); i++)
			{
				TimedOutputEvent ev = events.get(i);
				if (ev.RelTime > 0)
					// recv.wait(ev.RelTime);
					Thread.sleep(ev.RelTime);
				
				Object obj = ev.OutCommand;
				ShortMessage msg = new ShortMessage();
				if( obj != null && obj.getClass() == ShortMessage.class)
				{
					msg = (ShortMessage) obj;
//					Debug.showMessage("recv.send " + msg.getCommand());
					recv.send(msg, ev.AbsTime);
					// recv.send(msg, -1 );
					
				}
				else
				{
					if( obj != null )
					{
						byte bytes[] = Conversion.splitLong((Long) obj);
						try
						{
//							Debug.showMessage(bytes);
							msg.setMessage(bytes[0], bytes[1], bytes[2], bytes[3]);
							recv.send(msg, ev.AbsTime);
						}
						catch (InvalidMidiDataException e)
						{
							Debug.showException(e, "TimedMidiStream::Play");
							e.printStackTrace();
						}
					}
					else
						Debug.showMessage("ups");
				}
			}
			
//			Debug.showMessage("alles gespielt, schliesen");
			
			recv.close();
			synth.close();
			dev.close();
			
//			Debug.showMessage("geschlossen");
		}
		catch (MidiUnavailableException e)
		{
			Debug.showException(e, "Exception: MidiUnavailable");
			e.printStackTrace();
		}
		/*catch (InvalidMidiDataException e)
		{
			Debug.showMessage("Exception: " + e.getMessage());
			e.printStackTrace();
		}*/
		catch (InterruptedException e)
		{
			Debug.showException(e, "Exception: Interrupted");
			e.printStackTrace();
		}
		catch(IllegalMonitorStateException e)
		{
			Debug.showException(e, "Exception: IllegalMonitorState");
			e.printStackTrace();
		}
	}
	
	/**
	 * Unused!
	 * Call doesn't have an effect
	 */
	public void setWorkbookPath(String path)
	{
		
	}
}
