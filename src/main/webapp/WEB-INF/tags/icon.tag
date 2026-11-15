<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="name" required="true" type="java.lang.String" %>
<svg class="icon" aria-hidden="true" focusable="false">
  <use href="${pageContext.request.contextPath}/static/img/icons.svg#${name}"></use>
</svg>
