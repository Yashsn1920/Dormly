<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Notices" active="notices">
  <t:pageTitle title="Notices"/>
  <section class="card" style="max-width:720px">
    <h2>Post a Notice</h2>
    <c:if test="${not empty errors}">
      <div class="flash flash--error" role="alert"><strong>Error</strong><span>Review the highlighted fields.</span></div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/warden/notices">
      <t:field name="title" label="Title" value="${form.title}" error="${errors.title}" required="true" maxLength="150"/>
      <t:field name="message" label="Message" type="textarea" value="${form.message}" error="${errors.message}" required="true" maxLength="10000"/>
      <button class="btn btn-primary" type="submit">Post Notice</button>
    </form>
  </section>
  <section style="margin-top:32px">
    <h2>Posted Notices</h2>
    <c:choose>
      <c:when test="${empty notices}"><t:emptyState message="No notices have been posted."/></c:when>
      <c:otherwise>
        <div class="stack">
          <c:forEach var="notice" items="${notices}">
            <article class="card notice-card">
              <span class="corner-label">Official</span>
              <p>Warden's Office · <time class="mono muted"><c:out value="${hc:dateTime(notice.postedAt)}"/></time></p>
              <h3><c:out value="${notice.title}"/></h3>
              <p class="notice-message"><c:out value="${notice.message}"/></p>
              <form method="post" action="${pageContext.request.contextPath}/warden/notices/delete" data-confirm="Delete this notice?">
                <input type="hidden" name="id" value="<c:out value='${notice.id}'/>">
                <button class="btn btn-danger btn-sm" type="submit">Delete</button>
              </form>
            </article>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </section>
</t:layout>
