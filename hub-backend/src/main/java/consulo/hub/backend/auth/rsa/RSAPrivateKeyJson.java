package consulo.hub.backend.auth.rsa;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;

/**
 * @author VISTALL
 * @since 11/05/2023
 */
public class RSAPrivateKeyJson
{
	public BigInteger modulus;
	public BigInteger privateExponent;

	public RSAPrivateKeyJson()
	{
	}

	public RSAPrivateKeyJson(RSAPrivateKey k)
	{
		modulus = k.getModulus();
		privateExponent = k.getPrivateExponent();
	}

	public RSAPrivateKeySpec toSpec()
	{
		return new RSAPrivateKeySpec(modulus, privateExponent);
	}
}
