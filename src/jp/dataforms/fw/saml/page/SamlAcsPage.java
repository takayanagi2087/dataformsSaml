package jp.dataforms.fw.saml.page;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.http.HttpSession;
import jp.dataforms.fw.app.base.page.BasePage;
import jp.dataforms.fw.app.user.dao.UserDao;
import jp.dataforms.fw.app.user.dao.UserInfoTable;
import jp.dataforms.fw.controller.WebEntryPoint;
import jp.dataforms.fw.exception.ApplicationException;
import jp.dataforms.fw.exception.ApplicationException.ResponseMode;
import jp.dataforms.fw.response.RedirectResponse;
import jp.dataforms.fw.response.Response;
import jp.dataforms.fw.saml.util.SAMLResponse;
import jp.dataforms.fw.servlet.DataFormsServlet;
import jp.dataforms.fw.util.JsonUtil;
/**
 * SAML ACS ページクラス。
 */
public class SamlAcsPage extends BasePage {
	/**
	 * Logger.
	 */
	private static Logger logger = LogManager.getLogger(SamlAcsPage.class);

	/**
	 * ログインIDのキー。
	 */
	private static String loginIdKey = null;
	
	/**
	 * loginIdのkeyを取得します。
	 */
	static {
		try {
			Context initContext = new InitialContext();
			SamlAcsPage.loginIdKey = (String) initContext.lookup("java:/comp/env/samlLoginIdKey");
			logger.info("samlLoginIdKey=" + SamlAcsPage.loginIdKey );
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
	
	
	/**
	 * コンストラクタ。
	 */
	public SamlAcsPage() {
		this.setMenuItem(false);
	}

	/**
	 * システムへのログインを行います。
	 * <pre>
	 * この処理はUserInfoTableに登録されているユーザ情報を参照しログインします。
	 * SAMLのユーザー情報でのみログインする場合、このクラスから派生したページを作成し、
	 * このメソッドをオーバーライドしてください。
	 * </pre>
	 * @param ui SAMLユーザー情報。
	 * @throws Exception 例外。
	 */
	protected void login(final Map<String, String> ui) throws Exception {
		String loginId = ui.get(SamlAcsPage.loginIdKey);
		UserDao dao = new UserDao(this);
		if (dao.queryUserInfo(loginId) == null) {
			ApplicationException ex = new ApplicationException(this.getPage(), "error.samlloginfail");
			ex.setResponseMode(ResponseMode.REDIRECT_TO_ERROR_PAGE);
			throw ex;
		}
		String password = (String) dao.queryPassword(loginId);
		logger.debug("password=" + password);
		Map<String, Object> loginInfo = new HashMap<String, Object>();
		loginInfo.put(UserInfoTable.Entity.ID_LOGIN_ID, loginId);
		loginInfo.put(UserInfoTable.Entity.ID_PASSWORD, password);
		Map<String, Object> userInfo = dao.login(loginInfo, false);
		logger.debug("userInfo=" + JsonUtil.encode(userInfo, true));
		HttpSession session = this.getPage().getRequest().getSession();
		session.setAttribute(WebEntryPoint.USER_INFO, userInfo);
		logger.debug("loginId=" + loginId);
	}

	/**
	 * SAML応答情報の検証を行います。
	 * @param samlResponse SAML応答情報。
	 * @return ユーザ情報。
	 * @throws Exception
	 */
	protected Map<String, String> validate(final String samlResponse) throws Exception {
		SAMLResponse resp = new SAMLResponse();
		logger.debug("samlResponse=" + samlResponse);
		// SAML認証情報正しいか検証を行う。
		Map<String, String> ui = resp.getUserInfo(samlResponse);
		logger.info("userInfo=" + JsonUtil.encode(ui, true));
		return ui;
	}
	
	/**
	 * SAMLの認証結果をチェックし、認証に成功していれば、システムのページに遷移します。
	 */
	@Override
	public Response getHtml(Map<String, Object> params) throws Exception {
		String samlResponse = (String) params.get("SAMLResponse");
		if (samlResponse != null) {
			Map<String, String> ui = this.validate(samlResponse);
			this.login(ui);
			String context = this.getRequest().getContextPath();
			String url = context + DataFormsServlet.getConf().getApplication().getTopPage() + "." + this.getPageExt();
			return new RedirectResponse(url);
		} else {
			ApplicationException ex = new ApplicationException(this.getPage(), "error.samlloginfail");
			ex.setResponseMode(ResponseMode.REDIRECT_TO_ERROR_PAGE);
			throw ex;
		}
	}

}
