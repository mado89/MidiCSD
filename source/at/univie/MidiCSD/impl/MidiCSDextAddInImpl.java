package at.univie.MidiCSD.impl;

import at.univie.MidiCSD.Debug;

import com.sun.star.sheet.XCellRangeAddressable;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.table.CellRangeAddress;
import com.sun.star.table.XCellRange;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;


public final class MidiCSDextAddInImpl extends WeakBase implements 
	com.sun.star.lang.XServiceInfo,
	at.univie.MidiCSD.XMidiCSDextAddIn,
	com.sun.star.lang.XLocalizable
{
	@SuppressWarnings("unused") //because its really never read, but maybe 
															//usefull to have it
	private final XComponentContext m_xContext;
	private static final String m_implementationName = MidiCSDextAddInImpl.class.getName();
	private static final String[] m_serviceNames = { 
		"at.univie.MidiCSDextAddIn" };
	private Locale m_locale = new Locale();
	
	
	public MidiCSDextAddInImpl( XComponentContext context )
	{
		m_xContext = context;
	};
	
	public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
		XSingleComponentFactory xFactory = null;
		
		if ( sImplementationName.equals( m_implementationName ) )
			xFactory = Factory.createComponentFactory(MidiCSDextAddInImpl.class, m_serviceNames);
		return xFactory;
	}
	
	public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
		return Factory.writeRegistryServiceInfo(m_implementationName,
			m_serviceNames,
			xRegistryKey);
	}
	
	// com.sun.star.lang.XServiceInfo:
	public String getImplementationName() {
		return m_implementationName;
	}
	
	public boolean supportsService( String sService ) {
		int len = m_serviceNames.length;
		
		for( int i=0; i < len; i++) {
			if (sService.equals(m_serviceNames[i]))
				return true;
		}
		return false;
	}
	
	public String[] getSupportedServiceNames() {
		return m_serviceNames;
	}
	
	public Locale getLocale()
	{
		return m_locale;
	}
	
	public void setLocale(Locale l)
	{
		m_locale= l;
	}
	
	/*//working one
	public String CSDRangeAddress(XCellRange range)
	{
		XPropertySet ps;
		ps= (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, range);
    
    try
		{
			return (String) ps.getPropertyValue("AbsoluteName");
		}
		catch (UnknownPropertyException e)
		{
			Debug.showMessage("csdrangeaddress unkownproperty");
			e.printStackTrace();
			return e.getMessage();
		}
		catch (WrappedTargetException e)
		{
			Debug.showMessage("csdrangeaddress wrappedtarget");
			e.printStackTrace();
			return e.getMessage();
		}
	}*/
	
	public String CSDRangeAddress(XPropertySet xps, XCellRange range)
	{
		XPropertySet ps;
		ps= (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, range);
    
    try
		{
			return (String) ps.getPropertyValue("AbsoluteName");
		}
		catch (UnknownPropertyException e)
		{
			Debug.showException(e, "CSDRangeAddress" );
			e.printStackTrace();
			return e.getMessage();
		}
		catch (WrappedTargetException e)
		{
			Debug.showException(e, "CSDRangeAddress" );
			e.printStackTrace();
			return e.getMessage();
		}
		/*if(range.getClass() == String.class )
		{
			return (String) range;
		}
		else if( range.getClass().isArray() )
		{
			try
			{
				XCellRange xcr= XCellRange.class.cast(range);
				Debug.showMessage("geht?: " + (xcr != null ));
			}
			catch( ClassCastException e)
			{
				Debug.showMessage("ClassCastException");
			}
			return "passt";
		}
		Debug.showMessage(range.getClass().getName());
		return "";*/
	}
	
	public String CSDRightFrom(XPropertySet xps, XCellRange startcell)
	{
		XSpreadsheet xs= null;
		CellRangeAddress address;
		XCellRange range;
		String sheetname = null;
		int row, col;
		
		XPropertySet ps2;
		ps2= (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, startcell);
		
		XCellRangeAddressable xAdd = (XCellRangeAddressable)UnoRuntime.queryInterface(
			XCellRangeAddressable.class, startcell);
		address = xAdd.getRangeAddress();
		
		row= address.StartRow;
		col= address.StartColumn + 1;
		
		//Debug.showMessage("1");
		
		try
		{
			sheetname= (String) ps2.getPropertyValue("AbsoluteName");
			//Debug.showMessage("2");
			sheetname= sheetname.substring(0, sheetname.indexOf('.') );
			sheetname= sheetname.substring(1);
			
			Object o= UnoRuntime.queryInterface(XSpreadsheetDocument.class, xps);
			XSpreadsheetDocument x= (XSpreadsheetDocument) o;
			
			XSpreadsheets xss = x.getSheets();
			
			Object o2= xss.getByName( sheetname );
			xs= (XSpreadsheet) UnoRuntime.queryInterface(XSpreadsheet.class, o2);
			
			//TODO: isEmpty doesn't work on OS X??
			//while( !xs.getCellByPosition(col, row).getFormula().isEmpty() ) col++;
			while( !xs.getCellByPosition(col, row).getFormula().equals("") ) col++;
			
			//Debug.showMessage("geht 3 (" + address.StartRow + "/" + address.StartColumn + ") (" +
			//	row + "/" + col + ")" );
			
			//create the range
			range= xs.getCellRangeByPosition(address.StartColumn, address.StartRow, col-1, row);
			
			//Debug.showMessage("geht 4");
			
			XPropertySet ps;
			ps= (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, range);
			
			//Debug.showMessage("csdrightfrom success");
			
			return (String) ps.getPropertyValue("AbsoluteName");
			
			//Debug.showMessage("x?" + ( x != null ));
			//Debug.showMessage(UnoRuntime.queryInterface(XSpreadsheetDocument.class, xps).
			//.getClass().getName());
			/*XNameAccess xna;
			Debug.showMessage("3");
			Object o= xps.getPropertyValue("SheetLinks");
			xna = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, o);
			h= "";
			for(String hh : xna.getElementNames() )
			{
			h+= hh + "\n";
			}
			Debug.showMessage(h);
			Debug.showMessage("4" + sheetname);
			xs= (XSpreadsheet) xna.getByName(sheetname);*/
		}
		catch (UnknownPropertyException e)
		{
			Debug.showException(e, "CSDRightFrom" );
			e.printStackTrace();
		}
		catch (WrappedTargetException e)
		{
			Debug.showException(e, "CSDRightFrom" );
			e.printStackTrace();
		}
		catch (NoSuchElementException e)
		{
			Debug.showException(e, "CSDRightFrom" );
			e.printStackTrace();
		}
		catch (IndexOutOfBoundsException e)
		{
			Debug.showException(e, "CSDRightFrom" );
			e.printStackTrace();
			return "error " + e.getMessage();
		}
		
		//Debug.showMessage("csdright from no success");
		
		return null;
    
    /*XComponent document = xd.getCurrentComponent();
    
    Debug.showMessage("m_xCurrentComponent != null?: " + (m_xCurrentComponent != null) );
    
    //XModel xmodel = (XModel) UnoRuntime.queryInterface(XModel.class, document);
    XModel xmodel = (XModel) UnoRuntime.queryInterface(XModel.class, m_xCurrentComponent);
    
    Debug.showMessage("m_xmodel != null?: " + (xmodel != null) );
		
    xSpreadsheetDocument= (XSpreadsheetDocument) UnoRuntime.queryInterface(
    XSpreadsheetDocument.class, xmodel);
		
		XSpreadsheets xss = xSpreadsheetDocument.getSheets();
		
		try
		{
		Object o= xss.getByName( sheetname );
		xs= (XSpreadsheet) UnoRuntime.queryInterface(XSpreadsheet.class, o);
		}
		catch (NoSuchElementException e)
		{
		Debug.showMessage( "NoSuchElement " + e.getMessage() );
		e.printStackTrace();
		}
		catch (WrappedTargetException e)
		{
		Debug.showMessage( "WrappedTarget: " + e.getMessage() );
		e.printStackTrace();
		}
		
		XCellRangeAddressable xAdd = (XCellRangeAddressable)UnoRuntime.queryInterface(
		XCellRangeAddressable.class, startcell);
    address = xAdd.getRangeAddress();
		
    row= address.StartRow;
    col= address.StartColumn + 1;
    
    Debug.showMessage("works!");
    
    try
		{
		//going right until we find an empty cell
		while( !xs.getCellByPosition(col, row).getFormula().isEmpty() ) col++;
		
		Debug.showMessage("geht 3 (" + address.StartRow + "/" + address.StartColumn + ") (" +
		row + "/" + col + ")" );
		
		//create the range
		range= xs.getCellRangeByPosition(address.StartColumn, address.StartRow, col-1, row);
		
		Debug.showMessage("geht 4");
		
		XPropertySet ps;
		ps= (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, range);
		
		Debug.showMessage("geht 5");
		
		return (String) ps.getPropertyValue("AbsoluteName");
		}
		catch (IndexOutOfBoundsException e)
		{
		e.printStackTrace();
		return "error " + e.getMessage();
		}
		catch (UnknownPropertyException e)
		{
		e.printStackTrace();
		return "error " + e.getMessage();
		}
		catch (WrappedTargetException e)
		{
		e.printStackTrace();
		return "error " + e.getMessage();
		}*/
	}
	
	public String CSDDownFrom(XPropertySet xps, XCellRange startcell)
	{
		XSpreadsheet xs= null;
		CellRangeAddress address;
		XCellRange range;
		String sheetname = null;
		int row, col;
		
		XPropertySet ps2;
		ps2= (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, startcell);
		
		XCellRangeAddressable xAdd = (XCellRangeAddressable)UnoRuntime.queryInterface(
			XCellRangeAddressable.class, startcell);
		address = xAdd.getRangeAddress();
		
		row= address.StartRow + 1;
		col= address.StartColumn;
		
		try
		{
			sheetname= (String) ps2.getPropertyValue("AbsoluteName");
			sheetname= sheetname.substring(0, sheetname.indexOf('.') );
			sheetname= sheetname.substring(1);
			
			Object o= UnoRuntime.queryInterface(XSpreadsheetDocument.class, xps);
			XSpreadsheetDocument x= (XSpreadsheetDocument) o;
			
			XSpreadsheets xss = x.getSheets();
			
			Object o2= xss.getByName( sheetname );
			xs= (XSpreadsheet) UnoRuntime.queryInterface(XSpreadsheet.class, o2);
			
			//TODO: isEmpty doesn't work on OS X??
			//while( !xs.getCellByPosition(col, row).getFormula().isEmpty() ) row++;
			while( !xs.getCellByPosition(col, row).getFormula().equals("") ) row++;
			
			//Debug.showMessage("geht 3 (" + address.StartRow + "/" + address.StartColumn + ") (" +
			//	row + "/" + col + ")" );
			
			//create the range
			range= xs.getCellRangeByPosition(address.StartColumn, address.StartRow, col, row-1);
			
			//get its address
			XPropertySet ps;
			ps= (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, range);
			
			return (String) ps.getPropertyValue("AbsoluteName");
		}
		catch (UnknownPropertyException e)
		{
			Debug.showException(e, "CSDDownFrom" );
			e.printStackTrace();
		}
		catch (WrappedTargetException e)
		{
			Debug.showException(e, "CSDDownFrom" );
			e.printStackTrace();
		}
		catch (NoSuchElementException e)
		{
			Debug.showException(e, "CSDDownFrom" );
			e.printStackTrace();
		}
		catch (IndexOutOfBoundsException e)
		{
			Debug.showException(e, "CSDDownFrom" );
			e.printStackTrace();
			return null;
		}
		
		//Debug.showMessage("csd downfrom no success");
		return null;
	}
	
	public String CSDRightDownFrom(XPropertySet xps, XCellRange startcell)
	{
		XSpreadsheet xs= null;
		CellRangeAddress address;
		XCellRange range;
		String sheetname = null;
		int row, col;
		int maxcol;
		
		XPropertySet ps2;
		ps2= (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, startcell);
		
		XCellRangeAddressable xAdd = (XCellRangeAddressable)UnoRuntime.queryInterface(
			XCellRangeAddressable.class, startcell);
		address = xAdd.getRangeAddress();
		
		row= address.StartRow;
		col= address.StartColumn;
		maxcol= col;
		
		try
		{
			sheetname= (String) ps2.getPropertyValue("AbsoluteName");
			sheetname= sheetname.substring(0, sheetname.indexOf('.') );
			sheetname= sheetname.substring(1);
			
			Object o= UnoRuntime.queryInterface(XSpreadsheetDocument.class, xps);
			XSpreadsheetDocument x= (XSpreadsheetDocument) o;
			
			XSpreadsheets xss = x.getSheets();
			
			Object o2= xss.getByName( sheetname );
			xs= (XSpreadsheet) UnoRuntime.queryInterface(XSpreadsheet.class, o2);
			
			//TODO: isEmpty doesn't work on OS X??
			//while( !xs.getCellByPosition(col, row).getFormula().isEmpty() )
			while( !xs.getCellByPosition(col, row).getFormula().equals("") )
			{
				//TODO: isEmpty doesn't work on OS X??
				//while( !xs.getCellByPosition(col, row).getFormula().isEmpty() ) col++;
				while( !xs.getCellByPosition(col, row).getFormula().equals("") ) col++;
				if( col > maxcol )
					maxcol= col;
				col= address.StartColumn;
				row++;
			}
			
			//Debug.showMessage("geht 3 (" + address.StartRow + "/" + address.StartColumn + ") (" +
			//	row + "/" + col + ")" );
			
			//create the range
			range= xs.getCellRangeByPosition(address.StartColumn, address.StartRow, maxcol - 1, row-1);
			
			//get its address
			XPropertySet ps;
			ps= (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, range);
			
			return (String) ps.getPropertyValue("AbsoluteName");
		}
		catch (UnknownPropertyException e)
		{
			Debug.showException(e, "CSDRightDownFrom" );
			e.printStackTrace();
		}
		catch (WrappedTargetException e)
		{
			Debug.showException(e, "CSDRightDownFrom" );
			e.printStackTrace();
		}
		catch (NoSuchElementException e)
		{
			Debug.showException(e, "CSDRightDownFrom" );
			e.printStackTrace();
		}
		catch (IndexOutOfBoundsException e)
		{
			Debug.showException(e, "CSDRightDownFrom" );
			e.printStackTrace();
			return null;
		}
		
		//Debug.showMessage("csd downfrom no success");
		return null;
	}
}
