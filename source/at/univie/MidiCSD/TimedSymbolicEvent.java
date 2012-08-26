/**
 * 
 */
package at.univie.MidiCSD;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
public class TimedSymbolicEvent
{
	public long AbsTime;
	public long RelTime;
	public int  channel;
	public String EventName;
	public String[] EventParams;
	
	public void CopyValues(TimedSymbolicEvent ev)
	{
		this.AbsTime= ev.AbsTime;
		this.RelTime= ev.RelTime;
		this.channel= ev.channel;
		this.EventName= ev.EventName;
		this.EventParams= ev.EventParams.clone();		
	}
	
	public String toString()
	{
		String h= "";
		for(String hh : EventParams)
			h+= "(" + hh + ")";
		return "[" + AbsTime + "," + EventName + "," + channel + " [" + h + 
			"] " + RelTime;
	}
}

/*
' MidiCSD
' © 2009 Erich Neuwirth
' a toolkit for playing music and producing sound effects from Excel
' ------------------------------------------------------------------

Option Explicit

Public AbsTime As Long
Public RelTime As Long
Public channel As Integer
Public EventName As String
Public EventParams As Variant

Public Sub CopyValues(Template As TimedSymbolicEvent)
    AbsTime = Template.AbsTime
    RelTime = Template.RelTime
    channel = Template.channel
    EventName = Template.EventName
    EventParams = Template.EventParams
End Sub
*/