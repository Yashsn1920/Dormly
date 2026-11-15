<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="Sign In">
  <div class="login-layout">
    <section>
      <p class="label">Dormly</p>
      <h1><span class="title-block">Hostel Management</span></h1>
      <ol class="intro-steps">
        <li><span class="step-number">1</span> Register with your USN</li>
        <li><span class="step-number">2</span> Raise a complaint</li>
        <li><span class="step-number">3</span> Track it to resolution</li>
      </ol>
    </section>
    <section class="card login-card">
      <div class="card-header"><span class="icon-box"><svg class="icon"><use href="${pageContext.request.contextPath}/static/img/icons.svg#user"></use></svg></span><h2>Sign In</h2></div>
      <c:if test="${not empty error}">
        <div class="flash flash--error" role="alert"><strong>Error</strong><span><c:out value="${error}"/></span></div>
      </c:if>
      <form method="post" action="${pageContext.request.contextPath}/login">
        <div class="field">
          <label for="email">Email Address <span class="required-mark" aria-hidden="true"></span></label>
          <input id="email" name="email" type="email" autocomplete="username" required maxlength="120"
                 value="<c:out value='${email}'/>">
        </div>
        <div class="field">
          <label for="password">Password <span class="required-mark" aria-hidden="true"></span></label>
          <input id="password" name="password" type="password" autocomplete="current-password" required>
        </div>
        <button class="btn btn-primary" type="submit">Sign In</button>
      </form>
      <p>New student? <a href="${pageContext.request.contextPath}/register">Register</a></p>
    </section>
  </div>
</t:layout>
