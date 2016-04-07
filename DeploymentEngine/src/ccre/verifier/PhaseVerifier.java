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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import ccre.deployment.Artifact;
import ccre.deployment.Jar;
import ccre.log.Logger;
import ccre.verifier.BytecodeParser.ReferenceInfo;
import ccre.verifier.ClassParser.CPInfo;
import ccre.verifier.ClassParser.ClassFile;
import ccre.verifier.ClassParser.MethodInfo;
import ccre.verifier.ClassParser.TypeInfo;

public class PhaseVerifier {
    public static void main(String[] args) throws Exception {
        PhaseVerifier.verify(new Jar(new File("../CommonChickenRuntimeEngine/CCRE.jar")));
    }

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
        if (method == null) {
            throw new NullPointerException();
        }
        if (known.containsKey(method)) {
            return known.get(method);
        }
        Phase phase = getDeclaredPhase(method);
        MethodInfo[] superMethods = getMethodSuperMatches(method, true);
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
            } else if (phase != superPhase) {
                warn(method.declaringClass.getSourceFile(), method.getLineNumberFor(0) - 1, "Mismatched phase between method and overridden method");
                // TODO: update output phase
            }
        }
        known.put(method, phase);
        return phase;
    }

    private MethodInfo[] getMethodSuperMatches(MethodInfo m, boolean ignoreIfExternal) throws ClassNotFoundException {
        ClassFile c = getSuperClass(m.declaringClass, ignoreIfExternal);
        ArrayList<MethodInfo> mis = new ArrayList<>();
        while (c != null) {
            MethodInfo info = getDeclaredMethod(c, m.name, m.parameters);
            if (info != null) {
                mis.add(info);
            }
            c = getSuperClass(c, ignoreIfExternal);
        }
        for (ClassFile ci : getAllSuperInterfaces(m.declaringClass, true)) {
            MethodInfo info = this.getDeclaredMethod(ci, m.name, m.parameters);
            if (info != null) {
                mis.add(info);
            }
        }
        return mis.toArray(new MethodInfo[mis.size()]);
    }

    private ClassFile getSuperClass(ClassFile c, boolean ignoreIfExternal) throws ClassNotFoundException {
        return c.super_class == null ? null : loadClass(c.super_class, ignoreIfExternal);
    }

    private MethodInfo getProvidedMethod(ClassFile clas, String name, TypeInfo[] parameters) throws ClassNotFoundException {
        ClassFile c = clas;
        while (c != null) {
            MethodInfo info = this.getDeclaredMethod(c, name, parameters);
            if (info != null) {
                return info;
            }
            c = getSuperClass(c, true);
        }
        for (ClassFile ci : getAllSuperInterfaces(clas, true)) {
            MethodInfo info = this.getDeclaredMethod(ci, name, parameters);
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    private ClassFile[] getAllSuperInterfaces(ClassFile c, boolean ignoreIfExternal) throws ClassNotFoundException {
        HashSet<ClassFile> cf = new HashSet<>();
        enumerateAllSuperInterfaces(c, cf, ignoreIfExternal);
        cf.remove(c);
        return cf.toArray(new ClassFile[cf.size()]);
    }

    private void enumerateAllSuperInterfaces(ClassFile c, Collection<ClassFile> cf, boolean ignoreIfExternal) throws ClassNotFoundException {
        if (c == null) {
            throw new NullPointerException();
        }
        if (cf.add(c)) {
            ClassFile superClass = getSuperClass(c, ignoreIfExternal);
            if (superClass != null) { // if external, don't need to enumerate
                enumerateAllSuperInterfaces(superClass, cf, ignoreIfExternal);
            }
            for (ClassFile ci : getSuperInterfaces(c, ignoreIfExternal)) {
                if (ci != null) { // if external, don't need to enumerate
                    enumerateAllSuperInterfaces(ci, cf, ignoreIfExternal);
                }
            }
        }
    }

    private ClassFile[] getSuperInterfaces(ClassFile c, boolean ignoreIfExternal) throws ClassNotFoundException {
        ClassFile[] cf = new ClassFile[c.interfaces.length];
        for (int i = 0; i < cf.length; i++) {
            cf[i] = loadClass(c.interfaces[i], ignoreIfExternal);
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
            ClassFile superClass = loadClass(sup.name, false);
            // It's okay to ignore external classes - they shouldn't depend on
            // our classes, so there's no way for the superclass to be found in
            // there, if we could load it.
            ClassFile subSuper = getSuperClass(loadClass(sub.name, false), true);
            return subSuper != null && isSuperOrSameClassOf(superClass, subSuper);
        } else {
            return false;
        }
    }

    private boolean isSuperOrSameClassOf(ClassFile sup, ClassFile sub) throws ClassNotFoundException {
        if (sup.equals(sub)) {
            return true;
        } else {
            // It's okay to ignore external classes - they shouldn't depend on
            // our classes, so there's no way for the superclass to be found in
            // there, if we could load it.
            ClassFile superClass = getSuperClass(sub, true);
            return superClass != null && isSuperOrSameClassOf(sup, superClass);
        }
    }

    private ClassFile loadClass(String class_, boolean nullForExternal) throws ClassNotFoundException {
        if (class_.indexOf('/') != -1) {
            throw new IllegalArgumentException("Class names cannot contain slashes!");
        }
        if (loaded.containsKey(class_)) {
            ClassFile cf = loaded.get(class_);
            if (cf == null && !(nullForExternal && isNameExternal(class_))) {
                throw new ClassNotFoundException("Could not load class: " + class_);
            }
            return cf;
        } else {
            ClassFile cf;
            try {
                cf = ClassParser.parse(artifact.loadClassFile(class_));
                if (!cf.this_class.equals(class_)) {
                    throw new ClassNotFoundException("Could not load class due to mismatched names!");
                }
            } catch (IOException e) {
                if (nullForExternal && isNameExternal(class_)) {
                    cf = null;
                } else {
                    throw new ClassNotFoundException("Could not load class: " + class_, e);
                }
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

    private Phase getDeclaredPhase(MethodInfo m) throws ClassFormatException {
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
        if (m.name.equals("<clinit>")) {
            if (found != null) {
                throw new ClassFormatException("Expected no annotations on static initializers.");
            }
            found = Phase.SETUP;
        } else if (m.name.equals("<init>")) {
            if (found == null) {
                found = Phase.SETUP;
            }
            if (found != Phase.SETUP) {
                warn(m.declaringClass.getSourceFile(), m.getLineNumberFor(0) - 1, "Initializer declared with non-SETUP phase");
            }
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
        verify(loadClass(className, false));
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
        Phase p = getPhase(m);
        if (p == null) {
            // warn("No phase declared on method: " + m);
        } else {
            for (RefInfo target : enumerateReferences(m)) {
                Phase tp = getPhase(target.callee);
                if (tp == null) {
                    warn(target.callerFile, target.callerLine, "Call to unphased method " + target.callee.declaringClass.this_class + "." + target.callee.name);
                } else if (tp != p) {
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
            cp.requireTagOf(ClassParser.CONSTANT_Methodref, ClassParser.CONSTANT_InterfaceMethodref);
            String class_name = m.declaringClass.getConst(cp.alpha).asClass();
            CPInfo name_and_type = m.declaringClass.getConst(cp.beta);
            name_and_type.requireTag(ClassParser.CONSTANT_NameAndType);
            String name = m.declaringClass.getConst(name_and_type.alpha).asUTF8();
            String descriptor = m.declaringClass.getConst(name_and_type.beta).asUTF8();
            ClassFile loadClass = this.loadClass(class_name, true);
            if (loadClass == null) {
                continue; // external
            }
            MethodInfo referencedMethod = this.getProvidedMethod(loadClass, name, ClassParser.parseMethodDescriptorArguments(descriptor));
            if (referencedMethod == null) {
                throw new ClassFormatException("Cannot resolve reference to " + class_name + "." + name + descriptor + " in " + m);
            }
            RefInfo rfi = new RefInfo();
            rfi.callerFile = ri.getSourceFile();
            rfi.callerLine = ri.getLineNumber();
            rfi.callee = referencedMethod;
            refs.add(rfi);
        }
        return refs.toArray(new RefInfo[refs.size()]);
    }

    private void warn(String file, int line, String string) {
        warnings++;
        Logger.warning("[VERIFIER] (" + file + ":" + line + ") " + string);
    }
}
