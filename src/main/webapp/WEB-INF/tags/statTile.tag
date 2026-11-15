<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="value" required="true" type="java.lang.Object" %>
<%@ attribute name="tone" required="false" type="java.lang.String" %>
<div class="stat-tile">
  <span class="stat-number<c:if test='${not empty tone}'> stat-number--<c:out value='${tone}'/></c:if>">
    <c:out value="${value}"/>
  </span>
  <span class="stat-label"><c:out value="${label}"/></span>
</div>
