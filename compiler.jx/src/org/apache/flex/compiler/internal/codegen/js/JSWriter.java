/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.compiler.internal.codegen.js;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.js.IJSWriter;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.as.IASBlockWalker;

public class JSWriter implements IJSWriter
{
    protected IASProject project;

    protected List<ICompilerProblem> problems;

    protected ICompilationUnit compilationUnit;

    @SuppressWarnings("unused")
    private boolean enableDebug;

    /**
     * Create a JSApplication writer.
     * 
     * @param application the JSApplication model to be encoded
     * @param useCompression use ZLIB compression if true
     */
    public JSWriter(IASProject project, List<ICompilerProblem> problems,
            ICompilationUnit compilationUnit, boolean enableDebug)
    {
        this.project = project;
        this.problems = problems;
        this.compilationUnit = compilationUnit;
        this.enableDebug = enableDebug;
    }

    @Override
    public void close() throws IOException
    {
        //outputBuffer.close();
    }

    @Override
    public void writeTo(OutputStream out)
    {
        JSFilterWriter writer = (JSFilterWriter) JSSharedData.backend
                .createWriterBuffer(project);
        IASEmitter emitter = JSSharedData.backend.createEmitter(writer);
        IASBlockWalker walker = JSSharedData.backend.createWalker(project,
                problems, emitter);

        walker.visitCompilationUnit(compilationUnit);

        try
        {
            out.write(writer.toString().getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int writeTo(File out) throws FileNotFoundException, IOException
    {
        return 0;
    }

}
