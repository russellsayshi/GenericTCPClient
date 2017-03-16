import java.io.*;
import java.util.*;
import java.net.*;

public class Client {
    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private Commands commands;
    private static final int PING_TIMEOUT = 1500;
    private static final int DEFAULT_READ_TIMEOUT = 200;
    public static void main(String[] args) {
        new Client();
    }
    
    public Client() {
        commands = new Commands();
        System.out.println("Try typing \"help\".");
        System.out.print("client> ");
        Scanner scan = new Scanner(System.in);
        while(scan.hasNextLine()) {
            String line = scan.nextLine();
            processInput(line);
            System.out.print("client> ");
        }
        scan.close();
    }
    
    private class Commands {
        public void help() {
            System.out.println("<> = required, [] = optional");
            System.out.println("Try:");
            System.out.println("    connect <ip> <port>");
            System.out.println("    send <int/double/utfstring/bytestring/float/char8/...> <value>");
            System.out.println("    read <int/double/utfstring/bytestring/float/char8/...> <value>");
            System.out.println("    timeout [newValue]");
            System.out.println("    status");
            System.out.println("    ping");
            System.out.println("    close");
        }
        public void close() {
            if(clientSocket == null) {
                System.err.println("Error: client socket is null.");
            } else {
                try {
                    clientSocket.close();
                    System.out.println("Closed.");
                } catch(IOException ioe) {
                    System.err.println(ioe);
                }
                clientSocket = null;
            }
        }
        public void connect(String hostname, String portStr) {
            if(clientSocket != null) {
                System.err.println("Still connected to another socket. Closing...");
                close();
            }
            try {
                int port = Integer.valueOf(portStr);
                System.out.println("Connecting to " + hostname + ":" + portStr + "...");
                clientSocket = new Socket(hostname, port);
                clientSocket.setSoTimeout(DEFAULT_READ_TIMEOUT);
                System.out.println("Socket established.");
                output = new DataOutputStream(clientSocket.getOutputStream());
                input = new DataInputStream(clientSocket.getInputStream());
            } catch(NumberFormatException nfe) {
                System.err.println("Incorrect number format.");
            } catch(IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        }
        public void send(String type, String data) {
            try {
                if(type.equalsIgnoreCase("utfstring")) {
                    output.writeUTF(data);
                } else if(type.equalsIgnoreCase("float")) {
                    output.writeFloat(Float.valueOf(data));
                } else if(type.equalsIgnoreCase("int")) {
                    output.writeInt(Integer.valueOf(data));
                } else if(type.equalsIgnoreCase("long")) {
                    output.writeLong(Long.valueOf(data));
                } else if(type.equalsIgnoreCase("byte")) {
                    output.writeByte(Byte.valueOf(data));
                } else if(type.equalsIgnoreCase("bytestring")) {
                    for(int i = 0; i < data.length(); i++) {
                        output.writeByte((byte)data.charAt(i));
                    }
                    output.writeByte(0);
                } else if(type.equalsIgnoreCase("double")) {
                    output.writeDouble(Double.valueOf(data));
                } else if(type.equalsIgnoreCase("boolean")) {
                    output.writeBoolean(Boolean.valueOf(data));
                } else if(type.equalsIgnoreCase("char16")) {
                    if(data.length() != 1) {
                        System.err.println("Chars should be 1 character.");
                        return;
                    } else {
                        output.writeChar(data.charAt(0));
                    }
                } else if(type.equalsIgnoreCase("char8")) {
                    if(data.length() != 1) {
                        System.err.println("Chars should be 1 character.");
                        return;
                    } else {
                        output.writeByte((byte)data.charAt(0));
                    }
                } else {
                    System.err.println("Unrecognized data type: " + type);
                    return;
                }
                System.out.println("Sent '" + data + "'.");
            } catch(NumberFormatException nfe) {
                System.err.println("Number format exception.");
            } catch(IOException ioe) {
                System.err.println(ioe.getMessage());
            } catch(NullPointerException npe) {
                System.err.println("Null pointer exception. Are you connected to a server?");
            }
        }
        public void read(String type) {
            try {
                if(type.equalsIgnoreCase("utfstring")) {
                    System.out.println("Read: " + input.readUTF());
                } else if(type.equalsIgnoreCase("float")) {
                    System.out.println("Read: " + input.readFloat());
                } else if(type.equalsIgnoreCase("int")) {
                    System.out.println("Read: " + input.readInt());
                } else if(type.equalsIgnoreCase("long")) {
                    System.out.println("Read: " + input.readLong());
                } else if(type.equalsIgnoreCase("byte")) {
                    System.out.println("Read: " + input.readByte());
                } else if(type.equalsIgnoreCase("bytestring")) {
                    System.out.print("Read: ");
                    while(true) {
                        byte next = input.readByte();
                        if(next == 0) break;
                        System.out.print((char)next);
                    }
                    System.out.println();
                } else if(type.equalsIgnoreCase("double")) {
                    System.out.println("Read: " + input.readDouble());
                } else if(type.equalsIgnoreCase("boolean")) {
                    System.out.println("Read: " + input.readBoolean());
                } else if(type.equalsIgnoreCase("char16")) {
                    System.out.println("Read: " + input.readChar());
                } else if(type.equalsIgnoreCase("char8")) {
                    System.out.println("Read: " + (char)input.readByte());
                } else {
                    System.err.println("Unrecognized data type: " + type);
                }
            } catch(IOException ioe) {
                System.err.println(ioe.getMessage());
            } catch(NullPointerException npe) {
                System.err.println("Null pointer exception. Are you connected to a server?");
            }
        }
        public void status() {
            if(clientSocket == null) {
                System.err.println("Not connected.");
                return;
            }
            System.out.println("Connected: " + clientSocket.isConnected());
            System.out.println("Address: " + clientSocket.getInetAddress());
        }
        public void ping() {
            try {
                System.out.println("Socket reachable: " +
                    clientSocket.getInetAddress().isReachable(PING_TIMEOUT));
            } catch(IOException ioe) {
                System.err.println("IOException determining if socket reachable: " +
                    ioe.getMessage());
                System.err.println("Recommend closing socket.");
            } catch(NullPointerException npe) {
                System.err.println("Connect to a socket first.");
            }
        }
        public void timeout() {
            try {
                System.out.println("SO_TIMEOUT is "+ clientSocket.getSoTimeout());
            } catch(NullPointerException npe) {
                System.err.println("Connect to a socket first.");
            } catch(SocketException se) {
                System.err.println("Socket exception: " + se.getMessage());
            }
        }
        public void setTimeout(String timeout) {
            try {
                clientSocket.setSoTimeout(Integer.valueOf(timeout));
                System.out.println("Timeout set to '" + timeout + "'.");
            } catch(NullPointerException npe) {
                System.err.println("Connect to a socket first.");
            } catch(NumberFormatException nfe) {
                System.err.println("Not a valid integer.");
            } catch(SocketException se) {
                System.err.println("Socket exception: " + se.getMessage());
            }
        }
    }
    
    private static boolean arrayIsEmpty(Object[] arr) {
        if(arr == null) {
            return true;
        } else if(arr.length == 0) {
            return true;
        } else {
            for(Object obj : arr) {
                if(arr != null) {
                    return false;
                }
            }
            return true;
        }
    }
    
    private static void wrongNumberArguments(int have, int expected) {
        if(have > expected) {
            System.err.print("Too many");
        } else if(have < expected) {
            System.err.print("Too few");
        } else {
            System.err.println("Internal error.");
            return;
        }
        System.err.println(" arguments. Have: " + have + " expected: " + expected + ".");
    }
    
    private static boolean expectArguments(String[] args, int number) {
        boolean res = hasArguments(args, number);
        if(res) {
            return true;
        } else {
            wrongNumberArguments(args.length, number);
            return false;
        }
    }
    
    private static boolean hasArguments(String[] args, int number) {
        if(arrayIsEmpty(args)) {
            return number == 0;
        } else {
            if(args.length == number) {
                //right number of args
                return true;
            } else {
                //wrong number of args
                return false;
            }
        }
    }
    
    private void processCommand(String command, String rawArgs, String... args) {
        //try {
            if(command.equalsIgnoreCase("help")) {
                commands.help();
            } else if(command.equalsIgnoreCase("close")) {
                if(expectArguments(args, 0)) {
                    commands.close();
                }
            } else if(command.equalsIgnoreCase("connect")) {
                if(expectArguments(args, 2)) {
                    commands.connect(args[0], args[1]);
                }
            } else if(command.equalsIgnoreCase("send")) {
                int loc = rawArgs.indexOf(" ");
                String type = rawArgs.substring(0, loc);
                String data = rawArgs.substring(loc+1);
                commands.send(type, data);
            } else if(command.equalsIgnoreCase("read")) {
                if(expectArguments(args, 1)) {
                    commands.read(args[0]);
                }
            } else if(command.equalsIgnoreCase("timeout")) {
                if(hasArguments(args, 0)) {
                    //no args
                    commands.timeout();
                } else if(hasArguments(args, 1)) {
                    //1 arg
                    commands.setTimeout(args[0]);
                } else {
                    System.err.println("Expected 0 arguments to read timeout value, 1 argument to set timeout value.");
                }
            } else if(command.equalsIgnoreCase("status")) {
                if(expectArguments(args, 0)) {
                    commands.status();
                }
            } else if(command.equalsIgnoreCase("ping")) {
                if(expectArguments(args, 0)) {
                    commands.ping();
                }
            } else {
                System.err.println("Unknown command: " + command);
            }
        /*} catch(NullPointerException|ArrayIndexOutOfBoundsException e) {
               System.err.println("Error running command. Did you give enough arguments?");
        }*/
    }
    
    private void processInput(String line) {
        //discover information about string
        //and then call the proper method
        int spaceIndex = line.indexOf(" ");
        if(spaceIndex == -1) {
            //either one command with no args
            //or nothing
            if(line.length() == 0) {
                //nothing
                return; //we don't care
            } else {
                //we have an argless command
                processCommand(line.trim(), "");
            }
        } else {
            String command = line.substring(0, spaceIndex);
            String rawArgs = line.substring(spaceIndex+1);
            String[] args = rawArgs.split(" ");
            processCommand(command, rawArgs, args);
        }
    }
}
