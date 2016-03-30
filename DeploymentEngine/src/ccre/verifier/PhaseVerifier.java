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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import ccre.deployment.Artifact;
import ccre.log.Logger;
import ccre.verifier.ClassParser.ClassFile;
import ccre.verifier.ClassParser.ClassFormatException;
import ccre.verifier.ClassParser.MethodInfo;
import ccre.verifier.ClassParser.TypeInfo;

public class PhaseVerifier {
    private final Artifact artifact;
    private final HashMap<MethodInfo, Phase> known = new HashMap<>();
    private final HashMap<String, ClassFile> loaded = new HashMap<>();
    private int warnings;

    private PhaseVerifier(Artifact artifact) {
        this.artifact = artifact;
    }

    public static void verify(Artifact artifact) throws ClassNotFoundException, ClassFormatException {
        int warnings = new PhaseVerifier(artifact).verifyAll();
        Logger.warning("Found " + warnings + " warnings during phase verification.");
    }

    private Phase getPhase(MethodInfo method) throws ClassNotFoundException, ClassFormatException {
        if (known.containsKey(method)) {
            return known.get(method);
        }
        Phase phase = getDeclaredPhase(method);
        MethodInfo superMethod = getMethodSuperclassMatch(method);
        if (superMethod == null && method.isAnnotationPresent(Override.class.getName())) {
            warn("@Override method does not actually override superclass method");
        }
        Phase superPhase = superMethod != null ? getPhase(superMethod) : null;
        if (phase != null && superPhase != null && phase != superPhase) {
            warn("Mismatched phase between method and overridden method");
        }
        known.put(method, phase);
        return phase;
    }

    private MethodInfo getMethodSuperclassMatch(MethodInfo m) throws ClassNotFoundException {
        ClassFile c = getSuperClass(m.declaringClass);
        while (c != null) {
            MethodInfo info = getDeclaredMethod(c, m.name, m.parameters);
            if (info != null) {
                return info;
            }
            c = getSuperClass(c);
        }
        return null;
    }

    private ClassFile getSuperClass(ClassFile c) throws ClassNotFoundException {
        return c.super_class == null ? null : loadClass(c.super_class);
    }

    private MethodInfo getDeclaredMethod(ClassFile c, String name, TypeInfo[] parameters) throws ClassNotFoundException {
        MethodInfo result = null;
        for (MethodInfo m : c.methods) {
            if (m.name.equals(name) && Arrays.equals(m.parameters, parameters) && (result == null || isSuperTypeOf(result.returnType, m.returnType))) {
                result = m;
            }
        }
        return result;
    }

    private boolean isSuperTypeOf(TypeInfo sup, TypeInfo sub) throws ClassNotFoundException {
        if (sup.equals(sub)) {
            return true;
        } else if (sup.isArray()) {
            return sub.isArray() && isSuperTypeOf(sup.element, sub.element);
        } else if (sub.isArray()) {
            return sup.isClass && sup.name.equals("java/lang/Object");
        } else if (sup.isClass && sub.isClass) {
            return isSuperOrSameClassOf(loadClass(sup.name), getSuperClass(loadClass(sub.name)));
        } else {
            return false;
        }
    }

    private boolean isSuperOrSameClassOf(ClassFile sup, ClassFile sub) throws ClassNotFoundException {
        if (sup.equals(sub)) {
            return true;
        } else if (sub.super_class == null) {
            return false;
        } else {
            return isSuperOrSameClassOf(sup, getSuperClass(sub));
        }
    }

    private ClassFile loadClass(String class_) throws ClassNotFoundException {
        if (class_.indexOf('/') != -1) {
            throw new IllegalArgumentException("Classes cannot contain slashes!");
        }
        ClassFile cf = loaded.get(class_);
        if (cf == null) {
            try {
                cf = ClassParser.parse(artifact.loadClassFile(class_));
                if (!cf.this_class.equals(class_.replace('.', '/'))) {
                    throw new ClassNotFoundException("Could not load class due to mismatched names!");
                }
            } catch (IOException e) {
                throw new ClassNotFoundException("Could not load class: " + class_, e);
            }
            loaded.put(class_, cf);
        }
        return cf;
    }

    private Phase getDeclaredPhase(MethodInfo m) throws ClassFormatException {
        Phase found = null;
        for (Phase p : Phase.values()) {
            if (m.isAnnotationPresent(p.annot.getName())) {
                if (found == null) {
                    found = p;
                } else {
                    warn("Multiple phases declared on method: " + m);
                }
            }
        }
        if (found == null) {
            // TODO: maybe warn?
        }
        return found;
    }

    private int verifyAll() throws ClassNotFoundException, ClassFormatException {
        warnings = 0;
        for (String className : artifact.listClassNames()) {
            verify(className);
        }
        return warnings;
    }

    private void verify(String className) throws ClassNotFoundException, ClassFormatException {
        verify(loadClass(className));
    }

    private void verify(ClassFile cls) throws ClassNotFoundException, ClassFormatException {
        for (MethodInfo m : cls.methods) {
            verify(m);
        }
    }

    private void verify(MethodInfo m) throws ClassNotFoundException, ClassFormatException {
        Phase p = getPhase(m);
        if (p == null) {
            warn("No phase declared on method: " + m);
        } else {
            for (MethodInfo target : enumerateReferences(m)) {
                Phase tp = getPhase(target);
                if (tp != null && tp != p) {
                    warn("Phase mismatch between method and called method: calls " + tp + " but is " + p);
                }
            }
        }
    }

    private MethodInfo[] enumerateReferences(MethodInfo m) {
//        byte[] bytecode = m.getCode();
        return null; // TODO
    }

    private void warn(String string) {
        warnings++;
        Logger.warning("[VERIFIER] " + string);
    }
}
