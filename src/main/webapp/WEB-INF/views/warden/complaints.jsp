<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Complaints" active="complaints">
  <t:pageTitle title="Complaints"/>
  <section class="card">
    <h2>Filter Complaints</h2>
    <form method="get" action="${pageContext.request.contextPath}/warden/complaints">
      <div class="form-grid">
        <div class="field"><label for="status">Status</label><select id="status" name="status"><option value="">All</option>
          <c:forEach var="item" items="${statuses}"><option value="${item.name()}" <c:if test="${filter.status == item}">selected</c:if>><c:out value="${item.displayName}"/></option></c:forEach>
        </select></div>
        <div class="field"><label for="category">Category</label><select id="category" name="category"><option value="">All</option>
          <c:forEach var="item" items="${categories}"><option value="${item.name()}" <c:if test="${filter.category == item}">selected</c:if>><c:out value="${item.displayName}"/></option></c:forEach>
        </select></div>
        <div class="field"><label for="priority">Priority</label><select id="priority" name="priority"><option value="">All</option>
          <c:forEach var="item" items="${priorities}"><option value="${item.name()}" <c:if test="${filter.priority == item}">selected</c:if>><c:out value="${item.displayName}"/></option></c:forEach>
        </select></div>
        <t:field name="block" label="Block" value="${filter.block}"/>
        <t:field name="roomNo" label="Room" value="${filter.roomNo}"/>
        <t:field name="from" label="From" type="date" value="${filter.fromDate}"/>
        <t:field name="to" label="To" type="date" value="${filter.toDate}"/>
        <t:field name="q" label="Keyword" value="${filter.keyword}"/>
      </div>
      <div class="cluster">
        <button class="btn btn-primary" type="submit">Apply Filters</button>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/warden/complaints">Reset</a>
      </div>
    </form>
  </section>
  <p class="mono"><c:out value="${complaints.size()}"/> complaints found.</p>
  <c:choose>
    <c:when test="${empty complaints}">
      <t:emptyState message="No complaints found." actionLabel="Reset" actionHref="/warden/complaints"/>
    </c:when>
    <c:otherwise>
      <div class="table-wrap">
        <table>
          <thead><tr><th scope="col">ID</th><th scope="col">Student</th><th scope="col">Category</th>
            <th scope="col">Title</th><th scope="col">Priority</th><th scope="col">Status</th>
            <th scope="col">Raised</th><th scope="col">Assigned To</th><th scope="col">Open</th></tr></thead>
          <tbody><c:forEach var="complaint" items="${complaints}">
            <tr <c:if test="${complaint.overdue}">class="overdue-row"</c:if>>
              <td>#<c:out value="${complaint.id}"/></td>
              <td><c:out value="${complaint.student.name}"/><br><span class="mono"><c:out value="${complaint.student.roomLabel}"/></span></td>
              <td><c:out value="${complaint.category.displayName}"/></td><td><c:out value="${complaint.title}"/></td>
              <td><t:priorityMarker priority="${complaint.priority}"/></td>
              <td><t:statusBadge status="${complaint.status}"/><c:if test="${complaint.overdue}"><t:overdueBadge/></c:if></td>
              <td><c:out value="${hc:dateTime(complaint.createdAt)}"/></td><td><c:out value="${complaint.assignedTo}"/></td>
              <td><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/warden/complaints/view?id=${complaint.id}">Open</a></td>
            </tr>
          </c:forEach></tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
