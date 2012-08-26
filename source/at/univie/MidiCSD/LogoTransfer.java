package at.univie.MidiCSD;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
public class LogoTransfer implements java.awt.datatransfer.ClipboardOwner
{
	public String LogoString(String[][] inRange)
	{
		return LogoString(inRange,true);
	}
	
	public String LogoString(String[][] inRange, boolean WithTimestamp)
	{
		int i, j, nRows, nCols;
		int CommandOffsetBase = (WithTimestamp) ? 1 : 0;
		String cmdString;
		String channelString;
		long pitchval;
		String pitchString = "";
		String noteString = "";
		String volString= "";
		String durString= "";
		String ret= "";
		
		nRows= inRange.length;
		
		for(i= 0; i < nRows; i++)
		{
			String tmp= "";
			nCols= inRange[i].length;
			
			cmdString= inRange[i][CommandOffsetBase].toLowerCase();
			channelString= inRange[i][CommandOffsetBase + 1].toLowerCase();
			
			if( cmdString.equals("note") || cmdString.equals("noteon") || cmdString.equals("noteoff") )
			{
					//tmp= cmdString + " " + channelString;
					pitchval= Integer.parseInt( inRange[i][CommandOffsetBase + 2] );
					if( pitchval < 0 )
					{
						int h= CommandOffsetBase + 2;
						if( cmdString.equals("note") )
							h+= 1;
						for(j= h; j < nCols - 1; j++)
						{
							noteString= inRange[i][j];
							if( noteString.length() > 0 )
							{
								if( j > CommandOffsetBase + 2 )
									pitchString+= " ";
								pitchString+= noteString;
							}
						}
						pitchString= "[" + pitchString + "]";
					}
					else
					{
						pitchString= inRange[i][CommandOffsetBase + 2];
					}
					
					volString= inRange[i][CommandOffsetBase + 3].replace(',', '.');
					durString= inRange[i][CommandOffsetBase + 4];
					tmp= "[" + cmdString + " " + channelString + " " + pitchString + 
						" " + volString + " " + durString + "]";
						
			}
			else
			{
				tmp= "[" + cmdString + " ";
				for(j= CommandOffsetBase+1; j < nCols; j++)
					tmp+= ( (j > 1) ? " " : "" ) + inRange[i][j].replace(',', '.');
				tmp+= "]";
			}
			
			if( WithTimestamp )
				tmp= "[" + inRange[i][0] + " " + tmp + "]";
			
			tmp+= "\r\n";
			ret+= tmp;
		}
		
		return ret;
	}
	
	public void AsLogoToClipboard(String[][] inRange)
	{
		AsLogoToClipboard(inRange, true);
	}
	
	public void AsLogoToClipboard(String[][] inRange, boolean WithTimestamp)
	{
		//String topleftString;
		//String topsecondString;
		//String hasPhraseName;
		String outString;
		String titleString = "";
		
		//topleftString = inRange[0][0];
    //topsecondString = inRange[0][1];
    
    if( Helper.hasTitleLine(inRange) )
    {
    	titleString= "to " + Helper.StreamNameOfRange(inRange);
    	if( Helper.hasTimeStamps(inRange) )
    	{
    		titleString+= ".commands";
    	}
    	titleString+= "\r\noutput [\r\n";
    	
    	//Set workRange = inRange.Range(Cells(2, 1), Cells(inRange.Rows.Count, inRange.Columns.Count))
    	String[][] workRange= new String[inRange.length - 1][inRange[0].length];
    	for(int i= 1; i < inRange.length; i++)
    	{
    		for(int j= 0; j < inRange[0].length; j++)
    			workRange[i-1][j]= inRange[i][j];
    	}
    	outString= titleString + IdentString(LogoString(workRange), 2) + " ]\r\nend\r\n";
    }
    else
    {
    	outString= LogoString(inRange);
    }
    
    StringSelection stringSelection = new StringSelection( outString );
    Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents( stringSelection, this );
	}
	
	public String IdentString(String inString, int Ident)
	{
		String spaceString= "";
		String workString;
		
		for(int i=0;i<Ident;i++) spaceString+= " ";
		
		workString= inString.replace("\r\n", "\r\n" + spaceString );
		
		if( workString.startsWith(spaceString))
			return workString;
		else
			return spaceString + workString;
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		//do nothing
	}
}
