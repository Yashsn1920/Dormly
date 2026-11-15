<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Complaint #${complaint.id}" active="complaints">
  <div class="page-title">
    <h1>Complaint #<c:out value="${complaint.id}"/></h1>
    <div><t:statusBadge status="${complaint.status}"/><c:if test="${overdue}"><t:overdueBadge/></c:if></div>
  </div>
  <c:if test="${overdue}"><div class="flash flash--error" role="alert"><strong>Overdue</strong><span>This complaint is past its due date.</span></div></c:if>
  <c:if test="${not empty flash}"><t:flash message="${flash}"/></c:if>
  <div class="grid grid-12">
    <div class="span-8 stack">
      <section class="card">
        <h2><c:out value="${complaint.title}"/></h2>
        <p><c:out value="${complaint.description}"/></p>
        <dl class="definition-list">
          <dt>Category</dt><dd><c:out value="${complaint.category.displayName}"/></dd>
          <dt>Priority</dt><dd><t:priorityMarker priority="${complaint.priority}"/></dd>
          <dt>Raised</dt><dd><c:out value="${hc:dateTime(complaint.createdAt)}"/></dd>
          <c:if test="${complaint.status.name() != 'CLOSED' and complaint.status.name() != 'REJECTED'}">
            <dt>Due</dt><dd><c:out value="${hc:dateTime(complaint.dueAt)}"/></dd>
          </c:if>
          <c:if test="${not empty complaint.assignedTo}"><dt>Assigned To</dt><dd><c:out value="${complaint.assignedTo}"/></dd></c:if>
        </dl>
        <c:if test="${not empty complaint.imagePath}">
          <img class="complaint-image" src="${pageContext.request.contextPath}/files/complaint?id=${complaint.id}" alt="Image attached to complaint #${complaint.id}">
        </c:if>
      </section>
      <section class="card">
        <h2>Student</h2>
        <dl class="definition-list">
          <dt>Name</dt><dd><c:out value="${complaint.student.name}"/></dd>
          <dt>USN</dt><dd><c:out value="${complaint.student.usn}"/></dd>
          <dt>Room</dt><dd><c:out value="${complaint.student.roomLabel}"/></dd>
          <dt>Phone</dt><dd><c:out value="${complaint.student.phone}"/></dd>
          <dt>Email</dt><dd><c:out value="${complaint.student.email}"/></dd>
        </dl>
      </section>
      <section class="card">
        <div class="card-header"><span class="icon-box"><t:icon name="list"/></span><h2>Progress</h2></div>
        <t:timeline updates="${timeline}"/>
      </section>
    </div>
    <aside class="card span-4">
      <h2>Actions</h2>
      <c:if test="${not empty allowedNextStatuses}">
        <form method="post" action="${pageContext.request.contextPath}/warden/complaints/update">
          <input type="hidden" name="id" value="<c:out value='${complaint.id}'/>">
          <div class="field"><label for="status">Update Status</label><select id="status" name="status" required>
            <c:forEach var="status" items="${allowedNextStatuses}">
              <c:if test="${status.name() != 'REJECTED'}"><option value="${status.name()}"><c:out value="${status.displayName}"/></option></c:if>
            </c:forEach>
          </select></div>
          <t:field name="remark" label="Remark" type="textarea" maxLength="500"/>
          <button class="btn btn-primary" type="submit">Update Status</button>
        </form>
      </c:if>
      <section>
        <h3>Assign</h3>
        <c:if test="${not empty complaint.assignedTo}"><p>Currently assigned to <c:out value="${complaint.assignedTo}"/>.</p></c:if>
        <form method="post" action="${pageContext.request.contextPath}/warden/complaints/assign">
          <input type="hidden" name="id" value="<c:out value='${complaint.id}'/>">
          <label for="assignedTo">Assigned To</label>
          <input id="assignedTo" name="assignedTo" maxlength="100" list="staff-suggestions" value="<c:out value='${complaint.assignedTo}'/>">
          <datalist id="staff-suggestions">
            <option value="Electrician"><option value="Plumber"><option value="Housekeeping">
            <option value="Network Technician"><option value="Mess Supervisor"><option value="Security Staff">
          </datalist>
          <button class="btn btn-secondary" type="submit">Assign</button>
        </form>
      </section>
      <c:if test="${canReject}">
        <section>
          <h3>Reject Complaint</h3>
          <form method="post" action="${pageContext.request.contextPath}/warden/complaints/reject" data-confirm="Reject this complaint?">
            <input type="hidden" name="id" value="<c:out value='${complaint.id}'/>">
            <t:field name="reason" label="Reason" type="textarea" required="true" maxLength="500"/>
            <button class="btn btn-danger" type="submit">Reject Complaint</button>
          </form>
        </section>
      </c:if>
      <c:if test="${empty allowedNextStatuses}"><p class="banner">No further actions are available for this complaint.</p></c:if>
      <c:if test="${not empty complaint.rating}">
        <section><h3>Feedback</h3>
          <p><c:out value="${hc:squares(complaint.rating, 5)}"/> · <c:out value="${complaint.rating}"/> / 5</p>
          <p><c:out value="${complaint.feedback}"/></p>
        </section>
      </c:if>
    </aside>
  </div>
</t:layout>
