<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="Access Denied">
  <section class="card" style="max-width:640px;margin:48px auto">
    <t:pageTitle title="Access Denied"/>
    <p><c:out value="${errorMessage}" default="You do not have permission to view this page."/></p>
    <c:if test="${not empty statusCode}"><p class="mono">Status <c:out value="${statusCode}"/></p></c:if>
    <c:choose>
      <c:when test="${not empty currentUser}">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}${currentUser.dashboardPath}">Return to Dashboard</a>
      </c:when>
      <c:otherwise><a class="btn btn-primary" href="${pageContext.request.contextPath}/login">Sign In</a></c:otherwise>
    </c:choose>
  </section>
</t:layout>
