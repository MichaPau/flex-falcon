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

package org.apache.flex.compiler.problems;

import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.common.SourceLocation;

/**
 * Compiler problem created when the code generator detects unreachable code.
 */
public class UnreachableBlockProblem extends SemanticWarningProblem
{
    public static final String DESCRIPTION =
        "No code emitted for unreachable statement(s).";

    public static final int warningCode = 5042;
    
    /**
     * Construct an UnreachableBlockProblem with source position
     * info determined (and verified) by the caller.
     * @param fileName the name of the file, normalized.
     * @param lineNumber the line within the file.
     */
    public UnreachableBlockProblem(String fileName, int lineNumber)
    {
        super(new SourceLocation(fileName, ISourceLocation.UNKNOWN, ISourceLocation.UNKNOWN, lineNumber, ISourceLocation.UNKNOWN));
    }
}
