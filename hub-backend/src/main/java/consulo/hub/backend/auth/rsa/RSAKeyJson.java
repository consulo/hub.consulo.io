package consulo.hub.backend.auth.rsa;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author VISTALL
 * @since 11/05/2023
 */
public class RSAKeyJson
{
	public RSAPublicKeyJson publicKey;
	public RSAPrivateKeyJson privateKey;

	public RSAKeyJson()
	{
	}

	public RSAKeyJson(RSAPublicKey publicKey, RSAPrivateKey privateKey)
	{
		this.publicKey = new RSAPublicKeyJson(publicKey);
		this.privateKey = new RSAPrivateKeyJson(privateKey);
	}
}
