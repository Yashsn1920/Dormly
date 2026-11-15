<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Complaint Details">
  <div class="page-title">
    <h1>Complaint #<c:out value="${complaint.id}"/></h1>
    <div><t:statusBadge status="${complaint.status}"/><c:if test="${overdue}"><t:overdueBadge/></c:if></div>
  </div>
  <div class="grid grid-12">
    <div class="span-8 stack">
      <section class="card">
        <h2><c:out value="${complaint.title}"/></h2>
        <p><c:out value="${complaint.description}"/></p>
        <c:if test="${not empty complaint.imagePath}">
          <img class="complaint-image" src="${pageContext.request.contextPath}/files/complaint?id=${complaint.id}"
               alt="Image attached to complaint #${complaint.id}">
        </c:if>
      </section>
      <section class="card">
        <div class="card-header"><span class="icon-box"><t:icon name="list"/></span><h2>Progress</h2></div>
        <t:timeline updates="${timeline}"/>
      </section>
    </div>
    <aside class="span-4 stack">
      <section class="card">
        <h2>Details</h2>
        <dl class="definition-list">
          <dt>Category</dt><dd><c:out value="${complaint.category.displayName}"/></dd>
          <dt>Priority</dt><dd><t:priorityMarker priority="${complaint.priority}"/></dd>
          <dt>Raised</dt><dd><c:out value="${hc:dateTime(complaint.createdAt)}"/></dd>
          <c:if test="${complaint.status.name() != 'CLOSED' and complaint.status.name() != 'REJECTED'}">
            <dt>Due</dt><dd><c:out value="${hc:dateTime(complaint.dueAt)}"/></dd>
          </c:if>
          <c:if test="${not empty complaint.assignedTo}">
            <dt>Assigned</dt><dd><c:out value="${complaint.assignedTo}"/></dd>
          </c:if>
        </dl>
      </section>
      <c:if test="${canConfirm or canReopen or canRate}">
        <section class="card">
          <h2>Actions</h2>
          <c:if test="${canConfirm}">
            <p>Confirm only if the issue is fixed.</p>
            <form method="post" action="${pageContext.request.contextPath}/student/complaints/confirm">
              <input type="hidden" name="id" value="<c:out value='${complaint.id}'/>">
              <button class="btn btn-success" type="submit">Confirm Resolution</button>
            </form>
          </c:if>
          <c:if test="${canReopen}">
            <form method="post" action="${pageContext.request.contextPath}/student/complaints/reopen">
              <input type="hidden" name="id" value="<c:out value='${complaint.id}'/>">
              <t:field name="remark" label="Reason for Reopening" type="textarea" required="true" maxLength="500"/>
              <button class="btn btn-danger" type="submit">Reopen Complaint</button>
            </form>
          </c:if>
          <c:if test="${canRate}">
            <form method="post" action="${pageContext.request.contextPath}/student/complaints/feedback">
              <input type="hidden" name="id" value="<c:out value='${complaint.id}'/>">
              <fieldset class="field">
                <legend class="label">Rate the Service</legend>
                <div class="rating-options">
                  <c:forEach var="value" begin="1" end="5">
                    <input id="rating-${value}" name="rating" type="radio" value="${value}" required>
                    <label for="rating-${value}"><c:out value="${value}"/></label>
                  </c:forEach>
                </div>
              </fieldset>
              <t:field name="comment" label="Comment (Optional)" type="textarea" maxLength="500"/>
              <button class="btn btn-primary" type="submit">Submit Feedback</button>
            </form>
          </c:if>
        </section>
      </c:if>
      <c:if test="${not empty complaint.rating}">
        <section class="card">
          <h2>Your Feedback</h2>
          <p><span class="mono"><c:out value="${hc:squares(complaint.rating, 5)}"/></span>
            <c:out value="${complaint.rating}"/> / 5</p>
          <p><c:out value="${complaint.feedback}"/></p>
        </section>
      </c:if>
      <button class="btn btn-secondary no-print" type="button" onclick="window.print()">Print</button>
    </aside>
  </div>
</t:layout>
