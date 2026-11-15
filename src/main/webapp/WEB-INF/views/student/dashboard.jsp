<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Dashboard" active="dashboard">
  <t:pageTitle title="Dashboard" actionLabel="Raise a Complaint" actionHref="/student/complaints/new"/>
  <p>Signed in as <strong><c:out value="${currentUser.name}"/></strong> · Room <c:out value="${currentUser.roomLabel}"/></p>
  <section class="stat-panel" aria-label="Complaint summary">
    <t:statTile label="Total" value="${stats.total}"/>
    <t:statTile label="Pending" value="${stats.pending}" tone="pending"/>
    <t:statTile label="In Progress" value="${stats.inProgress}" tone="progress"/>
    <t:statTile label="Resolved" value="${stats.resolved}" tone="resolved"/>
  </section>
  <div class="grid grid-12" style="margin-top:24px">
    <section class="card span-8">
      <div class="card-header"><span class="icon-box"><t:icon name="list"/></span><h2>Recent Complaints</h2></div>
      <c:choose>
        <c:when test="${empty recent}">
          <t:emptyState message="No complaints found." actionLabel="Raise a Complaint" actionHref="/student/complaints/new"/>
        </c:when>
        <c:otherwise>
          <div class="stack" style="margin-top:16px">
            <c:forEach var="complaint" items="${recent}">
              <article class="complaint-card<c:if test='${complaint.overdue}'> overdue-row</c:if>">
                <div class="complaint-card__head">
                  <span class="mono">#<c:out value="${complaint.id}"/></span>
                  <t:statusBadge status="${complaint.status}"/>
                </div>
                <h3><c:out value="${complaint.title}"/></h3>
                <p class="muted"><c:out value="${complaint.category.displayName}"/> · <t:priorityMarker priority="${complaint.priority}"/></p>
                <c:if test="${complaint.overdue}"><t:overdueBadge/></c:if>
                <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/student/complaints/view?id=${complaint.id}">View Details</a>
              </article>
            </c:forEach>
          </div>
          <a class="btn btn-secondary" href="${pageContext.request.contextPath}/student/complaints">View All Complaints</a>
        </c:otherwise>
      </c:choose>
    </section>
    <aside class="card span-4">
      <div class="card-header"><span class="icon-box"><t:icon name="megaphone"/></span><h2>Latest Notices</h2></div>
      <c:choose>
        <c:when test="${empty latestNotices}"><t:emptyState message="No notices have been posted."/></c:when>
        <c:otherwise>
          <ul>
            <c:forEach var="notice" items="${latestNotices}">
              <li><strong><c:out value="${notice.title}"/></strong><br><span class="muted mono"><c:out value="${hc:date(notice.postedAt)}"/></span></li>
            </c:forEach>
          </ul>
          <a href="${pageContext.request.contextPath}/student/notices">All Notices</a>
        </c:otherwise>
      </c:choose>
    </aside>
  </div>
</t:layout>
