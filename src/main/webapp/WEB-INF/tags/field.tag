<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="name" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="value" required="false" type="java.lang.String" %>
<%@ attribute name="type" required="false" type="java.lang.String" %>
<%@ attribute name="error" required="false" type="java.lang.String" %>
<%@ attribute name="hint" required="false" type="java.lang.String" %>
<%@ attribute name="required" required="false" type="java.lang.Boolean" %>
<%@ attribute name="maxLength" required="false" type="java.lang.Integer" %>
<div class="field<c:if test='${not empty error}'> field--error</c:if>">
  <label for="${name}"><c:out value="${label}"/>
    <c:if test="${required}"><span class="required-mark" aria-hidden="true"></span><span class="sr-only"> required</span></c:if>
  </label>
  <c:choose>
    <c:when test="${type == 'textarea'}"><textarea id="${name}" name="${name}"
      <c:if test="${required}">required</c:if><c:if test="${not empty maxLength}"> maxlength="${maxLength}"</c:if>><c:out value="${value}"/></textarea></c:when>
    <c:otherwise><input id="${name}" name="${name}" type="${empty type ? 'text' : type}"
      <c:if test="${type != 'password' and not empty value}">value="<c:out value='${value}'/>"</c:if>
      <c:if test="${required}">required</c:if><c:if test="${not empty maxLength}"> maxlength="${maxLength}"</c:if>></c:otherwise>
  </c:choose>
  <c:if test="${not empty hint}"><small class="field-hint"><c:out value="${hint}"/></small></c:if>
  <c:if test="${not empty error}"><small class="field-error" role="alert">! <c:out value="${error}"/></small></c:if>
</div>
