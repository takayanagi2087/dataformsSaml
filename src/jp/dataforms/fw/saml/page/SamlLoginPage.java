package jp.dataforms.fw.saml.page;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.dataforms.fw.app.base.page.BasePage;
import jp.dataforms.fw.response.RedirectResponse;
import jp.dataforms.fw.response.Response;
import jp.dataforms.fw.saml.util.SAMLInitializer;
import jp.dataforms.fw.saml.util.SAMLRequest;

/**
 * SAMLログインページ。
 */
public class SamlLoginPage extends BasePage {
	/**
	 * Logger.
	 */
	private static Logger logger = LogManager.getLogger(SamlLoginPage.class);
	
	/**
	 * SAMLの初期化処理。
	 */
	static {
		SAMLInitializer.init();
	}

	/**
	 * コンストラクタ。
	 */
	public SamlLoginPage() {
	}

	/**
	 * {@inheritDoc}
	 * ログインしている場合メニューに表示しないように制御します。
	 */
	@Override
	public boolean isAuthenticated(final Map<String, Object> params) throws Exception {
		// ログインしている場合メニューに表示しない。
		return this.getUserInfo() != null ? false : true;
	}
	
	/**
	 * SAMLのIdPにリダイレクトする。
	 */
	@Override
	public Response getHtml(Map<String, Object> params) throws Exception {
		SAMLRequest req = new SAMLRequest();
		String redirectUrl = req.getRedirectURL();
		logger.debug("** redirectUrl=" + redirectUrl);
		return new RedirectResponse(redirectUrl);
	}
}
