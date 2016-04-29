/*
 * Copyright 2016 Cel Skeggs.
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
package ccre.verifier;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import ccre.drivers.ByteFiddling;
import ccre.log.Logger;
import ccre.verifier.BytecodeParser.ReferenceInfo;

class ClassParser extends DataInputStream {

    public static final int ACC_PUBLIC = 0x0001,
            ACC_PRIVATE = 0x0002,
            ACC_PROTECTED = 0x0004,
            ACC_STATIC = 0x0008,
            ACC_FINAL = 0x0010,
            ACC_SUPER = 0x0020, // reused
            ACC_SYNCHRONIZED = 0x0020, // reuse
            ACC_VOLATILE = 0x0040, // reused
            ACC_BRIDGE = 0x0040, // reuse
            ACC_TRANSIENT = 0x0080, // reused
            ACC_VARARGS = 0x0080, // reuse
            ACC_NATIVE = 0x0100,
            ACC_INTERFACE = 0x0200,
            ACC_ABSTRACT = 0x0400,
            ACC_STRICT = 0x0800,
            ACC_SYNTHETIC = 0x1000,
            ACC_ANNOTATION = 0x2000,
            ACC_ENUM = 0x4000;
    public static final int CONSTANT_Class = 7,
            CONSTANT_Fieldref = 9,
            CONSTANT_Methodref = 10,
            CONSTANT_InterfaceMethodref = 11,
            CONSTANT_String = 8,
            CONSTANT_Integer = 3,
            CONSTANT_Float = 4,
            CONSTANT_Long = 5,
            CONSTANT_Double = 6,
            CONSTANT_NameAndType = 12,
            CONSTANT_Utf8 = 1,
            CONSTANT_MethodHandle = 15,
            CONSTANT_MethodType = 16,
            CONSTANT_InvokeDynamic = 18;

    public static enum ClassFormatVersion {
        JAVA_8
    }

    public static class CPInfo {
        public CPInfo[] pool;
        public int tag;

        // name_index, class_index, string_index, constant_int, constant_float,
        // reference_kind, bootstrap_method_attr_index
        public int alpha;
        // name_and_type_index, descriptor_index, reference_index
        public int beta;
        // bytes (when int, float)
        public long u64;
        // bytes (when Utf8)
        public String bytes;

        @Override
        public String toString() {
            return "[" + tag + "] " + alpha + " " + beta + ": " + u64 + " " + bytes;
        }

        public String asClass() throws ClassFormatException {
            this.requireTag(CONSTANT_Class);
            String class_name = this.getConst(this.alpha).asUTF8();
            if (class_name.indexOf('.') != -1) {
                throw new ClassFormatException("Binary class name should not include dots!");
            }
            return class_name.replace('/', '.');
        }

        public String asUTF8() throws ClassFormatException {
            this.requireTag(CONSTANT_Utf8);
            return this.bytes;
        }

        private CPInfo getConst(int i) {
            if (i == 0) {
                throw new IllegalArgumentException("Zeroth index of constant pool is reserved.");
            }
            return pool[i];
        }

        public void requireTag(int tag) throws ClassFormatException {
            if (this.tag != tag) {
                throw new ClassFormatException("Expected a tag of " + tag + " but got " + this.tag + "!");
            }
        }

        public void requireTagOf(int... tags) throws ClassFormatException {
            for (int tag : tags) {
                if (tag == this.tag) {
                    return; // success
                }
            }
            throw new ClassFormatException("Expected a tag in " + Arrays.toString(tags) + " but got " + this.tag + "!");
        }
    }

    public static class FieldInfo {
        public ClassFile declaringClass;
        public int access;
        public String name;
        public String descriptor;
        public AttributeInfo[] attributes;
    }

    public static class MethodInfo {
        public ClassFile declaringClass;
        public int access;
        public String name;
        public String descriptor;
        public AttributeInfo[] attributes;
        // calculated from descriptor
        public TypeInfo[] parameters;
        public TypeInfo returnType;
        public byte[] code;
        public ExceptionHandlerInfo[] handlers;
        public int[] linenumtable;

        @Override
        public String toString() {
            return declaringClass + "." + name + descriptor;
        }

        public void fillOutContents() throws ClassFormatException {
            parameters = parseMethodDescriptorArguments(descriptor);
            returnType = parseMethodDescriptorReturnType(descriptor);
            byte[] attr = getAttribute("Code");
            code = null;
            handlers = null;
            if (attr != null) {
                try {
                    ByteArrayDataInput din = new ByteArrayDataInput(attr);
                    // ignore max_stack and max_locals
                    din.readUnsignedShort();
                    din.readUnsignedShort();
                    byte[] code = new byte[din.readInt()];
                    din.readFully(code);
                    ExceptionHandlerInfo[] handlers = new ExceptionHandlerInfo[din.readUnsignedShort()];
                    for (int i = 0; i < handlers.length; i++) {
                        ExceptionHandlerInfo info = new ExceptionHandlerInfo();
                        info.start_pc = din.readUnsignedShort();
                        info.end_pc = din.readUnsignedShort();
                        info.handler_pc = din.readUnsignedShort();
                        int cti = din.readUnsignedShort();
                        info.catch_type = cti == 0 ? null : this.declaringClass.getConst(cti).asClass();
                        handlers[i] = info;
                    }
                    AttributeInfo[] info = new AttributeInfo[din.readUnsignedShort()];
                    for (int i = 0; i < info.length; i++) {
                        info[i] = new AttributeInfo();
                        info[i].name = this.declaringClass.getConst(din.readUnsignedShort()).asUTF8();
                        info[i].bytes = new byte[din.readInt()];
                        din.readFully(info[i].bytes);
                    }
                    if (!din.isEOF()) {
                        throw new ClassFormatException("Junk found at end of Code attribute!");
                    }
                    int[] linenumtable = null;
                    for (AttributeInfo in : info) {
                        if (in.name.equals("LineNumberTable")) {
                            if (in.bytes.length < 2) {
                                throw new ClassFormatException("Incorrectly-sized LineNumberTable!");
                            }
                            int line_number_table_length = ByteFiddling.asInt16BE(in.bytes, 0);
                            if (in.bytes.length - 2 != line_number_table_length * 4) {
                                throw new ClassFormatException("Incorrectly-sized LineNumberTable!");
                            }
                            linenumtable = new int[line_number_table_length * 2];
                            for (int i = 0; i < linenumtable.length; i++) {
                                linenumtable[i] = ByteFiddling.asInt16BE(in.bytes, 2 + i * 2);
                            }
                            break;
                        }
                    }
                    this.code = code;
                    this.handlers = handlers;
                    this.linenumtable = linenumtable;
                } catch (EOFException ex) {
                    throw new ClassFormatException("Code parser reached the end of the code attribute!", ex);
                }
            }
        }

        public byte[] getAttribute(String name) {
            for (AttributeInfo info : attributes) {
                if (name.equals(info.name)) {
                    return info.bytes;
                }
            }
            return null;
        }

        public boolean isAnnotationPresent(String name) throws ClassFormatException {
            if (name.contains("/")) {
                throw new IllegalArgumentException("Annotation name must contain only slashes; no dots.");
            }
            for (String attr : new String[] { "RuntimeVisibleAnnotations", "RuntimeInvisibleAnnotations" }) {
                byte[] attrs = getAttribute(attr);
                if (attrs == null) {
                    continue;
                }
                try {
                    ByteArrayDataInput din = new ByteArrayDataInput(attrs);
                    int num_annotations = din.readUnsignedShort();
                    for (int i = 0; i < num_annotations; i++) {
                        String desc = parseAnnotationInfo(din);
                        TypeInfo type = parseFieldDescriptor(desc);
                        if (!type.isClass) {
                            throw new ClassFormatException("Expected annotations to be of annotation types!");
                        }
                        if (name.equals(type.name)) {
                            return true;
                        }
                    }
                } catch (EOFException ex) {
                    throw new ClassFormatException("Annotation parser reached the end of the attribute!", ex);
                }
            }
            return false;
        }

        private String parseAnnotationInfo(ByteArrayDataInput din) throws ClassFormatException, EOFException {
            String type = declaringClass.getConst(din.readUnsignedShort()).asUTF8();
            int num_element_value_pairs = din.readUnsignedShort();
            for (int j = 0; j < num_element_value_pairs; j++) {
                String element_name = declaringClass.getConst(din.readUnsignedShort()).asUTF8();
                // discard element_name
                discardElementValue(din);
            }
            return type;
        }

        private void discardElementValue(ByteArrayDataInput din) throws EOFException, ClassFormatException {
            int tag = din.readUnsignedByte();
            switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
            case 'c':
                din.readUnsignedShort(); // and discard it
                break;
            case 'e':
                din.readUnsignedShort();
                din.readUnsignedShort(); // and discard them
                break;
            case '[':
                int count = din.readUnsignedShort();
                for (int i = 0; i < count; i++) {
                    discardElementValue(din);
                }
                break;
            case '@':
                parseAnnotationInfo(din); // and discard its
            }
        }

        public int getLineNumberFor(int bytecode_index) {
            if (linenumtable == null) {
                return 0;
            }
            int found_from = -1, line_number = 0;
            for (int i = 0; i < linenumtable.length; i += 2) {
                int start_pc = linenumtable[i], line_no = linenumtable[i + 1];
                if (bytecode_index >= start_pc && start_pc > found_from) {
                    found_from = start_pc;
                    line_number = line_no;
                }
            }
            return line_number;
        }

        public boolean isGetter() {
            // TODO: do this better?
            // Look for: aload_0, getfield, *return (except just return)
            if (this.code == null) {
                return false;
            } else if (this.code.length == 4) {
                return (this.code[0] & 0xFF) == 0xB2 && (this.code[3] & 0xFF) >= 0xAC && (this.code[3] & 0xFF) <= 0xB0;
            } else if (this.code.length == 5) {
                return (this.code[0] & 0xFF) == 0x2A && (this.code[1] & 0xFF) == 0xB4 && (this.code[4] & 0xFF) >= 0xAC && (this.code[4] & 0xFF) <= 0xB0;
            } else {
                return false;
            }
        }

        public boolean isSolitary() throws ClassFormatException {
            return this.code != null && new BytecodeParser(this).getReferenceCount() == 0;
        }

        public ReferenceInfo getOneRef() throws ClassFormatException {
            BytecodeParser bytecode = new BytecodeParser(this);
            if (this.code == null || bytecode.getReferenceCount() != 1) {
                return null;
            }
            ReferenceInfo[] ri = bytecode.getReferences();
            if (ri.length == 1) {
                return ri[0];
            }
            return null;
        }
    }

    public static class TypeInfo {
        public static final TypeInfo VOID = new TypeInfo(false, "void", null);
        public static final TypeInfo BYTE = new TypeInfo(false, "byte", null), CHAR = new TypeInfo(false, "char", null);
        public static final TypeInfo DOUBLE = new TypeInfo(false, "double", null), FLOAT = new TypeInfo(false, "float", null);
        public static final TypeInfo INT = new TypeInfo(false, "int", null), LONG = new TypeInfo(false, "long", null);
        public static final TypeInfo SHORT = new TypeInfo(false, "short", null), BOOL = new TypeInfo(false, "boolean", null);

        public final boolean isClass;
        public final String name; // contains no slashes, just dots
        public final TypeInfo element;

        private TypeInfo(boolean isClass, String name, TypeInfo element) {
            if (name.contains("/")) {
                throw new IllegalArgumentException("Invalid TypeInfo");
            }
            this.isClass = isClass;
            this.name = name;
            this.element = element;
        }

        public static TypeInfo getArrayOf(TypeInfo element) {
            return new TypeInfo(false, element.name + "[]", element);
        }

        public static TypeInfo getClassFor(String name) {
            return new TypeInfo(true, name, null);
        }

        public boolean isArray() {
            return element != null;
        }

        @Override
        public String toString() {
            return "[type " + name + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, isClass, element);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeInfo) {
                TypeInfo ti = (TypeInfo) obj;
                return Objects.equals(name, ti.name) && isClass == ti.isClass && Objects.equals(element, ti.element);
            } else {
                return false;
            }
        }
    }

    private static int scanMethodDescriptor(String descriptor) throws ClassFormatException {
        if (descriptor.charAt(0) != '(') {
            throw new ClassFormatException("Invalid method descriptor: missing opening paren");
        }
        int close = descriptor.indexOf(')');
        if (close == -1) {
            throw new ClassFormatException("Invalid method descriptor: missing closing paren");
        }
        if (descriptor.indexOf('(', 1) != -1 || descriptor.indexOf(')', close + 1) != -1) {
            throw new ClassFormatException("Invalid method descriptor: too many parens");
        }
        return close;
    }

    public static TypeInfo parseMethodDescriptorReturnType(String descriptor) throws ClassFormatException {
        int close = scanMethodDescriptor(descriptor);
        String ret = descriptor.substring(close + 1);
        if (ret.isEmpty()) {
            throw new ClassFormatException("Invalid method descriptor: no return type");
        }
        if (ret.charAt(0) == 'V') {
            if (ret.length() > 1) {
                throw new ClassFormatException("Invalid method descriptor: return type has garbage at the end");
            }
            return TypeInfo.VOID;
        } else {
            ArrayList<TypeInfo> list = new ArrayList<>();
            if (parseTypeDescriptor(ret, list) != ret.length()) {
                throw new ClassFormatException("Invalid method descriptor: return type has garbage at the end");
            }
            if (list.size() != 1) {
                throw new RuntimeException("Oops... that really shouldn't have happened.");
            }
            return list.get(0);
        }
    }

    public static TypeInfo[] parseMethodDescriptorArguments(String descriptor) throws ClassFormatException {
        int close = scanMethodDescriptor(descriptor);
        ArrayList<TypeInfo> list = new ArrayList<>();
        for (int i = 1; i < close;) {
            i += parseTypeDescriptor(descriptor.substring(i, close), list);
        }
        return list.toArray(new TypeInfo[list.size()]);
    }

    public static TypeInfo parseFieldDescriptor(String descriptor) throws ClassFormatException {
        ArrayList<TypeInfo> arr = new ArrayList<>(); // not a good way to do it
                                                     // TODO: refactor
        if (parseTypeDescriptor(descriptor, arr) != descriptor.length()) {
            throw new ClassFormatException("Did not consume entire field descriptor!");
        }
        if (arr.size() != 1) {
            throw new RuntimeException("Oops... that really shouldn't have happened.");
        }
        return arr.get(0);
    }

    // returns the number of consumed bytes
    private static int parseTypeDescriptor(String descriptor, ArrayList<TypeInfo> out) throws ClassFormatException {
        switch (descriptor.charAt(0)) {
        case 'B':
            out.add(TypeInfo.BYTE);
            return 1;
        case 'C':
            out.add(TypeInfo.CHAR);
            return 1;
        case 'D':
            out.add(TypeInfo.DOUBLE);
            return 1;
        case 'F':
            out.add(TypeInfo.FLOAT);
            return 1;
        case 'I':
            out.add(TypeInfo.INT);
            return 1;
        case 'J':
            out.add(TypeInfo.LONG);
            return 1;
        case 'S':
            out.add(TypeInfo.SHORT);
            return 1;
        case 'Z':
            out.add(TypeInfo.BOOL);
            return 1;
        case '[':
            int prev = parseTypeDescriptor(descriptor.substring(1), out);
            out.set(out.size() - 1, TypeInfo.getArrayOf(out.get(out.size() - 1)));
            return prev + 1;
        case 'L':
            int last = descriptor.indexOf(';');
            if (descriptor.indexOf('.') != -1) {
                throw new ClassFormatException("Did not expect dots in type descriptor.");
            }
            out.add(TypeInfo.getClassFor(descriptor.substring(1, last).replace('/', '.')));
            return last + 1;
        default:
            throw new ClassFormatException("Invalid type descriptor: invalid descriptor header " + descriptor.charAt(0));
        }
    }

    public static class AttributeInfo {
        public String name;
        public byte[] bytes;
    }

    public static class ExceptionHandlerInfo {
        public int start_pc, end_pc, handler_pc;
        public String catch_type;
    }

    public static class ClassFile {
        public ClassFormatVersion version;
        public CPInfo[] constant_pool;
        public int access;
        public String this_class;
        public String super_class;
        public String[] interfaces;
        public FieldInfo[] fields;
        public MethodInfo[] methods;
        public AttributeInfo[] attributes;

        public CPInfo getConst(int i) {
            if (i == 0) {
                throw new IllegalArgumentException("Zeroth index of constant pool is reserved.");
            }
            return constant_pool[i];
        }

        public String getSourceFile() throws ClassFormatException {
            for (AttributeInfo info : attributes) {
                if (info.name.equals("SourceFile")) {
                    if (info.bytes.length < 2) {
                        throw new ClassFormatException("SourceFile attribute is too short!");
                    }
                    int sourcefile_index = ByteFiddling.asInt16BE(info.bytes, 0);
                    return this.getConst(sourcefile_index).asUTF8();
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "[class " + this_class + "]";
        }
    }

    public ClassParser(InputStream input) {
        super(input);
    }

    public ClassFile readClassFile() throws IOException {
        ClassFile file = new ClassFile();
        file.version = readClassHeader();
        file.constant_pool = readConstantPool();
        file.access = readUnsignedShort();
        file.this_class = readConstant(file).asClass();
        int superIndex = readUnsignedShort();
        file.super_class = superIndex == 0 ? null : file.getConst(superIndex).asClass();
        file.interfaces = readInterfaces(file);
        file.fields = readFields(file);
        file.methods = readMethods(file);
        file.attributes = readAttributes(file);
        return file;
    }

    // note: consumes a byte if wrong
    public void requireEOF() throws IOException {
        if (this.read() != -1) {
            throw new IOException("Not at EOF, as expected!");
        }
    }

    public FieldInfo[] readFields(ClassFile file) throws IOException {
        FieldInfo[] out = new FieldInfo[readUnsignedShort()];
        for (int i = 0; i < out.length; i++) {
            out[i] = readField(file);
        }
        return out;
    }

    public FieldInfo readField(ClassFile file) throws IOException {
        FieldInfo info = new FieldInfo();
        info.declaringClass = file;
        info.access = readUnsignedShort();
        info.name = readConstant(file).asUTF8();
        info.descriptor = readConstant(file).asUTF8();
        info.attributes = readAttributes(file);
        return info;
    }

    public MethodInfo[] readMethods(ClassFile file) throws IOException {
        MethodInfo[] out = new MethodInfo[readUnsignedShort()];
        for (int i = 0; i < out.length; i++) {
            out[i] = readMethod(file);
        }
        return out;
    }

    public MethodInfo readMethod(ClassFile file) throws IOException {
        MethodInfo info = new MethodInfo();
        info.declaringClass = file;
        info.access = readUnsignedShort();
        info.name = readConstant(file).asUTF8();
        info.descriptor = readConstant(file).asUTF8();
        info.attributes = readAttributes(file);
        info.fillOutContents();
        return info;
    }

    public AttributeInfo[] readAttributes(ClassFile file) throws IOException {
        AttributeInfo[] info = new AttributeInfo[readUnsignedShort()];
        for (int i = 0; i < info.length; i++) {
            info[i] = readAttribute(file);
        }
        return info;
    }

    public AttributeInfo readAttribute(ClassFile file) throws IOException {
        AttributeInfo info = new AttributeInfo();
        info.name = readConstant(file).asUTF8();
        info.bytes = new byte[readInt()];
        readFully(info.bytes);
        return info;
    }

    public String[] readInterfaces(ClassFile file) throws IOException {
        String[] out = new String[readUnsignedShort()];
        for (int i = 0; i < out.length; i++) {
            out[i] = readConstant(file).asClass();
        }
        return out;
    }

    public CPInfo readConstant(ClassFile file) throws IOException {
        return file.getConst(readUnsignedShort());
    }

    public CPInfo readNullableConstant(ClassFile file) throws IOException {
        int c = readUnsignedShort();
        return c == 0 ? null : file.getConst(c);
    }

    public ClassFormatVersion readClassHeader() throws IOException {
        if (readInt() != 0xCAFEBABE) {
            throw new ClassFormatException("Invalid magic number");
        }
        int minor = readUnsignedShort();
        int major = readUnsignedShort();
        if (minor != 0 || major != 0x34) {
            throw new ClassFormatException("Unsupported class version: " + major + "." + minor);
        }
        return ClassFormatVersion.JAVA_8;
    }

    public CPInfo[] readConstantPool() throws IOException {
        CPInfo[] info = new CPInfo[readUnsignedShort()];
        // we skip over info[0] because that's not really a thing according to
        // Java.
        for (int i = 1; i < info.length; i++) {
            info[i] = readConstantPoolEntry();
            info[i].pool = info;
            if (info[i].tag == CONSTANT_Long || info[i].tag == CONSTANT_Double) {
                i++; // double-wide
            }
        }
        return info;
    }

    public CPInfo readConstantPoolEntry() throws IOException {
        CPInfo info = new CPInfo();
        info.tag = readUnsignedByte();
        switch (info.tag) {
        case CONSTANT_Class:
        case CONSTANT_String:
            info.alpha = readUnsignedShort();
            break;
        case CONSTANT_Fieldref:
        case CONSTANT_Methodref:
        case CONSTANT_InterfaceMethodref:
        case CONSTANT_NameAndType:
        case CONSTANT_InvokeDynamic:
            info.alpha = readUnsignedShort();
            info.beta = readUnsignedShort();
            break;
        case CONSTANT_Integer:
        case CONSTANT_Float:
            info.alpha = readInt();
            break;
        case CONSTANT_Long:
        case CONSTANT_Double:
            info.u64 = readLong();
            break;
        case CONSTANT_Utf8:
            info.bytes = readUTF();
            break;
        case CONSTANT_MethodHandle:
            info.alpha = readUnsignedByte();
            info.beta = readUnsignedShort();
            break;
        case CONSTANT_MethodType:
            // yes, this is supposed to be beta. because the same
            // descriptor_index mapping as another method.
            info.beta = readUnsignedShort();
            break;
        default:
            throw new ClassFormatException("Constant pool type not understood: " + info.tag);
        }
        return info;
    }

    public static ClassFile parse(InputStream input) throws IOException {
        try (ClassParser parser = new ClassParser(input)) {
            ClassFile f = parser.readClassFile();
            parser.requireEOF();
            return f;
        }
    }
}
