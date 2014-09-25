/**
 * 
 */
package at.univie.MidiCSD;

//import com.sun.jna.Library;
//import com.sun.jna.Native;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import at.univie.MidiCSD.impl.MidiCSDextImpl;
import at.univie.MidiCSD.midiplayer.IPlayer;
import at.univie.MidiCSD.midiplayer.MidiPlayer;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
public class TimedMidiStream
{
	public String streamname;
	private ArrayList<TimedSymbolicEvent> sEventQueue;
	//Private OutQueue As Collection
	private boolean CommandsInFileFormat;
	private ArrayList<TimedOutputEvent> oEvents;

	private IMidiMsg OutFormat;

	private long CurrentTime;
	public String myFileName;
	//Private MyQueueName As String
	private File file;
	private MidiPlayer midiplayer;
	private IPlayer player;
	
	private class inl
	{
		public inl(int i, long absTime)
		{
			this.index= i;
			this.time= absTime;
		}

		int index;
		long time;
	};
	
	private class inl2
	{
		public inl2(int i, String s)
		{
			this.index= i;
			this.s= s;
		}

		int index;
		String s;
	};
	
	/*private interface CLibrary extends Library
	{
    public int chmod(String path, int mode);
	}*/
	
	public IMidiMsg getOutFormat()
	{
		return OutFormat;
	}
	
	public TimedMidiStream()
	{
		sEventQueue= new ArrayList<TimedSymbolicEvent>();
		CurrentTime = 0;
    myFileName = "tempmid";
    DeviceOutput();
	}
	
	public void DeviceOutput()
	{
    CommandsInFileFormat= false;
    OutFormat = new MidiMsgDevice();
	}
	
	public void FileOutput()
	{
		CommandsInFileFormat= true;
    OutFormat= null;
    OutFormat= new MidiMsgMidiCsvFile();
	}
	
	public void Clear()
	{
		sEventQueue= null;
		sEventQueue= new ArrayList<TimedSymbolicEvent>();
		CurrentTime = 0;
	}
	
	public void AddTimedEvent(String mTime, String mEvent, String channel, String[] Params/*ParamArray Params() As Variant*/)
	{
		String[] tmpParams= Params;
		String[] NoteParams= new String[2];
		String[] Pitches= null;
		TimedSymbolicEvent currEvent;
		
		long mTimei= Long.parseLong(mTime);
		
		if( Params != null && Params.length > 0 )
		{
			tmpParams= Params;
		}
		
		//Debug.showMessage("TimedMidiStream::AddTimedEvent");
		
		if( mEvent.equals("note"))
		{
			int Duration;
			int i;
			
			Duration= Integer.parseInt(tmpParams[2]);
			if( tmpParams.length >= 0 )
			{
				//Pitches= new int[1];
				//Pitches[0]= Integer.parseInt(tmpParams[0]);
				Pitches= new String[1];
				Pitches[0]= tmpParams[0];
			}
			else
			{
				/*
						TODO: PitchCount = 0
            For i = (LBound(tmpParams) + 3) To UBound(tmpParams)
                If Not IsEmpty(tmpParams(i)) And Not Len(tmpParams(i)) = 0 Then
                    PitchCount = PitchCount + 1
                End If
            Next i
            ReDim Pitches(1 To PitchCount)
            j = 0
            For i = (LBound(tmpParams) + 3) To UBound(tmpParams)
                If Not IsEmpty(tmpParams(i)) And Not Len(tmpParams(i)) = 0 Then
                    j = j + 1
                    Pitches(j) = tmpParams(i)
                End If
            Next i
				 */
			}
			
			for(i= 0; i < Pitches.length; i++)
			{
				currEvent= new TimedSymbolicEvent();
				
				currEvent.AbsTime = Long.parseLong(mTime);
        currEvent.EventName = "noteon";
        currEvent.channel = Integer.parseInt(channel);
        NoteParams[0] = Pitches[i];
        NoteParams[1] = tmpParams[1];
        currEvent.EventParams= NoteParams;
        sEventQueue.add( currEvent );
        
        currEvent= new TimedSymbolicEvent();
        if( mTimei + Duration - 1 > 0 )
        	currEvent.AbsTime= mTimei + Duration - 1;
        else
        	currEvent.AbsTime= 0;
        currEvent.EventName = "noteon";
        currEvent.channel = Integer.parseInt(channel);
        NoteParams[0] = Pitches[i];
        NoteParams[1] = "0";
        currEvent.EventParams = NoteParams;
        sEventQueue.add( currEvent );
			}
		}
		else if( mEvent.equals("advance") || mEvent.equals("pause") || 
				mEvent.equals("delay") || mEvent.equals("mastertune") )
		{
			double TuneSemi;
			TuneSemi= Double.parseDouble(tmpParams[0]);
			tmpParams[0]= "" + (int) TuneSemi;
			
			currEvent= new TimedSymbolicEvent();
			currEvent.AbsTime = mTimei;
      currEvent.EventName = "mastertunecoarse";
      currEvent.channel = Integer.parseInt(channel);
      currEvent.EventParams = tmpParams;
      
      currEvent= new TimedSymbolicEvent();
      TuneSemi= TuneSemi - (int)TuneSemi;
      tmpParams[0] = "" + 100 * TuneSemi;
      currEvent.AbsTime = mTimei;
      currEvent.EventName = "mastertunefine";
      currEvent.channel = Integer.parseInt(channel);
      currEvent.EventParams = tmpParams;
      sEventQueue.add( currEvent );
		}
		else
		{
			currEvent= new TimedSymbolicEvent();
			currEvent.AbsTime = mTimei;
      currEvent.EventName = mEvent;
      currEvent.channel = Integer.parseInt(channel);
      currEvent.EventParams = tmpParams;
      sEventQueue.add( currEvent );
		}
	}
	
	public void AddDelayedEvent(String mDelay, String mEvent, String channel, String[] Params/*ParamArray Params() As Variant*/)
	{
		String[] tmpParams= Params;
		String[] NoteParams= null;
		String[] Pitches= null;
		TimedSymbolicEvent currEvent;
		
		if( Params != null && Params.length > 0 )
		{
			tmpParams= Params;
		}
    
		//Debug.showMessage("TimedMidiStream::AddDelayedEvent " + mDelay);
		
		CurrentTime+= Integer.parseInt(mDelay);
		currEvent = new TimedSymbolicEvent();
		
		//Debug.showMessage("1");
		
		if( mEvent.equals("note"))
		{
			int Duration;
			int i;
			
			Duration= Integer.parseInt(tmpParams[2]);
			if( tmpParams.length >= 0 )
			{
				//Pitches= new int[1];
				//Pitches[0]= Integer.parseInt(tmpParams[0]);
				Pitches= new String[1];
				Pitches[0]= tmpParams[0];
			}
			else
			{
				/*
						TODO: PitchCount = 0
            For i = (LBound(tmpParams) + 3) To UBound(tmpParams)
                If Not IsEmpty(tmpParams(i)) And Not Len(tmpParams(i)) = 0 Then
                    PitchCount = PitchCount + 1
                End If
            Next i
            ReDim Pitches(1 To PitchCount)
            j = 0
            For i = (LBound(tmpParams) + 3) To UBound(tmpParams)
                If Not IsEmpty(tmpParams(i)) And Not Len(tmpParams(i)) = 0 Then
                    j = j + 1
                    Pitches(j) = tmpParams(i)
                End If
            Next i
				 */
			}
			
			for(i= 0; i < Pitches.length; i++)
			{
				currEvent= new TimedSymbolicEvent();
				NoteParams= new String[2];
		        currEvent.AbsTime = CurrentTime;
		        currEvent.EventName = "noteon";
		        currEvent.channel = Integer.parseInt(channel);
		        NoteParams[0] = Pitches[i];
		        NoteParams[1] = tmpParams[1];
		        currEvent.EventParams= NoteParams;
		        sEventQueue.add( currEvent );
		        
		        currEvent= new TimedSymbolicEvent();
		        if( CurrentTime + Duration - 1 > 0 )
		        	currEvent.AbsTime= CurrentTime + Duration - 1;
		        else
		        	currEvent.AbsTime= 0;
		        currEvent.EventName = "noteon";
		        currEvent.channel = Integer.parseInt(channel);
		        NoteParams= new String[2];
		        NoteParams[0] = Pitches[i];
		        NoteParams[1] = "0";
		        currEvent.EventParams = NoteParams;
		        sEventQueue.add( currEvent );
			}
		}
		else if( mEvent.equals("advance") || mEvent.equals("pause") || 
				mEvent.equals("delay") || mEvent.equals("mastertune") )
		{
			double TuneSemi;
			TuneSemi= Double.parseDouble(tmpParams[0]);
			tmpParams[0]= "" + (int) TuneSemi;
			
			currEvent= new TimedSymbolicEvent();
			currEvent.AbsTime = CurrentTime;
      currEvent.EventName = "mastertunecoarse";
      currEvent.channel = Integer.parseInt(channel);
      currEvent.EventParams = tmpParams;
      sEventQueue.add( currEvent );
      
      currEvent= new TimedSymbolicEvent();
      TuneSemi= TuneSemi - (int)TuneSemi;
      tmpParams[0] = "" + 100 * TuneSemi;
      currEvent.AbsTime = CurrentTime;
      currEvent.EventName = "mastertunefine";
      currEvent.channel = Integer.parseInt(channel);
      currEvent.EventParams = tmpParams;
      sEventQueue.add( currEvent );
		}
		else
		{
			currEvent= new TimedSymbolicEvent();
			currEvent.AbsTime = CurrentTime;
      currEvent.EventName = mEvent;
      currEvent.channel = Integer.parseInt(channel);
      currEvent.EventParams = tmpParams;
      sEventQueue.add( currEvent );
		}
	}
	
	public TimedMidiStream clone()
	{
		TimedSymbolicEvent newEv;
		TimedMidiStream outStream;
		
		outStream= new TimedMidiStream();
		for(int i= 0; i < sEventQueue.size(); i++)
		{
			newEv= new TimedSymbolicEvent();
			newEv.CopyValues(sEventQueue.get(i));
			outStream.AddSymbolicEvent( newEv );
		}
		
		return outStream;
	}
	
	public void AddSymbolicEvent(TimedSymbolicEvent ev)
	{
		sEventQueue.add( ev );
	}
	
	public void ShortenDurations()
	{
		ShortenDurations(1);
	}
	
	public void ShortenDurations(long deltaTime)
	{
		for(TimedSymbolicEvent ev : sEventQueue)
		{
			if( ev.EventName.equals("noteon") )
			{
				String vol= ev.EventParams[1].replace(',', '.');
				double myVol= Double.parseDouble(vol);
				if( myVol == 0 )
				{
					long h= ev.AbsTime - deltaTime;
					if( h > 0)
						ev.AbsTime= h;
					else
						ev.AbsTime= 0;
				}
			}
			else if( ev.EventName.equals("noteoff") )
			{
				long h= ev.AbsTime - deltaTime;
				if( h > 0 )
					ev.AbsTime= h;
				else
					ev.AbsTime= 0;
			}
			else if( ev.EventName.equals("note") )
			{
				long myDur;
				
				myDur= Long.parseLong(ev.EventParams[2]) - deltaTime;
				if( myDur > 0 )
					ev.EventParams[2]= "" + myDur;
				else
					ev.EventParams[2]= "0";
			}
		}
		ReOrderByTime();
		MakeRelTime();
	}
	
	private void MakeRelTime()
	{
		long currTime;
		long LastTime= 0;
		
		for(TimedSymbolicEvent ev : sEventQueue)
		{
			currTime= ev.AbsTime;
			ev.RelTime= currTime - LastTime;
			LastTime= currTime;
		}
	}

	@SuppressWarnings("unchecked")
	private void ReOrderByTime()
	{
		ArrayList<inl> times= new ArrayList<inl>();
		int i;
		int N= sEventQueue.size();
		
		for(i= 0; i < N; i++)
			times.add(new inl(i, sEventQueue.get(i).AbsTime));
		
		//sort
		java.util.Collections.sort(times, new java.util.Comparator()
		{
			public int compare(Object a, Object b) {
				return (int) (((inl)a).time - ((inl)b).time);
			}
		} );
		
		long[] newindex= new long[N];
		for(i= 0; i < N; i++)
			newindex[i]= times.get(i).index;
			
		
		ReOrderByIndex( newindex );
		
		/*Dim QueueCopy As Collection
    Dim UnsortedTimes() As Variant
    '    Dim UnsortedTimes() As Long
    Dim NewIndex() As Long
    Dim mySize As Long
    Dim ev As TimedSymbolicEvent
    Dim i As Long
    mySize = sEventQueue.Count
    ReDim UnsortedTimes(1 To mySize)
    i = 1
    For Each ev In sEventQueue
        UnsortedTimes(i) = ev.AbsTime
        i = i + 1
    Next ev
    NewIndex = SortIndex(UnsortedTimes, "LessOrEqual")
    
    ReOrderByIndex NewIndex*/
	}

	@SuppressWarnings("unchecked")
	private void ReOrderByIndex(long[] index)
	{
		ArrayList<TimedSymbolicEvent> QueueCopy;
		QueueCopy= (ArrayList<TimedSymbolicEvent>) sEventQueue.clone();
		
		sEventQueue.clear();
		
		for(int i= 0; i < QueueCopy.size(); i++)
		{
			sEventQueue.add( QueueCopy.get( (int) index[i]));
		}
		
		/*Dim ev As TimedSymbolicEvent
    Dim QueueCopy As Collection
    Dim i As Long
    Set QueueCopy = New Collection
    For Each ev In sEventQueue
        QueueCopy.Add ev
    Next ev
    ClearCollection sEventQueue
    For i = 1 To QueueCopy.Count
        sEventQueue.Add QueueCopy.Item(myIndex(i))
    Next i*/
	}

	public void Play()
	{
		Play(0);
	}
	
	public void Play(int DeviceNum)
	{
		DeviceOutput();
		MakeCommands();
		MakeRelTime();
		
		//Debug.debugEventQueue(sEventQueue);
		//Debug.debugEventQueue(oEvents);
		
		// this.player.setMidiPlayer(player);
		this.player.Play(this, DeviceNum);
		
    /*Dim mOut As MidiOut
    Dim i As Integer
    Dim msg As MidiMSG
    Dim ev As TimedOutputEvent
    Dim NewStream As TimedMidiStream
    On Error GoTo FinishDevice
    Application.EnableCancelKey = xlErrorHandler
    DeviceOutput
    MakeCommands
    MakeRelTime
    Set mOut = New MidiOut
    mOut.OpenDevice DeviceNum
    For Each ev In oEvents
        If ev.RelTime >= 0 Then
            mOut.WaitMilli ev.RelTime
            mOut.Out ev.OutCommand
        End If
    Next ev
    mOut.CloseDevice
    Set mOut = Nothing
    Application.EnableCancelKey = xlInterrupt
    Exit Sub
FinishDevice:
    Set msg = New MidiMSG
    msg.DeviceOutput
    For i = 1 To 16
        mOut.Out msg.AllNotesOff(i)
    Next i
    mOut.CloseDevice
    Set mOut = Nothing
    Application.EnableCancelKey = xlInterrupt
    */
	}

	private void MakeCommands()
	{
		ReOrderByTime();
		MakeRelTime();
		
		oEvents= new ArrayList<TimedOutputEvent>();
		
		//Debug.showMessage("MakeCommands");
		
		for(TimedSymbolicEvent ev : sEventQueue)
		{
			if( ev.AbsTime >= 0 )
			{
				Object cmd= MakeMidiCommand(ev.EventName, ev.channel, ev.EventParams);
				if( cmd != null )
				{
					//Debug.showMessage(cmd.getClass().getName());
					//if( false ) //cmd is Array
					//{
						/*
						 * For i = LBound(oCmd) To UBound(oCmd)
              Set outEv = New TimedOutputEvent
              outEv.RelTime = evRelTime
              outEv.AbsTime = evAbsTime
              outEv.OutCommand = oCmd(i)
              oEvents.Add outEv
              Set outEv = Nothing
              evRelTime = 0
          		Next i
						 */
					//}
					//else
					//{
						TimedOutputEvent e= new TimedOutputEvent();
						e.RelTime= ev.RelTime;
						e.AbsTime= ev.AbsTime;
						e.OutCommand= cmd;
						oEvents.add(e);
					//}
				}
			}
		}
	}
	
	private Object MakeMidiCommand(String mName, int mChan, String[] mArgs )
	{
		MidiMSG OutFormat= new MidiMSG();
		Object ret;
		
		if( CommandsInFileFormat )
			OutFormat.FileOutput();
		else
			OutFormat.DeviceOutput();
		
		mName= mName.toLowerCase();
			
		if(mName.equals("noteon"))
		{
			String vol= mArgs[1].replace(',', '.');
			ret= OutFormat.NoteOn(mChan, Integer.parseInt(mArgs[0]), Double.parseDouble(vol));
		}
		else if(mName.equals("noteoff"))
			ret= OutFormat.NoteOff(mChan,Integer.parseInt(mArgs[0]), 0);
		else if(mName.equals("keypressure"))
		{
			String h= mArgs[1].replace(',', '.');
			ret= OutFormat.KeyPressure(mChan, Integer.parseInt(mArgs[0]), Double.parseDouble(h));
		}
		else if(mName.equals("controller"))
		{
			String h= mArgs[1].replace(',', '.');
			ret= OutFormat.Controller(mChan, Integer.parseInt(mArgs[0]), Double.parseDouble(h));
		}
		else if(mName.equals("instrument"))
			ret= OutFormat.Instrument(mChan, Integer.parseInt(mArgs[0]));
		else if(mName.equals("channelpressure"))
		{
			String press= mArgs[0].replace(',', '.');
			ret= OutFormat.ChannelPressure(mChan, Double.parseDouble(press));
		}
		else if(mName.equals("pitchbend"))
		{
			String bend= mArgs[0].replace(',', '.');
			ret= OutFormat.PitchBend(mChan, Double.parseDouble(bend));
		}
		else if(mName.equals("sysex"))
		{
			if( OutFormat.getOutputToFile() )
				ret= "SysEx ";
			else
				ret= "";
			
			for(int i = 0; i < mArgs.length; i++)
			{
				if( OutFormat.getOutputToFile() )
					ret= ((String ) ret) + Conversion.ZeroPad(Integer.toHexString(Integer.parseInt(mArgs[i])));
				else
					ret= ((String ) ret) + Character.forDigit(Integer.parseInt(mArgs[i]), 10);
			}
		}
		else if(mName.equals("modulation"))
		{
			String h= mArgs[0].replace(',', '.');
			ret= OutFormat.Modulation(mChan, Double.parseDouble(h));
		}
		else if(mName.equals("volume"))
		{
			String h= mArgs[0].replace(',', '.');
			ret= OutFormat.Volume(mChan, Double.parseDouble(h));
		}
		else if(mName.equals("expression"))
		{
			String h= mArgs[0].replace(',', '.');
			ret= OutFormat.Expression(mChan, Double.parseDouble(h));
		}
		else if(mName.equals("pan"))
		{
			String h= mArgs[0].replace(',', '.');
			ret= OutFormat.Pan(mChan, Double.parseDouble(h));
		}
		else if(mName.equals("sustain"))
		{
			String h= mArgs[0].replace(',', '.');
			ret= OutFormat.Sustain(mChan, Double.parseDouble(h));
		}
		else if(mName.equals("reverb"))
		{
			String h= mArgs[0].replace(',', '.');
			ret= OutFormat.Reverb(mChan, Double.parseDouble(h));
		}
		else if(mName.equals("chorus"))
		{
			String h= mArgs[0].replace(',', '.');
			ret= OutFormat.Chorus(mChan, Double.parseDouble(h));
		}
		else if(mName.equals("resetcontrollers"))
			ret= OutFormat.ResetControllers(mChan);
		else if(mName.equals("allnotesoff"))
			ret= OutFormat.AllNotesOff(mChan);
		else if(mName.equals("bankselectmsb"))
			ret= OutFormat.BankSelectMSB(mChan, Integer.parseInt(mArgs[0]));
		else if(mName.equals("bankselectlsb"))
			ret= OutFormat.BankSelectLSB(mChan, Integer.parseInt(mArgs[0]));
		else if(mName.equals("breath"))
			ret= OutFormat.Breath(mChan, Double.parseDouble(mArgs[0]));
		else if(mName.equals("footpedal"))
		{
			String h= mArgs[0].replace(',', '.');
			ret= OutFormat.FootPedal(mChan, Double.parseDouble(h));
		}
		else if(mName.equals("portamentotime"))
			ret= OutFormat.PortamentoTime(mChan, Integer.parseInt(mArgs[0]));
		else if(mName.equals("pitchbendrange"))
			ret= OutFormat.PitchBendRange(mChan, Integer.parseInt(mArgs[0]));
		else if(mName.equals("mastertunecoarse"))
			ret= OutFormat.MasterTuneCoarse(mChan, Integer.parseInt(mArgs[0]));
		else if(mName.equals("mastertunefine"))
			ret= OutFormat.MasterTuneFine(mChan, Integer.parseInt(mArgs[0]));
		else
		{
			Debug.showMessage("Da sollte ich nicht her mName=" + mName);
			ret= null;
		}
		
		return ret;
	}

	public void WriteMidiFile(String path, String FileName, boolean KeepTextFile)
	{
		
		myFileName= FileName;
		
		try
		{
			MakeMF(path);
			MakeMidiFromText( path, KeepTextFile );
		}
		catch (IOException e)
		{
			Debug.showException(e, "MidiCSD::WriteMidiFile");
			e.printStackTrace();
		}
	}

	private void MakeMidiFromText(String path, boolean keepTextFile)
	{
		String CurrFileName;
		File parser;
		File textFile;
		//String basepath; //Path where the package is located
		String commandString; //Command to be executed
		String cmdpath; //Full path to the Csvmidi tool
		String csvmidi; //Name of the executeable
		String sep; //Path separator /, \
		int os= 0; //help to provide faster Os dependent actions
		String line= null;
		String h= "";
		
		if (myFileName.equals(""))
			CurrFileName = "tempmid";
		else
			CurrFileName = myFileName;
		
		try
		{
			//Path workaround begin
			textFile= new File(path + CurrFileName + ".csv");
			String path2;
			path2= textFile.getCanonicalPath();
			path2= path2.substring(0, path2.length() - CurrFileName.length() - 4);
			
			try
			{
				String osName= System.getProperty("os.name");
				sep= System.getProperty("file.separator");
				
				if( osName.startsWith("Windows") )
				{
					csvmidi= "Csvmidi.exe";
					os= 1;
				}
				else if( osName.equals("Linux") )
				{
					csvmidi= "Csvmidi.lin";
					os= 2;
				}
				else if( osName.equals("Mac OS X") )
				{
					csvmidi= "Csvmidi.osx";
					os= 4;
				}
				else
				{
					String osType= System.getProperty("os.arch");
					String osVersion= System.getProperty("os.version");
					Debug.showMessage("Operating system type =>" + osType + "\n" +
						"Operating system name =>" + osName + "\n" +
						"Operating system version =>" + osVersion);
					
					csvmidi= "Csvmidi.lin";
				}
				
				parser= new File(MidiCSDextImpl.PackagePath + sep + "midicsv" + sep + csvmidi);
				cmdpath= parser.getAbsolutePath();
				
				if( os == 1 ) //Os == Windows
				{
					cmdpath= "\"" + cmdpath + "\"";
				}
				else if( os == 4) //Os == Mac Os
				{
					cmdpath= "/usr/local/bin/csvmidi";
				}
				else if( os == 2 ) //Os == Linux
				{
					if( !parser.setExecutable(true) )
						Debug.showMessage(parser.getName() + " kann nicht ausgefuehrt werden");
					
					/*
					 * Martin Dobiasch replaced this on 2012-08-26
					 * The problem is that the path could contain a blank which
					 * could cause troubles somehow in the past it was possible
					 *  to just place the command in " ", but now this doesn't
					 *  work anymore so we need to replace any special
					 *   characters ...
					 */
					// cmdpath= "\"" + cmdpath + "\"";
					
					cmdpath.replace(" ", "\\ ");
				}
					
					
				commandString= cmdpath + " " + path2 + CurrFileName + ".csv " + path2
						+ CurrFileName + ".mid";
				
				// System.out.println("Write File: " + path2 + CurrFileName + ".mid");
				
				/*
				if( os == 4 ) //aus, leck mich doch, noch ein Workaround
					commandString = "/usr/local/bin/csvmidi " + path2 + CurrFileName + 
						".csv " + path2 + CurrFileName + ".mid";
				*/
//				Debug.showMessage("Exec: " + commandString);
				
				Runtime rt= Runtime.getRuntime();
				Process pr= rt.exec(commandString);
//				Debug.showMessage("after rt.exec");
				
				BufferedReader input= new BufferedReader(new InputStreamReader(pr
						.getInputStream()));
				
				while( (line= input.readLine()) != null )
				{
					h+= line + "\n";
				}
				
				int exitVal= pr.waitFor();
				if( exitVal != 0 )
					Debug.showMessage("Exited with error code " + exitVal);
				if( h != "" )
					Debug.showMessage(h);
				
				//Begin T2MF - Code
				/*
				T2Mf t2mf= new T2Mf();
				
				//Debug.showMessage("vor aufruf");
				t2mf.t2mf((path2 + CurrFileName + ".txt"),
						(path2+CurrFileName+".mid"));
				*/
				//end T2MF - Code
			}
			catch( Exception e)
			{
				Debug.showException(e, "MakeMidiFromText");
			}
			
			if( !keepTextFile )
				textFile.delete();
			
			textFile= null;
			parser= null;
			myFileName= CurrFileName + ".mid";
			
			//old code
			/*commandString= path2 + "t2mf " + path2 + CurrFileName + ".txt " + 
				path2 + CurrFileName + ".mid";
			
			Debug.showMessage(commandString);
			
			Runtime rt = Runtime.getRuntime();
			// Process pr = rt.exec("cmd /c dir");
			Process pr = rt.exec(commandString);
			
			BufferedReader input = new BufferedReader(new InputStreamReader(pr
					.getInputStream()));
			
			String line = null;
			String h = "";
			
			while ((line = input.readLine()) != null)
			{
				h += line + "\n";
			}
			
			int exitVal = pr.waitFor();
			Debug.showMessage("Exited with error code " + exitVal);
			Debug.showMessage(h);*/
		}
		catch (Exception e)
		{
			Debug.showException(e, "TimedMidiStream::MakeMidiFromText");
			e.printStackTrace();
		}

		/*
		Dim TaskID As Long
    Dim hProc As Long
    Dim lExitCode
    Dim ACCESS_TYPE As Long
    Dim STILL_ACTIVE As Long
    Dim commandString As String
    Dim CurrFileName
    If myFileName = "" Then
        CurrFileName = "tempmid"
    Else
        CurrFileName = myFileName
    End If
    ACCESS_TYPE = &H400
    STILL_ACTIVE = &H103
    commandString = "t2mf " & CurrFileName & ".txt" & " " & CurrFileName & ".mid"
    TaskID = Shell(commandString, vbHidden)
    hProc = OpenProcess(ACCESS_TYPE, False, TaskID)
    Do
        GetExitCodeProcess hProc, lExitCode
        DoEvents
    Loop While lExitCode = STILL_ACTIVE
    If Not KeepTextFile Then Kill CurrFileName & ".txt"
		*/
	}

	private void MakeMF(String path) throws IOException
	{
		String CurrFileName;
		BufferedWriter out;
		if( myFileName.equals("") )
			CurrFileName= "tempmid.csv";
		else
			CurrFileName= myFileName + ".csv";
		
		//Debug.showMessage("MakeMF 1 " + CurrFileName);
		FileOutput();
		MakeCommands();
		MakeRelTime();
		out= OpenTextFile( path + CurrFileName );
		WriteProlog(out);
		WriteCommands(out);
		WriteEpilog( out, this.Duration() );
		CloseTextFile(out);
	}

	private long Duration()
	{
		long ret= 0;
		for(TimedSymbolicEvent ev : sEventQueue )
		{
			if( ev.AbsTime > ret )
				ret= ev.AbsTime;
		}

		return ret;
	}

	private BufferedWriter OpenTextFile(String FileName) throws IOException
	{
		file= new File(FileName);
		BufferedWriter out= new BufferedWriter(new FileWriter(file));
		return out;
	}
	
	private void WriteProlog(BufferedWriter out) throws IOException
	{
		out.write("0, 0, Header, 1, 1, 480\r\n"); //TODO: format = 1 ??, division = 480?
		out.write("1, 0, Start_track\r\n");

		//Begin T2Mf - Code
		/*
		out.write("Mfile   1   1   1000\r\n");
	  out.write("MTrk\r\n");
	  out.write("0   tempo 1000000\r\n");
	  */
		//End T2Mf - Code
	}
	
	private void WriteCommands(BufferedWriter out) throws IOException
	{
		//Debug.showMessage("WriteCommands");
		for(TimedOutputEvent ov : oEvents )
		{
			//Debug.showMessage("WriteCommands");
			out.write("1, " + ov.AbsTime + ", " + ov.OutCommand+ "\r\n");
		}
		
		//Begin T2Mf - Code
		/*
		for(TimedOutputEvent ov : oEvents )
		{
			out.write(ov.AbsTime + " " + ov.OutCommand + "\r\n");
		}
		*/
		//End T2Mf - Code
	}
	
	private void WriteEpilog(BufferedWriter out, long TotalTime) throws IOException
	{
		out.write("1, " + TotalTime + ", End_track\r\n");
		out.write("0, 0, End_of_file\r\n");
		//Begin T2Mf - Code
		/*
		out.write( TotalTime + "   Meta TrkEnd\r\n" );
    out.write( "TrkEnd\r\n" );
    */
		//End T2Mf - Code
	}
	
	private void CloseTextFile(BufferedWriter out) throws IOException
	{
		out.close();
		file= null;
	}
	
	/**
	 * Merge an existing stream to this one
	 * @param inStream
	 */
	public void Merge(TimedMidiStream inStream)
	{
		TimedSymbolicEvent evCopy;
		for(TimedSymbolicEvent ev: inStream.sEventQueue)
		{
			evCopy= new TimedSymbolicEvent();
			evCopy.CopyValues(ev);
			this.sEventQueue.add(evCopy);
		}
	}
	
	/**
	 * Shifting/Delaying stream.
	 * @param deltaTime
	 */
	public void TimeShift(int deltaTime)
	{
		//Debug.showMessage("Shifting Time by " + deltaTime);
		for(TimedSymbolicEvent ev: sEventQueue)
			ev.AbsTime+= deltaTime;
	}

	/**
	 * Change Speed of stream by factor
	 * @param factor
	 */
	public void ChangeSpeed(double factor)
	{
		for(TimedSymbolicEvent ev: sEventQueue)
		{
			ev.AbsTime*= factor;
			ev.RelTime*= factor;
			
			if( ev.EventName.equals("note"))
			{
				ev.EventParams[2]= "" + (int)( ( Long.parseLong(ev.EventParams[2]) * factor ) );
			}
		}
	}

	/**
	 * Change volume of stream by factor
	 * @param factor
	 */
	public void ChangeVol(double factor)
	{
		for(TimedSymbolicEvent ev: sEventQueue)
		{
			if( ev.EventName.equals("note") || ev.EventName.equals("noteon") )
			{
				long a= (long) (Long.parseLong(ev.EventParams[1]) * factor);
				if( a < 1 )
					ev.EventParams[1]= "" + a;
				else
					ev.EventParams[1]= "1";
			}
		}
	}

	/**
	 * Transepose stream up by offset
	 * @param offset
	 */
	public void Transpose(int offset)
	{
		for(TimedSymbolicEvent ev: sEventQueue)
			if( ev.EventName.equals("note") || ev.EventName.equals("noteon") )
				ev.EventParams[0]= "" + ( Integer.parseInt( ev.EventParams[0]) + offset );
	}

	/**
	 * Inverts stream around FixedPitch. Like turning the sheet upside down
	 * @param FixedPitch
	 */
	public void Invert(int FixedPitch)
	{
		for(TimedSymbolicEvent ev: sEventQueue)
		{
			if( ev.EventName.equals("note") || ev.EventName.equals("noteon") )
			{
				int pitch;
				pitch= Integer.parseInt( ev.EventParams[0] );
				pitch= 2 * FixedPitch - pitch;
				ev.EventParams[0]= "" + pitch;
			}
		}
		this.MakeRelTime();
	}
	
	public void Reverse()
	{
		long totTime;
		int currDur;
		
		totTime= this.Duration();
		this.CombineNoteOnNoteOff();
		//Debug.debugEventQueue(sEventQueue);
		
		for(TimedSymbolicEvent ev : sEventQueue )
		{
			if( ev.EventName.equals("note") )
			{
				currDur= Integer.parseInt( ev.EventParams[2] );
				ev.AbsTime= totTime - ev.AbsTime - currDur;
			}
			else
				ev.AbsTime= totTime - ev.AbsTime;
		}
		
		//Debug.debugEventQueue(sEventQueue);
		
		ReOrderByTime();
		MakeRelTime();
		//Debug.debugEventQueue(sEventQueue);
		this.SeparateNoteOnNoteOff();
		//Debug.debugEventQueue(sEventQueue);
	}
	
	private void SeparateNoteOnNoteOff()
	{
		ArrayList<TimedSymbolicEvent> newQueue;
		String[] evParams, newParams;
		TimedSymbolicEvent newEv;
		
		newQueue= new ArrayList<TimedSymbolicEvent>();
		
		for(TimedSymbolicEvent ev : sEventQueue )
		{
			if( ev.EventName.equals("note") )
			{
				evParams= ev.EventParams;
        newParams= evParams;
        newParams= (String[]) Helper.Preserve(newParams, 2);
        newEv= new TimedSymbolicEvent();
        newEv.EventName= "noteon";
        newEv.channel= ev.channel;
        newEv.AbsTime= ev.AbsTime;
        newEv.EventParams= newParams;
        newQueue.add( newEv );
        newEv= new TimedSymbolicEvent();
        newParams= (String[]) Helper.Preserve(newParams, 2);
        newParams[1]= "0";
        newEv.EventName= "noteon";
        newEv.channel= ev.channel;
        newEv.AbsTime= ev.AbsTime + Long.parseLong( evParams[2] );
        newEv.EventParams= newParams;
        newQueue.add( newEv );
			}
			else
				newQueue.add( ev );
		}
		
		sEventQueue= newQueue;
		
		MakeRelTime();
		ReOrderByTime();
	}

	private void CombineNoteOnNoteOff()
	{
	  TimedMidiStream NotesQueue, OtherQueue;
	  NotesQueue = this.clone();
	  OtherQueue = this.clone();
	  NotesQueue.SelectNoteEvents();
	  OtherQueue.SelectNonNoteEvents();
	  NotesQueue.ReorderByChannelPitchTime();
	  NotesQueue.CombineHelper();
	  OtherQueue.Merge( NotesQueue );
	  
	  sEventQueue = OtherQueue.sEventQueue;
	  
	  this.ReOrderByTime();
	}
	
	private void CombineHelper()
	{
		TimedSymbolicEvent curritem, nextitem;
		//String[] currParams;
    String[] newParams;
    ArrayList<TimedSymbolicEvent> newQueue;
		
		for(TimedSymbolicEvent ev : sEventQueue )
		{
			if( ev.equals("noteoff") )
			{
				//currParams = ev.EventParams;
				newParams = ev.EventParams;
				newParams= (String[]) Helper.Preserve( newParams, 2);
				newParams[1] = "0";
				ev.EventName = "noteon";
			}
		}
		
		for(int i= 0; i < sEventQueue.size() - 1; i++)
		{
			curritem= sEventQueue.get(i);
      nextitem= sEventQueue.get(i + 1);
      if( curritem.channel == nextitem.channel &&
	      curritem.EventParams[0].equals( nextitem.EventParams[0] ) &&
	      Integer.parseInt(curritem.EventParams[1]) > 0 &&
	      nextitem.EventParams[1].equals("0") )
      {
      	//currParams = curritem.EventParams;
        newParams = curritem.EventParams;
        newParams= Helper.Preserve(newParams, 3);
        newParams[2] = "" + ( nextitem.AbsTime - curritem.AbsTime );
        curritem.EventName = "note";
        curritem.EventParams = newParams;
        nextitem.EventName = "todelete";
      }
		}
		
		newQueue = new ArrayList<TimedSymbolicEvent>();
		for(TimedSymbolicEvent ev : sEventQueue )
		{
        if( !ev.EventName.equals("todelete") )
            newQueue.add( ev );
		}
    sEventQueue= newQueue;
	}

	private void SelectNoteEvents()
	{
		ArrayList<TimedSymbolicEvent> list;
		list= new ArrayList<TimedSymbolicEvent>();
		for(TimedSymbolicEvent ev : sEventQueue )
	  {
	      if( ev.EventName.equals("note") || 
	      		ev.EventName.equals("noteon") ||
	      		ev.EventName.equals("noteoff") )
	      {
	          list.add(ev);
	      }
	  }
		sEventQueue= list;
	}
	
	private void SelectNonNoteEvents()
	{
		ArrayList<TimedSymbolicEvent> list;
		list= new ArrayList<TimedSymbolicEvent>();
		for(TimedSymbolicEvent ev : sEventQueue )
	  {
	      if( ev.EventName.equals("note") || 
	      		ev.EventName.equals("noteon") ||
	      		ev.EventName.equals("noteoff") )
	      {
	      	
	      }
	      else
	          list.add(ev);
	  }
		sEventQueue= list;
	}
	
	//@SuppressWarnings("unchecked")
	@SuppressWarnings("unchecked")
	private void ReorderByChannelPitchTime()
	{
		ArrayList<inl2> list= new ArrayList<inl2>();
		int i;

		i= 0;
	  for(TimedSymbolicEvent ev : sEventQueue )
	  {
	  	list.add(new inl2(i, Conversion.GetChannelPitchTimeVolume(ev)));
	  	i++;
	  }
	  
	  //sort
		java.util.Collections.sort(list, new java.util.Comparator()
		{
			public int compare(Object a, Object b) {
				return ((inl2) a).s.compareTo(((inl2) b).s);
			}
		} );
		
		int N= sEventQueue.size();
		long[] newindex= new long[N];
		for(i= 0; i < N; i++)
			newindex[i]= list.get(i).index;
	  
		ReOrderByIndex( newindex );
	}

	/**
	 * Enlarges the stream to howOften-times
	 * @param howOften
	 */
	public void Repeat(int howOften)
	{
		TimedMidiStream copy;
		long dur= this.Duration();
		
		for(int i= 1; i < howOften; i++)
		{
			copy= this.clone();
			copy.TimeShift((int) (i * dur) );
			this.Merge(copy);
		}
	}

	/**
	 * Rechannel the stream. All oldchannel will be changed to newchannel
	 * @param oldchannel
	 * @param newchannel
	 */
	public void Rechannel(int oldchannel, int newchannel)
	{
		for(TimedSymbolicEvent ev: sEventQueue)
			if( ev.channel == oldchannel )
				ev.channel= newchannel;
	}

	public ArrayList<TimedOutputEvent> getOutputEvents()
	{
		return oEvents;
	}
	
	@Deprecated
	public void setMidiPlayer(MidiPlayer midiplayer)
	{
		this.midiplayer= midiplayer;
	}
	
	public void setPlayer(IPlayer player)
	{
		this.player= player;
	}
}
