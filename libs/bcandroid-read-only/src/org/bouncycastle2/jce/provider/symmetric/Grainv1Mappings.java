package org.bouncycastle2.jce.provider.symmetric;

import java.util.HashMap;

public class Grainv1Mappings
    extends HashMap
{
    public Grainv1Mappings()
    {
        put("Cipher.Grainv1", "org.bouncycastle2.jce.provider.symmetric.Grainv1$Base");
        put("KeyGenerator.Grainv1", "org.bouncycastle2.jce.provider.symmetric.Grainv1$KeyGen");
    }
}
