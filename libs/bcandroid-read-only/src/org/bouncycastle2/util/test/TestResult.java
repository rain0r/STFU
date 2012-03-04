package org.bouncycastle2.util.test;

public interface TestResult
{
    public boolean isSuccessful();
    
    public Throwable getException();
    
    public String toString();
}
