package org.bouncycastle2.bcpg;

import java.io.*;

/**
 */
public class SymmetricEncIntegrityPacket 
    extends InputStreamPacket
{    
    int        version;
    
    SymmetricEncIntegrityPacket(
        BCPGInputStream    in)
        throws IOException
    {
        super(in);
        
        version = in.read();
    }
}
