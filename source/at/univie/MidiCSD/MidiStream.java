/**
 * 
 */
package at.univie.MidiCSD;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
public class MidiStream
{
	
}

/*
 * ' MidiCSD
' © 2009 Erich Neuwirth
' a toolkit for playing music and producing sound effects from Excel
' ------------------------------------------------------------------

Option Explicit
Private Const MIDIPROP_SET = &H80000000
Private Const MIDIPROP_GET = &H40000000

'  These are intentionally both non-zero so the app cannot accidentally
'  leave the operation off and happen to appear to work due to default
'  action.

Private Const MIDIPROP_TIMEDIV = &H1&
Private Const MIDIPROP_TEMPO = &H2&

Const MEVT_F_SHORT = &H0&
Const MEVT_F_LONG = &H80000000
Const MEVT_F_CALLBACK = &H40000000
Const MIDISTRM_ERROR = -2


Private Type MIDIPROPTIMEDIV
    cbStruct As Long
    dwTimeDiv As Long
End Type

Private Type MIDIPROPTEMPO
    cbStruct As Long
    dwTempo As Long
End Type

Private Type MMTIME
    wType As Long
    u As Long
End Type

Private Type MidiEvent
    dwDeltaTime As Long          '  Ticks since last event
    dwStreamID As Long           '  Reserved; must be zero
    dwEvent As Long              '  Event type and parameters
    '    dwParms(1) As Long           '  Parameters if this is a long event
End Type

Private Type MIDIHDR
    lpData As String             '  pointer to locked data block
    '    lpData As Long             '  pointer to locked data block
    dwBufferLength As Long       '  length of data in data block
    dwBytesRecorded As Long      '  used for input only
    dwUser As Long               '  for client's use
    dwFlags As Long              '  assorted flags (see defines)
    lpNext As Long               '  reserved for driver
    reserved As Long             '  reserved for driver
End Type




'Private Declare Function VarPtr Lib "msvbvm60.dll" (ByVal Var As Any) As Long

'Private Declare Function StrPtr Lib "msvbvm60.dll" (Var As Any) As Long

Private Declare Function midiStreamOpen Lib "winmm.dll" (phms As Long, puDeviceID As Long, _
                                                         ByVal cMidi As Long, ByVal dwCallback As Long, ByVal dwInstance As Long, ByVal fdwOpen As Long) As Long
Private Declare Function midiStreamClose Lib "winmm.dll" (ByVal hms As Long) As Long


'Private Declare Function midiStreamProperty Lib "winmm.dll" (ByVal hms As Long, lppropdata As Byte, _
 ByVal dwProperty As Long) As Long
Private Declare Function midiStreamProperty Lib "winmm.dll" (ByVal hms As Long, lppropdata As MIDIPROPTEMPO, _
                                                             ByVal dwProperty As Long) As Long
Private Declare Function midiStreamPosition Lib "winmm.dll" (ByVal hms As Long, lpmmt As MMTIME, _
                                                             ByVal cbmmt As Long) As Long
Private Declare Function midiStreamOut Lib "winmm.dll" (ByVal hms As Long, pmh As MIDIHDR, _
                                                        ByVal cbmh As Long) As Long
Private Declare Function midiStreamPause Lib "winmm.dll" (ByVal hms As Long) As Long
Private Declare Function midiStreamRestart Lib "winmm.dll" (ByVal hms As Long) As Long
Private Declare Function midiStreamStop Lib "winmm.dll" (ByVal hms As Long) As Long


Private Declare Function midiOutPrepareHeader Lib "winmm.dll" _
                                              (ByVal hMidiOut As Long, lpMidiOutHdr As MIDIHDR, _
                                               ByVal uSize As Long) As Long
Private Declare Function midiOutUnprepareHeader Lib "winmm.dll" _
                                                (ByVal hMidiOut As Long, lpMidiOutHdr As MIDIHDR, _
                                                 ByVal uSize As Long) As Long

Private Declare Function midiConnect Lib "winmm.dll" (ByVal hmi As Long, ByVal hmo As Long, pReserved As Any) As Long
Private Declare Function midiDisconnect Lib "winmm.dll" (ByVal hmi As Long, ByVal hmo As Long, pReserved As Any) As Long

Private Declare Sub Sleep Lib "Kernel32" (ByVal dwMilliseconds As Long)

Private hMidiStream As Long

Public Sub OpenDevice(DeviceNum As Long)
    Dim localdevice As Long
    Dim lPDevicenum As Long
    localdevice = DeviceNum - 1
    '    lPDevicenum = VarPtr(localdevice)
    Dim rc As Long
    '    rc = midiStreamOpen(hMidiStream, DeviceNum - 1, 1&, 0&, 0&, 0&)
    rc = midiStreamOpen(hMidiStream, localdevice, 1&, 0&, 0&, 0&)
End Sub

Public Sub CloseDevice()
    Dim rc As Long
    rc = midiStreamClose(hMidiStream)
End Sub

Public Function GetTempo() As Long
    Dim mytempo As MIDIPROPTEMPO
    Dim rc As Long
    mytempo.cbStruct = Len(mytempo)
    rc = midiStreamProperty(hMidiStream, mytempo, MIDIPROP_GET + MIDIPROP_TEMPO)
    GetTempo = mytempo.dwTempo
End Function

Public Function GetTimeDiv() As Long
    Dim mytempo As MIDIPROPTEMPO
    Dim rc As Long
    mytempo.cbStruct = Len(mytempo)
    rc = midiStreamProperty(hMidiStream, mytempo, MIDIPROP_GET + MIDIPROP_TIMEDIV)
    GetTimeDiv = mytempo.dwTempo
End Function

Public Sub SetTempo(tempo As Long)
    Dim mytempo As MIDIPROPTEMPO
    Dim rc As Long
    mytempo.cbStruct = Len(mytempo)
    mytempo.dwTempo = tempo
    rc = midiStreamProperty(hMidiStream, mytempo, MIDIPROP_SET + MIDIPROP_TEMPO)
End Sub

Public Sub SetTimeDiv(TimeDivVal As Long)
    Dim mytempo As MIDIPROPTEMPO
    Dim rc As Long
    mytempo.cbStruct = Len(mytempo)
    mytempo.dwTempo = TimeDivVal
    rc = midiStreamProperty(hMidiStream, mytempo, MIDIPROP_SET + MIDIPROP_TIMEDIV)
End Sub


' So funktioniert das nicht

Sub Out(deltaTime As Long, MidiMessage As Long)
    Dim mEvt As MidiEvent
    Dim mHdr As MIDIHDR
    Dim Length As Long
    Dim hdrLength As Long
    Dim rc As Long
    mEvt.dwDeltaTime = deltaTime
    mEvt.dwStreamID = 0
    mEvt.dwEvent = MidiMessage
    Length = Len(mEvt)
    mHdr.lpData = VarPtr(mEvt)
    mHdr.dwBufferLength = Length
    mHdr.dwBytesRecorded = Length
    '    mHdr.dwUser = 0
    mHdr.dwFlags = 0
    hdrLength = Len(mHdr)
    '    rc = midiOutPrepareHeader(hMidiStream, mHdr, Len(mHdr))
    rc = midiOutPrepareHeader(hMidiStream, mHdr, hdrLength)
    If rc <> 0 Then Exit Sub
    '    rc = midiStreamOut(hMidiStream, mHdr, Len(mHdr)) ''' das geht nicht
    rc = midiOutPrepareHeader(hMidiStream, mHdr, hdrLength)
    If rc <> 0 Then Exit Sub
    rc = midiOutUnprepareHeader(hMidiStream, mHdr, hdrLength)
    '    rc = midiOutPrepareHeader(hMidiStream, mHdr, Len(mHdr))
    If rc <> 0 Then Exit Sub
End Sub

Public Sub WaitMilli(mSec As Long)
    Sleep mSec
End Sub

Public Sub start()
    Dim rc As Long
    rc = midiStreamRestart(hMidiStream)
End Sub

Sub Outout()
    Dim mEvt As MidiEvent
    Dim mHdr As MIDIHDR
    Dim Length As Long
    Dim hdrLength As Long

    Dim MyMsg As String

    MyMsg = Chr(0) & Chr(0) & Chr(0) & Chr(0) & _
            Chr(0) & Chr(0) & Chr(0) & Chr(0) & _
            Chr(0) & Chr(144) & Chr(60) & Chr(127)


    Dim rc As Long
    '    mEvt.dwDeltaTime = DeltaTime
    '    mEvt.dwStreamID = 0
    '    mEvt.dwEvent = MidiMessage
    '    Length = Len(mEvt)
    '    mHdr.lpData = VarPtr(mEvt)

    Length = Len(MyMsg)
    mHdr.lpData = MyMsg

    mHdr.dwBufferLength = Length
    mHdr.dwBytesRecorded = Length
    '    mHdr.dwUser = 0
    mHdr.dwFlags = 0
    hdrLength = Len(mHdr)
    '    rc = midiOutPrepareHeader(hMidiStream, mHdr, Len(mHdr))
    rc = midiOutPrepareHeader(hMidiStream, mHdr, hdrLength)
    If rc <> 0 Then Exit Sub
    rc = midiStreamOut(hMidiStream, mHdr, Len(mHdr))    ''' das geht nicht
    If rc <> 0 Then Exit Sub

    '    rc = midiOutPrepareHeader(hMidiStream, mHdr, hdrLength)

    rc = midiStreamRestart(hMidiStream)
    If rc <> 0 Then Exit Sub

    Sleep 2000

    '    rc = midiStreamStop(hMidiStream)

    rc = midiOutUnprepareHeader(hMidiStream, mHdr, hdrLength)
    '    rc = midiOutPrepareHeader(hMidiStream, mHdr, Len(mHdr))
    If rc <> 0 Then Exit Sub
End Sub
*/
