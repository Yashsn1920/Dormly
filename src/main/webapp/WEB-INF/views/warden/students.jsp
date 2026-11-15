<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Students" active="students">
  <t:pageTitle title="Students"/>
  <nav class="tabs" aria-label="Student account status">
    <a href="${pageContext.request.contextPath}/warden/students?status=PENDING" <c:if test="${selectedStatus.name() == 'PENDING'}">aria-current="page"</c:if>>Pending</a>
    <a href="${pageContext.request.contextPath}/warden/students?status=ACTIVE" <c:if test="${selectedStatus.name() == 'ACTIVE'}">aria-current="page"</c:if>>Active</a>
    <a href="${pageContext.request.contextPath}/warden/students?status=INACTIVE" <c:if test="${selectedStatus.name() == 'INACTIVE'}">aria-current="page"</c:if>>Inactive</a>
  </nav>
  <c:choose>
    <c:when test="${empty students}"><t:emptyState message="No students in this category."/></c:when>
    <c:otherwise>
      <div class="table-wrap">
        <table>
          <thead><tr><th scope="col">Name</th><th scope="col">USN</th><th scope="col">Email</th>
            <th scope="col">Phone</th><th scope="col">Room</th><th scope="col">Registered</th><th scope="col">Actions</th></tr></thead>
          <tbody><c:forEach var="student" items="${students}">
            <tr>
              <td><c:out value="${student.name}"/></td><td><c:out value="${student.usn}"/></td>
              <td><c:out value="${student.email}"/></td><td><c:out value="${student.phone}"/></td>
              <td><c:out value="${student.roomLabel}"/></td><td><c:out value="${hc:dateTime(student.createdAt)}"/></td>
              <td>
                <c:choose>
                  <c:when test="${selectedStatus.name() == 'PENDING'}">
                    <form method="post" action="${pageContext.request.contextPath}/warden/students/approve">
                      <input type="hidden" name="id" value="<c:out value='${student.id}'/>">
                      <button class="btn btn-success btn-sm" type="submit">Approve</button>
                    </form>
                  </c:when>
                  <c:when test="${selectedStatus.name() == 'ACTIVE'}">
                    <form method="post" action="${pageContext.request.contextPath}/warden/students/deactivate" data-confirm="Deactivate this student account?">
                      <input type="hidden" name="id" value="<c:out value='${student.id}'/>">
                      <button class="btn btn-danger btn-sm" type="submit">Deactivate</button>
                    </form>
                  </c:when>
                  <c:otherwise>
                    <form method="post" action="${pageContext.request.contextPath}/warden/students/reactivate">
                      <input type="hidden" name="id" value="<c:out value='${student.id}'/>">
                      <button class="btn btn-success btn-sm" type="submit">Reactivate</button>
                    </form>
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach></tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
