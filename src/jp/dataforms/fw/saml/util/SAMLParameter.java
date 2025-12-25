package jp.dataforms.fw.saml.util;

import lombok.Getter;

/**
 * SAMLパラメータ。
 */
public class SAMLParameter {
	/**
	 * SPのメタデータ。
	 */
	@Getter
	private SPMetadata spMetadata = null;
	
	/**
	 * IdPメタデータ。
	 */
	@Getter
	private IdPMetadata idpMetadata = null;

	/**
	 * コンストラクタ。
	 * @throws Exception 例外。
	 */
	public SAMLParameter() throws Exception {
		this.spMetadata = new SPMetadata();
		this.idpMetadata = new IdPMetadata();
	}
}
