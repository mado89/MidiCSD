/**
 * 
 */
package at.univie.MidiCSD;

import java.util.ArrayList;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
public class Helper
{
	public static boolean hasTitleLine(String[][] inRange)
	{
		if( inRange[0].length <= 1 || inRange.length <= 1 )
			return false;
		
		return !isPhraseCommand(inRange[0][0]);
	}
	
  public static boolean isPhraseCommand(String inString)
	{
		String[] phraseCommands = {"note", "noteon", "noteoff", "pause", "instrument", 
        "expression", "pitchbend", "pan", "sustain", "reverb", 
        "chorus", "modulation", "volume", "mastertune", "controller", 
        "channelpressure", "keypressure", "sysex", "resetcontrollers", 
        "allnotesoff", "bankselectlsb", "bankselectmsb", "breath", 
        "footpedal", "portamentotime", "pitchbendrange"};
		String inStringa = inString.toLowerCase();
		
		for(int i= 0; i < phraseCommands.length; i++)
		{
			if( inStringa.equals(phraseCommands[i]))
				return true;
		}
		
		return false;
	}
  
  public static String StreamNameOfRange(String[][] inRange)
  {
  	if( hasTitleLine(inRange))
  	{
  		if( !inRange[0][0].equals("reltime") && !inRange[0][0].equals("abstime")
  				&& !inRange[0][0].equals("") )
  		{
  			return inRange[0][0];
  		}
  		if( !inRange[0][1].equals("reltime") && !inRange[0][1].equals("abstime")
  				&& !inRange[0][1].equals("") )
  		{
  			return inRange[0][1];
  		}
  	}
  	
		return MidiQueue.DefaultStreamName;
  }
  
  public static boolean hasTimeStamps(String[][] inRange)
  {
  	if( inRange.length <= 1 || inRange[0].length <= 1 )
  		return false;
  	
  	try //HasTimeStamps = TypeName(inRange.Cells(2, 1).Value) = "Double"
  	{
  		Integer.parseInt(	inRange[1][0] );
  	}
  	catch( NumberFormatException nfe )
  	{
  		return false;
  	}
  	return true;
  }
  
  public static String TimeStampString(String[][] inRange)
  {
  	String ret, workstring;
  	
  	if( !hasTimeStamps(inRange) )
  		return "";
  	
  	ret= "reltime";
  	workstring= inRange[0][1].toLowerCase();
  	if( workstring.equals("reltime") || workstring.equals("abstime"))
  		return workstring;
  	workstring= inRange[0][0].toLowerCase();
  	if( workstring.equals("reltime") || workstring.equals("abstime"))
  		return workstring;
  	
  	return ret;
  }
  
  public static String PhraseNameFromRange(String[][] inRange)
  {
  	if( !hasTitleLine(inRange))
  		return MidiQueue.DefaultStreamName;
  	
  	String tmp;
  	
  	tmp= inRange[0][0];
  	if( !tmp.equals("reltime") && !tmp.equals("abstime") && !tmp.equals("") )
  		return tmp;
  	
  	tmp= inRange[0][1];
  	if( !tmp.equals("reltime") && !tmp.equals("abstime") && !tmp.equals("") )
  		return tmp;
  	
  	//nothing found yet
  	return MidiQueue.DefaultStreamName;
  }
  
  public static String[] ArrayRight(String[][] Cells, int currow, int curcol )
  {
  	ArrayList<String> ret= new ArrayList<String>();
  	//String leftcell= Cells[currow][curcol];
  	//String rightcell= leftcell;
  	
	  int col= curcol;
	  
	  //Debug.showMessage("ArrayRight" + Cells[currow][col]);
	  
	  //TODO: isEmpty doesn't work on OS X ?
	  //while( col < Cells[currow].length && !Cells[currow][col].isEmpty() )
	  while( col < Cells[currow].length && !Cells[currow][col].equals("") )
	  {
	  	//Debug.showMessage(Cells[currow][col]);
	  	ret.add(Cells[currow][col]);
	  	col++;
	  }
	  
	  //Debug.showMessage("compl s" + ret.size());
	  String[] rret= new String[ret.size()];
	  //Debug.showMessage("array created");
	  rret= ret.toArray(rret);
	  
	  //Debug.showMessage("ArrayRight end");
	  
  	return rret;
  }
  
  public static String[][] createStringMat(int rows, int cols)
  {
  	String[][] ret;
  	
  	ret= new String[rows][];
  	
  	for(int i= 0; i < rows; i++)
  		ret[i]= new String[cols];
  	
  	return ret;
  }

	public static String[] Preserve(String[] array, int newCount)
	{
		String[] ret= new String[newCount];
		int i;
		int s;
		
		if( newCount > array.length)
			s= array.length;
		else
			s= newCount;
		
		for(i= 0; i < s; i++)
			ret[i]= array[i];
		for(;i < newCount; i++)
			ret[i]= null;
		
		return ret;
	}
}
