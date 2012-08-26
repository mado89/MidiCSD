/**
 * 
 */
package at.univie.MidiCSD.impl;

/**
 * @author Martin Dobiasch
 *
 */
public class AboutDlg
{
	public AboutDlg()
	{
		String msg;
		msg= "                   MidiCSD\n";
		msg+= "    producing music and sound effects\n";
		msg+= "         from within calc/excel\n";
		msg+= "\n";
		msg+= "(c) 2009 Erich Neuwirth, University of Vienna\n";
		msg+= "    2010 Calc Portation, Martin Dobiasch";
		
		// Debug.showMessageXframe(msg, "About");
		javax.swing.JFrame frame;
		
		frame= new javax.swing.JFrame();
		frame.setLocation(100, 100);
		frame.setSize(300, 200);
		frame.setVisible(true);

		javax.swing.JOptionPane.showMessageDialog(
        frame, msg, "About",
        javax.swing.JOptionPane.INFORMATION_MESSAGE);
    frame.dispose();
	}
}
