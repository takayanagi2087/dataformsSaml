# dataformsSaml.jar dataforms3.jar 用SAML認証モジュール.

## Description
dataformsSaml.jarは、dataforms3.jar(3.1.0-SNAPSHOT以降のバージョン)で作成したWebアプリケーションにSAML認証機能を追加します。

## Install

* SPメタデータの作成。
 SPメタデータの例を以下に示します。<br>
 重要なポイントはSPのIDとACSのURLを指定することです。<br>
 dataformsSaml.jarにはデフォルトのACSが用意されているので、それでよければ"./dataforms/saml/page/SamlAcsPage.html"を刺す完全なURLを指定します。<br>
 ACSのカスタマイズも可能です。<br>

```
<EntityDescriptor entityID="https://www.dataforms.jp/dataforms3app/metadata.xml" xmlns="urn:oasis:names:tc:SAML:2.0:metadata">
  <SPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
    <AssertionConsumerService 
        Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
        Location="https://www.dataforms.jp/dataforms3app/dataforms/saml/page/SamlAcsPage.html"
        index="1" isDefault="true"/>
  </SPSSODescriptor>
</EntityDescriptor>
```
 
* SPメタデータをIdPに登録し、IdPメタデータを取得。
 促成したSPメタデータをIdPに登録し、IdPからIdPメタデータ(XMLファイル)を取得します。
* IdPのログインIDと同じログインIDのユーザをdataformsのユーザー管理で登録。
* dataforms3.jar (3.1.0-SNAPSHOT以降のバージョン)のプロジェクトでこのモジュールを追加。

``` 
	<dependencies>
		<dependency>
			<groupId>jp.dataforms</groupId>
			<artifactId>dataformsSaml</artifactId>
			<version>3.1.0-SNAPSHOT</version>
		</dependency>
	</dependencies>
```

* context.xmlに以下の設定を追加。

```
	<!-- 
	SAML認証関連設定
	 -->
	<!-- SAML認証のSPメタデータのパス -->
	<Environment name="samlSpMetadata"  value="SPメタデータのパス" type="java.lang.String" override="false" />

	<!-- SAML認証IdPメタデータのパス -->
	<Environment name="samlIdpMetadata" value="IdPメタデータのパス" type="java.lang.String" override="false" />

	<!-- SAMLレスポンスの解析結果からloginIdのkey -->
	<Environment name="samlLoginIdKey" value="UserID" type="java.lang.String" override="false" />
```

この状態でアプリケーションをアクセスするとIdPが提供する認証画面に遷移します。<br>
IdPの認証が成功するとdataforms3.jarのユーザーテーブルを検索し、対応するユーザーが存在した場合そのユーザーでログインします。<br>
その認証手順は以下のようになります。<br>

① dataforms3.jarのWebアプリをアクセスすると、index.htmlが./dataforms/app/top/page/TopPage.htmlに転送する。<br>
② ./dataforms/app/top/page/TopPage.htmlをアクセスするとjp.dataforms.fw.app.top.page.TopPageクラスが動作。<br>
③ ログインしていない場合TopPage#toLoginPageを呼び出す。<br>
④ TopPage#toLoginPageではシステム内にjp.dataforms.fw.saml.page.SamlLoginPageが存在するかチェックし存在した場合./dataforms/saml/page/SamlLoginPage.htmlに遷移する。<br>
存在しない場合/dataforms/app/login/page/LoginPage.htmlに遷移する。<br>
⑤.SamlLoginPageはIdPメタデータにしたがって、IdPのログイン画面に遷移する。<br>
⑥.IdPはSPメタデータにしたがって、./dataforms/saml/page/SamlAcsPage.htmlに認証結果をPOSTする。<br>
⑦.SamlAcsPage.htmlに対応するjp.dataforms.fw.saml.page.SamlAcsPageでは認証結果の正当性を確認し、問題なければdataformsのユーザーテーブルの権限設定に従ってユーザのセッションを作成し、サイトマップに遷移する。<br>

## Customization
### SAML認証,dataforms認証の併用
同じサーバーをLA内Nから hoge.localという名前でアクセスするとdataformsのログイン画面を使用し、
インターネット経由でhoge.co.jpというサーバー名でアクセスした場合SAML認証をする場合、
jp.dataforms.fw.app.top.page.TopPageから派生したHogeTopPageというクラスを作成し、
HogeTopPage#toLoginPageメソッドをオーバーライドします。
HogeTopPage#toLoginPageメソッドでは、リクエストされたURLに応じたログインページに遷移する処理を記述します。
index.htmlは作成したHogeTopPageに遷移するように修正し、WEB-INF/dataforms.conf.jsoncの
application.topPageのURLをHogeTopPageに対応するものに書き換えます。
### dataformsのユーザーテーブルを使用せずにログインする方法
dataformsの持つユーザテーブルを使用しないで、IdPから渡される情報のみでログインセッションを構成する場合は
jp.dataforms.fw.saml.page.SamlAcsPageから派生したHogeAcsPageを作成します。
HogeAcsPageではloginメソッドをオーバーライドし、このメソッドでIdPから渡された情報からログインセッションを作成する処理を実装してください。
この時IdPに登録するSPメタデータのACSページのURLはHogeAcsPageのURLを指定する必要があります。

## Licence
[MIT](https://github.com/takayanagi2087/dataforms/blob/master/LICENSE)

