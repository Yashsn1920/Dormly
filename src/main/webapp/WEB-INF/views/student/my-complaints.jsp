<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="My Complaints" active="complaints">
  <t:pageTitle title="My Complaints" actionLabel="Raise a Complaint" actionHref="/student/complaints/new"/>
  <nav class="tabs" aria-label="Complaint status filters">
    <a href="${pageContext.request.contextPath}/student/complaints" <c:if test="${empty selectedStatus}">aria-current="page"</c:if>>All</a>
    <c:forEach var="status" items="${statuses}">
      <a href="${pageContext.request.contextPath}/student/complaints?status=${status.name()}"
         <c:if test="${selectedStatus == status}">aria-current="page"</c:if>>
        <c:out value="${status.displayName}"/>
      </a>
    </c:forEach>
  </nav>
  <p class="mono">Showing <c:out value="${complaints.size()}"/> complaints.</p>
  <c:choose>
    <c:when test="${empty complaints}">
      <c:choose>
        <c:when test="${not empty selectedStatus}">
          <t:emptyState message="No complaints found for this status."/>
        </c:when>
        <c:otherwise>
          <t:emptyState message="No complaints found." actionLabel="Raise a Complaint" actionHref="/student/complaints/new"/>
        </c:otherwise>
      </c:choose>
    </c:when>
    <c:otherwise>
      <div class="stack">
        <c:forEach var="complaint" items="${complaints}">
          <article class="complaint-card<c:if test='${complaint.overdue}'> overdue-row</c:if>">
            <div class="complaint-card__head">
              <span class="mono">#<c:out value="${complaint.id}"/></span>
              <t:statusBadge status="${complaint.status}"/>
            </div>
            <h2><c:out value="${complaint.title}"/></h2>
            <p><span class="badge"><c:out value="${complaint.category.displayName}"/></span></p>
            <p class="muted"><c:out value="${hc:dateTime(complaint.createdAt)}"/>
              <c:if test="${not empty complaint.assignedTo}"> · Assigned to <c:out value="${complaint.assignedTo}"/></c:if>
            </p>
            <t:priorityMarker priority="${complaint.priority}"/>
            <c:if test="${complaint.overdue}"><t:overdueBadge/></c:if>
            <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/student/complaints/view?id=${complaint.id}">View Details</a>
          </article>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
