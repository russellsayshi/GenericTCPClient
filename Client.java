import java.io.*;
import java.util.*;
import java.net.*;

public class Client {
    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private Commands commands;
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
            System.out.println("Try:");
            System.out.println("    connect <ip> <port>");
            System.out.println("    send <int/double/string/float/...> <value>");
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
            try {
                int port = Integer.valueOf(portStr);
                System.out.println("Connecting to " + hostname + ":" + portStr + "...");
                clientSocket = new Socket(hostname, port);
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
                if(type.equalsIgnoreCase("string")) {
                    System.out.println("Sending UTF-8 string...");
                    output.writeUTF(data);
                } else if(type.equalsIgnoreCase("float")) {
                    output.writeFloat(Float.valueOf(data));
                } else if(type.equalsIgnoreCase("int")) {
                    output.writeInt(Integer.valueOf(data));
                } else if(type.equalsIgnoreCase("long")) {
                    output.writeLong(Long.valueOf(data));
                } else if(type.equalsIgnoreCase("byte")) {
                    output.writeByte(Byte.valueOf(data));
                } else if(type.equalsIgnoreCase("double")) {
                    output.writeDouble(Double.valueOf(data));
                } else if(type.equalsIgnoreCase("boolean")) {
                    output.writeBoolean(Boolean.valueOf(data));
                } else if(type.equalsIgnoreCase("char")) {
                    if(data.length() != 0) {
                        System.err.println("Chars should be 1 character.");
                    } else {
                        output.writeChar(data.charAt(0));
                    }
                } else {
                    System.err.println("Unrecognized data type: " + type);
                }
            } catch(NumberFormatException nfe) {
                System.err.println("Number format exception.");
            } catch(IOException ioe) {
                System.err.println(ioe.getMessage());
            } catch(NullPointerException npe) {
                System.err.println("Null pointer exception. Are you connected to a server?");
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