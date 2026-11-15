<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Student Profile" active="profile">
  <t:pageTitle title="Student Profile"/>
  <div class="profile-grid">
    <aside class="card">
      <div class="avatar"><c:out value="${hc:initial(user.name)}"/></div>
      <h2><c:out value="${user.name}"/></h2>
      <span class="role-tag role-tag--student">Student</span>
      <dl class="definition-list" style="margin-top:24px">
        <dt>Email</dt><dd><c:out value="${user.email}"/></dd>
        <dt>Phone</dt><dd><c:out value="${user.phone}"/></dd>
        <dt>USN</dt><dd><c:out value="${user.usn}"/></dd>
        <dt>Room</dt><dd><c:out value="${user.roomLabel}"/></dd>
        <dt>Status</dt><dd><c:out value="${user.status.displayName}"/></dd>
        <dt>Member Since</dt><dd><c:out value="${hc:date(user.createdAt)}"/></dd>
      </dl>
    </aside>
    <div class="stack">
      <section class="card">
        <h2>Personal Details</h2>
        <form method="post" action="${pageContext.request.contextPath}/student/profile">
          <div class="form-grid">
            <t:field name="name" label="Name" value="${form.name != null ? form.name : user.name}" error="${errors.name}" required="true" maxLength="100"/>
            <t:field name="phone" label="Phone" value="${form.phone != null ? form.phone : user.phone}" error="${errors.phone}" required="true" maxLength="10"/>
            <div class="field"><label for="email">Email</label><input id="email" value="<c:out value='${user.email}'/>" readonly></div>
            <div class="field"><label for="usn">USN</label><input id="usn" value="<c:out value='${user.usn}'/>" readonly></div>
            <t:field name="block" label="Block" value="${form.block != null ? form.block : user.block}" error="${errors.block}" required="true" maxLength="10"/>
            <t:field name="roomNo" label="Room Number" value="${form.roomNo != null ? form.roomNo : user.roomNo}" error="${errors.roomNo}" required="true" maxLength="10"/>
          </div>
          <button class="btn btn-success" type="submit">Save Changes</button>
        </form>
      </section>
      <section class="card">
        <h2>Change Password</h2>
        <form method="post" action="${pageContext.request.contextPath}/student/profile/password">
          <div class="form-grid">
            <t:field name="currentPassword" label="Current Password" type="password" error="${errors.currentPassword}" required="true"/>
            <t:field name="newPassword" label="New Password" type="password" error="${errors.newPassword}" hint="At least 8 characters, including a letter and a digit." required="true"/>
            <t:field name="confirmPassword" label="Confirm New Password" type="password" error="${errors.confirmPassword}" required="true"/>
          </div>
          <button class="btn btn-primary" type="submit">Change Password</button>
        </form>
      </section>
    </div>
  </div>
</t:layout>
