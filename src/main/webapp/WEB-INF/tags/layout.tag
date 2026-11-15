<%@ tag pageEncoding="UTF-8" body-content="scriptless" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="title" required="true" type="java.lang.String" %>
<%@ attribute name="active" required="false" type="java.lang.String" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#EAEDF2">
  <title><c:out value="${title}"/> · Dormly</title>
  <link rel="icon" href="${pageContext.request.contextPath}/static/img/dormly-logo.png" type="image/png">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/tokens.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/components.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/layout.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/pages.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/print.css" media="print">
  <script src="${pageContext.request.contextPath}/static/js/app.js" defer></script>
</head>
<c:choose>
  <c:when test="${empty currentUser}"><body></c:when>
  <c:when test="${currentUser.role.name() == 'WARDEN'}"><body class="theme-warden"></c:when>
  <c:otherwise><body class="theme-student"></c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${empty currentUser}">
    <header class="site-header public-header">
      <a class="brand" href="${pageContext.request.contextPath}/">
        <img class="brand-mark" src="${pageContext.request.contextPath}/static/img/dormly-logo.png" alt="">
        <span class="brand-name">Dormly</span>
      </a>
    </header>
    <main class="public-shell">
      <t:flash message="${flash}"/>
      <jsp:doBody/>
    </main>
  </c:when>
  <c:otherwise>
    <header class="site-header">
      <a class="brand" href="${pageContext.request.contextPath}/">
        <img class="brand-mark" src="${pageContext.request.contextPath}/static/img/dormly-logo.png" alt="">
        <span class="brand-name">Dormly</span>
      </a>
      <details class="mobile-menu">
        <summary>Menu</summary>
        <nav class="site-nav" aria-label="Main navigation">
          <c:choose>
            <c:when test="${currentUser.role.name() == 'STUDENT'}">
              <a href="${pageContext.request.contextPath}/student/dashboard" <c:if test="${active == 'dashboard'}">aria-current="page"</c:if>>Dashboard</a>
              <a href="${pageContext.request.contextPath}/student/complaints/new">Raise a Complaint</a>
              <a href="${pageContext.request.contextPath}/student/complaints" <c:if test="${active == 'complaints'}">aria-current="page"</c:if>>My Complaints</a>
              <a href="${pageContext.request.contextPath}/student/notices" <c:if test="${active == 'notices'}">aria-current="page"</c:if>>Notices</a>
              <a href="${pageContext.request.contextPath}/student/profile" <c:if test="${active == 'profile'}">aria-current="page"</c:if>>Profile</a>
            </c:when>
            <c:otherwise>
              <a href="${pageContext.request.contextPath}/warden/dashboard" <c:if test="${active == 'dashboard'}">aria-current="page"</c:if>>Dashboard</a>
              <a href="${pageContext.request.contextPath}/warden/complaints" <c:if test="${active == 'complaints'}">aria-current="page"</c:if>>Complaints</a>
              <a href="${pageContext.request.contextPath}/warden/students" <c:if test="${active == 'students'}">aria-current="page"</c:if>>Students</a>
              <a href="${pageContext.request.contextPath}/warden/notices" <c:if test="${active == 'notices'}">aria-current="page"</c:if>>Notices</a>
              <a href="${pageContext.request.contextPath}/warden/reports" <c:if test="${active == 'reports'}">aria-current="page"</c:if>>Reports</a>
              <a href="${pageContext.request.contextPath}/warden/profile" <c:if test="${active == 'profile'}">aria-current="page"</c:if>>Profile</a>
            </c:otherwise>
          </c:choose>
        </nav>
      </details>
      <nav class="site-nav desktop-nav" aria-label="Main navigation">
        <c:choose>
          <c:when test="${currentUser.role.name() == 'STUDENT'}">
            <a href="${pageContext.request.contextPath}/student/dashboard" <c:if test="${active == 'dashboard'}">aria-current="page"</c:if>>Dashboard</a>
            <a href="${pageContext.request.contextPath}/student/complaints/new" class="btn btn-accent">Raise Complaint</a>
            <a href="${pageContext.request.contextPath}/student/complaints" <c:if test="${active == 'complaints'}">aria-current="page"</c:if>>My Complaints</a>
            <a href="${pageContext.request.contextPath}/student/notices" <c:if test="${active == 'notices'}">aria-current="page"</c:if>>Notices</a>
            <a href="${pageContext.request.contextPath}/student/profile" <c:if test="${active == 'profile'}">aria-current="page"</c:if>>Profile</a>
          </c:when>
          <c:otherwise>
            <a href="${pageContext.request.contextPath}/warden/dashboard" <c:if test="${active == 'dashboard'}">aria-current="page"</c:if>>Dashboard</a>
            <a href="${pageContext.request.contextPath}/warden/complaints" <c:if test="${active == 'complaints'}">aria-current="page"</c:if>>Complaints</a>
            <a href="${pageContext.request.contextPath}/warden/students" <c:if test="${active == 'students'}">aria-current="page"</c:if>>Students</a>
            <a href="${pageContext.request.contextPath}/warden/notices" <c:if test="${active == 'notices'}">aria-current="page"</c:if>>Notices</a>
            <a href="${pageContext.request.contextPath}/warden/reports" <c:if test="${active == 'reports'}">aria-current="page"</c:if>>Reports</a>
            <a href="${pageContext.request.contextPath}/warden/profile" <c:if test="${active == 'profile'}">aria-current="page"</c:if>>Profile</a>
          </c:otherwise>
        </c:choose>
      </nav>
      <div class="header-user">
        <strong><c:out value="${currentUser.name}"/></strong>
        <span class="role-tag role-tag--${currentUser.role.name().toLowerCase()}"><c:out value="${currentUser.role.displayName}"/></span>
        <form method="post" action="${pageContext.request.contextPath}/logout">
          <button class="btn btn-primary btn-sm" type="submit">Log Out</button>
        </form>
      </div>
    </header>
    <main class="page-shell">
      <t:flash message="${flash}"/>
      <jsp:doBody/>
    </main>
  </c:otherwise>
</c:choose>
<footer class="site-footer">Dormly · Hostel Complaint Management System</footer>
<dialog data-confirm-dialog aria-labelledby="confirm-title">
  <h2 id="confirm-title">Confirm Action</h2>
  <p data-confirm-message></p>
  <div class="dialog-actions">
    <button class="btn btn-secondary" type="button" data-confirm-cancel>Cancel</button>
    <button class="btn btn-danger" type="button" data-confirm-submit>Confirm</button>
  </div>
</dialog>
</body>
</html>
