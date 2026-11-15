<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="message" required="false" type="com.hostel.util.FlashMessage" %>
<c:if test="${not empty message}">
  <div class="flash flash--${message.type.name().toLowerCase()}" role="status">
    <strong>
      <c:choose>
        <c:when test="${message.type.name() == 'SUCCESS'}">Success</c:when>
        <c:when test="${message.type.name() == 'ERROR'}">Error</c:when>
        <c:otherwise>Notice</c:otherwise>
      </c:choose>
    </strong>
    <span><c:out value="${message.message}"/></span>
  </div>
</c:if>
