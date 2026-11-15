<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="Raise a Complaint">
  <t:pageTitle title="Raise a Complaint"/>
  <section class="card" style="max-width:720px;margin-inline:auto">
    <c:if test="${not empty duplicateWarning}">
      <div class="flash flash--error" role="alert">
        <strong>Similar Complaint</strong>
        <div>
          <p>A similar complaint is already open for your room.</p>
          <article class="complaint-card">
            <div class="complaint-card__head"><span class="mono">#<c:out value="${duplicateWarning.id}"/></span>
              <t:statusBadge status="${duplicateWarning.status}"/></div>
            <h3><c:out value="${duplicateWarning.title}"/></h3>
            <p><c:out value="${duplicateWarning.createdAt}"/></p>
            <a href="${pageContext.request.contextPath}/student/complaints/view?id=${duplicateWarning.id}">View Existing Complaint</a>
          </article>
          <p>If you attached an image, please select it again.</p>
        </div>
      </div>
    </c:if>
    <c:if test="${not empty errors}">
      <div class="flash flash--error" role="alert"><strong>Error</strong><span>Please correct the highlighted fields.</span></div>
    </c:if>
    <p class="banner">Room: <strong><c:out value="${currentUser.roomLabel}"/></strong> · Taken from your profile.</p>
    <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/student/complaints">
      <div class="field<c:if test='${not empty errors.category}'> field--error</c:if>">
        <label for="category">Category <span class="required-mark" aria-hidden="true"></span></label>
        <select id="category" name="category" required>
          <option value="">Choose a category</option>
          <c:forEach var="category" items="${categories}">
            <option value="${category.name()}" <c:if test="${form.category == category.name()}">selected</c:if>>
              <c:out value="${category.displayName}"/>
            </option>
          </c:forEach>
        </select>
        <c:if test="${not empty errors.category}"><small class="field-error" role="alert"><c:out value="${errors.category}"/></small></c:if>
      </div>
      <t:field name="title" label="Title" value="${form.title}" error="${errors.title}" required="true" maxLength="150"/>
      <t:field name="description" label="Description" type="textarea" value="${form.description}" error="${errors.description}" required="true" maxLength="10000"/>
      <div class="field">
        <label for="image">Image (Optional)</label>
        <input id="image" name="image" type="file" accept=".jpg,.jpeg,.png,image/jpeg,image/png">
        <small class="file-name" data-file-name>No file selected</small>
        <small class="field-hint">JPG or PNG, maximum 2 MB.</small>
        <c:if test="${not empty errors.image}"><small class="field-error" role="alert"><c:out value="${errors.image}"/></small></c:if>
      </div>
      <c:if test="${not empty duplicateWarning}"><input type="hidden" name="confirmDuplicate" value="true"></c:if>
      <aside class="card card--info"><strong>Automatic priority</strong>
        <p>Priority is assigned automatically from the category and cannot be changed.</p>
      </aside>
      <div class="cluster" style="margin-top:24px">
        <button class="btn btn-primary" type="submit">
          <c:choose><c:when test="${not empty duplicateWarning}">Submit Anyway</c:when><c:otherwise>Submit Complaint</c:otherwise></c:choose>
        </button>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/student/complaints">Cancel</a>
      </div>
    </form>
  </section>
</t:layout>
