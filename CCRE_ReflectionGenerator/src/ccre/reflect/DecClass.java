/*
 * Copyright 2014 Colby Skeggs
 * 
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 * 
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
package ccre.reflect;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

public class DecClass {

    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_SUPER = 0x0020;
    public static final int ACC_SYNCHRONIZED = 0x0020;
    public static final int ACC_NATIVE = 0x0100;
    public static final int ACC_INTERFACE = 0x0200;
    public static final int ACC_ABSTRACT = 0x0400;
    public static final int ACC_STRICT = 0x0800;
    public static final int ACC_SYNTHETIC = 0x1000;
    public static final int ACC_ANNOTATION = 0x2000;
    public static final int ACC_ENUM = 0x4000;

    public DecMethod[] methods;
    public DecField[] fields;
    public String[] interfaces;
    public String superclassname, name;
    public int flags;
    public ArrayList<Object> constpool;
    public boolean include_all = false;

    public DecClass(boolean include_all) {
        this.include_all = include_all;
    }

    public static final class DecField {

        public String name, desc;
        public int flags;

        public boolean is(int mod) {
            return (flags & mod) != 0;
        }
    }

    public static final class DecMethod {

        public String name, desc;
        public int flags;

        public boolean is(int mod) {
            return (flags & mod) != 0;
        }
    }

    public boolean declaresMethod(String name, String desc, Map<String, DecClass> others) {
        for (DecMethod dm : methods) {
            if (dm.is(ACC_PUBLIC) && !dm.is(ACC_STATIC) && dm.name.equals(name) && dm.desc.equals(desc)) {
                return true;
            }
        }
        DecClass superclass = others.get(superclassname);
        if (superclass != null) {
            return superclass.declaresMethod(name, desc, others);
        }
        for (String iface : interfaces) {
            DecClass iclass = others.get(iface);
            if (iclass != null && iclass.declaresMethod(name, desc, others)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNameFiltered(String name) {
        for (String entry : new String[]{"com/", "sun", "org/", "edu/wpi/first/wpilibj/camera/", "javax/microedition/midlet/", "javax/microedition/rms/",
            "edu/wpi/first/wpilibj/CANJaguar", "edu/wpi/first/wpilibj/buttons/", "edu/wpi/first/wpilibj/command/", "edu/wpi/first/wpilibj/can/",
            "edu/wpi/first/wpilibj/image/", "edu/wpi/first/wpilibj/networktables", "tests/", "edu/wpi/first/wpilibj/communication/", "java/sql",
            "com/sun/squawk/ServiceOperation/", "edu/wpi/first/wpilibj/communication/UsageReporting", "java/util/Calendar", "javax/", "java/applet",
            "edu/wpi/first/wpilibj/DriverStationEnhancedIO", "javax/microedition/io/HttpConnection", "javax/microedition/io/StreamConnectionNotifier",
            "edu/wpi/first/wpilibj/tables/", "edu/wpi/first/wpilibj/SPIDevice", "javax/microedition/io/UDPDatagramConnection", "java/awt", "java/security",
            "edu/wpi/first/wpilibj/PIDController", "edu/wpi/first/wpilibj/smartdashboard/", "edu/wpi/first/wpilibj/livewindow/", "java/rmi",
            "edu/wpi/first/wpilibj/Dashboard", "edu/wpi/first/wpilibj/HiTechnicColorSensor", "edu/wpi/first/wpilibj/SerialPort", "oracle/", "java/nio",
            "edu/wpi/first/wpilibj/AnalogTrigger", "javax/microedition/io/DatagramConnection", "edu/wpi/first/wpilibj/Skeleton", "java/util/zip",
            "edu/wpi/first/wpilibj/visa/", "edu/wpi/first/wpilibj/util/", "edu/wpi/first/wpilibj/SimpleRobot", "edu/wpi/first/wpilibj/Sendable",
            "edu/wpi/first/wpilibj/SafePWM", "edu/wpi/first/wpilibj/RobotDrive", "edu/wpi/first/wpilibj/PID", "edu/wpi/first/wpilibj/MotorSafety",
            "edu/wpi/first/wpilibj/Kinect", "edu/wpi/first/wpilibj/Joystick", "edu/wpi/first/wpilibj/InterruptableSensorBase", "java/text", "java/math",
            "edu/wpi/first/wpilibj/I2C", "edu/wpi/first/wpilibj/Gyro", "edu/wpi/first/wpilibj/GearTooth", "edu/wpi/first/wpilibj/fpga/tDMA", "java/beans",
            "edu/wpi/first/wpilibj/fpga/ExpectedFPGASignature", "edu/wpi/first/wpilibj/fpga/tWatchdog", "java/util/TimeZone", "java/util/Date",
            "java/lang/management", "java/util/concurrent", "java/net", "java/lang/invoke", "java/util/jar", "java/util/spi", "java/lang/reflect",
            "java/lang/instrument", "java/util/regex", "java/util/prefs", "java/lang/ref", "jdk/internal", "java/io/Object", "java/util/logging"}) {
            if (name.startsWith(entry)) {
                return true;
            }
        }
        return name.endsWith("Exception") || name.endsWith("Error");
    }

    public void process(PrintStream out, Map<String, DecClass> others, ArrayList<String> astr) {
        if ((flags & ACC_PUBLIC) == 0 || name.contains("$")) { // TODO: Find a better way than checking for $.
            return;
        }
        if (!include_all && isNameFiltered(name)) {
            return;
        }
        String path = name.replace('/', '.');
        for (DecField df : fields) {
            if (!df.is(ACC_PUBLIC) || df.desc.contains("$")) {
                continue;
            }
            String conv = "";
            switch (df.desc.charAt(0)) {
                case 'I':
                    conv = "Integer.valueOf";
                    break;
                case 'Z':
                    conv = "Boolean.valueOf";
                    break;
                case 'B':
                    conv = "Byte.valueOf";
                    break;
                case 'C':
                    conv = "Character.valueOf";
                    break;
                case 'F':
                    conv = "Float.valueOf";
                    break;
                case 'J':
                    conv = "Long.valueOf";
                    break;
                case 'S':
                    conv = "Short.valueOf";
                    break;
                case 'D':
                    conv = "Double.valueOf";
                    break;
            }
            if (df.is(ACC_STATIC)) {
                out.println("\t\t\tcase " + astr.size() + ": return " + conv + "(" + path + "." + df.name + ");");
                astr.add(path + "/" + df.name + "?_0RS");
                if (!df.is(ACC_FINAL)) {
                    out.println("\t\t\tcase " + astr.size() + ": return " + conv + "(" + path + "." + df.name + " = ((" + makeObjectTypeCast(df.desc) + ") args[0])" + makeObjectCastSuffix(df.desc) + ");");
                    astr.add(path + "/" + df.name + "!_1VS");
                }
            } else {
                out.println("\t\t\tcase " + astr.size() + ": return " + conv + "(((" + path + ")self)." + df.name + ");");
                astr.add(path + "/" + df.name + "?_0R");
                if (!df.is(ACC_FINAL)) {
                    out.println("\t\t\tcase " + astr.size() + ": return " + conv + "(((" + path + ")self)." + df.name + " = ((" + makeObjectTypeCast(df.desc) + ") args[0])" + makeObjectCastSuffix(df.desc) + ");");
                    astr.add(path + "/" + df.name + "!_1V");
                }
            }
        }
        for (DecMethod dm : methods) {
            if (!dm.is(ACC_PUBLIC) || dm.desc.contains("$")) {
                continue;
            }
            if (dm.name.contains("info")) {
                System.out.flush();
            }
            String dma = getMethodArgumentCasts(dm.desc);
            int argn = 0;
            int index = 0;
            while (index < dma.length()) {
                index = dma.indexOf('[', index);
                if (index == -1) {
                    break;
                }
                int end = dma.indexOf(']', index);
                if (end == index + 1) {
                    index = end;
                    continue;
                }
                int arg;
                try {
                    arg = Integer.parseInt(dma.substring(index + 1, end));
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("Wait, what: " + dma.substring(index + 1, end));
                }
                index = end;
                if (arg > argn) {
                    argn = arg + 1;
                }
            }
            boolean isvoid = dm.desc.endsWith("V");
            String conv = "";
            if (dm.desc.charAt(dm.desc.length() - 2) != '[') {
                switch (dm.desc.charAt(dm.desc.length() - 1)) {
                    case 'I':
                        conv = "Integer.valueOf";
                        break;
                    case 'Z':
                        conv = "Boolean.valueOf";
                        break;
                    case 'B':
                        conv = "Byte.valueOf";
                        break;
                    case 'C':
                        conv = "Character.valueOf";
                        break;
                    case 'F':
                        conv = "Float.valueOf";
                        break;
                    case 'J':
                        conv = "Long.valueOf";
                        break;
                    case 'S':
                        conv = "Short.valueOf";
                        break;
                    case 'D':
                        conv = "Double.valueOf";
                        break;
                }
            }
            if (dm.is(ACC_STATIC)) {
                if (isvoid) {
                    out.println("\t\t\tcase " + astr.size() + ": " + path + "." + dm.name + "(" + dma + "); return null;");
                    astr.add(path + "/" + dm.name + dm.desc + "_" + argn + "VS");
                } else {
                    out.println("\t\t\tcase " + astr.size() + ": return " + conv + "(" + path + "." + dm.name + "(" + dma + "));");
                    astr.add(path + "/" + dm.name + dm.desc + "_" + argn + "RS");
                }
            } else if (dm.name.equals("<init>")) {
                if ((flags & ACC_ABSTRACT) == 0) {
                    out.println("\t\t\tcase " + astr.size() + ": return new " + path + "(" + dma + ");");
                    astr.add(path + "/<new>" + dm.desc + "_" + argn + "RS");
                }
            } else {
                if (superclassname != null) {
                    DecClass found = others.get(superclassname);
                    if (found != null && found.declaresMethod(dm.name, dm.desc, others)) {
                        continue; // Already declared, we don't need it again.
                    }
                }
                boolean shouldContinue = false;
                for (String iface : interfaces) {
                    DecClass found = others.get(iface);
                    if (found != null && found.declaresMethod(dm.name, dm.desc, others)) {
                        shouldContinue = true; // Already declared, we don't need it again.
                        break;
                    }
                }
                if (shouldContinue) {
                    continue;
                }
                if (isvoid) {
                    out.println("\t\t\tcase " + astr.size() + ": ((" + path + ")self)." + dm.name + "(" + dma + "); return null;");
                    astr.add(path + "/" + dm.name + dm.desc + "_" + argn + "V");
                } else {
                    out.println("\t\t\tcase " + astr.size() + ": return " + conv + "(((" + path + ")self)." + dm.name + "(" + dma + "));");
                    astr.add(path + "/" + dm.name + dm.desc + "_" + argn + "R");
                }
            }
        }
    }

    private static String getMethodArgumentCasts(String desc) {
        if (!desc.startsWith("(")) {
            throw new IllegalArgumentException("Must start with (: " + desc);
        }
        desc = desc.substring(1, desc.indexOf(')'));
        int id = 0;
        int i = 0;
        StringBuilder out = new StringBuilder();
        while (id < desc.length()) {
            if (i != 0) {
                out.append(", ");
            }
            out.append('(');
            char c = desc.charAt(id++);
            String suffix = "", outersuffix = "";
            while (c == '[') {
                suffix += "[]";
                c = desc.charAt(id++);
            }
            if (suffix.isEmpty()) {
                switch (c) {
                    case 'Z':
                        out.append("(Boolean)");
                        outersuffix = ".booleanValue()";
                        break;
                    case 'B':
                        out.append("(Byte)");
                        outersuffix = ".byteValue()";
                        break;
                    case 'C':
                        out.append("(Character)");
                        outersuffix = ".charValue()";
                        break;
                    case 'S':
                        out.append("(Short)");
                        outersuffix = ".shortValue()";
                        break;
                    case 'I':
                        out.append("(Integer)");
                        outersuffix = ".intValue()";
                        break;
                    case 'J':
                        out.append("(Long)");
                        outersuffix = ".longValue()";
                        break;
                    case 'F':
                        out.append("(Float)");
                        outersuffix = ".floatValue()";
                        break;
                    case 'D':
                        out.append("(Double)");
                        outersuffix = ".doubleValue()";
                        break;
                    case 'L':
                        out.append("(").append(desc.substring(id, desc.indexOf(';', id)).replace('/', '.')).append(')');
                        id = desc.indexOf(';', id) + 1;
                        break;
                    default:
                        throw new IllegalArgumentException("Bad: " + c);
                }
            } else {
                switch (c) {
                    case 'Z':
                        out.append("(boolean");
                        break;
                    case 'B':
                        out.append("(byte");
                        break;
                    case 'C':
                        out.append("(char");
                        break;
                    case 'S':
                        out.append("(short");
                        break;
                    case 'I':
                        out.append("(int");
                        break;
                    case 'J':
                        out.append("(long");
                        break;
                    case 'F':
                        out.append("(float");
                        break;
                    case 'D':
                        out.append("(double");
                        break;
                    case 'L':
                        out.append('(').append(desc.substring(id, desc.indexOf(';', id)).replace('/', '.'));
                        id = desc.indexOf(';', id) + 1;
                        break;
                    default:
                        throw new IllegalArgumentException("Bad: " + c);
                }
                out.append(suffix).append(')');
            }
            out.append("args[").append(i++).append("])").append(outersuffix);
        }
        return out.toString();
    }

    private static String makeObjectCastSuffix(String desc) {
        if (desc.length() == 1) {
            char c = desc.charAt(0);
            if (c == 'Z') {
                return ".booleanValue()";
            } else if (c == 'B') {
                return ".byteValue()";
            } else if (c == 'C') {
                return ".charValue()";
            } else if (c == 'S') {
                return ".shortValue()";
            } else if (c == 'I') {
                return ".intValue()";
            } else if (c == 'J') {
                return ".longValue()";
            } else if (c == 'F') {
                return ".floatValue()";
            } else if (c == 'D') {
                return ".doubleValue()";
            }
        }
        return "";
    }

    private static String makeObjectTypeCast(String desc) {
        if (desc.startsWith("[")) {
            return makeObjectTypeCast(desc.substring(1)).concat("[]");
        } else if (desc.startsWith("L") && desc.endsWith(";")) {
            return desc.substring(1, desc.length() - 1).replace('/', '.');
        } else if (desc.length() == 1) {
            char c = desc.charAt(0);
            if (c == 'Z') {
                return "Boolean";
            } else if (c == 'B') {
                return "Byte";
            } else if (c == 'C') {
                return "Character";
            } else if (c == 'S') {
                return "Short";
            } else if (c == 'I') {
                return "Integer";
            } else if (c == 'J') {
                return "Long";
            } else if (c == 'F') {
                return "Float";
            } else if (c == 'D') {
                return "Double";
            }
        }
        throw new RuntimeException("Bad descriptor: " + desc);
    }

    public void loadclass(DataInputStream in) throws IOException {
        if (in.readUnsignedShort() != 0xCAFE || in.readUnsignedShort() != 0xBABE) {
            throw new IOException("Bad magic number!");
        }
        in.readInt(); // Major and minor versions are ignored.
        int constant_count = in.readUnsignedShort();
        constpool = new ArrayList<Object>();
        constpool.add(null);
        //System.out.println(constant_count);
        for (int i = 1; i < constant_count; i++) {
            byte b = in.readByte();
            switch (b) {
                case 1: // UTF8
                    constpool.add(in.readUTF());
                    break;
                case 3: // INTEGER
                case 4: // FLOAT
                case 9: // FIELDREF
                case 10: // METHODREF
                case 11: // INTERFACEMETHOD
                case 12: // NAMETYPEREF
                case 16: // METHODTYPE
                case 18: // INVOKEDYNAMIC
                    constpool.add(in.readInt());
                    break;
                case 7: // CLASSREF
                case 8: // STRING
                    constpool.add((int) in.readUnsignedShort());
                    break;
                case 5: // LONG
                case 6: // DOUBLE
                    constpool.add(in.readLong());
                    constpool.add(null);
                    i++;
                    break;
                case 15: // METHODHANDLE
                    in.readByte();
                    in.readShort();
                    constpool.add(null);
                    break;
                default:
                    throw new RuntimeException("Bad: " + b);
            }
        }
        /*for (int i=0; i<constpool.size(); i++) {
         System.out.println(i + " => " + constpool.get(i));
         }*/
        // done loading constant pool
        this.flags = in.readUnsignedShort();
        int nid = in.readUnsignedShort();
        //System.out.println(nid);
        this.name = (String) constpool.get((Integer) constpool.get(nid));
        int superid = in.readUnsignedShort();
        this.superclassname = superid == 0 ? null : (String) constpool.get((Integer) constpool.get(superid));
        interfaces = new String[in.readUnsignedShort()];
        for (int i = 0; i < interfaces.length; i++) {
            interfaces[i] = (String) constpool.get((Integer) constpool.get(in.readUnsignedShort()));
        }
        fields = new DecField[in.readUnsignedShort()];
        for (int i = 0; i < fields.length; i++) {
            DecField field = new DecField();
            fields[i] = field;
            field.flags = in.readUnsignedShort();
            field.name = (String) constpool.get(in.readUnsignedShort());
            field.desc = (String) constpool.get(in.readUnsignedShort());
            int attrcnt = in.readUnsignedShort();
            for (int j = 0; j < attrcnt; j++) {
                in.readUnsignedShort(); // Ignore names
                in.readFully(new byte[in.readInt()]); // Skip body
            }
        }
        methods = new DecMethod[in.readUnsignedShort()];
        for (int i = 0; i < methods.length; i++) {
            DecMethod method = new DecMethod();
            methods[i] = method;
            method.flags = in.readUnsignedShort();
            method.name = (String) constpool.get(in.readUnsignedShort());
            method.desc = (String) constpool.get(in.readUnsignedShort());
            int attrcnt = in.readUnsignedShort();
            for (int j = 0; j < attrcnt; j++) {
                in.readUnsignedShort(); // Ignore names
                in.readFully(new byte[in.readInt()]); // Skip body
            }
        }
        int attrcount = in.readUnsignedShort();
        for (int i = 0; i < attrcount; i++) {
            in.readUnsignedShort(); // Ignore names
            in.readFully(new byte[in.readInt()]); // Skip body
        }
        if (in.read() != -1) {
            throw new IOException("Class not over!");
        }
    }
}
