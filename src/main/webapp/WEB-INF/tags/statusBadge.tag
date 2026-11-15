<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="status" required="true" type="com.hostel.model.enums.ComplaintStatus" %>
<span class="badge badge--${status.name().toLowerCase()}"><c:out value="${status.displayName}"/></span>
