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
    private final String DRIVER_NAME = "org.h2.Driver";
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
