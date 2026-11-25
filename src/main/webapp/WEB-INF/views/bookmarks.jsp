<%@ page pageEncoding="UTF-8" %>
<%@ include file="../includes/header.jspf" %>
<section class="container page"><p class="eyebrow">KEEP A LITTLE PLACE FOR LATER</p><h1>Stories close to your heart.</h1><p class="subtitle">Your bookmarked doorways, ready when you are.</p><c:choose><c:when test="${empty books}"><div class="empty-state"><svg class="large-icon"><use href="#i-heart"/></svg><h2>Something will catch your heart.</h2><p>Tap the heart on any book to keep it here.</p><a class="button" href="${pageContext.request.contextPath}/discover">Wander the shelves →</a></div></c:when><c:otherwise><div class="book-grid"><%@ include file="../includes/cards.jspf" %></div></c:otherwise></c:choose></section>
<%@ include file="../includes/footer.jspf" %>
