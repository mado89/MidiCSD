/**
 * 
 */
package at.univie.MidiCSD.midiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import at.univie.MidiCSD.Debug;
import at.univie.MidiCSD.TimedMidiStream;
import at.univie.MidiCSD.impl.MidiCSDextImpl;

/**
 * @author Martin Dobiasch
 * This player creates a file and plays it using MidiPlayer
 * Important: Set MidiPlayer before executing Play()
 */
public class TimidityPlayer implements IPlayer
{
	/*
	 * Thread which starts MidiPlayer.jar
	 */
	private class Player extends Thread
	{
		String filename;
		
		public Player(String filename)
		{
			this.filename= filename;
		}
		
		public void run()
		{
			String cmd;
			String MidiPlayer;
			@SuppressWarnings("unused") //Because for Debuggin it can be used
			String out;
			String line;
			
			cmd= "timidity " + filename;
			out= "";
			
			try
			{
				Runtime rt= Runtime.getRuntime();
				Process pr;
				
				System.out.println(cmd);
				
				pr= rt.exec(cmd);
				
				BufferedReader input= new BufferedReader(new InputStreamReader(pr
						.getInputStream()));
				
				while( (line= input.readLine()) != null )
				{
					out+= line + "\n";
				}
			
				//TODO: For Debugging Issues it would be nice to have that
				/*
				int exitVal= pr.waitFor();
				if( exitVal != 0 )
					System.out.println("MidiPlayer.jar exited with error code " + exitVal);
				
				if( h != "" )
					System.out.println(h);
				*/
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				Debug.showException(e, "Exception while playing: " + filename);
			}
		}
	}
	
	
	private String wb_path= "";
	
	public void Play(TimedMidiStream stream, int deviceNum)
	{
		stream.WriteMidiFile(wb_path, "", false);
		
//		Debug.showMessage("Try to play: " + wb_path + stream.myFileName);
		
		new Player(wb_path + stream.myFileName).start();
		/*
		 stream.getMidiPlayer().PlayMidiFile("", 
				wb_path + stream.myFileName);
		
		while( stream.getMidiPlayer().isPlaying() )
		{
			Thread.yield();
		}
		*/
		//midifile.delete();
	}
	
	public void PlayFile(String filename)
	{
		new Player(filename).start();
	}
	
	public void setWorkbookPath(String path)
	{
		wb_path= path;
	}
	
}
