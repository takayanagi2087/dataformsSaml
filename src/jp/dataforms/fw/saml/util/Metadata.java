package jp.dataforms.fw.saml.util;

import java.io.File;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import lombok.Getter;

/**
 * MetadataのXMLファイルの基本クラスです。
 */
public class Metadata {
	
	/**
	 * XMLパース結果。
	 */
	@Getter
	private Document document = null;
	
	/**
	 * コンストラクタ。
	 * @param xml メタデータXMLファイル。
	 * @throws Exception 例外。
	 */
	public Metadata(final File xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		this.document = builder.parse(xml);
	}
	
	/**
	 * NamespaceContextを取得します。
	 * @return NamespaceContext。
	 */
	protected NamespaceContext getNamespaceContext() {
		return new javax.xml.namespace.NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				if ("ds".equals(prefix)) {
					return "http://www.w3.org/2000/09/xmldsig#";
				} else if ("md".equals(prefix)) {
					return "urn:oasis:names:tc:SAML:2.0:metadata";
				}
				return null;
			}

			@Override
			public String getPrefix(String uri) {
				return null;
			}

			@Override
			public java.util.Iterator<String> getPrefixes(String uri) {
				return null;
			}
		};
	}

}
