package jp.dataforms.fw.saml.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * SAML応答情報。
 */
public class SAMLResponse extends SAMLParameter {
	/**
	 * Logger.
	 */
	private static Logger logger = LogManager.getLogger(SAMLResponse.class);
	/**
	 * コンストラクタ。
	 * @throws Exception 例外。
	 */
	public SAMLResponse() throws Exception {
		super();
	}
	
	/**
	 * BASE64デコードをしXMLを取得します。
	 * @param samlResponse BASE64の応答情報。
	 * @return XML。む
	 */
	protected String getXML(final String samlResponse) {
		String sr = samlResponse.replaceAll("\n", "").replaceAll("\r", "");
		byte[] decoded = Base64.getDecoder().decode(sr);
		String xml = new String(decoded, StandardCharsets.UTF_8);
		logger.debug("SAMLResponse XML=" + xml);
		return xml;
	}
	
    /**
     * XMLからSAML Responseを取得する。
     * @param xml XMLテキスト。
     * @return SAML Response。
     * @throws Exception 例外。
     */
    protected Response getSamlResponse(final String xml) throws Exception {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	factory.setNamespaceAware(true);
    	DocumentBuilder builder = factory.newDocumentBuilder();
    	Document document = null;
    	try (ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
    		document = builder.parse(is);
    	}
    	Element element = document.getDocumentElement();
    	XMLObject xmlObject = XMLObjectProviderRegistrySupport.getUnmarshallerFactory()
    	        .getUnmarshaller(element)
    	        .unmarshall(element);
    	return (Response) xmlObject;
    }
    
    /**
     * SAML応答情報を確認します。
     * @param r SAML応答情報。
     */
    protected void validate(final Response r) throws Exception {
		X509Certificate  idpCert = this.getIdpMetadata().getSigningX509Certificate();
		BasicX509Credential credential = new BasicX509Credential(idpCert);
		if (r.getSignature() != null) {
			SignatureValidator.validate(r.getSignature(), credential);
		} else {
			r.getAssertions().forEach((assertion) -> {
		        if (assertion.getSignature() != null) {
		            try {
		                SignatureValidator.validate(assertion.getSignature(), credential);
		            } catch (Exception e) {
		                throw new RuntimeException(e.getMessage(), e);
		            }
		        }
		    });
		}
		String entryID = this.getSpMetadata().getEntryID();
		r.getAssertions().forEach(assertion -> {
		    assertion.getConditions().getAudienceRestrictions().forEach(audienceRestriction -> {
		        audienceRestriction.getAudiences().forEach(audience -> {
		            String audienceURI = audience.getURI();
		            if (!entryID.equals(audienceURI)) {
		                throw new RuntimeException("AudienceRestriction NG: " + audienceURI);
		            }
		        });
		    });
		});			
    }
	
	/**
	 * ユーザ情報を出力。
	 * @param r SAML Response。
	 * @return ユーザ情報。
	 */
	protected Map<String, String> getAssertions(final Response r) {
		Map<String, String> ret = new HashMap<String, String>();
		r.getAssertions().forEach(assertion -> {
			assertion.getAttributeStatements().forEach(attrStatement -> {
				attrStatement.getAttributes().forEach(attr -> {
					String attrName = attr.getName();
					attr.getAttributeValues().forEach(value -> {
						String attrValue = value.getDOM().getTextContent();
//						out.println(attrName + " = " + attrValue);
						ret.put(attrName, attrValue);
					});
				});
			});
		});
		return ret;
	}
    
	/**
	 * ユーザー情報を取得します。
	 * @param samlResponse SAMLの応答情報。
	 * @return ユーザー情報マップ。
	 * @throws Exception 例外。
	 */
	public Map<String, String> getUserInfo(final String samlResponse) throws Exception {
		String xml = this.getXML(samlResponse);
		Response response = this.getSamlResponse(xml);
		this.validate(response);
		Map<String, String> ret = this.getAssertions(response);
		logger.debug("UserInfo=" + ret);
		return ret;
	}
}
