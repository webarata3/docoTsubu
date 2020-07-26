# docoTsubu変更

## スクリプトレットの除去

```jsp:loginResult.jsp
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
