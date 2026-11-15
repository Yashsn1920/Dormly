<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Reports" active="reports">
  <div class="page-title">
    <h1>Reports</h1>
    <div class="cluster no-print">
      <form method="get" action="${pageContext.request.contextPath}/warden/reports">
        <label class="sr-only" for="month">Month</label>
        <input id="month" name="month" type="month" value="<c:out value='${selectedMonth}'/>">
        <button class="btn btn-primary" type="submit">Generate Report</button>
      </form>
      <button class="btn btn-secondary" type="button" onclick="window.print()">Print</button>
    </div>
  </div>
  <p class="print-only">Report for <c:out value="${selectedMonth}"/> · Generated <c:out value="${hc:date(generatedDate)}"/></p>
  <section class="stat-panel" aria-label="Monthly report summary">
    <t:statTile label="Complaints" value="${report.total}"/>
    <t:statTile label="Avg Resolution" value="${hc:hours(overallAvgResolutionHours)}"/>
    <div class="stat-tile">
      <span class="stat-number"><c:choose><c:when test="${empty overallAvgRating}">No data</c:when><c:otherwise><c:out value="${overallAvgRating}"/>/5</c:otherwise></c:choose></span>
      <span class="stat-label">Avg Rating</span>
    </div>
  </section>
  <div class="grid grid-12" style="margin-top:24px">
    <section class="card span-6">
      <h2>Complaints per Month</h2>
      <div class="month-bars" role="list">
        <c:forEach var="item" items="${monthlyCounts}">
          <div class="month-bar" role="listitem">
            <span class="mono"><c:out value="${item.count}"/></span>
            <div class="month-bar-fill<c:if test='${item.yearMonth.toString() == selectedMonth}'> is-selected</c:if>"
                 style="height:${hc:percent(item.count, hc:maxMonthlyCount(monthlyCounts))}%"></div>
            <span class="month-label"><c:out value="${item.yearMonth}"/></span>
          </div>
        </c:forEach>
      </div>
    </section>
    <section class="card span-6">
      <h2>By Status · <c:out value="${selectedMonth}"/></h2>
      <ul class="status-counts">
        <c:forEach var="entry" items="${report.statusCounts}">
          <li><t:statusBadge status="${entry.key}"/> <strong class="mono"><c:out value="${entry.value}"/></strong></li>
        </c:forEach>
      </ul>
    </section>
  </div>
  <section class="card" style="margin-top:24px">
    <h2>Category Performance</h2>
    <c:choose>
      <c:when test="${empty categoryStats}"><t:emptyState message="No data available for this month."/></c:when>
      <c:otherwise>
        <c:set var="lowestRated" value="${hc:lowestRated(categoryStats)}"/>
        <div class="table-wrap">
          <table>
            <thead><tr><th scope="col">Category</th><th scope="col">Complaints</th><th scope="col">Avg Rating</th>
              <th scope="col">Ratings</th><th scope="col">Avg Resolution</th></tr></thead>
            <tbody><c:forEach var="stat" items="${categoryStats}">
              <tr>
                <td><c:out value="${stat.category.displayName}"/>
                  <c:if test="${not empty lowestRated and stat.category == lowestRated.category}">
                    <span class="badge badge--reopened">Lowest Rated</span>
                  </c:if>
                </td>
                <td><c:out value="${stat.count}"/></td>
                <td>
                  <c:choose><c:when test="${empty stat.avgRating}">No data</c:when>
                    <c:otherwise><span class="mono"><c:out value="${hc:squares(stat.avgRating, 5)}"/></span> <c:out value="${stat.avgRating}"/></c:otherwise>
                  </c:choose>
                </td>
                <td><c:out value="${stat.ratingCount}"/></td>
                <td><c:out value="${hc:hours(stat.avgResolutionHours)}"/></td>
              </tr>
            </c:forEach></tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </section>
</t:layout>
