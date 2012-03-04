package org.bouncycastle2.crypto.tls;

import org.bouncycastle2.crypto.digests.SHA1Digest;
import org.bouncycastle2.crypto.signers.DSADigestSigner;
import org.bouncycastle2.crypto.signers.DSASigner;

class TlsDSSSigner
    extends DSADigestSigner
{
    TlsDSSSigner()
    {
        super(new DSASigner(), new SHA1Digest());
    }
}
