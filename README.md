# dataformsSaml.jar dataforms3.jar 用SAML認証モジュール.

## Description
dataformsSaml.jarは、dataforms3.jar(3.1.0-SNAPSHOT以降のバージョン)で作成したWebアプリケーションにSAML認証機能を追加します。

## Install

* SPメタデータの作成。
* SPメタデータをIdPに登録し、IdPメタデータを取得。
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

この状態でアプリケーションをアクセスするとIdPが提供する認証画面に遷移します。
IdPの認証が成功するとdataforms3.jarのユーザーテーブルを検索し、対応するユーザーが存在した場合そのユーザーでログインします。

## Customization
### SAML認証,dataforms認証の切り替え
### dataformsのユーザーテーブルを使用せずにログインする方法

## Requirement

## Licence
[MIT](https://github.com/takayanagi2087/dataforms/blob/master/LICENSE)

