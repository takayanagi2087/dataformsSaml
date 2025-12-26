package jp.dataforms.fw.saml.util;

import java.io.File;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

/**
 * IdPメタデータクラスです。
 */
public class IdPMetadata extends Metadata {

	/**
	 * Logger.
	 */
	private static Logger logger = LogManager.getLogger(IdPMetadata.class);
	
	
	/**
	 * IdPメタデータ。
	 */
	private static String metadata = null;
	
	/**
	 * IdPメタデータのパスを取得します。
	 */
	static {
		try {
			Context initContext = new InitialContext();
			IdPMetadata.metadata = (String) initContext.lookup("java:/comp/env/samlIdpMetadata");
			logger.info("samlIdpMetadata=" + IdPMetadata.metadata);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
	
	
	/**
	 * IdPメタデータのパスを取得します。
	 * @return IdPメタデータのパス。
	 */
	public static String getMetadata() {
		return metadata;
	}

	/**
	 * コンストラクタ。
	 * @throws Exception 例外。
	 */
	public IdPMetadata() throws Exception {
		super(new File(IdPMetadata.metadata));
	}

	/**
	 * 署名検証用の証明書を取得します。
	 * @return 署名検証用の証明書。
	 * @throws Exception 例外。
	 */
	private String getSigningX509CertificateText() throws Exception {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(this.getNamespaceContext());
		String expression = "//md:KeyDescriptor[@use='signing']//ds:X509Certificate";
		XPathExpression expr = xpath.compile(expression);
		Node node = (Node) expr.evaluate(this.getDocument(), XPathConstants.NODE);
		return node.getTextContent();
	}
	
	/**
	 * 署名検証用の証明書を取得します。
	 * @return 署名検証用の証明書。
	 * @throws Exception 例外。
	 */
	public X509Certificate getSigningX509Certificate() throws Exception {
		String base64cert = this.getSigningX509CertificateText();
		String cleaned = base64cert.replaceAll("\\s+", "");
		byte[] decoded = Base64.getDecoder().decode(cleaned);
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) factory.generateCertificate(new java.io.ByteArrayInputStream(decoded));
		logger.debug("X509Certificate=" + cert);
		return cert;
	}

	/**
	 * IdPへのリダイレクションURLを取得します。
	 * @return IdPへのリダイレクションURL。
	 * @throws Exception 例外。
	 */
	public String getHttpRedirectURL() throws Exception {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(this.getNamespaceContext());
		String expression = "//md:SingleSignOnService[@Binding='urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect']/@Location";
		XPathExpression expr = xpath.compile(expression);
		String url = (String) expr.evaluate(this.getDocument(), XPathConstants.STRING);
		logger.debug("HttpRedirectURL=" + url);
		return url;
	}
}
