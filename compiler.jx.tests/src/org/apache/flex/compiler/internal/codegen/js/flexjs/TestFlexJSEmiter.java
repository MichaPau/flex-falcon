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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogEmiter;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSEmiter extends TestGoogEmiter
{
    @Override
    public void setUp()
    {
        project = new FlexJSProject(workspace);

        super.setUp();
    }

    @Override
    @Test
    public void testSimple()
    {
        String code = "package com.example.components {"
                + "import spark.components.Button;"
                + "public class MyTextButton extends Button {"
                + "public function MyTextButton() {if (foo() != 42) { bar(); } }"
                + "private var _privateVar:String = \"do \";"
                + "public var publicProperty:Number = 100;"
                + "public function myFunction(value: String): String{"
                + "return \"Don't \" + _privateVar + value; }";
        IFileNode node = compileAS(code);
        asBlockWalker.visitFile(node);
        assertOut("goog.provide('com.example.components.MyTextButton');\n\n/**\n * @constructor\n * @extends {spark.components.Button}\n */\ncom.example.components.MyTextButton = function() {\n\tgoog.base(this);\n\tif (foo() != 42) {\n\t\tbar();\n\t}\n}\ngoog.inherits(com.example.components.MyTextButton, spark.components.Button);\n\n/**\n * @private\n * @type {string}\n */\ncom.example.components.MyTextButton.prototype._privateVar = \"do \";\n\n/**\n * @type {number}\n */\ncom.example.components.MyTextButton.prototype.publicProperty = 100;\n\n/**\n * @expose\n * @param {string} value\n * @return {string}\n */\ncom.example.components.MyTextButton.prototype.myFunction = function(value) {\n\treturn \"Don't \" + this._privateVar + value;\n};");
    }

    @Override
    @Test
    public void testDefaultParameter()
    {
        IFunctionNode node = getMethodWithPackage("function method1(p1:int, p2:int, p3:int = 3, p4:int = 4):int{return p1 + p2 + p3 + p4;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number} p1\n * @param {number} p2\n * @param {number=} p3\n * @param {number=} p4\n * @return {number}\n */\n"
                + "foo.bar.FalconTest_A.prototype.method1 = function(p1, p2, p3, p4) {\n"
                + "\tp3 = typeof p3 !== 'undefined' ? p3 : 3;\n"
                + "\tp4 = typeof p4 !== 'undefined' ? p4 : 4;\n"
                + "\treturn p1 + p2 + p3 + p4;\n}");
    }

    @Override
    @Test
    public void testDefaultParameter_Body()
    {
        IFunctionNode node = getMethodWithPackage("function method1(bar:int = 42, bax:int = 4):void{if (a) foo();}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {number=} bar\n * @param {number=} bax\n */\n"
                + "foo.bar.FalconTest_A.prototype.method1 = function(bar, bax) {\n"
                + "\tbar = typeof bar !== 'undefined' ? bar : 42;\n"
                + "\tbax = typeof bax !== 'undefined' ? bax : 4;\n"
                + "\tif (a)\n\t\tfoo();\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
