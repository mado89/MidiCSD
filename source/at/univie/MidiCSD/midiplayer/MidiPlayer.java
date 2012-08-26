package at.univie.MidiCSD.midiplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import at.univie.MidiCSD.Debug;

/**
 * @author Martin Dobiasch
 *
 */
public class MidiPlayer
{
	private class Player extends Thread
	{
		Sequencer seq;
		Sequence mySeq;
		Synthesizer synth;
		ArrayList<Player> m_playing;
		String name;
		
		Player(String name, Sequencer seq, Sequence mySeq, Synthesizer synth, ArrayList<Player> m_playing)
		{
			this.name= name;
			this.seq= seq;
			this.mySeq= mySeq;
			this.synth= synth;
			this.m_playing= m_playing;
		}
		
		public void run()
		{
			try
			{
				/*wait(n);
				synchronized(n)
				{
					n++;
					notifyAll();
				}*/
				
				seq.setSequence(mySeq);
				long length = mySeq.getMicrosecondLength();
				m_playing.add(this);
				seq.start();
				Thread.sleep(length/1000);
				seq.close();
				synth.close();
				m_playing.remove(this);
				
				/*wait(n);
				synchronized(n)
				{
					n--;
					notifyAll();
				}*/
			}
			catch (InvalidMidiDataException e)
			{
				Debug.showException(e, "MidiPlayer::run InvalidMidiDataException");
			}
			catch (InterruptedException e)
			{
				seq.close();
				synth.close();
				//Debug.showMessage("MidiCSD::PlayMidiFile: Interrupted" + e.getMessage());
			}
			
		}
	}
	
	ArrayList<Player> m_playing= new ArrayList<Player>();
	
	public void PlayMidiFile(String name, String filename)
	{
		Sequencer seq= null;
		Transmitter seqTrans= null;
		Synthesizer synth;
		Receiver synthRcvr= null;
		File midiFile= null;
		
		try
		{
			seq = MidiSystem.getSequencer();
			seqTrans = seq.getTransmitter();
			synth = MidiSystem.getSynthesizer();
			synthRcvr = synth.getReceiver();
			midiFile = new File(filename);
			
			if (seq == null)
			{
				Debug.showMessage("MidiCSD::PlayMidiFile: Sequencer nicht gefunden!");
			}
			else
			{
				seq.open();
				
				seqTrans.setReceiver(synthRcvr);
				
				Sequence mySeq;
				mySeq = MidiSystem.getSequence(midiFile);
				
				new Player(name, seq, mySeq, synth,m_playing).start();
			}
		}
		catch (MidiUnavailableException e)
		{
			Debug.showException(e, "MidiCSD::PlayMidiFile: MidiUnavailable" + e.getMessage());
		}
		catch (InvalidMidiDataException e)
		{
			Debug.showException(e, "MidiCSD::PlayMidiFile: InvalidMidiDataException" + e.getMessage());
		}
		catch (IOException e)
		{
			Debug.showException(e, "MidiCSD::PlayMidiFile:IOException (fn:" + filename + ")");
		}
	}
	
	public void stop(String name)
	{
		if( m_playing.size() > 0 )
		{
			for(Player p : m_playing )
			{
				if( p.name.equals(name) )
				{
					p.interrupt();
					m_playing.remove(p);
				}
			}
		}
	}
	
	public void stop()
	{
		if( m_playing.size() > 0 )
		{
			for(Player p : m_playing )
			{
				p.interrupt();
				m_playing.remove(p);
			}
		}
	}
	
	public boolean isPlaying()
	{
		if( m_playing.size() > 0 )
			return true;
		return false;
	}
}
