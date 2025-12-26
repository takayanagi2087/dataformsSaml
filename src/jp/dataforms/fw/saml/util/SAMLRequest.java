package jp.dataforms.fw.saml.util;

import java.net.URLEncoder;
import java.time.Instant;
import java.util.Base64;
import java.util.zip.Deflater;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDPolicyBuilder;
import org.w3c.dom.Element;

/**
 * SAMLRequestの生成クラス。
 */
public class SAMLRequest extends SAMLParameter {
	/**
	 * Logger.
	 */
	private static Logger logger = LogManager.getLogger(SAMLRequest.class);
	
	/**
	 * コンストラクタ。
	 * @throws Exception 例外。
	 */
	public SAMLRequest() throws Exception {
		super();
	}
	
	/**
	 * 認証要求情報を作成します。
	 * @param issuerValue Issuer(EntityID)。
	 * @param acsUrl ACSのURL。
	 * @param destination IdPのエンドポイント。
	 * @param entityID SPのEntityID。
	 * @return 認証要求情報。
	 */
	protected AuthnRequest buildAuthnRequest(String issuerValue, String acsUrl, String destination, String entityID) {
		XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();

		// AuthnRequest Builder
		AuthnRequestBuilder authnRequestBuilder = (AuthnRequestBuilder) builderFactory
				.getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
		AuthnRequest authnRequest = authnRequestBuilder.buildObject();

		// 設定
		authnRequest.setID("_" + java.util.UUID.randomUUID()); // 一意なID
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		authnRequest.setIssueInstant(Instant.now());
		authnRequest.setDestination(destination); // IdPのSSOエンドポイントURL
		authnRequest.setForceAuthn(false);
		authnRequest.setIsPassive(false);

		// Issuer
		IssuerBuilder issuerBuilder = (IssuerBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(issuerValue);
		authnRequest.setIssuer(issuer);

		// NameIDPolicy
		NameIDPolicyBuilder nameIDPolicyBuilder = (NameIDPolicyBuilder) builderFactory
				.getBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME);
		NameIDPolicy nameIDPolicy = nameIDPolicyBuilder.buildObject();
		nameIDPolicy.setFormat(NameIDType.TRANSIENT);
		nameIDPolicy.setAllowCreate(true);
		authnRequest.setNameIDPolicy(nameIDPolicy);

		// AssertionConsumerServiceURL
		authnRequest.setAssertionConsumerServiceURL(acsUrl);

		// ProtocolBinding (通常はHTTP-POST)
		authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);

		return authnRequest;
	}

	/**
	 * 認証要求情報をXMLファイルに変換します。
	 * @param authnRequest 認証要求情報。
	 * @return XMLファイル。
	 * @throws Exception 例外。
	 */
	protected String toXMLString(AuthnRequest authnRequest) throws Exception {
		MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(authnRequest);
		Element element = marshaller.marshall(authnRequest);

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		java.io.StringWriter writer = new java.io.StringWriter();
		transformer.transform(new DOMSource(element), new StreamResult(writer));

		String xml = writer.toString();
		logger.debug("AuthnRequest xml=" + xml);
		return xml;
	}

	/**
	 * XMLリクエストをBase64エンコードします。
	 * @param xmlRequest XMLリクエスト。
	 * @return Base64エンコード結果。
	 * @throws Exception 例外。
	 */
	protected String encodeAuthnRequest(String xmlRequest) throws Exception {
		// Deflate圧縮
		Deflater deflater = new Deflater(Deflater.DEFLATED, true);
		deflater.setInput(xmlRequest.getBytes("UTF-8"));
		deflater.finish();

		byte[] buffer = new byte[1024];
		int length = deflater.deflate(buffer);
		byte[] deflated = new byte[length];
		System.arraycopy(buffer, 0, deflated, 0, length);

		// Base64エンコード
		String base64 = Base64.getEncoder().encodeToString(deflated);

		// URLエンコード
		return URLEncoder.encode(base64, "UTF-8");
	}
	
	/**
	 * SAML認証要求情報含めたリダイレクションURLを取得します。
	 * @return リダイレクションURL。
	 * @throws Exception 例外。
	 */
	public String getRedirectURL() throws Exception {
		String entryID = this.getSpMetadata().getEntryID();
		String acsURL = this.getSpMetadata().getAcsURL();
		String idpSSOUrl = this.getIdpMetadata().getHttpRedirectURL();
		AuthnRequest ar = this.buildAuthnRequest(
				entryID, // Issuer(EntityID)
				acsURL, // ACS URL
				idpSSOUrl, // IdPのSSOエンドポイント
				entryID // SP EntityID
		);
		String xml = this.toXMLString(ar);
		String enc = this.encodeAuthnRequest(xml);
		String redirectUrl = idpSSOUrl + "?SAMLRequest=" + enc;
		logger.debug("redirectUrl=" + redirectUrl);
		return redirectUrl;
	}
}
