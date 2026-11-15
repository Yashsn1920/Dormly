<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="title" required="true" type="java.lang.String" %>
<%@ attribute name="actionLabel" required="false" type="java.lang.String" %>
<%@ attribute name="actionHref" required="false" type="java.lang.String" %>
<div class="page-title">
  <h1><c:out value="${title}"/></h1>
  <c:if test="${not empty actionLabel and not empty actionHref}">
    <a class="btn btn-primary" href="${pageContext.request.contextPath}${actionHref}">
      <c:out value="${actionLabel}"/>
    </a>
  </c:if>
</div>
