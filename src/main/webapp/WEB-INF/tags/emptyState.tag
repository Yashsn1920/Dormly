<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="message" required="true" type="java.lang.String" %>
<%@ attribute name="actionLabel" required="false" type="java.lang.String" %>
<%@ attribute name="actionHref" required="false" type="java.lang.String" %>
<div class="empty-state">
  <p><c:out value="${message}"/></p>
  <c:if test="${not empty actionLabel and not empty actionHref}">
    <a href="${pageContext.request.contextPath}${actionHref}"><c:out value="${actionLabel}"/></a>
  </c:if>
</div>
