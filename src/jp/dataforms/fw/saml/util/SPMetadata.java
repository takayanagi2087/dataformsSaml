package jp.dataforms.fw.saml.util;

import java.io.File;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SPメタデータクラスです。
 */
public class SPMetadata extends Metadata {

	/**
	 * Logger.
	 */
	private static Logger logger = LogManager.getLogger(SPMetadata.class);
	
	
	/**
	 * SPメタデータ。
	 */
	private static String metadata = null;
	
	/**
	 * SPメタデータのパスを取得します。
	 */
	static {
		try {
			Context initContext = new InitialContext();
			SPMetadata.metadata = (String) initContext.lookup("java:/comp/env/samlSpMetadata");
			logger.info("samlSpMetadata=" + SPMetadata.metadata);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

	}
	
	
	
	/**
	 * コンストラクタ。
	 * @throws Exception 例外。
	 */
	public SPMetadata() throws Exception {
		super(new File(SPMetadata.metadata));
	}

	/**
	 * SPのentryIDを取得します。
	 * @return entryID。
	 * @throws Exception 例外。
	 */
	public String getEntryID() throws Exception {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(this.getNamespaceContext());
		String expression = "/md:EntityDescriptor/@entityID";
		XPathExpression expr = xpath.compile(expression);
		String entityID = (String) expr.evaluate(this.getDocument(), XPathConstants.STRING);
		logger.debug("entityID=" + entityID);
		return entityID;
	}
	
	/**
	 * ACSのURLを取得します。
	 * @return ACSのURL。
	 * @throws Exception 例外。
	 */
	public String getAcsURL() throws Exception {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(this.getNamespaceContext());
		String expression = "//md:AssertionConsumerService/@Location";
		XPathExpression expr = xpath.compile(expression);
		String location = (String) expr.evaluate(this.getDocument(), XPathConstants.STRING);
		logger.debug("AcsURL=" + location);
		return location;
	}

}
