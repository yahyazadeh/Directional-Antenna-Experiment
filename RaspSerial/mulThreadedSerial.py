#!/usr/bin/env python

# Reference program : https://github.com/IECS/MansOS/blob/master/tools/serial/ser.py
#
# Multi-threaded python script to collect data from serial
#
#

import time, serial, sys, threading, argparse, glob
from sys import platform as _platform
import socket

#global variables

global flDone
global cliArgs
global writeBuffer
global portsList
global portCount
global defaultStartPort
global defaultServerIP

def getUserInput(prompt):
    if sys.version[0] >= '3':
        return input(prompt)
    else:
        return raw_input(prompt)


def listenSerial(threadNum):
    global flDone
    global cliArgs
    global writeBuffer
    global portsList
    try:
        ser = serial.Serial(portsList[threadNum], cliArgs.baudRate, timeout=cliArgs.timeout,
                            parity=serial.PARITY_NONE, rtscts=cliArgs.flowcontrol)
        ser.flushInput()
        ser.flushOutput()
    except serial.SerialException as ex:
        sys.stderr.write("\nSerial exception:\n\t{}".format(ex))
        flDone = True
        return
    sys.stderr.write("Using port {}, baudrate {}\n".format(ser.portstr, cliArgs.baudRate))
    while (not flDone):
        # write
        if writeBuffer:
            for c in writeBuffer:
                ser.write(bytearray([c]))
            writeBuffer = ""
        # read
        serLen = ser.inWaiting()
        if serLen > 0:
            s = ser.read(serLen)
            sendMsg(s, threadNum, cliArgs.tcpStartPort, cliArgs.serverIP)
            #if type(s) is str: 
            #    sys.stdout.write(s)
            #else:
            #    for c in s:
            #        sys.stdout.write( "{}".format(chr(c)) )
            #sys.stdout.flush()
        # allow other threads to run
        time.sleep(1)
    sys.stderr.write("\nDone\n")
    ser.close()
    return 0


def serialPortsList():
    """ Lists serial port names
        :raises EnvironmentError:
            On unsupported or unknown platforms
        :returns:
            A list of the serial ports available on the system
    """
    if sys.platform.startswith('linux'):
        ports = glob.glob('/dev/tty[U]*')
    else:
        raise EnvironmentError('Unsupported platform')
    result = []
    for port in ports:
        try:
            s = serial.Serial(port)
            s.close()
            result.append(port)
        except (OSError, serial.SerialException):
            pass
    result = sorted( result )
    return result


def getCliArgs():
    global portsList
    global portCount
    global defaultStartPort
    global defaultServerIP
    #defaultSerialPort = "/dev/ttyUSB0"
    #print portsList
    defaultSerialPort = portsList[portCount]
    defaultBaudRate = 38400
    defaultStartPort = 1030
    defaultServerIP = '192.168.0.1'
    version = "0.6/2016.06.30"
    parser = argparse.ArgumentParser(description="MansOS serial communicator", prog="ser")
    parser.add_argument('-s', '--serial_port', dest='serialPort', action='store', default=defaultSerialPort,
        help='serial port to listen (default: ' + defaultSerialPort + ' )')
    parser.add_argument('-b', '--baud_rate', dest='baudRate', action='store', default=defaultBaudRate,
        help='baud rate (default: ' + str(defaultBaudRate) + ')')
    parser.add_argument('-p', '--platform', dest='platform', action='store', default='telosb',
        help='platform (default: telosb)')
    parser.add_argument('-f', '--flowcontrol', dest='flowcontrol', action='store', default=False,
        help='enable hardware flow control (default: False)')
    parser.add_argument('-t', '--timeout', dest='timeout', action='store', default=1,
        help='timeout for serial (default: 1)')
    parser.add_argument('--version', action='version', version='%(prog)s ' + version)
    parser.add_argument('-l', '--list', action="store_true", default=False,
        help='list available serial ports')
    parser.add_argument('-tSP', '--tcpStartPort', action="store", dest='tcpStartPort', default=defaultStartPort,
                        help='TCP Start Port (default: 1030)')
    parser.add_argument('-sIP', '--serverIP', action="store", dest='serverIP', default=defaultServerIP,
                        help='Destination Server IP (default: 192.168.0.1)')
    return parser.parse_args()


def sendMsg(data, threadNum, tcpStartPort, serverIP):
    global defaultServerIP
    global defaultStartPort
    # Create a TCP/IP socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # Connect the socket to the port where the server is listening
    server_address = (serverIP, int(tcpStartPort) + threadNum)
    try:
        sock.connect(server_address)
        sock.sendall(data)
        sock.close()
    except:
        sys.stderr.write('Error in opening the socket')


def main():
    global flDone
    global cliArgs
    global writeBuffer
    global portsList
    global portCount
    portCount = 0
    flDone = False
    portsList = serialPortsList()
    if len(portsList)<=0 :
        sys.stderr.write("No serial ports found!\n")
        return 1
    cliArgs = getCliArgs()
    sys.stderr.write("MansOS serial port access app, press Ctrl+C to exit\n")
    # Detect the platform. Serial ports are named differently for each
    if sys.platform.startswith('linux') or sys.platform.startswith('cygwin'):
        sys.stderr.write("Detected Linux\n")
    elif sys.platform.startswith('darwin'):
        sys.stderr.write("Detected Darwin\n")
    else:
        sys.stderr.write("Assuming Windows\n")
    writeBuffer = ""
    if cliArgs.list:
        sys.stderr.write("Available serial ports: ")
        sys.stderr.write( str(portsList) )
        sys.stderr.write("\n")
        return 0
    for ports in portsList:
        threading.Thread(target=listenSerial,args=[portCount]).start()
        portCount +=1
    # Keyboard scanning loop
    writeBuffer = ""
    while (not flDone):
        try:
            s = getUserInput("")
        except BaseException as ex:
            sys.stderr.write("\nKeyboard interrupt\n")
            flDone = True
            return 0

        writeBuffer += s + '\r\n'
    return 0

if __name__ == "__main__":
    main()
