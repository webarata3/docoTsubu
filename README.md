# docoTsubu変更

## スクリプトレットの除去

`loginResult.jsp`

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
 <head>
  <meta charset="UTF-8">
  <title>どこつぶ</title>
 </head>
 <body>
  <h1>どこつぶログイン</h1>
  <c:if test="${not empty loginUser}">
   <p>ログインに成功しました</p>
   <p>ようこそ<c:out value="${loginUser.name}" />さん</p>
   <a href="/docoTsubu/Main">つぶやき投稿・閲覧へ</a>
  </c:if>
  <c:if test="${empty loginUser}">
   <p>ログインに失敗しました</p>
   <a href="/docoTsubu/">TOPへ</a>
  </c:if>
 </body>
</html>
```

## コンテキストパスをリテラルにしない

`loginResult.jsp`は2箇所変更。

```diff
-   <a href="/docoTsubu/Main">つぶやき投稿・閲覧へ</a>
+   <a href="<c:url value='/Main' />">つぶやき投稿・閲覧へ</a>
```

```diff
-   <a href="/docoTsubu/">TOPへ</a>
+   <a href="<c:url value='/Main' />">TOPへ</a>
```

`logout.jsp`は2箇所変更。

```diff
+<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
```

```diff
-  <a href="/docoTsubu/">トップへ</a>
+  <a href="<c:url value='/' />">トップへ</a>
```

`main.jsp`は3箇所変更。

```diff
-   <a href="/docoTsubu/Logout">ログアウト</a>
+   <a href="<c:url value='Logout' />">ログアウト</a>
```

```diff
-  <p><a href="/docoTsubu/Main">更新</a></p>
+  <p><a href="<c:url value='/Main' />">更新</a></p>
```

```diff
-  <form action="/docoTsubu/Main" method="post">
+  <form action="<c:url value='/Main' />" method="post">
```

`Main.java`は1箇所変更。

```diff
-            response.sendRedirect("/docoTsubu/");
+            response.sendRedirect(getServletContext().getContextPath() + "/");
```

## つぶやき投稿後

つぶやき投稿後は`forward`でJSPを表示するとブラウザの更新でフォームが再送信されてしまう。そのため、`/Main`にリダイレクトするように変更する。

`Main.java`の`doPost`を変更する。

```diff
-        GetMutterListLogic getMutterListLogic = new GetMutterListLogic();
-        List<Mutter> mutterList = getMutterListLogic.execute();
-        request.setAttribute("mutterList", mutterList);
-
-        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/main.jsp");
-        dispatcher.forward(request, response);
+        response.sendRedirect(getServletContext().getContextPath() + "/Main");
```

また、つぶやきがない場合にエラーメッセージを表示しているが、上記の変更をするとエラーが表示できなくなるので、次の変更を行う。

`doPost`のメッセージをセッションスコープに変更する。

```diff
-            request.setAttribute("errorMsg", "つぶやきが入力されていません");
+            HttpSession session = request.getSession();
+            session.setAttribute("errorMsg", "つぶやきが入力されていません");
```

`doGet`でエラーメッセージがあれば、セッションスコープからリクエストスコープに変更する。

```diff
+        String errorMsg = (String) session.getAttribute("errorMsg");
+
+        if (errorMsg != null) {
+            session.removeAttribute("errorMsg");
+            request.setAttribute("errorMsg", errorMsg);
+        }
```

## DAOの変更

全体的な見直し。
- `Connectin`取得をメソッドに切り出し
- `Connection`、`ResultSet`等をtry-with-resourcesに変更
- `Class.forName`の除去

```java
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Mutter;

public class MutterDAO {
    private final String JDBC_URL = "jdbc:h2:file:C:/first/2019/Servlet/DB/jugyo;AUTO_SERVER=TRUE";
    private final String DB_USER = "sa";
    private final String DB_PASS = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
    }

    public List<Mutter> findAll() {
        List<Mutter> mutterList = new ArrayList<Mutter>();
        String sql = "SELECT id, name, text FROM mutter ORDER BY id DESC";
        try (Connection conn = getConnection();
                PreparedStatement pStmt = conn.prepareStatement(sql);
                ResultSet rs = pStmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String userName = rs.getString("name");
                String text = rs.getString("text");
                Mutter mutter = new Mutter(id, userName, text);
                mutterList.add(mutter);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return mutterList;
    }

    public boolean create(Mutter mutter) {
        String sql = "INSERT INTO mutter(name, text) VALUES(?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pStmt = conn.prepareStatement(sql)) {
            pStmt.setString(1, mutter.getUserName());
            pStmt.setString(2, mutter.getText());

            int result = pStmt.executeUpdate();

            if (result != 1) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
```

## データベースのエラー対応

もとのプログラムでは、データベース関連でエラーが出てもエラーを握りつぶして何も起きません。ここではエラーページに飛ばすように変更します。

`error.jsp`を作成します。

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>どこつぶ</title>
</head>
<body>
<h1>どこつぶへようこそ</h1>
<p>内部でエラーが出ました。</p>
</body>
</html>
```

このエラー画面に飛ばすために`WEB-INF`に`web.xml`を作成します。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"                  
         version="4.0">
           <error-page>
 <exception-type>java.util.RuntimeException</exception-type>
  <location>/error.jsp</location>
 </error-page
</web-app>
```

最後に`MutterDao.java`を変更します。例外が発生した場合に、`RuntimeException`に変更して例外を投げ直します。

```java
-        } catch (SQLException e) {
-            e.printStackTrace();
-            return null;
+        } catch (SQLException e) {
+            throw new RuntimeException(e);
```

```java
-        } catch (SQLException e) {
-            e.printStackTrace();
-            return false;
+        } catch (SQLException e) {
+            throw new RuntimeException(e);
```

## Filterの設定

文字コードは`utf-8`限定で良いので`Fiter`で設定する。

`CharacterEncodingFilter.java`

```java
package servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter("/*")
public class CharacterEncodingFilter implements Filter {

    public CharacterEncodingFilter() {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("utf-8");
        chain.doFilter(request, response);
    }

    public void init(FilterConfig fConfig) throws ServletException {
    }
}
```

`Login.java`と`Main.java`のEncodingの設定を除去します。

```diff
-        request.setCharacterEncoding("utf-8");
```
