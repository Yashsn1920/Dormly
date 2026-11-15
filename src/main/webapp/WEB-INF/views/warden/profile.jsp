<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
<t:layout title="Warden Profile" active="profile">
  <t:pageTitle title="Warden Profile"/>
  <div class="profile-grid">
    <aside class="card">
      <div class="avatar"><c:out value="${hc:initial(user.name)}"/></div>
      <h2><c:out value="${user.name}"/></h2>
      <span class="role-tag role-tag--warden">Warden</span>
      <dl class="definition-list" style="margin-top:24px">
        <dt>Email</dt><dd><c:out value="${user.email}"/></dd>
        <dt>Phone</dt><dd><c:out value="${user.phone}"/></dd>
        <dt>Status</dt><dd><c:out value="${user.status.displayName}"/></dd>
      </dl>
    </aside>
    <div class="stack">
      <section class="card">
        <h2>Personal Details</h2>
        <form method="post" action="${pageContext.request.contextPath}/warden/profile">
          <div class="form-grid">
            <t:field name="name" label="Name" value="${form.name != null ? form.name : user.name}" error="${errors.name}" required="true" maxLength="100"/>
            <t:field name="phone" label="Phone" value="${form.phone != null ? form.phone : user.phone}" error="${errors.phone}" required="true" maxLength="10"/>
            <div class="field"><label for="email">Email</label><input id="email" value="<c:out value='${user.email}'/>" readonly></div>
          </div>
          <button class="btn btn-success" type="submit">Save Changes</button>
        </form>
      </section>
      <section class="card">
        <h2>Change Password</h2>
        <form method="post" action="${pageContext.request.contextPath}/warden/profile/password">
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
