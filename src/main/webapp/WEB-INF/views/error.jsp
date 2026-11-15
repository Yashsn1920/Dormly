<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="Request Error">
  <section class="card" style="max-width:640px;margin:48px auto">
    <t:pageTitle title="${statusCode == 404 ? 'Page Not Found' : 'Something Went Wrong'}"/>
    <p><c:out value="${errorMessage}" default="The request could not be completed."/></p>
    <c:if test="${not empty errors}">
      <ul role="alert">
        <c:forEach var="entry" items="${errors}">
          <li><c:out value="${entry.value}"/></li>
        </c:forEach>
      </ul>
    </c:if>
    <c:if test="${not empty statusCode}"><p class="mono">Status <c:out value="${statusCode}"/></p></c:if>
    <c:choose>
      <c:when test="${not empty currentUser}">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}${currentUser.dashboardPath}">Return to Dashboard</a>
      </c:when>
      <c:otherwise><a class="btn btn-primary" href="${pageContext.request.contextPath}/login">Sign In</a></c:otherwise>
    </c:choose>
  </section>
</t:layout>
