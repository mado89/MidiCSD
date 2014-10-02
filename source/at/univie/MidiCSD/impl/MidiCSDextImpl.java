package at.univie.MidiCSD.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

import at.univie.MidiCSD.Debug;
import at.univie.MidiCSD.Helper;
import at.univie.MidiCSD.LogoTransfer;
import at.univie.MidiCSD.MidiQueue;
import at.univie.MidiCSD.TimedMidiStream;
import at.univie.MidiCSD.midiplayer.DirectMidiPlayer;
import at.univie.MidiCSD.midiplayer.IPlayer;
import at.univie.MidiCSD.midiplayer.MidiPlayer;
import at.univie.MidiCSD.midiplayer.ToFilePlayer;
import at.univie.MidiCSD.midiplayer.TimidityPlayer;

import com.sun.star.awt.XWindow;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.lib.util.UrlToFileMapper;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.sheet.XCellRangeAddressable;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetView;
import com.sun.star.table.CellRangeAddress;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.uri.RelativeUriExcessParentSegments;
import com.sun.star.uri.UriReferenceFactory;
import com.sun.star.uri.XUriReference;
import com.sun.star.uri.XUriReferenceFactory;
import com.sun.star.util.URL;
import com.sun.star.util.XURLTransformer;
import com.sun.star.view.XSelectionSupplier;

public final class MidiCSDextImpl extends WeakBase implements
		com.sun.star.lang.XServiceInfo, at.univie.MidiCSD.XMidiCSDext,
		com.sun.star.frame.XDispatchProvider, com.sun.star.lang.XInitialization,
		com.sun.star.frame.XDispatch
{
	/*
	 * private class CallerObj { public String name; public
	 * java.lang.reflect.Method func;
	 * 
	 * public CallerObj(String name, java.lang.reflect.Method func) { this.name=
	 * name; this.func= func; } };
	 */

	private final XComponentContext m_xContext;
	private XMultiComponentFactory m_xMCF;
	private XFrame m_xFrame;
	private XController m_xController;
	@SuppressWarnings("unused")
	private XWindow m_xContainerWindow;
	@SuppressWarnings("unused")
	private XWindow m_xComponentWindow;
	private XModel m_xModel;
	@SuppressWarnings("unused")
	private XComponent m_xCurrentComponent;
	private static final String m_implementationName= MidiCSDextImpl.class
			.getName();
	private static final String[] m_serviceNames= {
			"at.univie.midicsd.midicsdext", "com.sun.star.frame.ProtocolHandler" };
	
	// this has to be static
	private static MidiQueue m_streamqueue;
	// private static MidiPlayer m_player;
	public static String PackagePath= "";
	private static IPlayer m_internplayer;
	private static boolean m_internplayer_init= false;
	private static String m_globalFileName= "";
	
	//private static File file;
	//private static BufferedWriter out;
	//private static boolean fileloaded= false;
	
	// private CallerObj[] meths= {new CallerObj("test", PlayCurrentQueue)};
	
	public MidiCSDextImpl(XComponentContext context)
	{
		m_xContext= context;
		m_xMCF= m_xContext.getServiceManager();

		Debug.showMessage("MidiCSDextImpl::MidiCSDextImpl");
		
		/*
		m_streamqueue= new MidiQueue();
		// m_xDesktop= null;
		
		initInternPlayer();
		*/
	}
	
	private void initInternPlayer()
	{
		if( m_internplayer_init == false )
		{
			m_internplayer= new TimidityPlayer();
		}
	}

	public static XSingleComponentFactory __getComponentFactory(
			String sImplementationName)
	{
		XSingleComponentFactory xFactory= null;
		
		if( sImplementationName.equals(m_implementationName) )
			xFactory= Factory.createComponentFactory(MidiCSDextImpl.class,
					m_serviceNames);
		return xFactory;
	}
	
	public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey)
	{
		return Factory.writeRegistryServiceInfo(m_implementationName,
				m_serviceNames, xRegistryKey);
	}
	
	// com.sun.star.lang.XServiceInfo:
	public String getImplementationName()
	{
		return m_implementationName;
	}
	
	public boolean supportsService(String sService)
	{
		int len= m_serviceNames.length;
		
		for( int i= 0; i < len; i++ )
		{
			if( sService.equals(m_serviceNames[i]) )
				return true;
		}
		return false;
	}
	
	public String[] getSupportedServiceNames()
	{
		return m_serviceNames;
	}
	
	public void initialize(Object[] aArguments) throws com.sun.star.uno.Exception
	{
		Debug.showMessage("MidiCSDextImpl::initialize");
		if( aArguments.length > 0 )
		{
			m_xFrame= (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(
					com.sun.star.frame.XFrame.class, aArguments[0]);
			if( m_xFrame != null )
			{
				m_xController= m_xFrame.getController();
				m_xContainerWindow= m_xFrame.getContainerWindow();
				m_xComponentWindow= m_xFrame.getComponentWindow();
				if( m_xController != null )
				{
					m_xModel= m_xController.getModel();
					if( m_xModel != null )
					{
						m_xCurrentComponent= (XComponent) UnoRuntime.queryInterface(
								XComponent.class, m_xModel);
					}
				}
			}
		}
		
		m_streamqueue= new MidiQueue();
		// m_player= new MidiPlayer();
		
		if( PackagePath.equals("") )
			updatePackagePath();
		
		initInternPlayer();
	}
	
	/**
	 * 
  */
	public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL,
			String sTargetFrameName, int iSearchFlags)
	{
		System.out.println("MidiCSDext::queryDispatch");
		if( aURL.Protocol.compareTo("at.univie.midicsd:") == 0 )
		{
			Debug.showMessage( aURL.Path );
			if( aURL.Path.compareTo("addselectiontocurrentqueue") == 0
					|| aURL.Path.compareTo("clearallqueues") == 0
					|| aURL.Path.compareTo("play") == 0
					|| aURL.Path.compareTo("writemfile") == 0
					|| aURL.Path.compareTo("playmfile") == 0
					|| aURL.Path.compareTo("run") == 0
					|| aURL.Path.compareTo("resetstreamtime") == 0
					|| aURL.Path.compareTo("filename") == 0
					|| aURL.Path.compareTo("tologo") == 0
					|| aURL.Path.compareTo("stopMfile") == 0
					|| aURL.Path.compareTo("about") == 0 )
				return this;
		}
		return null;
	}
	
	/**
     * 
     */
	public com.sun.star.frame.XDispatch[] queryDispatches(
			com.sun.star.frame.DispatchDescriptor[] seqDescriptors)
	{
		int nCount= seqDescriptors.length;
		com.sun.star.frame.XDispatch[] seqDispatcher= new com.sun.star.frame.XDispatch[seqDescriptors.length];
		
		for( int i= 0; i < nCount; ++i )
		{
			seqDispatcher[i]= queryDispatch(seqDescriptors[i].FeatureURL,
					seqDescriptors[i].FrameName, seqDescriptors[i].SearchFlags);
		}
		return seqDispatcher;
	}
	
	/**
     * 
     */
	public void dispatch(com.sun.star.util.URL aURL,
			com.sun.star.beans.PropertyValue[] aArguments)
	{
		Debug.showMessage("dispatch " + aURL.Protocol + aURL.Path);
		
		System.out.println("MidiCSDext::dispatch " + aURL.Path );
		Debug.showMessage(aURL.Path);
		try
		{
			if( aURL.Protocol.compareTo("at.univie.midicsd:") == 0 )
			{
				if( aURL.Path.compareTo("addselectiontocurrentqueue") == 0 )
				{
					// Debug.showMessage("AddPhrase!");
					try
					{
//						Debug.showMessage(" -- add phrase --- ");
						AddSelectionToCurrentQueue();
//						Debug.showMessage("Selection added with no errors");
					}
					catch (Exception e)
					{
						//e.printStackTrace();
						Debug.showException(e, "MidiCSDext::dispatch");
					}
				}
				else if( aURL.Path.compareTo("clearallqueues") == 0 )
				{
					ClearAllQueues();
					// Debug.showMessage("Queues cleard");
				}
				else if( aURL.Path.compareTo("play") == 0 )
				{
//					Debug.showMessage(" --- play --- ");
					PlayCurrentQueue();
//					Debug.showMessage("PlayCurrentQueue done with no errors");
				}
				else if( aURL.Path.compareTo("writemfile") == 0 )
				{
//					Debug.showMessage("Write begin");
					WriteMfile();
//					Debug.showMessage("WriteMfile done with no errors");
				}
				else if( aURL.Path.compareTo("playmfile") == 0 )
				{
					PlayMfile("");
					//Debug.showMessage("Playfile done with no errors");
				}
				else if( aURL.Path.compareTo("stopMfile") == 0 )
				{
					StopMfile();
					//Debug.showMessage("File stopped");
				}
				else if( aURL.Path.compareTo("tologo") == 0 )
				{
					SelectionAsLogo();
					//Debug.showMessage("tologo done");
				}
				else if( aURL.Path.compareTo("resetstreamtime") == 0 )
				{
					ResetStreamTime();
				}
				else if( aURL.Path.compareTo("filename") == 0 )
				{
					SetFileName();
				}
				else if( aURL.Path.compareTo("run") == 0 )
				{
					try
					{
						RunSelection();
					}
					catch (IndexOutOfBoundsException e)
					{
						Debug.showException(e, "MidiCSD::dispatch::run");
						e.printStackTrace();
					}
				}
				else if( aURL.Path.compareTo("about") == 0 )
				{
					new AboutDlg();
				}
			}
		}
		catch(Throwable t)
		{
			Debug.showMessageXframe("Exception: " + t.getMessage(), "Exception");
		}
	}
	
	/**
	 * @throws Exception
	 * 
	 */
	public void AddSelectionToCurrentQueue() throws Exception
	{
		String[][] range= getSelectionAsStringAr();
		
		if( range == null )
		{
			Debug
					.showException(new Exception(),
							"MidiCSDext::AddSelectionToCurrentQueue, getSelectionAsStringAr returned null");
			return;
		}
		
		if( range.length == 1 && range[0].length == 1 )
			range= ExtendSelection();
		
		// Debug.showMessage("AddSelectionToCurrentQueue: " + range );
		
		if( range != null ) // there is something to add
		{
			// Debug.showMessage(range);
			
			if( range[0].length < 5 )
				Debug.showMessageXframe("Wrong Format for AddPhrase", "Error");
			else
			{
				/*
				 * Change 2014-10-02 assert removed since done in AddToCurrentQueue
				 */
				// m_streamqueue.AssertQueue(MidiQueue.DefaultStreamName, true, false);
				// Debug.showMessage("s2");
				m_streamqueue.AddToCurrentQueue(range, true, true);
			}
		}
		// Debug.showMessage("s3");
	}
	
	public void PlayCurrentQueue()
	{
		TimedMidiStream tmp;
		
		// Debug.showMessage("PlayCurrentQueue: " + m_streamqueue.getCurrentStream());
		if( m_streamqueue.getCurrentStream() != null )
		{
			Debug.showMessage("current stream");
			tmp= m_streamqueue.getCurrentStream().clone();
			tmp.ShortenDurations();
			
			// tmp.setMidiPlayer(m_player);
			m_internplayer.setWorkbookPath(this.getWorkbookPath());
			tmp.setPlayer(m_internplayer);
			
			tmp.Play();
		}
		else
		{
			// there is no current Stream, try to play first one
			if( m_streamqueue.getFirstStream() != null )
			{
				tmp= m_streamqueue.getFirstStream().clone();
				tmp.ShortenDurations();
				
				// tmp.setMidiPlayer(m_player);
				m_internplayer.setWorkbookPath(this.getWorkbookPath());
				tmp.setPlayer(m_internplayer);
				
				tmp.Play();
			}
		}
	}
	
	private void ClearAllQueues()
	{
		m_streamqueue= null;
		m_streamqueue= new MidiQueue();
	}
	
	private void SetFileName()
	{
		String fn= javax.swing.JOptionPane.showInputDialog("Filename: ");
		m_streamqueue.getCurrentStream().myFileName= fn;
	}
	
	public void SelectionAsLogo()
	{
		LogoTransfer lt= new LogoTransfer();
		String[][] Selection;
		
		Selection= getSelectionAsStringAr();
		
		lt.AsLogoToClipboard(Selection);
	}
	
	/*@Deprecated
	private void T2Mf_Load()
	{
		if( T2Mf.isLoaded() )
			return;
		
		try
		{
			XPackageInformationProvider xPackageInformationProvider= PackageInformationProvider
					.get(m_xContext);
			String location= xPackageInformationProvider
					.getPackageLocation("at.univie.midicsd.midicsdext");
			Object oTransformer= m_xContext.getServiceManager()
					.createInstanceWithContext("com.sun.star.util.URLTransformer",
							m_xContext);
			XURLTransformer xTransformer= (XURLTransformer) UnoRuntime
					.queryInterface(XURLTransformer.class, oTransformer);
			com.sun.star.util.URL[] oURL= new com.sun.star.util.URL[1];
			oURL[0]= new com.sun.star.util.URL();
			oURL[0].Complete= location + "/lib/";
			xTransformer.parseStrict(oURL);
			T2Mf.load(oURL[0].Path + oURL[0].Name);
			// Debug.showMessage("loaded");
		}
		catch (com.sun.star.uno.Exception ex)
		{
			Debug.showException(ex, "MidiCSD::T2MF_Load");
			System.out.println(ex);
			System.out.println(ex.getStackTrace());
		}
	}*/
	
	private void WriteMfile()
	{
		// T2Mf_Load();
		updatePackagePath();
		
		// CurrentStream.WriteMidifile KeepTextFile:=False
		Debug.showMessage("WriteMfile: " + m_globalFileName);
		if( m_globalFileName != "" )
		{
			m_streamqueue.getCurrentStream().WriteMidiFile(getWorkbookPath(),
					m_globalFileName, false);
		}
		else
		{
			m_streamqueue.getCurrentStream().WriteMidiFile(getWorkbookPath(),
					m_streamqueue.getCurrentStream().myFileName, false);
		}
	}
	
	/**
	 * Returns the URL-Path of the current document
	 * 
	 * @return
	 */
	private String getWorkbookPath()
	{
		// XComponent document = m_xDesktop.getCurrentComponent();
		// XModel xmodel = (XModel) UnoRuntime.queryInterface(XModel.class,
		// document);
		
		// UnoRuntime.
		
		if( m_xModel != null )
		{
			com.sun.star.util.URL[] url= new com.sun.star.util.URL[1];
			url[0]= new com.sun.star.util.URL();
			
			XURLTransformer ut;
			try
			{
				ut= (XURLTransformer) UnoRuntime.queryInterface(
						com.sun.star.util.XURLTransformer.class, m_xMCF
								.createInstanceWithContext("com.sun.star.util.URLTransformer",
										m_xContext));
				url[0].Complete= m_xModel.getURL();
				if( ut.parseSmart(url, "") )
					return url[0].Path;
				else
					return "";
			}
			catch (Exception e)
			{
				// e.printStackTrace();
				Debug.showException(e, "getWorkbookPath");
				return "";
			}
		}
		
		return "";
	}
	
	private void PlayMfile(String filename)
	{
		String fname;
		
		if( filename.equals("") )
		{
			fname= getWorkbookPath() + m_streamqueue.getCurrentStream().myFileName;
		}
		else
		{
			fname= getWorkbookPath() + filename;
		}
		
		// Debug.showMessage(fname);
		
		PlayMidiFile(fname);
	}
	
	private void PlayMidiFile(String filename)
	{
		Debug.showMessage("PlayMidiFile: " + filename);
		m_internplayer.PlayFile(filename);
	}
	
	private void ResetStreamTime()
	{
		// TODO unused property in MidiCSD
		// m_streamqueue.getCurrentStream().;
	}
	
	private void StopMfile()
	{
		// TODO implement stop for IPlayer
		// m_player.stop();
	}
	
	private void RunSelection() throws IndexOutOfBoundsException
	{
		XSpreadsheetView xsv= (XSpreadsheetView) UnoRuntime.queryInterface(
				XSpreadsheetView.class, m_xController);
		XSpreadsheet xs= xsv.getActiveSheet();
		XCellRange xcr= getSelection();
		
		updatePackagePath();
		
		RunProgram(xcr, xs);
	}
	
	private void RunProgram(XCellRange range, XSpreadsheet xs)
			throws IndexOutOfBoundsException
	{
		String[][] selection= getCellRangeAsString(range, xs);
		
		XCellRangeAddressable xAdd= (XCellRangeAddressable) UnoRuntime
				.queryInterface(XCellRangeAddressable.class, range);
		CellRangeAddress address= xAdd.getRangeAddress();
		
		if( selection == null )
		{
			Debug
					.showMessage("MidiCSDext::RunSelection, getSelectionAsStringAr returned null");
			return;
		}
		
		if( selection.length == 1 && selection[0].length == 1 )
			selection= ExtendSelection();
		
		RunProgram(selection, address.StartRow, address.StartColumn, xs);
	}
	
	private void RunProgram(String[][] range, int row, int col, XSpreadsheet xs)
			throws IndexOutOfBoundsException
	{
		int NCols;
		int NRows;
		int i, j;
		String myStreamName;
		String commandName;
		String[] argList= null;
		
		NCols= range[0].length;
		NRows= range.length;
		
		for( i= 0; i < NRows; i++ )
		{
			String[] currentLine= range[i];
			myStreamName= currentLine[0];
			commandName= currentLine[1];
			
			// System.out.println(i + " " + commandName );
			
			if( NCols > 2 )
			{
				argList= new String[NCols - 2];
				
				for( j= 2; j < NCols; j++ )
					argList[j - 2]= range[i][j];
				
				DoCommand(myStreamName, commandName, argList, row + i, col, xs);
			}
			else
				DoCommand(myStreamName, commandName, null, row + i, col, xs);
		}
	}
	
	private void DoCommand(String myStreamName, String commandName,
			String[] argList, int row, int col, XSpreadsheet xs)
			throws IndexOutOfBoundsException
	{
		String cmd= commandName.toLowerCase();
		TimedMidiStream NewStream;
		
		cmd= cmd.trim();
		myStreamName= myStreamName.trim();
		
		if( cmd.equals("play") )
		{
			TimedMidiStream tmp;
			
			tmp= m_streamqueue.get(myStreamName);
			
			// tmp.setMidiPlayer(m_player);
			m_internplayer.setWorkbookPath(this.getWorkbookPath());
			tmp.setPlayer(m_internplayer);
			
			tmp.Play();
		}
		else if( cmd.equals("timeshift") )
		{
			// Debug.showMessage("timeshift");
			NewStream= m_streamqueue.get(myStreamName);
			NewStream.TimeShift(Integer.parseInt(argList[0]));
		}
		else if( cmd.equals("merge") )
		{
			NewStream= m_streamqueue.get(myStreamName);
			TimedMidiStream OldStream= m_streamqueue.get(argList[0]);
			if( NewStream == null || OldStream == null )
				Debug.showMessageXframe("Merge failed couldn't found streams", "Error");
			else
				NewStream.Merge(OldStream);
		}
		else if( cmd.equals("rechannel") )
		{
			NewStream= m_streamqueue.get(myStreamName);
			NewStream.Rechannel(Integer.parseInt(argList[0]), Integer
					.parseInt(argList[1]));
		}
		else if( cmd.equals("changespeed") )
		{
			NewStream= m_streamqueue.get(myStreamName);
			NewStream.ChangeSpeed(1 / Double.parseDouble(argList[0]));
		}
		else if( cmd.equals("changevol") )
		{
			NewStream= m_streamqueue.get(myStreamName);
			NewStream.ChangeVol(Double.parseDouble(argList[0]));
		}
		else if( cmd.equals("transpose") )
		{
			NewStream= m_streamqueue.get(myStreamName);
			NewStream.Transpose(Integer.parseInt(argList[0]));
		}
		else if( cmd.equals("midifile") )
		{
			//T2Mf_Load();
			updatePackagePath();
			
			TimedMidiStream hstream;
			
			hstream= m_streamqueue.get(myStreamName);
			NewStream= hstream.clone();
			NewStream.ShortenDurations();
			NewStream.myFileName= argList[0];
			if( argList != null && argList.length > 0 )
			{
				NewStream.WriteMidiFile(getWorkbookPath(), argList[0], false);
				hstream.myFileName= NewStream.myFileName;
			}
		}
		else if( cmd.equals("playfile") )
		{
			if( argList.length > 0 && argList[0] != null)
			{
				// System.out.println("Fileplay " + argList.length + " " + argList[0]);
				PlayMidiFile(getWorkbookPath() + argList[0] + ".mid");
			}
			else
			{
				TimedMidiStream s= m_streamqueue.get(myStreamName);
				if( s != null )
				{
					// System.out.println("Play Stream " + myStreamName + " fn: " + s.myFileName);
					PlayMfile(s.myFileName);
				}
				else
				{
					//TODO: report error
					System.out.println("Stream " + myStreamName + " not in Queue");
				}
			}
		}
		else if( cmd.equals("filename") )
		{
			NewStream= m_streamqueue.get(myStreamName);
			NewStream.myFileName= argList[0];
		}
		else if( cmd.equals("new") )
		{
			NewStream= new TimedMidiStream();
			NewStream.streamname= myStreamName;
			m_streamqueue.addStream(NewStream);
		}
		else if( cmd.equals("copy") )
		{
			// NewStream= new TimedMidiStream();
			String name= argList[0];
			NewStream= m_streamqueue.get(name);
			if( NewStream != null )
			{
				NewStream= NewStream.clone();
				if( m_streamqueue.get(myStreamName) != null )
					m_streamqueue.remove(myStreamName);
				NewStream.streamname= myStreamName;
				m_streamqueue.addStream(NewStream);
			}
			else
			{
				Debug.showMessageXframe("Failed to copy stream, input not found", "Error");
			}
		}
		else if( cmd.equals("define") )
		{
			XCellRange xcr= null;
			
			String[][] tmprange= null;
			
		// TODO: wenn Adresse nicht gueltig stuerzts ab
			xcr= xs.getCellRangeByName(argList[0]);
			tmprange= getCellRangeAsString(xcr, xs);
			
			if( tmprange != null )
			{
				m_streamqueue.AddToCurrentQueue(tmprange, false, false);
				
				if( myStreamName.equals("") )
				{
					XCell c= xs.getCellByPosition(col, row);
					c.setFormula(Helper.PhraseNameFromRange(tmprange));
				}
			}
		}
		else if( cmd.equals("defrel") )
		{
			XCellRange xcr= null;
			String[][] range= null;
			String name;
			
			// TODO: wenn Adresse nicht gueltig stuerzts ab
			xcr= xs.getCellRangeByName(argList[0]); 
			range= getCellRangeAsString(xcr, xs);
			
			name= m_streamqueue.NameOfAddedEventsFromRange(range, false, false,
					false, false);
			if( myStreamName.equals("") )
			{
				XCell c= xs.getCellByPosition(row, col);
				c.setFormula(name);
			}
			// resString =
			// NameOfAddedEventsFromRange(Range(ArgArray(LBound(ArgArray))),
			// False, False)
			// If Not LeftCell Is Nothing Then LeftCell.Value = resString
		}
		else if( cmd.equals("defabs") )
		{
			XCellRange xcr= null;
			String[][] range= null;
			String name;
			
			xcr= xs.getCellRangeByName(argList[0]); // TODO: wenn Adresse nicht
			// gueltig stuerzts ab
			range= getCellRangeAsString(xcr, xs);
			
			name= m_streamqueue.NameOfAddedEventsFromRange(range, false, true, false,
					false);
			if( myStreamName.equals("") )
			{
				XCell c= xs.getCellByPosition(row, col);
				c.setFormula(name);
			}
			// resString =
			// NameOfAddedEventsFromRange(Range(ArgArray(LBound(ArgArray))),
			// False, True)
			// If Not LeftCell Is Nothing Then LeftCell.Value = resString
		}
		else if( cmd.equals("clearall") )
		{
			ClearAllQueues();
		}
		else if( cmd.equals("delete") || cmd.equals("clear") )
		{
			if( m_streamqueue.get(myStreamName) != null )
				m_streamqueue.remove(myStreamName);
		}
		else if( cmd.equals("invert") )
		{
			NewStream= m_streamqueue.get(myStreamName);
			NewStream.Invert(Integer.parseInt(argList[0]));
		}
		else if( cmd.equals("reverse") )
		{
			NewStream= m_streamqueue.get(myStreamName);
			NewStream.Reverse();
		}
		else if( cmd.equals("run") )
		{
			XCellRange xcr= null;
			
			xcr= xs.getCellRangeByName(argList[0]); // TODO: wenn Adresse nicht
			// gueltig stuerzts ab
			
			RunProgram(xcr, xs);
		}
		else if( cmd.equals("repeat") )
		{
			NewStream= m_streamqueue.get(myStreamName);
			NewStream.Repeat(Integer.parseInt(argList[0]));
		}
	}
	
	/**
	 * @throws IndexOutOfBoundsException
	 * 
	 */
	private String[][] ExtendSelection() throws IndexOutOfBoundsException
	{
		int row, col, scol, srow;
		boolean emptystart= false;
		XCellRange cell;
		int maxcols= 0;
		ArrayList<ArrayList<String>> cells= new ArrayList<ArrayList<String>>();
		ArrayList<String> crow;
		String[][] ret;
		
		XSpreadsheetView xsv= (XSpreadsheetView) UnoRuntime.queryInterface(
				XSpreadsheetView.class, m_xController);
		XSpreadsheet xs= xsv.getActiveSheet();
		
		// Gets an interface to the selected cell.
		XInterface xi= (XInterface) m_xModel.getCurrentSelection();
		cell= (XCellRange) UnoRuntime.queryInterface(XCellRange.class, xi);
		
		// Gets the cell's address/location.
		XCellRangeAddressable xAdd= (XCellRangeAddressable) UnoRuntime
				.queryInterface(XCellRangeAddressable.class, cell);
		CellRangeAddress address= xAdd.getRangeAddress();

		row= address.StartRow;
		col= address.StartColumn;
		
		// go left
		// TODO: isEmpty doesn't work on OS X??
		while( col > 0 && !xs.getCellByPosition(col, row).getFormula().isEmpty() )
		// while( col > 0 && !xs.getCellByPosition(col, row).getFormula().equals("") )
			col--;
		// to be back in the range
		// TODO: isEmpty doesn't work on OS X??
		if( xs.getCellByPosition(col, row).getFormula().isEmpty() )
		// if( xs.getCellByPosition(col, row).getFormula().equals("") )
			col++;
		
		// maybe this was the last row and the most left cell is not filled
		// check if the left/up is filled
		if( row > 0 && col > 0
		// TODO: isEmpty doesn't work on OS X??
				// && !xs.getCellByPosition(col - 1, row - 1).getFormula().isEmpty() )
				&& !xs.getCellByPosition(col - 1, row - 1).getFormula().equals("") )
			col--;
		
		// go up
		// TODO: isEmpty doesn't work on OS X??
		while( row > 0 && !xs.getCellByPosition(col, row).getFormula().isEmpty() )
		// while( row > 0 && !xs.getCellByPosition(col, row).getFormula().equals("") )
			row--;
		// to be back in the range
		// TODO: isEmpty doesn't work on OS X??
		if( xs.getCellByPosition(col, row).getFormula().isEmpty() )
		// if( xs.getCellByPosition(col, row).getFormula().equals("") )
			row++;
		
		// maybe there is right up a cell which should be in the selection
		// aswell
		if( row > 0
		// TODO: isEmpty doesn't work on OS X??
				// && !xs.getCellByPosition(col + 1, row - 1).getFormula().isEmpty() )
				&& !xs.getCellByPosition(col + 1, row - 1).getFormula().equals("") )
		{
			row--;
			emptystart= true;
		}
		
		scol= col;
		srow= row;
		
		if( emptystart ) // skip the first cell
			col++;
		
		while( !xs.getCellByPosition(col, row).getFormula().isEmpty() )
		{
			crow= new ArrayList<String>();
			if( col == scol )
			{
				// TODO: isEmpty doesn't work on OS X??
				// if( xs.getCellByPosition(col, row).getFormula().isEmpty() )
				if( xs.getCellByPosition(col, row).getFormula().equals("") )
				{
					col++;
					crow.add("");
				}
			}
			else if( emptystart )
			{
				crow.add("");
				emptystart= false;
			}
			// TODO: isEmpty doesn't work on OS X??
			// while( !xs.getCellByPosition(col, row).getFormula().isEmpty() )
			while( !xs.getCellByPosition(col, row).getFormula().equals("") )
			{
				XCell c= xs.getCellByPosition(col, row);
				String f;
				f= c.getFormula();
				if( f.length() > 0 && f.charAt(0) == '=' )
				{
					XText cellText= (XText) UnoRuntime.queryInterface(XText.class, c);
					crow.add(cellText.getString());
				}
				else
					crow.add(f);
				col++;
			}
			row++;
			col= scol;
			if( crow.size() > maxcols )
				maxcols= crow.size();
			cells.add(crow);
		}
		
		// TODO: isEmpty doesn't work on OS X??
		// if( !xs.getCellByPosition(col + 1, row).getFormula().isEmpty() )
		if( !xs.getCellByPosition(col + 1, row).getFormula().equals("") )
		{
			crow= new ArrayList<String>();
			crow.add("");
			col++;
			// TODO: isEmpty doesn't work on OS X??
			// while( !xs.getCellByPosition(col, row).getFormula().isEmpty() )
			while( !xs.getCellByPosition(col, row).getFormula().equals("") )
			{
				XCell c= xs.getCellByPosition(col, row);
				String f;
				f= c.getFormula();
				if( f.length() > 0 && f.charAt(0) == '=' )
				{
					XText cellText= (XText) UnoRuntime.queryInterface(XText.class, c);
					crow.add(cellText.getString());
				}
				else
					crow.add(f);
				col++;
			}
			row++;
			col= scol;
			if( crow.size() > maxcols )
				maxcols= crow.size();
			cells.add(crow);
		}
		
		if( cells.size() > 0 )
		{
			// XSpreadsheetDocument doc= (XSpreadsheetDocument)
			// UnoRuntime.queryInterface(
			// XSpreadsheetDocument.class, m_xModel);
			XSelectionSupplier selsup= (XSelectionSupplier) UnoRuntime
					.queryInterface(XSelectionSupplier.class, m_xController);
			XCellRange range= xs.getCellRangeByPosition(scol, srow, scol + maxcols
					- 1, row - 1);
			try
			{
				selsup.select(range);
			}
			catch (IllegalArgumentException e)
			{
				Debug.showException(e, "ExtendSelection");
			}
			
			ret= new String[cells.size()][maxcols];
			for( int i= 0; i < cells.size(); i++ )
			{
				for( int j= 0; j < cells.get(i).size(); j++ )
				{
					ret[i][j]= cells.get(i).get(j);
				}
			}
			
			// Debug.showMessage(ret);
		}
		else
			ret= null;
		
		return ret;
	}
	
	/**
	 * Returns the current selected cells
	 * 
	 * @return XCellRange, selected cells in the current sheet
	 */
	private XCellRange getSelection()
	{
		// XComponent document = m_xDesktop.getCurrentComponent();
		// XModel xmodel = (XModel) UnoRuntime.queryInterface(XModel.class,
		// document);
		
		// Gets an interface to the selected cells.
		XInterface xi= (XInterface) m_xModel.getCurrentSelection();
		XCellRange xcr= (XCellRange) UnoRuntime
				.queryInterface(XCellRange.class, xi);
		
		return xcr;
	}
	
	/**
	 * Returns the content of the cells
	 * 
	 * @param xcr
	 *          the cell-range of which the data should come from
	 * @param xs
	 *          the worksheet in which the data is located
	 * @return String[][] in the format [row i][col i]
	 */
	private String[][] getCellRangeAsString(XCellRange xcr, XSpreadsheet xs)
	{
		// Gets the selected range's address/location.
		XCellRangeAddressable xAdd= (XCellRangeAddressable) UnoRuntime
				.queryInterface(XCellRangeAddressable.class, xcr);
		CellRangeAddress address= xAdd.getRangeAddress();
		
		int nRows= address.EndRow - address.StartRow + 1;
		int nCols= address.EndColumn - address.StartColumn + 1;
		int i, ii, j, jj;
		
		// String h= "" + nRows + "/" + nCols + "\n";
		
		if( nRows > 0 && nCols > 0 )
		{
			String[][] ret;
			
			ret= new String[nRows][];
			for( i= 0, ii= address.StartRow; i < nRows; i++, ii++ )
			{
				ret[i]= new String[nCols];
				for( j= 0, jj= address.StartColumn; j < nCols; j++, jj++ )
				{
					try
					{
						XCell c;
						
						c= xs.getCellByPosition(jj, ii);
						
						String f;
						f= c.getFormula();
						if( f.length() > 0 && f.charAt(0) == '=' )
						{
							XText cellText= (XText) UnoRuntime.queryInterface(XText.class, c);
							ret[i][j]= cellText.getString();
						}
						else
							ret[i][j]= f;
					}
					catch (IndexOutOfBoundsException e)
					{
						Debug.showException(e, "getCellRangeAsString");
						e.printStackTrace();
					}
					// h+= ret[i][j] + ")/";
					// h+= ret[i][j] + "/";
				}
				// h+= "\n";
			}
			
			// showMessage( h );
			
			return ret;
		}
		
		Debug.showMessage("nothing selected");
		
		return null;
	}
	
	/**
	 * Returns selected cells in the current/active worksheet
	 * 
	 * @return String[][]
	 * @see getCellRangeAsString, getSelection
	 */
	private String[][] getSelectionAsStringAr()
	{
		XSpreadsheetView xsv= (XSpreadsheetView) UnoRuntime.queryInterface(
				XSpreadsheetView.class, m_xController);
		XSpreadsheet xs= xsv.getActiveSheet();
		
		XCellRange xcr= getSelection();
		
		return getCellRangeAsString(xcr, xs);
	}
	
	/**
	 * 
	 */
	private void updatePackagePath()
	{
		try
		{
			XPackageInformationProvider xPackageInformationProvider= PackageInformationProvider
					.get(m_xContext);
			
			String location= xPackageInformationProvider
			//		.getPackageLocation("at.univie.midicsd.midicsdext");
					.getPackageLocation("at.univie.midicsdext"); //has to be the id from description.xml
			/*
			Debug.showMessage( "location: " + location );
			
			Object oTransformer= m_xContext.getServiceManager()
					.createInstanceWithContext("com.sun.star.util.URLTransformer",
							m_xContext);
			XURLTransformer xTransformer= (XURLTransformer) UnoRuntime
					.queryInterface(XURLTransformer.class, oTransformer);
			
			com.sun.star.util.URL[] oURL= new com.sun.star.util.URL[1];
			oURL[0]= new com.sun.star.util.URL();
			//oURL[0].Complete= location + "/midicsv/";
			oURL[0].Complete= location;
			xTransformer.parseStrict(oURL);
			
			PackagePath= oURL[0].Path + oURL[0].Name;
			//Debug.showMessage("Packpath: " + PackagePath);
			
			// some workaround to have a nice path
			// operating systems tend to have different likes about pathnames
			// URLs are a certain kind of paths but we need to transfer them
			// to OS-dependent paths for accessing parser and stuff like that
			//workaround begin
			String osName= System.getProperty("os.name");
			if( osName.startsWith("Windows") || osName.equals("Linux") )
				PackagePath= PackagePath.replaceAll("%20", "\\ ");
			else if( osName.equals("Mac OS X") )
				PackagePath= PackagePath.replaceAll("%20", " ");
			//workaround end
			*/
			XUriReferenceFactory f = UriReferenceFactory.create(m_xContext);
			XUriReference url = f.makeAbsolute(
					//f.parse(location), f.parse("./midicsv/Csvmidi.lin"), false,
					f.parse(location), f.parse(""), false,
				  RelativeUriExcessParentSegments.ERROR);
			if( url == null )
			{
				Debug.showMessage("error getting package path");
			}
			
			try
			{
				java.net.URL jnurl;
				jnurl= new java.net.URL(url.getUriReference());
				File f1= UrlToFileMapper.mapUrlToFile(jnurl);
				
				Debug.showMessage( PackagePath + "\r\n" + f1.getAbsolutePath() );
				PackagePath= f1.getAbsolutePath();
				
				//Debug.showMessage("Is Csvmidi.lin canRead: " + f1.canRead() );
			}
			catch (MalformedURLException e)
			{
				Debug.showException(e, "MidiCSDextImpl::updatePackagePath");
				e.printStackTrace();
			}
			/*catch (IOException e)
			{
				Debug.showException(e, "MidiCSDextImpl::updatePackagePath");
				e.printStackTrace();
			}*/
			
			//Debug.showMessage( url.getUriReference() );

		}
		catch (Throwable t)
		{
			//Debug.showException(ex, "MidiCSD::updatePackagePath");
			System.out.println(t.getMessage());
			t.printStackTrace();
		}
	}
	
	public void addStatusListener(XStatusListener arg0, URL arg1)
	{
		// TODO Auto-generated method stub
		
	}

	public void removeStatusListener(XStatusListener arg0, URL arg1)
	{
		// TODO Auto-generated method stub
		
	}
	
	public void setFileName(String FileName)
	{
		m_globalFileName= FileName;
	}
}
