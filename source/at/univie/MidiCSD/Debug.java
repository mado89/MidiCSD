/**
 * 
 */
package at.univie.MidiCSD;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//import at.univie.MidiCSD.impl.MidiCSDextImpl;

import com.sun.star.beans.Property;
import com.sun.star.beans.XPropertySet;

/**
 * @author Martin Dobiasch
 *
 */
public class Debug
{
	static javax.swing.JFrame jframe;
	private static BufferedWriter out= null;
	private static boolean FILE_OUTPUT= false;
	
	public static void showMessage( String sMessage )
	{
		/*if( jframe == null )
		{
			jframe= new javax.swing.JFrame();
			jframe.setLocation(100, 100);
			jframe.setSize(300, 200);
			jframe.setVisible(true);
		}
		
		javax.swing.JOptionPane.showMessageDialog(
				jframe, sMessage, "Debugging information",
		javax.swing.JOptionPane.INFORMATION_MESSAGE);
		jframe.dispose();*/
		if( FILE_OUTPUT ) {
			if( out == null )
				openfile();
			
			try
			{
				out.write(sMessage + "\r\n");
				out.flush();
			}
			catch (IOException e1)
			{
				Debug.showException(e1, "queryDispatch");
			}
		}
		System.err.println("Debug: " + sMessage);
	}
	
	private static void openfile()
	{
		//File file= new File(MidiCSDextImpl.PackagePath + System.getProperty("file.separator") + "midicsdlog.txt");
		File file= new File("/home/pi/midicsdlog.txt");
		try
		{
			out= new BufferedWriter(new FileWriter(file, true));
		}
		catch (IOException e)
		{
			//Debug.showMessage("const");
		}
	}
	
	@SuppressWarnings("unused")
	private static void showMessage( String sMessage, String title )
	{
		/*if( jframe == null )
		{
			jframe= new javax.swing.JFrame();
			jframe.setLocation(100, 100);
			jframe.setSize(300, 200);
			jframe.setVisible(true);
		}
    javax.swing.JOptionPane.showMessageDialog(
        jframe, sMessage, title,
        javax.swing.JOptionPane.INFORMATION_MESSAGE);
    jframe.dispose();*/
		
		showMessage(sMessage);
	}
	
	public static void showMessageXframe( String sMessage, String title )
	{
		/*javax.swing.JFrame frame;
		
		frame= new javax.swing.JFrame();
		frame.setLocation(100, 100);
		frame.setSize(300, 200);
		frame.setVisible(true);

		javax.swing.JOptionPane.showMessageDialog(
        frame, sMessage, title,
        javax.swing.JOptionPane.INFORMATION_MESSAGE);
    frame.dispose();*/
		System.err.println("xFrame: " + sMessage);
	}
	
	public static void showException(Exception e, String text)
	{
		//showMessage(text + "\n" + e.getClass().getName() + "\n" + e.getMessage(), "Exception");
		String m= "Exception: " + e.getClass().getName() + " " + text + 
				"\nMessage: " + e.getMessage();
		//showMessage("Message: " + e.getMessage() );
		
		java.lang.StackTraceElement[] trace= e.getStackTrace();
		int i;
		for(i= 0; i < trace.length; i++)
			m+= trace[i] + "\n";
		
		showMessageXframe(m, "Exception");
	}

	public static void showMessage(byte[] message)
	{
		String h= "";
		for(byte b : message)
		{
			h+= "[" + b + "]";
		}
		showMessage(h);
	}
	
	public static void showMessage(XPropertySet xps)
	{
		String h= "";
		int i= 1;
		for(Property p : xps.getPropertySetInfo().getProperties())
		{
			h+= p.Name + " " + p.Type + "; ";
			i++;
			if( i == 5)
			{
			h+="\n";
			i= 1;
			}
		}
		showMessage(h);
	}

	public static void showMessage(String[] list)
	{
		String h= "";
		for(String hh : list )
			h+= hh + "\n";
		showMessage(h);
	}
	
	public static void debugEventQueue(ArrayList<TimedSymbolicEvent> queue)
	{
		String h= "";
		for(TimedSymbolicEvent ev : queue)
		{
			h+= ev + "\n";
		}
		showMessage(h);
	}

	public static void showMessage(String[][] ret)
	{
		String h= "";
		for(int i= 0; i < ret.length; i++)
		{
			for(int j= 0; j < ret[i].length; j++)
				h+= "[" + ret[i][j] + "]";
			h+= "\n";
		}
		
		showMessage(h);
	}
}
