/**
 * 
 */
package at.univie.MidiCSD;

import java.util.ArrayList;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
public class MidiQueue
{
	private TimedMidiStream m_currentStream;
	private ArrayList<TimedMidiStream> m_streams;
	
	public static final String DefaultStreamName= "MyLittleSong";
	//public static final String DefaultFileName= "";
	
	public MidiQueue()
	{
		m_currentStream= new TimedMidiStream();
		m_streams= null;
	}
	
	public TimedMidiStream getCurrentStream()
	{
		return m_currentStream;
	}
	
	/**
	 * Get the first Stream in the Queue
	 * @return null if empty
	 */
	public TimedMidiStream getFirstStream()
	{
		if( m_streams == null || m_streams.size() < 1 )
			return null;
		else
			return m_streams.get(0);
	}
	
	/**
	 * get access to specified Stream in Queue
	 * @param name Name of the Stream
	 * @return Stream, null if not found
	 */
	public TimedMidiStream get(String name)
	{
		if( m_streams == null )
			return null;
		
		for(TimedMidiStream s : m_streams )
			if( s.streamname.equals(name) )
				return s;
		
		return null;
	}
	
	/**
	 * Adding a stream to the queue
	 * @param stream
	 */
	public void addStream(TimedMidiStream stream)
	{
		if( m_streams != null )
			m_streams.add(stream);
	}
	
	/**
	 * Method for asserting a stream
	 * @param QueueName
	 * @param Append, default Value false
	 * @param UseCurrent, default Value false
	 */
	public void AssertQueue(String QueueName, boolean Append, boolean UseCurrent)
	{
		//Debug.showMessage("AssertQueue " + Append + " " + UseCurrent + " " + m_currentStream);
		
		if( UseCurrent && !( m_currentStream == null ) )
			return;
		
		if( m_streams == null )
			m_streams= new ArrayList<TimedMidiStream>();
		
		//Debug.showMessage("assertQueue 0");
		
		if( QueueName.equals("") )
		{
			if( m_currentStream == null )
				m_currentStream= new TimedMidiStream();
			m_currentStream.streamname= DefaultStreamName;
			m_streams.add(m_currentStream); 
		}
		else
		{
			//Debug.showMessage("assertQueue a");
			int i;
			m_currentStream= null;
			for(i= 0; i < m_streams.size(); i++)
			{
				TimedMidiStream tmp= m_streams.get(i);
				if( tmp.streamname.equals(QueueName) )
				{
					i= m_streams.size();
					m_currentStream= tmp;
				}
			}
			//Debug.showMessage("assertQueue b");
			
			if( m_currentStream == null )
			{
				m_currentStream= new TimedMidiStream();
				m_currentStream.streamname= QueueName;
				m_streams.add( m_currentStream );
			}
			//Debug.showMessage("assertQueue c");
			
		}
		
		//Debug.showMessage("assertQueue d");
		
		if( !Append )
			m_currentStream.Clear();
		
		//Debug.showMessage("assertQueue e");
		
	}
	
	public void AddToCurrentQueue(String[][] inRange)
	{
		AddToCurrentQueue(inRange, true, true);
	}
	
	public void AddToCurrentQueue(String[][] inRange, boolean UseCurrent, boolean FromMenu)
	{
		//Debug.showMessage("AddToCurrentQueue" + UseCurrent + " " + FromMenu );
		if( Helper.hasTimeStamps(inRange) )
		{
			if( Helper.TimeStampString(inRange).equals("abstime") )
				AddEvents(inRange, true, UseCurrent, FromMenu);
			else
				AddEvents(inRange, false, UseCurrent, FromMenu);
		}
		else
		{
			/*
			 * There are no timestamps for the events to add -> create them manually
			 * First we check for title line and stuff
			 * Then we create copy for inRange with the new values
			 */
			String[][] tmp;
			if(!Helper.hasTitleLine(inRange))
			{
				tmp= Helper.createStringMat(inRange.length + 1, inRange[0].length + 1);
				tmp[0][0]= "reltime";
				tmp[0][1]= DefaultStreamName;
				
				int rows= inRange.length + 1;
				int cols= inRange[0].length;
				int i, j;
				tmp[1][0]= "0";
				for(j= 0; j < cols; j++)
					tmp[1][j+1]= inRange[0][j];
				for(i= 2; i < rows; i++)
				{
					//copy duration from above command down
					tmp[i][0]= inRange[i-2][4];
					//copy the rest of row of inRange to tmp row
					for(j= 0; j < cols; j++)
						tmp[i][j+1]= inRange[i-1][j];
				}
			}
			else
			{
				tmp= Helper.createStringMat(inRange.length, inRange[0].length + 1);
				if( Helper.TimeStampString(inRange).length() > 0 )
					tmp[0][0]= Helper.TimeStampString(inRange);
				else
					tmp[0][0]= "reltime";
				
				tmp[0][1]= Helper.PhraseNameFromRange(inRange);
				
				int rows= inRange.length;
				int cols= inRange[0].length;
				int i, j;
				tmp[1][0]= "0";
				for(j= 0; j < cols; j++)
					tmp[1][j+1]= inRange[1][j];
				for(i= 2; i < rows; i++)
				{
					//copy duration from above command down
					tmp[i][0]= inRange[i-1][4];
					//copy the rest of row of inRange to tmp row
					for(j= 0; j < cols; j++)
						tmp[i][j+1]= inRange[i][j];
				}
			}
			
			this.AddToCurrentQueue(tmp, UseCurrent, FromMenu);
			/*
			Set oldSel = Selection
       Set tmpSheet = ThisWorkbook.Sheets("TempSheet")
       tmpSheet.Cells.Clear
       If Not hasTitleLine(inRange) Then
           tmpSheet.Cells(1, 1).Value = "reltime"
           tmpSheet.Cells(1, 2).Value = DefaultStreamName
           Set sourceRange = inRange
           firstTargetLine = 2
           lastTargetLine = inRange.Rows.Count + 1
       Else
           If Len(TimeStampString(inRange)) > 0 Then
               tmpSheet.Cells(1, 1).Value = TimeStampString(inRange)
           Else
               tmpSheet.Cells(1, 1).Value = "reltime"
           End If
           tmpSheet.Cells(1, 2) = PhraseNameFromRange(inRange)
           Set sourceRange = Range(inRange.Cells(2, 1), inRange.Cells(inRange.Rows.Count, inRange.Columns.Count))
           firstTargetLine = 1
           lastTargetLine = inRange.Rows.Count
       End If
       inRange.Copy
       Set targetRange = Range(tmpSheet.Cells(firstTargetLine, 2), tmpSheet.Cells(lastTargetLine, 1 + inRange.Columns.Count))
       targetRange.PasteSpecial Paste:=xlPasteValues
       Application.CutCopyMode = False
       Set targetRange = tmpSheet.Cells(2, 1)
       targetRange.Value = 0
       Set targetRange = Range(tmpSheet.Cells(3, 1), _
           tmpSheet.Cells(lastTargetLine, 1))
       targetRange.FormulaR1C1 = "=R[-1]C[5]"
       targetRange.Copy
       targetRange.PasteSpecial Paste:=xlPasteValues
       Application.CutCopyMode = xlCopy
       oldSel.Select
       Set targetRange = Range(tmpSheet.Cells(1, 1), tmpSheet.Cells(lastTargetLine, 1 + inRange.Columns.Count))
       AddToCurrentQueue targetRange, UseCurrent:=UseCurrent, FromMenu:=FromMenu
       tmpSheet.Cells.Clear
			 */
		}
	}
	
	public String AddEvents(String[][] inRange, boolean UseAbsTime, boolean UseCurrent, boolean FromMenu)
	{
		 String res;
		 
		 res = NameOfAddedEventsFromRange(inRange, true, UseAbsTime, UseCurrent, FromMenu);
		 
		 return res;
	}
	
	public String NameOfAddedEventsFromRange(String[][] inRange, boolean doExtend, boolean UseAbsTime, 
			boolean UseCurrent, boolean FromMenu)
	{
		int startIndex;
		String phraseName;
		boolean LocalAbsTime;
		int i;
		
		//Debug.showMessage("NameOfAddedEventsFromRange 1");
		
		if( Helper.hasTitleLine(inRange))
			startIndex= 1;
		else
			startIndex= 0;
		
		if( FromMenu )
			phraseName= DefaultStreamName;
		else
			phraseName= Helper.PhraseNameFromRange(inRange);
		
		LocalAbsTime= (Helper.TimeStampString(inRange).toLowerCase()).equals("abstime");
		
		if( FromMenu )
			this.AssertQueue(phraseName, true, false);
		else
			this.AssertQueue(phraseName, false, false);
		
		//Debug.showMessage("NameOfAddedEventsFromRange 2");
		
		for(i= startIndex; i < inRange.length; i++)
		{
			//Debug.showMessage("NameOfAddedEventsFromRange 3" + i );
			if( LocalAbsTime )
				m_currentStream.AddTimedEvent(inRange[i][0], inRange[i][1], inRange[i][2], 
						Helper.ArrayRight(inRange, i, 3));
			else
			{
				m_currentStream.AddDelayedEvent(inRange[i][0], inRange[i][1], inRange[i][2], 
						Helper.ArrayRight(inRange, i, 3));
			}
		}
		
		return phraseName;
	}

	/*
	 * Remove a Stream from the queue
	 * @param StreamName Name of the Stream to be removed
	 */
	public void aa(){}
	
	/**
	 * Remove a Stream from the queue
	 * @param StreamName Name of the Stream to be removed
	 * @return Handle to the stream which was removed, null if none removed
	 */
	public TimedMidiStream remove(String StreamName)
	{
		if( m_streams == null )
			return null;
		
		for(int i= 0; i < m_streams.size(); i++)
		{
			TimedMidiStream s= m_streams.get(i);
			if( s.streamname.equals(StreamName) )
			{
				m_streams.remove(i);
				return s;
			}
		}
		return null;
	}
}
