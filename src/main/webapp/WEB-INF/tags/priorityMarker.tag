<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="priority" required="true" type="com.hostel.model.enums.Priority" %>
<span class="priority-marker" aria-label="<c:out value='${priority.displayName}'/> priority">
  <c:choose>
    <c:when test="${priority.name() == 'HIGH'}">■■■</c:when>
    <c:when test="${priority.name() == 'MEDIUM'}">■■□</c:when>
    <c:otherwise>■□□</c:otherwise>
  </c:choose>
  <c:out value="${priority.displayName}"/>
</span>
