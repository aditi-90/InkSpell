<%@ page pageEncoding="UTF-8" %>
<%@ include file="../includes/header.jspf" %>
<section class="empty-state container"><svg class="large-icon"><use href="#i-book"/></svg><p class="eyebrow">A PAUSE BETWEEN CHAPTERS</p><h1>This doorway is closed.</h1><p><c:choose><c:when test="${not empty error}"><c:out value="${error}"/></c:when><c:otherwise>The page is missing, your form expired, or this room needs a different key. Return to the hall and try again.</c:otherwise></c:choose></p><a class="button" href="${pageContext.request.contextPath}/">Return to the Great Hall</a></section>
<%@ include file="../includes/footer.jspf" %>
