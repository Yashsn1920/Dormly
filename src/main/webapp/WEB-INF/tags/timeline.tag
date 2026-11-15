<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<%@ attribute name="updates" required="true" type="java.util.List" %>
<ol class="timeline">
  <c:forEach var="update" items="${updates}">
    <li class="timeline-item">
      <span class="timeline-node" aria-hidden="true"></span>
      <t:statusBadge status="${update.status}"/>
      <time class="mono muted"><c:out value="${hc:dateTime(update.updatedAt)}"/></time>
      <p>by <c:out value="${update.updatedByName}"/></p>
      <c:if test="${not empty update.remark}"><p><c:out value="${update.remark}"/></p></c:if>
    </li>
  </c:forEach>
</ol>
