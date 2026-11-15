<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="Student Registration">
  <section class="card" style="max-width:720px;margin-inline:auto">
    <t:pageTitle title="Student Registration"/>
    <c:if test="${not empty errors}">
      <div class="flash flash--error" role="alert">
        <strong>Error</strong>
        <span>Please correct the highlighted fields.</span>
      </div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/register">
      <div class="form-grid">
        <t:field name="name" label="Full Name" value="${form.name}" error="${errors.name}" required="true" maxLength="100"/>
        <t:field name="usn" label="USN" value="${form.usn}" error="${errors.usn}" required="true" maxLength="20"/>
        <t:field name="email" label="Email" type="email" value="${form.email}" error="${errors.email}" required="true" maxLength="120"/>
        <t:field name="phone" label="Phone" value="${form.phone}" error="${errors.phone}" required="true" maxLength="10"/>
        <t:field name="block" label="Block" value="${form.block}" error="${errors.block}" required="true" maxLength="10"/>
        <t:field name="roomNo" label="Room Number" value="${form.roomNo}" error="${errors.roomNo}" required="true" maxLength="10"/>
        <t:field name="password" label="Password" type="password" error="${errors.password}" hint="At least 8 characters, including a letter and a digit." required="true"/>
        <t:field name="confirmPassword" label="Confirm Password" type="password" error="${errors.confirmPassword}" required="true"/>
      </div>
      <aside class="banner"><strong>Account approval</strong><span>Your account will be activated after approval by the warden.</span></aside>
      <div class="cluster" style="margin-top:24px">
        <button class="btn btn-primary" type="submit">Register</button>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/login">Back to Sign In</a>
      </div>
    </form>
  </section>
</t:layout>
