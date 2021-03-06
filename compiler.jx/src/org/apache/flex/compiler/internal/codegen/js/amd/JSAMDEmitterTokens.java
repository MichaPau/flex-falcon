package org.apache.flex.compiler.internal.codegen.js.amd;

import org.apache.flex.compiler.codegen.IEmitterTokens;

public enum JSAMDEmitterTokens implements IEmitterTokens
{
    DEFINE("define"), LENGTH("length"), ;

    private String token;

    private JSAMDEmitterTokens(String value)
    {
        token = value;
    }

    public String getToken()
    {
        return token;
    }
}
