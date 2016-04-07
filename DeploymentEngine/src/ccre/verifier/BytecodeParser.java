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

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import ccre.drivers.ByteFiddling;
import ccre.verifier.ClassParser.CPInfo;
import ccre.verifier.ClassParser.ClassFile;
import ccre.verifier.ClassParser.ExceptionHandlerInfo;
import ccre.verifier.ClassParser.MethodInfo;

public class BytecodeParser {
    private final byte[] code;
    private final ExceptionHandlerInfo[] handlers;
    private final int[] block_begins;
    private final ClassFile cf;
    private final MethodInfo method;

    public BytecodeParser(ClassParser.MethodInfo method) throws ClassFormatException {
        if (method.code == null || method.handlers == null) {
            throw new IllegalArgumentException("Cannot use method without bytecode: " + method);
        }
        this.method = method;
        this.code = method.code;
        this.handlers = method.handlers;
        this.cf = method.declaringClass;
        this.block_begins = scanBlocks();
    }

    private int[] scanBlocks() throws ClassFormatException {
        ArrayList<Integer> blocks = new ArrayList<>();
        blocks.add(0);
        for (ExceptionHandlerInfo info : handlers) {
            if (!blocks.contains(info.handler_pc)) {
                blocks.add(info.handler_pc);
            }
        }
        for (int i = 0; i < blocks.size(); i++) {
            scanFromBlock(i, (i2) -> {
                if (!blocks.contains(i2)) {
                    blocks.add(i2);
                }
            });
        }
        int[] out = new int[blocks.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = blocks.get(i);
        }
        return out;
    }

    private void scanFromBlock(int i, Consumer<Integer> out) throws ClassFormatException {
        try {
            while (true) {
                int opcode_offset = i;
                int op = code[i++] & 0xFF;
                if (op >= 0x00 && op <= 0x0F) {
                    // single-byte simple constant instructions
                } else if (op >= 0x15 && op <= 0x19) {
                    i++; // variable load instructions
                } else if (op >= 0x1A && op <= 0x35) {
                    // single-byte fixed-load instructions and array-load
                    // instructions
                } else if (op >= 0x36 && op <= 0x3A) {
                    i++; // variable store instructions
                } else if (op >= 0x3B && op <= 0x83) {
                    // single-byte fixed-store instructions, array-store
                    // instructions, stack manipulation instructions, and math
                    // instructions
                } else if (op >= 0x85 && op <= 0x98) {
                    // single-byte conversion instructions and non-branching
                    // comparison instructions
                } else if ((op >= 0x99 && op <= 0xA7) || (op >= 0xC6 && op <= 0xC7)) {
                    // ^^^^^^^^^^^^--- INCLUDES ABNORMAL CONDITIONS
                    // conditional branch instructions, goto, jsr
                    int branchbyte1 = code[i++] & 0xFF;
                    int branchbyte2 = code[i++] & 0xFF;
                    out.accept(opcode_offset + (short) ((branchbyte1 << 8) | branchbyte2));
                    if (op == 0xA7) { // goto
                        return; // no more code in this block
                    }
                } else if (op >= 0xAC && op <= 0xB1) {
                    // method return instructions
                    return; // no more code in this block
                } else if (op >= 0xB2 && op <= 0xB8) {
                    // field access instructions and some invocation
                    // instructions
                    i += 2;
                } else if (op >= 0xB9 && op <= 0xBA) {
                    // some invocation instructions
                    i += 4;
                } else if (op >= 0xC0 && op <= 0xC1) {
                    i += 2;
                } else if (op >= 0xC2 && op <= 0xC3) {
                    // no arguments for these
                } else {
                    switch (op) {
                    case 0x10: // bipush
                    case 0x12: // ldc
                    case 0xBC: // newarray
                        i++;
                        break;
                    case 0x11: // sipush
                    case 0x13: // ldc_w
                    case 0x14: // ldc2_w
                    case 0x84: // iinc
                    case 0xBB: // new
                    case 0xBD: // anewarray
                        i += 2;
                        break;
                    case 0xA9: // ret
                        // this is a jsr ret, which returns to after the jsr,
                        // which we've already looked at
                        i++;
                        return; // no more code in this block
                    case 0xAA: { // tableswitch
                        while (i % 4 != 0) {
                            i++;
                        }
                        int default_int = ByteFiddling.asInt32BE(code, i);
                        int low_int = ByteFiddling.asInt32BE(code, i + 4);
                        int high_int = ByteFiddling.asInt32BE(code, i + 8);
                        i += 12;
                        out.accept(default_int + opcode_offset);
                        for (int j = low_int; j <= high_int; j++) {
                            out.accept(ByteFiddling.asInt32BE(code, i) + opcode_offset);
                            i += 4;
                        }
                        break;
                    }
                    case 0xAB: { // lookupswitch
                        while (i % 4 != 0) {
                            i++;
                        }
                        int default_int = ByteFiddling.asInt32BE(code, i);
                        int npairs_int = ByteFiddling.asInt32BE(code, i + 4);
                        i += 8;
                        out.accept(default_int + opcode_offset);
                        for (int j = 0; j < npairs_int; j++) {
                            // ignore match of ByteFiddling.asInt32BE(code, i)
                            out.accept(ByteFiddling.asInt32BE(code, i + 4) + opcode_offset);
                            i += 8;
                        }
                        break;
                    }
                    case 0xBE: // arraylength
                        // no arguments
                        break;
                    case 0xBF: // athrow
                        // no arguments; end block because throw
                        return;
                    case 0xC4: { // wide
                        op = code[i++] & 0xFF;
                        if ((op >= 0x15 && op <= 0x19) || (op >= 0x36 && op <= 0x3A) || op == 0xA9) {
                            // wide load or save, or ret
                            i += 2;
                            // in case of ret, nothing special needed because
                            // that was handled at the jsr
                        } else if (op == 0x84) {
                            i += 4;
                        } else {
                            throw new ClassFormatException("Invalid wide opcode in bytecode: " + op);
                        }
                        break;
                    }
                    case 0xC5: // multianewarray
                        i += 3;
                        break;
                    case 0xC8: // goto_w
                    case 0xC9: { // jsr_w
                        int branchbyte1 = code[i++] & 0xFF;
                        int branchbyte2 = code[i++] & 0xFF;
                        int branchbyte3 = code[i++] & 0xFF;
                        int branchbyte4 = code[i++] & 0xFF;
                        out.accept(((branchbyte1 << 24) | (branchbyte2 << 16) | (branchbyte3 << 8) | branchbyte4) + opcode_offset);
                        if (op == 0xC8) {
                            return;
                        }
                        break;
                    }
                    default:
                        throw new ClassFormatException("Invalid opcode in bytecode: " + op);
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ClassFormatException("Invalid bytecode", ex);
        }
    }

    public static class ReferenceInfo {
        public CPInfo target;
        public int bytecode_index;
        public MethodInfo method;

        public String getSourceFile() throws ClassFormatException {
            return method.declaringClass.getSourceFile();
        }

        public int getLineNumber() {
            return method.getLineNumberFor(bytecode_index);
        }
    }

    public ReferenceInfo[] getReferences() throws ClassFormatException {
        ArrayList<ReferenceInfo> out = new ArrayList<>();
        for (int block : block_begins) {
            this.getBlockReferences(block, out);
        }
        return out.toArray(new ReferenceInfo[out.size()]);
    }

    private void getBlockReferences(int i, Collection<ReferenceInfo> references) throws ClassFormatException {
        try {
            while (true) {
                int original_offset = i;
                int op = code[i++] & 0xFF;
                if (op >= 0x00 && op <= 0x0F) {
                    // single-byte simple constant instructions
                } else if (op >= 0x15 && op <= 0x19) {
                    i++; // variable load instructions
                } else if (op >= 0x1A && op <= 0x35) {
                    // single-byte fixed-load instructions and array-load
                    // instructions
                } else if (op >= 0x36 && op <= 0x3A) {
                    i++; // variable store instructions
                } else if (op >= 0x3B && op <= 0x83) {
                    // single-byte fixed-store instructions, array-store
                    // instructions, stack manipulation instructions, and math
                    // instructions
                } else if (op >= 0x85 && op <= 0x98) {
                    // single-byte conversion instructions and non-branching
                    // comparison instructions
                } else if ((op >= 0x99 && op <= 0xA6) || (op >= 0xC6 && op <= 0xC7)) {
                    i += 2;
                } else if (op >= 0xAC && op <= 0xB1) {
                    // method return instructions
                    return; // no more code in this block
                } else if (op >= 0xB2 && op <= 0xB5) {
                    // field access instructions
                    i += 2;
                } else if (op == 0xB6) {
                    int indexbyte1 = code[i++] & 0xFF;
                    int indexbyte2 = code[i++] & 0xFF;
                    int index = (indexbyte1 << 8) | indexbyte2;
                    ReferenceInfo refInfo = new ReferenceInfo();
                    refInfo.target = cf.getConst(index);
                    refInfo.target.requireTag(ClassParser.CONSTANT_Methodref);
                    refInfo.method = method;
                    refInfo.bytecode_index = original_offset;
                    references.add(refInfo);
                } else if (op >= 0xB7 && op <= 0xB8) {
                    int indexbyte1 = code[i++] & 0xFF;
                    int indexbyte2 = code[i++] & 0xFF;
                    int index = (indexbyte1 << 8) | indexbyte2;
                    ReferenceInfo refInfo = new ReferenceInfo();
                    refInfo.target = cf.getConst(index);
                    refInfo.target.requireTagOf(ClassParser.CONSTANT_Methodref, ClassParser.CONSTANT_InterfaceMethodref);
                    refInfo.method = method;
                    refInfo.bytecode_index = original_offset;
                    references.add(refInfo);
                } else if (op == 0xB9) {
                    int indexbyte1 = code[i++] & 0xFF;
                    int indexbyte2 = code[i++] & 0xFF;
                    int index = (indexbyte1 << 8) | indexbyte2;
                    ReferenceInfo refInfo = new ReferenceInfo();
                    refInfo.target = cf.getConst(index);
                    refInfo.target.requireTag(ClassParser.CONSTANT_InterfaceMethodref);
                    refInfo.method = method;
                    refInfo.bytecode_index = original_offset;
                    references.add(refInfo);
                    i += 2;
                } else if (op == 0xBA) {
                    int indexbyte1 = code[i++] & 0xFF;
                    int indexbyte2 = code[i++] & 0xFF;
                    int index = (indexbyte1 << 8) | indexbyte2;
                    ReferenceInfo refInfo = new ReferenceInfo();
                    refInfo.target = cf.getConst(index);
                    refInfo.target.requireTag(ClassParser.CONSTANT_InvokeDynamic);
                    refInfo.method = method;
                    refInfo.bytecode_index = original_offset;
                    references.add(refInfo);
                    i += 2;
                } else if (op >= 0xC0 && op <= 0xC1) {
                    i += 2;
                } else if (op >= 0xC2 && op <= 0xC3) {
                    // no arguments for these
                } else {
                    switch (op) {
                    case 0x10: // bipush
                    case 0x12: // ldc
                    case 0xBC: // newarray
                        i++;
                        break;
                    case 0x11: // sipush
                    case 0x13: // ldc_w
                    case 0x14: // ldc2_w
                    case 0x84: // iinc
                    case 0xBB: // new
                    case 0xBD: // anewarray
                        i += 2;
                        break;
                    case 0xA9: // ret
                        // this is a jsr ret, which returns to after the jsr,
                        // which we've already looked at
                        i++;
                        return; // no more code in this block
                    case 0xAA: { // tableswitch
                        while (i % 4 != 0) {
                            i++;
                        }
                        int low_int = ByteFiddling.asInt32BE(code, i + 4);
                        int high_int = ByteFiddling.asInt32BE(code, i + 8);
                        i += 16 + 4 * (high_int - low_int);
                        break;
                    }
                    case 0xAB: { // lookupswitch
                        while (i % 4 != 0) {
                            i++;
                        }
                        int npairs_int = ByteFiddling.asInt32BE(code, i + 4);
                        i += 8 + 8 * npairs_int;
                        break;
                    }
                    case 0xBE: // arraylength
                        // no arguments
                        break;
                    case 0xA7: // goto
                    case 0xBF: // athrow
                        // no arguments; end block because throw
                        return;
                    case 0xC4: { // wide
                        op = code[i++] & 0xFF;
                        if ((op >= 0x15 && op <= 0x19) || (op >= 0x36 && op <= 0x3A) || op == 0xA9) {
                            // wide load or save, or ret
                            i += 2;
                            // in case of ret, nothing special needed because
                            // that was handled at the jsr
                        } else if (op == 0x84) {
                            i += 4;
                        } else {
                            throw new ClassFormatException("Invalid wide opcode in bytecode: " + op);
                        }
                        break;
                    }
                    case 0xC5: // multianewarray
                        i += 3;
                        break;
                    case 0xC8: // goto_w
                        i += 4;
                        return;
                    case 0xC9: { // jsr_w
                        i += 4;
                        break;
                    }
                    default:
                        throw new ClassFormatException("Invalid opcode in bytecode: " + op);
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ClassFormatException("Invalid bytecode", ex);
        }
    }
}
