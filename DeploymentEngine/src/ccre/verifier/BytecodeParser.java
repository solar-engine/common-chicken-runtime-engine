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
import java.util.function.Consumer;

import ccre.drivers.ByteFiddling;

public class BytecodeParser {
    private final byte[] code;
    private final int[] block_begins;

    public BytecodeParser(byte[] code) {
        this.code = code;
        this.block_begins = scanBlocks();
    }

    private int[] scanBlocks() {
        ArrayList<Integer> blocks = new ArrayList<>();
        blocks.add(0);
        for (int i = 0; i < blocks.size(); i++) {
            scanFromBlock(i, (i2) -> {
                if (!blocks.contains(i2)) {
                    blocks.add(i2);
                }
            });
        }
        return null;
    }

    private void scanFromBlock(int i, Consumer<Integer> out) {
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
            } else if (op >= 0x99 && op <= 0xA7) {
                // conditional branch instructions, goto, jsr
                int branchbyte1 = code[i++] & 0xFF;
                int branchbyte2 = code[i++] & 0xFF;
                out.accept((branchbyte1 << 8) | branchbyte2);
                if (op == 0xA7) { // goto
                    return; // no more code in this block
                }
            } else if (op >= 0xAC && op <= 0xB1) {
                // method return instructions
                return; // no more code in this block
            } else if (op >= 0xB2 && op <= 0xB8) {
                // field access instructions and some invocation instructions
                i += 2;
            } else if (op >= 0xB9 && op <= 0xBA) {
                // some invocation instructions
                i += 4;
            } else {
                switch (op) {
                case 0x10: // bipush
                case 0x12: // ldc
                    i++;
                    break;
                case 0x11: // sipush
                case 0x13: // ldc_w
                case 0x14: // ldc2_w
                case 0x84: // iinc
                    i += 2;
                    break;
                case 0xA9: // ret
                    // this is a jsr ret, which returns to after the jsr, which
                    // we've already looked at
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
                case 0xAB: {// lookupswitch
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
                
                }
            }
        }
    }
}
