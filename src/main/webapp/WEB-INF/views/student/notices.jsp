<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Notices" active="notices">
  <t:pageTitle title="Notices"/>
  <div class="banner"><t:icon name="megaphone"/><span>Official notices from the Warden's Office.</span></div>
  <c:choose>
    <c:when test="${empty notices}"><t:emptyState message="No notices have been posted."/></c:when>
    <c:otherwise>
      <div class="stack" style="margin-top:24px">
        <c:forEach var="notice" items="${notices}">
          <article class="card notice-card">
            <span class="corner-label">Official</span>
            <p><span class="avatar" style="display:inline-grid;width:32px;height:32px;font-size:1rem">W</span>
              Warden's Office · <time class="mono muted"><c:out value="${hc:dateTime(notice.postedAt)}"/></time></p>
            <h2><c:out value="${notice.title}"/></h2>
            <p class="notice-message"><c:out value="${notice.message}"/></p>
          </article>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
