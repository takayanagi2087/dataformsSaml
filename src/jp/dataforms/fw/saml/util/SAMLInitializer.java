package jp.dataforms.fw.saml.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensaml.core.config.InitializationService;

/**
 * SAML初期化クラス。
 */
public class SAMLInitializer {
	/**
	 * Logger.
	 */
	private static Logger logger = LogManager.getLogger(SAMLInitializer.class);

	/**
	 * 初期化フラグ。
	 */
	private static boolean initialized = false;

	/**
	 * 初期化処理。
	 */
	public static synchronized void init() {
		try {
			if (!initialized) {
				InitializationService.initialize();
				initialized = true;
				logger.info("SamlInitializer.init");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
