package consulo.hub.backend.auth.rsa;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * @author VISTALL
 * @since 11/05/2023
 */
public class RSAPublicKeyJson
{
	public BigInteger modulus;

	public BigInteger publicExponent;

	public RSAPublicKeyJson()
	{
	}

	public RSAPublicKeyJson(RSAPublicKey key)
	{
		modulus = key.getModulus();
		publicExponent = key.getPublicExponent();
	}

	public RSAPublicKeySpec toSpec()
	{
		return new RSAPublicKeySpec(modulus, publicExponent);
	}
}
