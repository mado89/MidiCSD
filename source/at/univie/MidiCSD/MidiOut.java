/**
 * 
 */
package at.univie.MidiCSD;

/**
 * @author Martin Dobiasch (java portation), Erich Neuwirth
 *
 */
public class MidiOut
{
	
}
/*
 * ' MidiCSD
' © 2009 Erich Neuwirth
' a toolkit for playing music and producing sound effects from Excel
' ------------------------------------------------------------------

Option Explicit
Private hMidi As Long

'  general error return values
Const MMSYSERR_NOERROR = 0                          '  no error
Const MMSYSERR_BASE = 0
Const MIDIERR_BASE = 64
Const MCIERR_BASE = 256
Const MMSYSERR_ERROR = (MMSYSERR_BASE + 1)          '  unspecifiederror
Const MMSYSERR_BADDEVICEID = (MMSYSERR_BASE + 2)    '  device ID out of Range
Const MMSYSERR_NOTENABLED = (MMSYSERR_BASE + 3)     '  driver failed enable
Const MMSYSERR_ALLOCATED = (MMSYSERR_BASE + 4)      '  device already allocated
Const MMSYSERR_INVALHANDLE = (MMSYSERR_BASE + 5)    '  device handle is invalid
Const MMSYSERR_NODRIVER = (MMSYSERR_BASE + 6)       '  no device driver present
Const MMSYSERR_NOMEM = (MMSYSERR_BASE + 7)          '  memory allocation Error()
Const MMSYSERR_NOTSUPPORTED = (MMSYSERR_BASE + 8)   '  function isn't supported
Const MMSYSERR_BADERRNUM = (MMSYSERR_BASE + 9)      '  error value out of Range
Const MMSYSERR_INVALFLAG = (MMSYSERR_BASE + 10)     '  invalid flag passed
Const MMSYSERR_INVALPARAM = (MMSYSERR_BASE + 11)    '  invalid parameter passed
Const MMSYSERR_LASTERROR = (MMSYSERR_BASE + 11)     '  last error in Range

Const MIDIERR_UNPREPARED = (MIDIERR_BASE + 0)       '  header not prepared
Const MIDIERR_STILLPLAYING = (MIDIERR_BASE + 1)     '  still something playing
Const MIDIERR_NOMAP = (MIDIERR_BASE + 2)            '  no current map
Const MIDIERR_NOTREADY = (MIDIERR_BASE + 3)         '  hardware is still busy
Const MIDIERR_NODEVICE = (MIDIERR_BASE + 4)         '  port no longer connected
Const MIDIERR_INVALIDSETUP = (MIDIERR_BASE + 5)     '  invalid setup
Const MIDIERR_LASTERROR = (MIDIERR_BASE + 5)        '  last error in range

'  Type codes which go in the high byte of the event DWORD of a stream buffer

'  Type codes 00-7F contain parameters within the low 24 bits
'  Type codes 80-FF contain a length of their parameter in the low 24
'  bits, followed by their parameter data in the buffer. The event
'  DWORD contains the exact byte length; the parm data itself must be
'  padded to be an even multiple of 4 Byte long.
'

Const MEVT_F_SHORT = &H0&
Const MEVT_F_LONG = &H80000000
Const MEVT_F_CALLBACK = &H40000000
Const MIDISTRM_ERROR = -2



'  MIDI output device capabilities structure
Private Type MIDIOUTCAPS
    wMid As Integer                  '  manufacturer ID
    wPid As Integer                  '  product ID
    vDriverVersion As Long           '  version of the driver
    szPname As String * 32           '  product name (NULL terminated string)
    wTechnology As Integer           '  type of device
    wVoices As Integer               '  # of voices (internal synth only)
    wNotes As Integer                '  max # of notes (internal synth only)
    wChannelMask As Integer          '  channels used (internal synth only)
    dwSupport As Long             '  functionality supported by driver
End Type

'  MIDI data block header
Private Type MIDIHDR
    lpData As String             '  pointer to locked data block
    dwBufferLength As Long       '  length of data in data block
    dwBytesRecorded As Long      '  used for input only
    dwUser As Long               '  for client's use
    dwFlags As Long              '  assorted flags (see defines)
    lpNext As Long               '  reserved for driver
    reserved As Long             '  reserved for driver
End Type

' Declare midi function calls

Private Declare Function midiOutOpen Lib "winmm.dll" _
                                     (lphMidiOut As Long, ByVal uDeviceID As Long, _
                                      ByVal dwCallback As Long, ByVal dwInstance As Long, _
                                      ByVal dwFlags As Long) As Long
Private Declare Function midiOutShortMsg Lib "winmm.dll" _
                                         (ByVal hMidiOut As Long, ByVal dwMsg As Long) As Long
Private Declare Function midiOutClose Lib "winmm.dll" _
                                      (ByVal hMidiOut As Long) As Long
Private Declare Function midiOutGetNumDevs Lib "winmm.dll" () As Integer
Private Declare Function midiOutGetDevCaps Lib "winmm.dll" Alias "midiOutGetDevCapsA" _
                                           (ByVal uDeviceID As Long, lpCaps As MIDIOUTCAPS, _
                                            ByVal uSize As Long) As Long
Private Declare Function midiOutPrepareHeader Lib "winmm.dll" _
                                              (ByVal hMidiOut As Long, lpMidiOutHdr As MIDIHDR, _
                                               ByVal uSize As Long) As Long
Private Declare Function midiOutUnprepareHeader Lib "winmm.dll" _
                                                (ByVal hMidiOut As Long, lpMidiOutHdr As MIDIHDR, _
                                                 ByVal uSize As Long) As Long
Private Declare Function midiOutLongMsg Lib "winmm.dll" _
                                        (ByVal hMidiOut As Long, lpMidiOutHdr As MIDIHDR, _
                                         ByVal uSize As Long) As Long
Private Declare Function midiOutGetErrorText Lib "winmm.dll" Alias "midiOutGetErrorTextA" _
                                             (ByVal err As Long, ByVal lpText As String, _
                                              ByVal uSize As Long) As Long
Private Declare Function midiOutReset Lib "winmm.dll" (ByVal hMidiOut As Long) As Long
Private Declare Function timeGetTime Lib "winmm.dll" () As Long

Private Declare Sub Sleep Lib "Kernel32" (ByVal dwMilliseconds As Long)

Private Declare Function mciSendString Lib "winmm.dll" Alias "mciSendStringA" _
                                       (ByVal lpstrCommand As String, _
                                        ByVal lpstrReturnString As String, _
                                        ByVal uReturnLength As Long, _
                                        ByVal hwndCallback As Long) As Long
Private Declare Function mciGetErrorString Lib "winmm.dll" Alias "mciGetErrorStringA" _
                                           (ByVal dwError As Long, ByVal lpstrBuffer As String, _
                                            ByVal uLength As Long) As Long


Dim LastErrorMsg As String

Public Sub OpenDevice(DeviceNum As Long)
    Dim rc As Integer
    On Error Resume Next
    CloseDevice
    rc = midiOutOpen(hMidi, DeviceNum - 1, 0&, 0&, 0&)
    If rc <> 0 Then LastErrorMsg = GetErrorText("Open", rc)
    Debug.Print LastErrorMsg
End Sub

Public Sub CloseDevice()
    On Error Resume Next
    midiOutClose hMidi
End Sub

Public Sub Out(mmsg As Variant)
    Dim i As Integer
    Dim localmsg As String
    If TypeName(mmsg) = "String" Then
        localmsg = mmsg
        OutLong localmsg
        Exit Sub
    End If
    If Not IsArray(mmsg) Then
        midiOutShortMsg hMidi, mmsg
        Exit Sub
    Else
        For i = LBound(mmsg) To UBound(mmsg)
            midiOutShortMsg hMidi, mmsg(i)
        Next i
        Exit Sub
    End If
End Sub

Public Sub WaitMilli(mSec As Long)
    Sleep mSec
End Sub

Public Function time()
    time = timeGetTime()
End Function

Private Function GetErrorText(mOpt As String, rc As Integer) As String
    Dim msgText As String * 132
    midiOutGetErrorText rc, msgText, 128
    GetErrorText = msgText
End Function

Public Sub Reset()
    Dim rc As Long
    rc = midiOutReset(hMidi)
End Sub

Private Sub OutLong(LongMidiMsg As String)
    Dim mHdr As MIDIHDR
    Dim rc As Long
    Dim Length As Long
    Length = Len(LongMidiMsg)
    mHdr.lpData = LongMidiMsg
    mHdr.dwBufferLength = Length
    mHdr.dwBytesRecorded = Length
    mHdr.dwUser = 0
    mHdr.dwFlags = 0
    rc = midiOutPrepareHeader(hMidi, mHdr, Len(mHdr))
    If rc <> 0 Then Exit Sub
    rc = midiOutLongMsg(hMidi, mHdr, Len(mHdr))
    If rc <> 0 Then Exit Sub
    mHdr.dwFlags = 0
    rc = midiOutUnprepareHeader(hMidi, mHdr, Len(mHdr))
    If rc <> 0 Then Exit Sub
End Sub

Public Function Devices() As String()
    Dim i As Long
    Dim nOut As Long
    Dim OutCaps As MIDIOUTCAPS
    Dim devnames() As String
    Dim localname As String
    nOut = midiOutGetNumDevs()
'    Debug.Print nOut
    ReDim devnames(1 To nOut)
    For i = 0 To nOut - 1
        midiOutGetDevCaps i, OutCaps, Len(OutCaps)
        localname = OutCaps.szPname
        localname = Left(localname, InStr(localname, Chr(0)))
        devnames(i + 1) = localname
    Next i
    Devices = devnames
End Function


*/
