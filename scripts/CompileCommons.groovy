/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Compiles sources under ${basedir}/src/commons
 *
 * @author Andres Almiray
 *
 * @since 0.1
 */

ant.property(environment:"env")
griffonHome = ant.antProject.properties."env.GRIFFON_HOME"

includeTargets << griffonScript("Compile")

target(compileCommons: "Compile common sources") {
    depends(checkVersion, parseArguments, classpath)
    def commons = "${basedir}/src/commons"
    def commonsdir = new File(commons)
    if(!commonsdir.exists()) return

    ant.mkdir(dir: classesDirPath)

    def upToDate = false
    if(hasSourcesOfType("${basedir}/src/commons", ".java"))
        upToDate |= sourcesUpToDate("${basedir}/src/commons", classesDirPath, ".java")
    if(hasSourcesOfType("${basedir}/src/commons", ".groovy"))
        upToDate |= sourcesUpToDate("${basedir}/src/commons", classesDirPath, ".groovy")
    if(upToDate) return

    ant.echo(message: "Compiling common sources to $classesDirPath")
    try {
        String classpathId = "griffon.compile.classpath"
        ant.groovyc(destdir: classesDirPath,
                    classpathref: classpathId) {
            src(path: commons)
            javac(classpathref: classpathId)
        }
    }
    catch (Exception e) {
        event("StatusFinal", ["Compilation error: ${e.message}"])
        ant.fail(message: "Could not compile common sources: " + e.class.simpleName + ": " + e.message)
    }
}

hasSourcesOfType = { src, srcsfx = ".java" ->
    def srcdir = new File(src.toString())
    def skipIt = new RuntimeException()
    try {
        srcdir.eachFileRecurse { sf ->
            if(sf.isDirectory()) return
            if(sf.toString().endsWith(srcsfx)) throw skipIt
        }
    } catch(x) {
       if(x == skipIt) return true
       throw x
    }
    return false
}

sourcesUpToDate = { src, dest, srcsfx = ".java", destsfx = ".class" ->
    def srcdir = new File(src)
    def destdir = new File(dest)
    def skipIt = new RuntimeException()
    try {
        srcdir.eachFileRecurse { sf ->
            if(sf.isDirectory()) return
            if(!sf.toString().endsWith(srcsfx)) return
            def target = new File(destdir.toString() + sf.toString().substring(srcdir.toString().length()) - srcsfx + destsfx)
            if(!target.exists()) throw skipIt
            if(sf.lastModified() > target.lastModified()) throw skipIt
        }
    } catch(x) {
       if(x == skipIt) return false
       throw x
    }
    return true
}

setDefaultTarget(compileCommons)