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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ccre.deployment.Artifact;
import ccre.deployment.Jar;
import ccre.log.Logger;
import ccre.storage.StorageSegment;
import ccre.verifier.BytecodeParser.ReferenceInfo;
import ccre.verifier.ClassParser.CPInfo;
import ccre.verifier.ClassParser.ClassFile;
import ccre.verifier.ClassParser.MethodInfo;
import ccre.verifier.ClassParser.TypeInfo;

/**
 * A tool that can verify that a chunk of code does not include any phase
 * mismatches that could lead to obscure robot code issues.
 *
 * @author skeggsc
 */
public class PhaseVerifier {
    private static final HashMap<String, Phase> externals = new HashMap<>();
    static {
        try (InputStream phin = PhaseVerifier.class.getResourceAsStream("java_phases.properties")) {
            HashMap<String, String> loaded = new HashMap<>();
            StorageSegment.loadProperties(phin, false, loaded);
            for (Map.Entry<String, String> ent : loaded.entrySet()) {
                externals.put(ent.getKey(), Phase.valueOf(ent.getValue().toUpperCase()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        PhaseVerifier.verify(new Jar(new File("../CommonChickenRuntimeEngine/CCRE.jar")));
    }

    private final Artifact artifact;
    private final HashMap<MethodInfo, Phase> known = new HashMap<>();
    private final HashMap<String, ClassFile> loaded = new HashMap<>();
    private int warnings;
    private final Artifact[] deps;

    private PhaseVerifier(Artifact artifact, Artifact[] deps) {
        this.artifact = artifact;
        this.deps = deps;
    }

    /**
     * Verifies that all classes in <code>target</code> are properly phased.
     * Warnings will be logged for every mistake.
     *
     * @param target the classes to verify.
     * @param deps all possible non-built-in dependencies of the classes.
     */
    public static void verify(Artifact target, Artifact... deps) {
        int warnings = new PhaseVerifier(target, deps).verifyAll();
        Logger.warning("Found " + warnings + " warnings during phase verification.");
    }

    private Phase getPhase(MethodInfo method) throws ClassNotFoundException, ClassFormatException {
        if (method == null) {
            throw new NullPointerException();
        }
        if (known.containsKey(method)) {
            return known.get(method);
        }
        Phase phase = getDeclaredPhase(method);
        if (!"<init>".equals(method.name)) {
            MethodInfo[] superMethods = getMethodSuperMatches(method);
            // TODO: handle the case of external overridden target
            if (superMethods.length == 0 && method.isAnnotationPresent(Override.class.getName())) {
                warn(method.declaringClass.getSourceFile(), method.getLineNumberFor(0) - 1, "@Override method does not actually override superclass method");
            }
            for (MethodInfo superMethod : superMethods) {
                Phase superPhase = getPhase(superMethod);
                if (superPhase == null) {
                    continue;
                }
                if (phase == null) {
                    phase = superPhase;
                } else if (phase != superPhase && phase != Phase.IGNORED) {
                    warn(method.declaringClass.getSourceFile(), method.getLineNumberFor(0) - 1, "Mismatched phase between method and overridden method: " + method + " overrides " + superMethod);
                    // TODO: update output phase
                }
            }
        }
        if (phase == null) {
            for (MethodInfo mi : method.declaringClass.methods) {
                if ((mi.access & ClassParser.ACC_BRIDGE) != 0 && mi.name.equals(method.name) && mi.parameters.length == method.parameters.length) {
                    BytecodeParser bcp = new BytecodeParser(mi);
                    ReferenceInfo[] refs = bcp.getReferences();
                    if (refs.length == 1) {
                        ReferenceInfo ri = refs[0];
                        MethodInfo target = getMethodRef(mi, ri.target);
                        if (target == method) {
                            phase = getPhase(mi);
                            break;
                        }
                    }
                }
            }
            if (phase == null && method.isSolitary()) {
                phase = Phase.IGNORED;
            }
            if (phase == null && (method.access & ClassParser.ACC_SYNTHETIC) != 0 && method.name.startsWith("access$")) {
                ReferenceInfo ref = method.getOneRef();
                if (ref != null) {
                    MethodInfo target = getMethodRef(method, ref.target);
                    if (target != method) {
                        phase = getPhase(target);
                    }
                }
            }
        }
        known.put(method, phase);
        return phase;
    }

    private MethodInfo[] getMethodSuperMatches(MethodInfo m) throws ClassNotFoundException {
        ClassFile c = getSuperClass(m.declaringClass);
        ArrayList<MethodInfo> mis = new ArrayList<>();
        while (c != null) {
            MethodInfo info = getDeclaredMethod(c, m.name, m.parameters);
            if (info != null) {
                mis.add(info);
            }
            c = getSuperClass(c);
        }
        for (ClassFile ci : getAllSuperInterfaces(m.declaringClass)) {
            MethodInfo info = this.getDeclaredMethod(ci, m.name, m.parameters);
            if (info != null) {
                mis.add(info);
            }
        }
        return mis.toArray(new MethodInfo[mis.size()]);
    }

    private ClassFile getSuperClass(ClassFile c) throws ClassNotFoundException {
        return c.super_class == null ? null : loadClass(c.super_class);
    }

    private MethodInfo getProvidedMethod(ClassFile clas, String name, TypeInfo[] parameters) throws ClassNotFoundException {
        ClassFile c = clas;
        while (c != null) {
            MethodInfo info = this.getDeclaredMethod(c, name, parameters);
            if (info != null) {
                return info;
            }
            c = getSuperClass(c);
        }
        for (ClassFile ci : getAllSuperInterfaces(clas)) {
            MethodInfo info = this.getDeclaredMethod(ci, name, parameters);
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    private ClassFile[] getAllSuperInterfaces(ClassFile c) throws ClassNotFoundException {
        HashSet<ClassFile> cf = new HashSet<>();
        enumerateAllSuperInterfaces(c, cf);
        cf.remove(c);
        return cf.toArray(new ClassFile[cf.size()]);
    }

    private void enumerateAllSuperInterfaces(ClassFile c, Collection<ClassFile> cf) throws ClassNotFoundException {
        if (c == null) {
            throw new NullPointerException();
        }
        if (cf.add(c)) {
            ClassFile superClass = getSuperClass(c);
            if (superClass != null) {
                enumerateAllSuperInterfaces(superClass, cf);
            }
            for (ClassFile ci : getSuperInterfaces(c)) {
                enumerateAllSuperInterfaces(ci, cf);
            }
        }
    }

    private ClassFile[] getSuperInterfaces(ClassFile c) throws ClassNotFoundException {
        ClassFile[] cf = new ClassFile[c.interfaces.length];
        for (int i = 0; i < cf.length; i++) {
            cf[i] = loadClass(c.interfaces[i]);
        }
        return cf;
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
            ClassFile superClass = loadClass(sup.name);
            ClassFile subSuper = getSuperClass(loadClass(sub.name));
            return subSuper != null && isSuperOrSameClassOf(superClass, subSuper);
        } else {
            return false;
        }
    }

    private boolean isSuperOrSameClassOf(ClassFile sup, ClassFile sub) throws ClassNotFoundException {
        if (sup.equals(sub)) {
            return true;
        } else {
            ClassFile superClass = getSuperClass(sub);
            return superClass != null && isSuperOrSameClassOf(sup, superClass);
        }
    }

    private InputStream loadClassFile(String class_) throws IOException {
        try {
            return artifact.loadClassFile(class_);
        } catch (IOException e) {
            for (Artifact art : deps) {
                try {
                    return art.loadClassFile(class_);
                } catch (IOException ex) {
                    // continue
                }
            }
            throw e;
        }
    }

    private ClassFile loadClass(String class_) throws ClassNotFoundException {
        if (class_.indexOf('/') != -1) {
            throw new IllegalArgumentException("Class names cannot contain slashes!");
        }
        if (loaded.containsKey(class_)) {
            return loaded.get(class_);
        } else {
            ClassFile cf;
            try {
                InputStream art = null;
                try {
                    art = loadClassFile(class_);
                } catch (FileNotFoundException | NoSuchFileException e1) {
                    if (isNameExternal(class_)) {
                        art = Object.class.getResourceAsStream("/" + class_.replace('.', '/') + ".class");
                    }
                    if (art == null) {
                        throw e1;
                    }
                }
                cf = ClassParser.parse(art);
                if (!cf.this_class.equals(class_)) {
                    throw new ClassNotFoundException("Could not load class due to mismatched names!");
                }
            } catch (IOException e) {
                throw new ClassNotFoundException("Could not load class: " + class_, e);
            }
            loaded.put(class_, cf);
            return cf;
        }
    }

    private boolean isNameExternal(String class_) {
        if (class_.contains("/")) {
            throw new IllegalArgumentException("Invalid slash in isNameExternal");
        }
        return class_.startsWith("java.");
    }

    private Phase getDeclaredPhase(MethodInfo m) throws ClassFormatException, ClassNotFoundException {
        if (m == null) {
            throw new NullPointerException();
        }
        Phase found = null;
        for (Phase p : Phase.values()) {
            if (m.isAnnotationPresent(p.annot.getName())) {
                if (found == null) {
                    found = p;
                } else {
                    warn(m.declaringClass.getSourceFile(), m.getLineNumberFor(0) - 1, "Multiple phases declared on method: " + m);
                }
            }
        }
        boolean fromInit = false;
        if (m.name.equals("<clinit>")) {
            if (found != null) {
                throw new ClassFormatException("Expected no annotations on static initializers.");
            }
            found = Phase.SETUP;
        } else if (m.name.equals("<init>")) {
            if (found == null) {
                found = isException(m.declaringClass) ? Phase.IGNORED : Phase.SETUP;
                fromInit = true;
            }
        } else if (m.isGetter() && found == null) {
            found = Phase.IGNORED;
        } else if ((m.declaringClass.access & ClassParser.ACC_ENUM) != 0 && m.name.equals("values") && m.parameters.length == 0) {
            if (found == null) {
                found = Phase.IGNORED;
            } else {
                warn(m.declaringClass.getSourceFile(), m.getLineNumberFor(0) - 1, "Enum values() declared with a phase");
            }
        }
        if (isNameExternal(m.declaringClass.this_class)) {
            Phase a = externals.get(m.declaringClass.this_class + "." + m.name + "(" + m.parameters.length + ")");
            Phase b = externals.get(m.declaringClass.this_class + "." + m.name);
            Phase c = externals.get(m.declaringClass.this_class + ".*");
            Phase look = (a != null ? a : b != null ? b : c);
            if (look != null) {
                if (found == null) {
                    found = look;
                } else if (found != look) {
                    if (fromInit) {
                        found = look;
                    } else {
                        warn(m.declaringClass.getSourceFile(), m.getLineNumberFor(0) - 1, "Attempt to declare external phase override on " + m);
                    }
                }
            }
        }
        return found;
    }

    private boolean isException(ClassFile cls) throws ClassNotFoundException {
        return cls.this_class.equals("java.lang.Throwable") || (cls.super_class != null && isException(getSuperClass(cls)));
    }

    private int verifyAll() {
        warnings = 0;
        for (String className : artifact.listClassNames()) {
            try {
                verify(className);
            } catch (ClassNotFoundException | ClassFormatException e) {
                Logger.severe("Could not phase-verify class: " + className + ": " + e.getMessage());
            }
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
        if (m == null) {
            throw new NullPointerException();
        }
        if (m.isAnnotationPresent(SuppressPhaseWarnings.class.getName())) {
            return;
        }
        Phase p = getPhase(m);
        if (p == null) {
            // warn("No phase declared on method: " + m);
        } else {
            for (RefInfo target : enumerateReferences(m)) {
                // TODO: handle the case where this is actually an unrelated
                // construction of the superclass
                if ("<init>".equals(m.name) && "<init>".equals(target.callee.name) && target.callee.declaringClass == getSuperClass(m.declaringClass)) {
                    // don't worry about superclass constructors.
                    continue;
                }
                Phase tp = getPhase(target.callee);
                if (tp == null) {
                    warn(target.callerFile, target.callerLine, "Call to unphased method " + target.callee.declaringClass.this_class + "." + target.callee.name + " from " + m.name + m.descriptor);
                } else if (!tp.allowedFrom(p)) {
                    warn(target.callerFile, target.callerLine, "Out-of-phase call from " + p + " to " + tp);
                }
            }
        }
    }

    private static final class RefInfo {
        public String callerFile;
        public int callerLine;
        public MethodInfo callee;
    }

    private RefInfo[] enumerateReferences(MethodInfo m) throws ClassFormatException, ClassNotFoundException {
        if ((m.access & ClassParser.ACC_ABSTRACT) != 0) {
            if (m.code != null || m.handlers != null) {
                throw new ClassFormatException("An abstract method incorrectly contains code.");
            }
            return new RefInfo[0];
        }
        BytecodeParser parser = new BytecodeParser(m);
        ArrayList<RefInfo> refs = new ArrayList<>();
        for (ReferenceInfo ri : parser.getReferences()) {
            CPInfo cp = ri.target;
            if (cp.tag == ClassParser.CONSTANT_InvokeDynamic) {
                // shouldn't actually matter... ignoring these. TODO: DON'T
                // IGNORE THE METHOD BODIES - THEY SHOULD BE VERIFIED.
                continue;
            }
            RefInfo rfi = new RefInfo();
            rfi.callerFile = ri.getSourceFile();
            rfi.callerLine = ri.getLineNumber();
            rfi.callee = getMethodRef(m, cp);
            refs.add(rfi);
        }
        return refs.toArray(new RefInfo[refs.size()]);
    }

    private MethodInfo getMethodRef(MethodInfo m, CPInfo cp) throws ClassFormatException, ClassNotFoundException {
        MethodInfo referencedMethod;
        cp.requireTagOf(ClassParser.CONSTANT_Methodref, ClassParser.CONSTANT_InterfaceMethodref);
        String class_name = m.declaringClass.getConst(cp.alpha).asClass();
        CPInfo name_and_type = m.declaringClass.getConst(cp.beta);
        name_and_type.requireTag(ClassParser.CONSTANT_NameAndType);
        String name = m.declaringClass.getConst(name_and_type.alpha).asUTF8();
        String descriptor = m.declaringClass.getConst(name_and_type.beta).asUTF8();
        ClassFile file = class_name.startsWith("[") ? loadClass("java.lang.Object") : loadClass(class_name);
        referencedMethod = this.getProvidedMethod(file, name, ClassParser.parseMethodDescriptorArguments(descriptor));
        if (referencedMethod == null) {
            throw new ClassFormatException("Cannot resolve reference to " + class_name + "." + name + descriptor + " in " + m);
        }
        return referencedMethod;
    }

    private void warn(String file, int line, String string) {
        warnings++;
        Logger.warning("[VERIFIER] (" + file + ":" + line + ") " + string);
    }
}
