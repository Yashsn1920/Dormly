<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Dashboard" active="dashboard">
  <t:pageTitle title="Dashboard"/>
  <c:if test="${pendingStudentCount > 0}">
    <div class="banner">
      <t:icon name="users"/>
      <span><c:out value="${pendingStudentCount}"/> students are awaiting approval.
        <a href="${pageContext.request.contextPath}/warden/students?status=PENDING">Review</a>
      </span>
    </div>
  </c:if>
  <section class="stat-panel" aria-label="Complaint summary">
    <t:statTile label="Total" value="${stats.total}"/>
    <t:statTile label="Pending" value="${stats.pending}" tone="pending"/>
    <t:statTile label="In Progress" value="${stats.inProgress}" tone="progress"/>
    <t:statTile label="Resolved" value="${stats.resolved}" tone="resolved"/>
    <t:statTile label="Overdue" value="${stats.overdue}" tone="overdue"/>
  </section>
  <div class="grid grid-12" style="margin-top:24px">
    <section class="card span-6">
      <div class="card-header"><span class="icon-box"><t:icon name="chart"/></span><h2>Complaints by Category</h2></div>
      <c:forEach var="stat" items="${categoryCounts}">
        <div class="category-bar">
          <span><c:out value="${stat.category.displayName}"/></span>
          <div class="bar-track" role="img" aria-label="${stat.count} complaints">
            <div class="bar-fill" style="width:${hc:percent(stat.count, stats.total)}%"></div>
          </div>
          <span class="mono"><c:out value="${stat.count}"/></span>
        </div>
      </c:forEach>
    </section>
    <section class="card span-6">
      <div class="card-header"><span class="icon-box"><t:icon name="clock"/></span><h2>Overdue Complaints</h2></div>
      <c:choose>
        <c:when test="${empty overdueComplaints}"><t:emptyState message="No overdue complaints."/></c:when>
        <c:otherwise>
          <div class="table-wrap">
            <table>
              <thead><tr><th scope="col">ID</th><th scope="col">Title</th><th scope="col">Room</th><th scope="col">Priority</th><th scope="col">Due</th><th scope="col">Open</th></tr></thead>
              <tbody><c:forEach var="complaint" items="${overdueComplaints}">
                <tr class="overdue-row">
                  <td>#<c:out value="${complaint.id}"/></td><td><c:out value="${complaint.title}"/></td>
                  <td><c:out value="${complaint.student.roomLabel}"/></td><td><c:out value="${complaint.priority.displayName}"/></td>
                  <td><c:out value="${hc:dateTime(complaint.dueAt)}"/></td>
                  <td><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/warden/complaints/view?id=${complaint.id}">Open</a></td>
                </tr>
              </c:forEach></tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </section>
  </div>
</t:layout>
