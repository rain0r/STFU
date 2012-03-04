package org.bouncycastle2.crypto.tls;

import org.bouncycastle2.crypto.encodings.PKCS1Encoding;
import org.bouncycastle2.crypto.engines.RSABlindedEngine;
import org.bouncycastle2.crypto.signers.GenericSigner;

class TlsRSASigner
    extends GenericSigner
{
    TlsRSASigner()
    {
        super(new PKCS1Encoding(new RSABlindedEngine()), new CombinedHash());
    }
}
